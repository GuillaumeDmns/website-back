package com.gdamiens.website.usage;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PageViewReaderTest {

    private static final String FIREFOX = "Mozilla/5.0 (X11; Linux x86_64; rv:131.0) Gecko/20100101 Firefox/131.0";

    private static String line(String host, String method, String uri, String status, String referrer, String userAgent) {
        return String.join("\t", "2026-10-14T21:30:00+02:00", host, method, uri, status, "203.0.113.7", referrer, userAgent);
    }

    @Test
    void readsAPageView() {
        PageViewReader.PageView view = PageViewReader.parse(line("www.guillaumedamiens.com", "GET", "/photos", "200",
            "https://www.google.com/search?q=guillaume", FIREFOX));

        assertThat(view).isNotNull();
        assertThat(view.at()).isEqualTo(Instant.parse("2026-10-14T19:30:00Z"));
        assertThat(view.host()).isEqualTo("guillaumedamiens.com");
        assertThat(view.path()).isEqualTo("/photos");
        // The referring site only, not its page (the search)
        assertThat(view.referrer()).isEqualTo("google.com");
        assertThat(view.visitor()).isEqualTo("203.0.113.7|" + FIREFOX);
    }

    @Test
    void countsPagesOnly() {
        assertThat(PageViewReader.isPage("/")).isTrue();
        assertThat(PageViewReader.isPage("/stops/IDFM:71264")).isTrue();
        assertThat(PageViewReader.isPage("/mentions-legales/index.html")).isTrue();
        assertThat(PageViewReader.isPage("/assets/index-abc.js")).isFalse();
        assertThat(PageViewReader.isPage("/main.dart.js")).isFalse();
        assertThat(PageViewReader.isPage("/favicon.svg")).isFalse();
        assertThat(PageViewReader.isPage("/wp-login.php")).isFalse();
        assertThat(PageViewReader.isPage("/canvaskit/canvaskit.wasm")).isFalse();
        assertThat(PageViewReader.isPage("/api/v2/traffic")).isFalse();
    }

    @Test
    void leavesOutRobotsFailuresAndOtherSites() {
        assertThat(PageViewReader.parse(line("guillaumedamiens.com", "GET", "/", "200", "-",
            "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"))).isNull();
        assertThat(PageViewReader.parse(line("guillaumedamiens.com", "GET", "/", "200", "-", "curl/8.5.0"))).isNull();
        assertThat(PageViewReader.parse(line("guillaumedamiens.com", "GET", "/", "200", "-", "-"))).isNull();
        assertThat(PageViewReader.parse(line("guillaumedamiens.com", "GET", "/nothing", "404", "-", FIREFOX))).isNull();
        assertThat(PageViewReader.parse(line("guillaumedamiens.com", "POST", "/", "200", "-", FIREFOX))).isNull();
        assertThat(PageViewReader.parse(line("5.249.162.182", "GET", "/", "200", "-", FIREFOX))).isNull();
        assertThat(PageViewReader.parse("not a line of the log")).isNull();
    }

    @Test
    void keepsTheReferringSiteWhenItIsAnother() {
        assertThat(PageViewReader.referrer("https://www.linkedin.com/in/someone")).isEqualTo("linkedin.com");
        assertThat(PageViewReader.referrer("https://app.guillaumedamiens.com/journey")).isNull();
        assertThat(PageViewReader.referrer("-")).isNull();
        assertThat(PageViewReader.referrer("not a url ::")).isNull();
    }
}
