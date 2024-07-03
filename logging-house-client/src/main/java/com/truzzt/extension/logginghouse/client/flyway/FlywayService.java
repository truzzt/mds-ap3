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

package com.truzzt.extension.logginghouse.client.flyway;

import com.truzzt.extension.logginghouse.client.flyway.connection.DatasourceProperties;
import com.truzzt.extension.logginghouse.client.flyway.connection.DriverManagerConnectionFactory;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.persistence.EdcPersistenceException;
import org.eclipse.edc.sql.datasource.ConnectionFactoryDataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.output.MigrateResult;
import org.flywaydb.core.api.output.RepairResult;

import java.util.List;
import javax.sql.DataSource;

public class FlywayService {

    private static final String MIGRATION_LOCATION_BASE = "classpath:migration";
    private static final String MIGRATION_TABLE_NAME = "flyway_schema_history_logginghouse";

    private final Monitor monitor;
    private final boolean tryRepairOnFailedMigration;
    private final boolean clean;

    public FlywayService(Monitor monitor, boolean tryRepairOnFailedMigration, boolean clean) {
        this.monitor = monitor;
        this.tryRepairOnFailedMigration = tryRepairOnFailedMigration;
        this.clean = clean;
    }

    public void cleanDatabase(DatasourceProperties datasourceProperties) {
        if (clean) {
            monitor.info("Running flyway clean.");
            var flyway = setupFlyway(datasourceProperties);
            flyway.clean();
        }
    }

    public void migrateDatabase(DatasourceProperties datasourceProperties) {
        var flyway = setupFlyway(datasourceProperties);
        flyway.info().getInfoResult().migrations.stream()
                .map(migration -> "Found migration: %s".formatted(migration.filepath))
                .forEach(monitor::info);

        try {
            var migrateResult = flyway.migrate();
            handleFlywayMigrationResult(migrateResult);
        } catch (FlywayException e) {
            if (tryRepairOnFailedMigration) {
                repairAndRetryMigration(flyway);
            } else {
                throw new EdcPersistenceException("Flyway migration failed", e);
            }
        }
    }

    private void repairAndRetryMigration(Flyway flyway) {
        try {
            var repairResult = flyway.repair();
            handleFlywayRepairResult(repairResult);
            var migrateResult = flyway.migrate();
            handleFlywayMigrationResult(migrateResult);
        } catch (FlywayException e) {
            throw new EdcPersistenceException("Flyway migration failed", e);
        }
    }

    private void handleFlywayRepairResult(RepairResult repairResult) {
        if (!repairResult.repairActions.isEmpty()) {
            var repairActions = String.join(", ", repairResult.repairActions);
            monitor.info("Repair actions: %s".formatted(repairActions));
        }

        if (!repairResult.warnings.isEmpty()) {
            var warnings = String.join(", ", repairResult.warnings);
            throw new EdcPersistenceException("Repairing failed: %s".formatted(warnings));
        }
    }

    private Flyway setupFlyway(DatasourceProperties datasourceProperties) {
        var dataSource = getDataSource(datasourceProperties);
        var migrationLocations = List.of(MIGRATION_LOCATION_BASE);
        return Flyway.configure()
                .baselineVersion(MigrationVersion.fromVersion("0.0.0"))
                .baselineOnMigrate(true)
                .failOnMissingLocations(true)
                .dataSource(dataSource)
                .table(MIGRATION_TABLE_NAME)
                .locations(migrationLocations.toArray(new String[0]))
                .cleanDisabled(!clean)
                .load();
    }

    private DataSource getDataSource(DatasourceProperties datasourceProperties) {
        var connectionFactory = new DriverManagerConnectionFactory(datasourceProperties);
        return new ConnectionFactoryDataSource(connectionFactory);
    }

    private void handleFlywayMigrationResult(MigrateResult migrateResult) {
        if (migrateResult.migrationsExecuted > 0) {
            monitor.info(String.format(
                    "Successfully migrated database from version %s to version %s",
                    migrateResult.initialSchemaVersion,
                    migrateResult.targetSchemaVersion));
        } else {
            monitor.info(String.format(
                    "No migration necessary. Current version is %s",
                    migrateResult.initialSchemaVersion));
        }
    }

}
