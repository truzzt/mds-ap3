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

    default String getEventToLogColumn() {
        return "event_to_log";
    }

    default String getCreatedAtColumn() {
        return "created_at";
    }

    String getInsertTemplate();

    default String getFormatAsJsonOperator() {
        return PostgresDialect.getJsonCastOperator();
    }

}
