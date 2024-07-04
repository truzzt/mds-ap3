ALTER TABLE edc_logging_house_message
    ALTER COLUMN consumer_id DROP NOT NULL,
    ALTER COLUMN provider_id DROP NOT NULL;