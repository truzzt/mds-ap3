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

import static java.lang.String.format;

public class BaseSqlDialectStatements implements LoggingHouseEventStatements {

    @Override
    public String getInsertTemplate() {
        return format("INSERT INTO %s (%s, %s) VALUES (?%s, ?)",
                getLoggingHouseMessageTable(),
                getEventToLogColumn(),
                getCreatedAtColumn(),
                getFormatAsJsonOperator()
        );
    }
}
