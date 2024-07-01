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

package com.truzzt.extension.logginghouse.client.store.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.truzzt.extension.logginghouse.client.spi.store.LoggingHouseMessageStore;
import com.truzzt.extension.logginghouse.client.spi.types.LoggingHouseMessage;
import com.truzzt.extension.logginghouse.client.store.sql.schema.LoggingHouseEventStatements;
import org.eclipse.edc.spi.persistence.EdcPersistenceException;
import org.eclipse.edc.sql.QueryExecutor;
import org.eclipse.edc.sql.store.AbstractSqlStore;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;
import org.eclipse.edc.transaction.spi.TransactionContext;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SqlLoggingHouseMessageStore extends AbstractSqlStore implements LoggingHouseMessageStore {

    private final LoggingHouseEventStatements statements;

    public SqlLoggingHouseMessageStore(DataSourceRegistry dataSourceRegistry,
                                       String dataSourceName,
                                       TransactionContext transactionContext,
                                       ObjectMapper objectMapper,
                                       LoggingHouseEventStatements statements,
                                       QueryExecutor queryExecutor) {
        super(dataSourceRegistry, dataSourceName, transactionContext, objectMapper, queryExecutor);
        this.statements = statements;
    }

    @Override
    public void save(LoggingHouseMessage event) {

        Objects.requireNonNull(event);
        Objects.requireNonNull(event.getEventToLog());
        Objects.requireNonNull(event.getCreatedAt());

        transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                queryExecutor.execute(connection, statements.getInsertTemplate(),
                        toJson(event.getEventToLog()),
                        mapFromZonedDateTime(event.getCreatedAt())
                );

            } catch (Exception e) {
                throw new EdcPersistenceException(e.getMessage(), e);
            }
        });
    }

    @Override
    public List<LoggingHouseMessage> listNotSent() {
        return new ArrayList<LoggingHouseMessage>();
    }

    private Long mapFromZonedDateTime(ZonedDateTime zonedDateTime) {
        return zonedDateTime != null ? zonedDateTime.toEpochSecond() : null;
    }

}
