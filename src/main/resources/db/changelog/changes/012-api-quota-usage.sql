--liquibase formatted sql

--changeset gdamiens:api-quota-usage
--comment: PRIM calls of the day per feature, saved every 30 s so that a restart doesn't count from zero
CREATE TABLE public.api_quota_usage (
    day          date        NOT NULL,
    feature      varchar(32) NOT NULL,
    api          varchar(32) NOT NULL,
    calls        integer     NOT NULL,
    guest_calls  integer     NOT NULL,
    updated_at   timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (day, feature)
);
