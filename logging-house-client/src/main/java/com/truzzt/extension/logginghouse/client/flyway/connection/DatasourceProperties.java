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

import org.eclipse.edc.spi.system.configuration.Config;

public class DatasourceProperties {
    public static final String LOGGING_HOUSE_DATASOURCE = "logginghouse";

    private static final String DATASOURCE_SETTING_NAME = "edc.datasource.logginghouse.name";
    private static final String DATASOURCE_SETTING_JDBC_URL = "edc.datasource.logginghouse.url";
    private static final String DATASOURCE_SETTING_USER = "edc.datasource.logginghouse.user";
    private static final String DATASOURCE_SETTING_PASSWORD = "edc.datasource.logginghouse.password";

    private final String name;
    private final String jdbcUrl;
    private final String user;
    private final String password;

    public DatasourceProperties(Config config) {
        name = config.getString(DATASOURCE_SETTING_NAME);
        jdbcUrl = config.getString(DATASOURCE_SETTING_JDBC_URL);
        user = config.getString(DATASOURCE_SETTING_USER);
        password = config.getString(DATASOURCE_SETTING_PASSWORD);
    }

    public String getName() {
        return name;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
