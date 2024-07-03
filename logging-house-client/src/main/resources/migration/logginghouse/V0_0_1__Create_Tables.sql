--
--  Copyright (c) 2024 Daimler TSS GmbH
--
--  This program and the accompanying materials are made available under the
--  terms of the Apache License, Version 2.0 which is available at
--  https://www.apache.org/licenses/LICENSE-2.0
--
--  SPDX-License-Identifier: Apache-2.0
--
--  Contributors:
--       Daimler TSS GmbH - Initial SQL Query
--

-- THIS SCHEMA HAS BEEN WRITTEN AND TESTED ONLY FOR POSTGRES

-- table: edc_logging_house_event
CREATE TABLE IF NOT EXISTS edc_logging_house_message
(
    logging_house_message_id BIGSERIAL NOT NULL,
    event_type VARCHAR NOT NULL,
    event_id VARCHAR NOT NULL,
    event_to_log JSON NOT NULL,
    create_process BOOLEAN NOT NULL,
    process_id VARCHAR NOT NULL,
    consumer_id VARCHAR NOT NULL,
    provider_id VARCHAR NOT NULL,
    status VARCHAR NOT NULL,
    created_at BIGINT NOT NULL,
    sent_at BIGINT,
    PRIMARY KEY (logging_house_message_id)
);
COMMENT ON COLUMN edc_logging_house_message.event_to_log IS 'Event to log serialized as JSON';

CREATE INDEX IF NOT EXISTS idx_edc_logging_house_message_status ON edc_logging_house_message (status);
