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

import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.system.configuration.Config;

import static org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry.DEFAULT_DATASOURCE;

public class DatasourceProperties {
    private static final String LOGGING_HOUSE_DATASOURCE = "logginghouse";

    private static final String DATASOURCE_SETTING_NAME = "edc.datasource.%s.name";
    private static final String DATASOURCE_SETTING_JDBC_URL = "edc.datasource.%s.url";
    private static final String DATASOURCE_SETTING_USER = "edc.datasource.%s.user";
    private static final String DATASOURCE_SETTING_PASSWORD = "edc.datasource.%s.password";

    private final String name;
    private final String jdbcUrl;
    private final String user;
    private final String password;

    public DatasourceProperties(Config config) {
        name = getSetting(config, String.format(DATASOURCE_SETTING_NAME, LOGGING_HOUSE_DATASOURCE),
                String.format(DATASOURCE_SETTING_NAME, DEFAULT_DATASOURCE));

        jdbcUrl = getSetting(config, String.format(DATASOURCE_SETTING_JDBC_URL, LOGGING_HOUSE_DATASOURCE),
                String.format(DATASOURCE_SETTING_JDBC_URL, DEFAULT_DATASOURCE));

        user = getSetting(config, String.format(DATASOURCE_SETTING_USER, LOGGING_HOUSE_DATASOURCE),
                String.format(DATASOURCE_SETTING_USER, DEFAULT_DATASOURCE));

        password = getSetting(config, String.format(DATASOURCE_SETTING_PASSWORD, LOGGING_HOUSE_DATASOURCE),
                String.format(DATASOURCE_SETTING_PASSWORD, DEFAULT_DATASOURCE));
    }

    private String getSetting(Config config, String setting, String defaultSetting) {
        try {
            return config.getString(setting);
        } catch (EdcException e) {
            return config.getString(defaultSetting);
        }
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
