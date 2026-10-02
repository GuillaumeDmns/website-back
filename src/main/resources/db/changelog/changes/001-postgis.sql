--liquibase formatted sql

--changeset gdamiens:postgis
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT count(*) FROM pg_extension WHERE extname = 'postgis'
CREATE EXTENSION postgis;
