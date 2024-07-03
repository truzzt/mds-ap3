/*
 *  Copyright (c) 2023 sovity GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       sovity GmbH - initial implementation
 *
 */

package com.truzzt.extension.logginghouse.client.flyway.connection;

import org.eclipse.edc.spi.persistence.EdcPersistenceException;
import org.eclipse.edc.sql.ConnectionFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DriverManagerConnectionFactory implements ConnectionFactory {

    private static final String CONNECTION_PROPERTY_USER = "user";
    private static final String CONNECTION_PROPERTY_PASSWORD = "password";

    private final DatasourceProperties datasourceProperties;

    public DriverManagerConnectionFactory(DatasourceProperties datasourceProperties) {
        this.datasourceProperties = datasourceProperties;
    }

    @Override
    public Connection create() {
        try {
            var properties = new Properties();
            properties.setProperty(CONNECTION_PROPERTY_USER, datasourceProperties.getUser());
            properties.setProperty(CONNECTION_PROPERTY_PASSWORD, datasourceProperties.getPassword());
            return DriverManager.getConnection(datasourceProperties.getJdbcUrl(), properties);
        } catch (SQLException e) {
            throw new EdcPersistenceException(e);
        }
    }
}
