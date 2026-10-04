# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Spring Boot 4 / Java 25 backend for Guillaume's website. It is mostly an API around Île-de-France Mobilités (IDFM) public transport data: it imports the IDFM GTFS into PostgreSQL (with PostGIS, used from SQL only: no geometry is mapped by JPA) and proxies IDFM PRIM real-time and Navitia APIs.

## Commands

```bash
./mvnw spring-boot:run                 # run locally (profile "dev" is active by default)
./mvnw clean package                   # build jar
```

## Architecture

Package root: `com.gdamiens.website`.

- **Security** (`security/WebSecurityConfig`): stateless JWT via Spring's OAuth2 resource server (HS256, Nimbus encoder/decoder). Authorities come from the `auth` claim with no prefix: `ROLE_ADMIN` or `ROLE_USER` (stored in `user.role`, sign-up gives `ROLE_USER`; admin-only endpoints use `@PreAuthorize("hasRole('ADMIN')")`). Open paths: `/api/signin`, `/api/signup`, `/api/token/refresh`, `/api/logout`, `/error`, Swagger/api-docs and `/public`. Sign-in/up return a 1 h access JWT plus an opaque refresh token (`RefreshTokenService`: SHA-256 hash stored in `public.refresh_token`, rotated on each use, reuse revokes all the user's tokens); the legacy `GET /api/refresh` (React site) renews from a valid JWT. CORS origins come from `application.cors-allowed-origins` (`application.yml`, wider `localhost:[*]` list in the `dev` profile document). `RateLimitFilter` (Bucket4j, in-memory buckets in `RateLimiter`, added after the bearer filter) limits `/api/**` per minute: per IP on auth endpoints, per user when authenticated, per IP otherwise (`application.rate-limit.*`); 429 + `Retry-After`. Client IPs come from `X-Forwarded-For` via `server.forward-headers-strategy: native`
- **Controllers** (`controller/`): all under `/api`. `IDFMNavitiaController` exposes Navitia endpoints; other `IDFM*Controller`s serve lines/stops read from the `gtfs` schema and real-time next passages; `IDFMMainController` triggers the GTFS import.
- **Services** (`service/`): IDFM services extend `AbstractIDFMService` (provides API keys and `prepareHttpRequest()` headers). External calls use `RestTemplate` + Apache HttpClient 5; URLs are centralised in `utils/Constants`.
- **Database schema**: owned by Liquibase (`resources/db/changelog/db.changelog-master.yaml`, formatted SQL changesets in `changes/`). Hibernate runs with `ddl-auto: validate`, so any table/column change needs a new changeset and matching entity types (e.g. `smallint` ↔ `Short`). `public` only holds `user`, `refresh_token` and `favorite`; everything else lives in the `gtfs` schema.
- **GTFS import**: `service/GtfsImportService` downloads the IDFM GTFS zip and streams each file into PostgreSQL with `COPY` (pgJDBC `CopyManager`), no Java row parsing. Each import clones the Liquibase-defined `gtfs` tables into `gtfs_new` (`LIKE`, without keys/indexes), loads them, runs `resources/gtfs/post_load.sql` (builds `shapes` LineStrings and the derived `route_stops`), copies keys and indexes from `gtfs`, then swaps `gtfs_new` → `gtfs` in the same transaction (failure = rollback, old data kept). Tables and columns use GTFS names (`gtfs.stops.stop_id`…); unknown header columns make the import fail until a changeset adds them. Triggered by cron in `IDFMUpdateBatch` (08:30 daily, 13:30 weekdays) or `POST /api/gtfs` (async, 409 if running).
- **Lines**: API line ids are IDFM ids (`C01371`), GTFS route ids are `IDFM:C01371` (`IDFMRoute.toRouteId/toLineId`). `TransportMode.fromGtfs()` maps `route_type` + agency (RER/TER/Transilien) + `N…` short names (Noctilien).
- **Mobility API v2** (`controller/v2/MobilityController`, `/api/v2/...`, DTO records in `controller/object/v2`): endpoints for the apps (nearby, stop area, departures, line, search). A stop area is a GTFS parent station (`location_type = 1`, `IDFM:71264`, every quay has one); it is also the SIRI stop-monitoring `MonitoringRef` (`STIF:StopArea:SP:71264:`) and the Navitia `stop_area:IDFM:71264`. GTFS reads go through `NetworkRepository` (`NamedParameterJdbcTemplate`, native SQL). `DepartureService` merges PRIM real time with GTFS scheduled departures for lines without real time, cached 30 s per stop area (`utils/TtlCache`, no cache library). `NetworkService.getLineDetail` groups trips by stop pattern, re-orients them (GTFS `direction_id` is unreliable, e.g. RER A) and merges express/short-turn patterns into branches. `SearchService`: lines matched locally, places from Navitia. `JourneyService` (`/api/v2/journeys`): Navitia journeys read as raw `JsonNode` (snake_case, only the needed fields) and mapped to options/sections with GTFS lines and stop areas; modes filter = forbidden Navitia physical modes; `earlier`/`later` cursors from Navitia's prev/next links. Navitia has no path for some trips (e.g. Transilien L direct trains: straight segments between stops); such rides (about one point per served stop) get the GTFS shape of their line cut between the two stops (`NetworkRepository.findShapeBetween`, PostGIS `ST_LineSubstring`), looked up in parallel within a 300 ms budget and cached 6 h. `FavoriteController` (`/api/v2/me/favorites`, user from the JWT subject): `public.favorite` rows (HOME/WORK one each, PLACE, STOP by stop area id, LINE by line id; partial unique indexes), returned with current GTFS stop/line data. `TrafficService` (`/traffic`, `/lines/{id}/disruptions`, `/stops/{id}/disruptions`): one Navitia `traffic_reports` call (PRIM has no `line_reports`; ~2 s, 25 MB) gives every disruption with its linked lines and stop areas; the snapshot is refreshed in the background when older than 2 min (stale served meanwhile and kept on failure); elevator outages are the `Ascenseur` tag, web messages are HTML turned into plain text. `VehicleService` (`/lines/{id}/vehicles`, cached 30 s): PRIM estimated-timetable of the line, calls at quays `IDFM:<id>` (or `IDFM:monomodalStopPlace:<id>` for RER) mapped to stop areas. Real vehicle journeys (SNCF, Métro 14) are matched against the line's branches (branch serving the next stop then most following calls in order). Most RATP lines and buses only give the next passages per stop under made-up journeys mixing vehicles (detected when > 20 % of journeys don't follow a branch): vehicles are then followed stop by stop along each branch (a passage continues the earliest vehicle seen at one of the two previous stops that can get there by then, since stops sometimes miss a vehicle; otherwise it is a vehicle that has left the previous stop; direction from the journey's destination). Progress between the stop left and the next one from the expected times. Each vehicle comes with its next calls (times, delay, platform) and, for SNCF, its mission code (`VehicleJourneyName`). Errors as `ProblemDetail` (`MobilityExceptionHandler`)
- **External API DTOs**: `idfm/` holds SIRI-style PRIM real-time response types; `idfm/navitia/` holds Navitia response types.

## Gotchas
- Stop ids : StopArea vs StopPoint vs monomodalStopPlace
- GTFS is theoretical while SIRI is realtime

## Other

- `Bruno/` is a Bruno (OpenCollection YAML) collection covering every `/api` endpoint: `Auth`, `Admin`, `Network (GTFS)`, `Real-time`, `Navitia/*`. Auth is collection-level (bearer `{{jwt}}` + a before-request script in `opencollection.yml` that signs in with the environment's `username`/`password` when the JWT is missing or expiring). Example ids are collection variables; Navitia "list" requests set the ids used by "by ID" requests. Add a request for each new endpoint.

## À ne pas faire
- Do not add dependency without asking
- Do not read `target/`, `src/main/resources/application-dev.yml`
- Do not write or run tests, neither coverage
