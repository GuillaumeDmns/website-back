package com.gdamiens.website.it;

import com.gdamiens.website.support.Api;
import com.gdamiens.website.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Favorites synced between the user's devices */
class FavoritesIT extends IntegrationTest {

    @Test
    void savesAStopAreaOnceWithItsLines() {
        Api user = newUser().api();

        Api.Response saved = user.post("/api/v2/me/favorites", Map.of("kind", "STOP", "stopAreaId", "IDFM:70001"));
        Api.Response again = user.post("/api/v2/me/favorites", Map.of("kind", "STOP", "stopAreaId", " IDFM:70001 "));

        assertThat(saved.status()).isEqualTo(201);
        JsonNode favorite = saved.json();
        assertThat(favorite.path("label").asString()).isEqualTo("Châtelet");
        assertThat(favorite.path("stop").path("lines").values().stream().map(line -> line.path("name").asString()))
            .containsExactly("1", "A", "38", "N11");
        assertThat(again.json().path("id").asLong()).isEqualTo(favorite.path("id").asLong());
        assertThat(user.get("/api/v2/me/favorites").json()).hasSize(1);
    }

    @Test
    void savesALine() {
        Api user = newUser().api();

        JsonNode favorite = user.post("/api/v2/me/favorites", Map.of("kind", "LINE", "lineId", "C01742")).json();

        assertThat(favorite.path("line").path("name").asString()).isEqualTo("A");
        assertThat(favorite.path("line").path("mode").asString()).isEqualTo("RER");
    }

    @Test
    void replacesHome() {
        Api user = newUser().api();

        user.post("/api/v2/me/favorites", place("HOME", "Old home"));
        user.post("/api/v2/me/favorites", place("HOME", "New home"));
        user.post("/api/v2/me/favorites", place("PLACE", "Gym"));
        user.post("/api/v2/me/favorites", place("PLACE", "Gym"));

        JsonNode favorites = user.get("/api/v2/me/favorites").json();
        assertThat(favorites.values().stream().map(favorite -> favorite.path("kind").asString() + ":" + favorite.path("label").asString()))
            .containsExactly("HOME:New home", "PLACE:Gym", "PLACE:Gym");
    }

    @Test
    void checksWhatIsSaved() {
        Api user = newUser().api();

        assertThat(user.post("/api/v2/me/favorites", Map.of("kind", "STOP", "stopAreaId", "IDFM:99999")).status()).isEqualTo(400);
        assertThat(user.post("/api/v2/me/favorites", Map.of("kind", "STOP", "stopAreaId", "IDFM:1001")).status()).isEqualTo(400);
        assertThat(user.post("/api/v2/me/favorites", Map.of("kind", "LINE", "lineId", "C99999")).status()).isEqualTo(400);
        assertThat(user.post("/api/v2/me/favorites", Map.of("kind", "PLACE", "lat", 48.85, "lon", 2.35)).status()).isEqualTo(400);
        Map<String, Object> farAway = place("WORK", "Office");
        farAway.put("lat", 120.0);
        assertThat(user.post("/api/v2/me/favorites", farAway).status()).isEqualTo(400);
        assertThat(user.post("/api/v2/me/favorites", Map.of("label", "No kind")).status()).isEqualTo(400);
        assertThat(user.post("/api/v2/me/favorites", Map.of("kind", "SOMETHING")).status()).isEqualTo(400);
        assertThat(user.get("/api/v2/me/favorites").json()).isEmpty();
    }

    @Test
    void onlyItsOwnerDeletesAFavorite() {
        Api owner = newUser().api();
        long id = owner.post("/api/v2/me/favorites", Map.of("kind", "LINE", "lineId", "C01371")).json().path("id").asLong();

        assertThat(newUser().api().delete("/api/v2/me/favorites/" + id).status()).isEqualTo(404);
        assertThat(owner.delete("/api/v2/me/favorites/" + id).status()).isEqualTo(204);
        assertThat(owner.delete("/api/v2/me/favorites/" + id).status()).isEqualTo(404);
        assertThat(owner.get("/api/v2/me/favorites").json()).isEmpty();
    }

    private static Map<String, Object> place(String kind, String label) {
        Map<String, Object> place = new HashMap<>();
        place.put("kind", kind);
        place.put("label", label);
        place.put("lat", 48.85);
        place.put("lon", 2.35);
        return place;
    }
}
