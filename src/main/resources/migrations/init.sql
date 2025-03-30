DO $$ BEGIN
    CREATE TYPE st_status AS ENUM (
        'not_registered',
        'registered',
        'practice_in_itmo',
        'company_info_waiting_approval',
        'company_info_returned',
        'ticket_waiting_submission',
        'ticket_returned',
        'ticket_waiting_signing',
        'ticket_signed'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

CREATE TABLE IF NOT EXISTS tg_user (
    chat_id                 bigint          PRIMARY KEY,
    is_admin                boolean         NOT NULL DEFAULT FALSE,
    is_banned               boolean         NOT NULL DEFAULT FALSE,
    username                text            NOT NULL
);

CREATE TABLE IF NOT EXISTS edu_stream (
    id                      bigint          PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    year                    varchar(4)      UNIQUE NOT NULL,
    date_from               date            NOT NULL,
    date_to                 date            NOT NULL
);

CREATE TABLE IF NOT EXISTS student (
    chat_id                 bigint          NOT NULL REFERENCES tg_user(chat_id) ON DELETE CASCADE,
    edu_stream_id           bigint          NOT NULL REFERENCES edu_stream(id) ON DELETE CASCADE,
    isu                     int             NOT NULL,
    st_group                int             NOT NULL,
    fullname                text            NOT NULL,
    status                  st_status       NOT NULL DEFAULT 'not_registered',
    comments                text            NOT NULL DEFAULT '',
    company_inn             int,
    company_name            text,
    company_lead_fullname   text,
    company_lead_phone      text,
    company_lead_email      text,
    company_lead_post       text,
    primary key (chat_id, edu_stream_id)
);
