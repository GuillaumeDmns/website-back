package com.gdamiens.website.service;

import com.gdamiens.website.client.GtfsDatasetClient;
import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.model.GtfsImport;
import com.gdamiens.website.repository.GtfsImportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.task.TaskExecutor;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Where the GTFS zip is downloaded, and what is left of it; the import itself is in GtfsImportIT */
class GtfsImportServiceTest {

    private static final String LINK = "https://example.com/IDFM_gtfs.zip";

    private static final long IMPORT_ID = 7;

    private final DataSource dataSource = mock(DataSource.class);

    private final GtfsDatasetClient client = mock(GtfsDatasetClient.class);

    private final GtfsImportRepository repository = mock(GtfsImportRepository.class);

    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);

    private final ApplicationProperties properties = new ApplicationProperties();

    /** Where the client was asked to download the zip */
    private final AtomicReference<Path> downloaded = new AtomicReference<>();

    @TempDir
    private Path temp;

    @BeforeEach
    void setUp() throws SQLException {
        properties.getGtfs().setMinFreeDiskGb(0);
        when(repository.start(any(), any())).thenReturn(IMPORT_ID);
        when(client.getFileUrl()).thenReturn(Optional.of(LINK));
        doAnswer(invocation -> {
            Path target = invocation.getArgument(1);
            downloaded.set(target);
            Files.writeString(target, "not a zip");
            return null;
        }).when(client).download(eq(LINK), any());
        // The load stops there: these tests are about the zip
        when(dataSource.getConnection()).thenThrow(new SQLException("no database in this test"));
    }

    private void importGtfs(Path downloadDir) {
        properties.getGtfs().setDownloadDir(downloadDir == null ? "" : downloadDir.toString());
        new GtfsImportService(dataSource, client, mock(TaskExecutor.class), repository, events, properties)
            .importGtfs(GtfsImport.MANUAL, Instant.parse("2026-11-12T07:00:00Z"));
    }

    private String error() {
        ArgumentCaptor<String> error = ArgumentCaptor.forClass(String.class);
        verify(repository).finish(eq(IMPORT_ID), eq(GtfsImport.FAILED), isNull(), error.capture());
        return error.getValue();
    }

    @Test
    void downloadsTheZipIntoTheConfiguredDirectoryAndDeletesItWhenTheLoadFails() {
        Path directory = temp.resolve("gtfs");

        importGtfs(directory);

        assertThat(downloaded.get().getParent()).isEqualTo(directory);
        assertThat(downloaded.get().getFileName().toString()).startsWith("gtfs-").endsWith(".zip");
        assertThat(error()).contains("no database in this test");
        assertThat(directory).isEmptyDirectory();
    }

    @Test
    void createsTheMissingDirectory() {
        Path directory = temp.resolve("var").resolve("gtfs");

        importGtfs(directory);

        assertThat(directory).isDirectory();
    }

    @Test
    void deletesTheZipOfADownloadThatFailsHalfway() {
        doAnswer(invocation -> {
            Path target = invocation.getArgument(1);
            Files.writeString(target, "half a z");
            throw new UncheckedIOException(new IOException("connection reset"));
        }).when(client).download(eq(LINK), any());

        importGtfs(temp);

        assertThat(error()).contains("connection reset");
        assertThat(temp).isEmptyDirectory();
    }

    @Test
    void usesTheTemporaryDirectoryWhenNoneIsConfigured() {
        importGtfs(null);

        assertThat(downloaded.get().getParent()).isEqualTo(Path.of(System.getProperty("java.io.tmpdir")).toAbsolutePath());
        assertThat(downloaded.get()).doesNotExist();
    }

    @Test
    void measuresTheFreeDiskOfTheDirectoryBeforeDownloading() {
        properties.getGtfs().setMinFreeDiskGb(Integer.MAX_VALUE);

        importGtfs(temp);

        assertThat(error()).contains("Not enough free disk for a GTFS import in " + temp.toAbsolutePath());
        verify(client, never()).download(any(), any());
        assertThat(temp).isEmptyDirectory();
    }

    @Test
    void saysWhichDirectoryCannotBeUsed() throws IOException {
        // A directory can't be created under a file: refused whoever runs the tests, root included
        Path directory = Files.createFile(temp.resolve("file")).resolve("gtfs");

        importGtfs(directory);

        assertThat(error()).contains("GTFS download directory " + directory, "application.gtfs.download-dir");
        verify(client, never()).download(any(), any());
    }

    @Test
    void deletesTheZipsLeftByInterruptedImportsOnly() throws IOException {
        Path left = Files.writeString(temp.resolve("gtfs-1234.zip"), "left by a killed import");
        Files.setLastModifiedTime(left, FileTime.from(Instant.now().minus(Duration.ofHours(2))));
        Path recent = Files.writeString(temp.resolve("gtfs-5678.zip"), "being imported by another application");
        Path other = Files.writeString(temp.resolve("notes.zip"), "not ours");
        Files.setLastModifiedTime(other, FileTime.from(Instant.now().minus(Duration.ofHours(2))));

        importGtfs(temp);

        assertThat(left).doesNotExist();
        assertThat(recent).exists();
        assertThat(other).exists();
    }
}
