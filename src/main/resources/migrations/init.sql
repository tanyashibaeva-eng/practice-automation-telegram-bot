DO $$ BEGIN
    CREATE TYPE st_status AS ENUM (
        'not_registered',
        'registered',
        'practice_in_itmo_markina',
        'practice_in_itmo_university',
        'company_info_waiting_approval',
        'company_info_returned',
        'practice_approved',
        'application_waiting_submission',
        'application_waiting_approval',
        'application_returned',
        'application_waiting_signing',
        'application_signed'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE st_practice_format AS ENUM (
        'not_specified',
        'offline',
        'hybrid',
        'online'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE st_practice_place AS ENUM (
        'not_specified',
        'itmo_markina',
        'itmo_university',
        'other_company'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

CREATE TABLE IF NOT EXISTS tg_user (
    chat_id                 bigint              PRIMARY KEY,
    is_admin                boolean             NOT NULL DEFAULT FALSE,
    is_banned               boolean             NOT NULL DEFAULT FALSE,
    username                text                NOT NULL
);

CREATE TABLE IF NOT EXISTS edu_stream (
    id                      bigint              PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name                    text                NOT NULL,
    year                    varchar(4)          NOT NULL,
    date_from               date                NOT NULL,
    date_to                 date                NOT NULL
);

CREATE TABLE IF NOT EXISTS student (
    chat_id                 bigint              NOT NULL REFERENCES tg_user(chat_id) ON DELETE CASCADE,
    edu_stream_id           bigint              NOT NULL REFERENCES edu_stream(id) ON DELETE CASCADE,
    isu                     int                 NOT NULL,
    st_group                varchar(8)          NOT NULL,
    fullname                text                NOT NULL,
    status                  st_status           NOT NULL DEFAULT 'not_registered',
    comments                text                NOT NULL DEFAULT '',
    call_status_comments    text                NOT NULL DEFAULT '',
    practice_place          st_practice_place   NOT NULL DEFAULT 'not_specified',
    practice_format         st_practice_format  NOT NULL DEFAULT 'not_specified',
    company_inn             int,
    company_name            text,
    company_lead_fullname   text,
    company_lead_phone      text,
    company_lead_email      text,
    company_lead_job_title  text,
    cell_hex_color          varchar(32)         NOT NULL DEFAULT 'FFFFFF',
    primary key (chat_id, edu_stream_id)
);
