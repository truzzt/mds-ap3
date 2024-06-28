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
    event_to_log JSON NOT NULL,
    created_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    PRIMARY KEY (logging_house_message_id)
);
COMMENT ON COLUMN edc_logging_house_message.event_to_log IS 'Event to log serialized as JSON';
