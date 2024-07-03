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

package com.truzzt.extension.logginghouse.client.flyway.migration;

import com.truzzt.extension.logginghouse.client.flyway.FlywayService;
import com.truzzt.extension.logginghouse.client.flyway.connection.DatasourceProperties;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.configuration.Config;

public class DatabaseMigrationManager {
    private final Config config;
    private final Monitor monitor;
    private final FlywayService flywayService;

    public DatabaseMigrationManager(Config config, Monitor monitor, FlywayService flywayService) {
        this.config = config;
        this.monitor = monitor;
        this.flywayService = flywayService;
    }

    public void migrate() {
        var datasourceProperties = new DatasourceProperties(config);
        monitor.info("Using datasource %s to apply flyway migrations".formatted(DatasourceProperties.LOGGING_HOUSE_DATASOURCE));

        flywayService.cleanDatabase(datasourceProperties);
        flywayService.migrateDatabase(datasourceProperties);
    }
}
