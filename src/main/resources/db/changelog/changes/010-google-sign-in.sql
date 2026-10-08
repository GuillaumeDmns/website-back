--liquibase formatted sql

--changeset gdamiens:google-sign-in
--comment: Google accounts: the Google subject (unique), the first name shown in the app, the last sign-in. Password accounts keep their login and password until they go away
ALTER TABLE public."user" ADD COLUMN google_sub varchar(255);
ALTER TABLE public."user" ADD COLUMN display_name varchar(255);
ALTER TABLE public."user" ADD COLUMN last_login_at timestamptz;
CREATE UNIQUE INDEX user_google_sub_key ON public."user" (google_sub);
