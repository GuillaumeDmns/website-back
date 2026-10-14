--liquibase formatted sql

--changeset gdamiens:usage-stats
--comment: Anonymous usage statistics of the administration: counts only, never an account, an IP or a position
-- API requests per hour (UTC), route (/api/v2/stops/{id}/departures), kind of caller and status
CREATE TABLE public.usage_hourly (
    hour          timestamptz  NOT NULL,
    endpoint      varchar(120) NOT NULL,
    caller        varchar(16)  NOT NULL CHECK (caller IN ('ACCOUNT', 'GUEST', 'ANONYMOUS')),
    status        varchar(8)   NOT NULL,
    requests      integer      NOT NULL,
    total_millis  bigint       NOT NULL,
    max_millis    integer      NOT NULL,
    PRIMARY KEY (hour, endpoint, caller, status)
);
-- Distinct members of a metric per Paris day (active accounts, guest devices, devices per platform or version,
-- visitors of a site), once the day is over: counted from usage_member, then deleted
CREATE TABLE public.usage_daily (
    day     date        NOT NULL,
    metric  varchar(80) NOT NULL,
    count   integer     NOT NULL,
    PRIMARY KEY (day, metric)
);
-- The day's members as keyed hashes (HMAC with the day's salt, 8 bytes): distinct counts of the day; the salt and the
-- hashes go at the end of the day, after which nothing links a count to anyone
CREATE TABLE public.usage_member (
    day     date        NOT NULL,
    metric  varchar(80) NOT NULL,
    member  bytea       NOT NULL,
    PRIMARY KEY (day, metric, member)
);
CREATE TABLE public.usage_salt (
    day   date  PRIMARY KEY,
    salt  bytea NOT NULL
);
-- Pages of the static sites viewed per Paris day (nginx log of the page loads, bots left out)
CREATE TABLE public.usage_page_daily (
    day    date         NOT NULL,
    host   varchar(64)  NOT NULL,
    path   varchar(200) NOT NULL,
    views  integer      NOT NULL,
    PRIMARY KEY (day, host, path)
);
-- Where the visitors of a site came from: the referring site only (its host), never the page
CREATE TABLE public.usage_referrer_daily (
    day       date         NOT NULL,
    host      varchar(64)  NOT NULL,
    referrer  varchar(100) NOT NULL,
    views     integer      NOT NULL,
    PRIMARY KEY (day, host, referrer)
);
-- How far the page log was read (its inode and offset), across restarts and rotations
CREATE TABLE public.usage_log_cursor (
    name        varchar(32)  PRIMARY KEY,
    inode       bigint       NOT NULL,
    position    bigint       NOT NULL,
    updated_at  timestamptz  NOT NULL DEFAULT now()
);
