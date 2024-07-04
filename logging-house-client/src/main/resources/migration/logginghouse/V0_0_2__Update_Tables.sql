-- previous version of the table:
--
-- CREATE TABLE IF NOT EXISTS edc_logging_house_message
-- (
--     logging_house_message_id BIGSERIAL NOT NULL,
--     event_type VARCHAR NOT NULL,
--     event_id VARCHAR NOT NULL,
--     event_to_log JSON NOT NULL,
--     create_process BOOLEAN NOT NULL,
--     process_id VARCHAR NOT NULL,
--     consumer_id VARCHAR NOT NULL,
--     provider_id VARCHAR NOT NULL,
--     status VARCHAR NOT NULL,
--     created_at BIGINT NOT NULL,
--     sent_at BIGINT,
--     PRIMARY KEY (logging_house_message_id)
-- );

-- remove the status column and add the receipt column, the status is inferred from the receipt (if not null, the status is 'SENT')
ALTER TABLE edc_logging_house_message
    DROP COLUMN IF EXISTS status,
    ADD COLUMN IF NOT EXISTS receipt VARCHAR;