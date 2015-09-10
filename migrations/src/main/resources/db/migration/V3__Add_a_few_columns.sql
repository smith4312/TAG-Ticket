ALTER TABLE AGENT ADD COLUMN access_level INT;

ALTER TABLE TICKET
    ADD COLUMN priority smallint DEFAULT 0,
    ADD COLUMN "time" timestamp with time zone;
