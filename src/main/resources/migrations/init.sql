DO $$ BEGIN
    CREATE TYPE st_status AS ENUM (
        'NOT_REGISTERED',
        'REGISTERED',
        'PRACTICE_IN_ITMO_MARKINA',
        'COMPANY_INFO_WAITING_APPROVAL',
        'COMPANY_INFO_RETURNED',
        'PRACTICE_APPROVED',
        'APPLICATION_WAITING_SUBMISSION',
        'APPLICATION_WAITING_APPROVAL',
        'APPLICATION_RETURNED',
        'APPLICATION_WAITING_SIGNING',
        'APPLICATION_SIGNED'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE st_practice_format AS ENUM (
        'NOT_SPECIFIED',
        'OFFLINE',
        'HYBRID',
        'ONLINE'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE st_practice_place AS ENUM (
        'NOT_SPECIFIED',
        'ITMO_MARKINA',
        'ITMO_UNIVERSITY',
        'OTHER_COMPANY'
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
    name                    text                UNIQUE NOT NULL,
    year                    smallint            NOT NULL,
    date_from               date                NOT NULL,
    date_to                 date                NOT NULL
);

CREATE TABLE IF NOT EXISTS student (
    chat_id                 bigint              NOT NULL REFERENCES tg_user(chat_id) ON DELETE CASCADE,
    edu_stream_id           bigint              NOT NULL REFERENCES edu_stream(id) ON DELETE CASCADE,
    isu                     int                 NOT NULL,
    st_group                varchar(8)          NOT NULL,
    fullname                text                NOT NULL,
    status                  st_status           NOT NULL DEFAULT 'NOT_REGISTERED',
    comments                text                NOT NULL DEFAULT '',
    call_status_comments    text                NOT NULL DEFAULT '',
    practice_place          st_practice_place   NOT NULL DEFAULT 'NOT_SPECIFIED',
    practice_format         st_practice_format  NOT NULL DEFAULT 'NOT_SPECIFIED',
    company_inn             int,
    company_name            text,
    company_lead_fullname   text,
    company_lead_phone      text,
    company_lead_email      text,
    company_lead_job_title  text,
    cell_hex_color          varchar(32)         NOT NULL DEFAULT 'FFFFFF',
    managed_manually        boolean             NOT NULL DEFAULT false,
    primary key (chat_id, edu_stream_id)
);
