--liquibase formatted sql
-- Tables that existed before Liquibase (created by Hibernate ddl-auto): marked as ran when already present

--changeset gdamiens:public-user
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'user'
CREATE TABLE public."user" (
    id       integer PRIMARY KEY,
    login    varchar(255),
    password varchar(255)
);
