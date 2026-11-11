package com.gdamiens.website.client;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GtfsDatasetClientTest {

    private static final String DATASET = "https://data.iledefrance-mobilites.fr/api/explore/v2.1/catalog/datasets/offre-horaires-tc-gtfs-idfm";

    @Test
    void theApiKeyOnlyGoesToTheDatasetsHost() {
        assertThat(GtfsDatasetClient.sameHost("https://data.iledefrance-mobilites.fr/api/v2/files/abc", DATASET)).isTrue();
        assertThat(GtfsDatasetClient.sameHost("https://DATA.iledefrance-mobilites.fr/files/abc", DATASET)).isTrue();
        // Wherever the dataset's record sends the download, the key stays home
        assertThat(GtfsDatasetClient.sameHost("https://files.example.com/IDFM-gtfs.zip", DATASET)).isFalse();
        assertThat(GtfsDatasetClient.sameHost("https://data.iledefrance-mobilites.fr.example.com/gtfs.zip", DATASET)).isFalse();
        assertThat(GtfsDatasetClient.sameHost("https://data.iledefrance-mobilites.fr:8443/gtfs.zip", DATASET)).isFalse();
        assertThat(GtfsDatasetClient.sameHost("not a link", DATASET)).isFalse();
    }
}
