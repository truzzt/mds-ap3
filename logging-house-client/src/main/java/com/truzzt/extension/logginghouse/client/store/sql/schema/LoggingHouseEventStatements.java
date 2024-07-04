/*
 *  Copyright (c) 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - Initial implementation
 *       truzzt GmbH - PostgreSQL implementation
 *
 */

package com.truzzt.extension.logginghouse.client.store.sql.schema;

import org.eclipse.edc.sql.dialect.PostgresDialect;

public interface LoggingHouseEventStatements {

    default String getLoggingHouseMessageTable() {
        return "edc_logging_house_message";
    }
    
    default String getIdColumn() {
        return "logging_house_message_id";
    }

    default String getEventTypeColumn() {
        return "event_type";
    }

    default String getEventIdColumn() {
        return "event_id";
    }

    default String getEventToLogColumn() {
        return "event_to_log";
    }

    default String getCreateProcessColumn() {
        return "create_process";
    }

    default String getProcessIdColumn() {
        return "process_id";
    }

    default String getConsumerIdColumn() {
        return "consumer_id";
    }

    default String getProviderIdColumn() {
        return "provider_id";
    }

    default String getStatusColumn() {
        return "status";
    }

    default String getCreatedAtColumn() {
        return "created_at";
    }

    default String getSentAtColumn() {
        return "sent_at";
    }

    default String getReceiptColumn() {
        return "receipt";
    }

    String getInsertTemplate();

    String getSelectPendingStatement();

    String getUpdateSentTemplate();

    default String getFormatAsJsonOperator() {
        return PostgresDialect.getJsonCastOperator();
    }

}
