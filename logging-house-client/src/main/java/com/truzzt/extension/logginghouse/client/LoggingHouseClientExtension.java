/*
 *  Copyright (c) 2024 truzzt GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       truzzt GmbH - Initial implementation
 *
 */

package com.truzzt.extension.logginghouse.client;

import com.truzzt.extension.logginghouse.client.events.LoggingHouseEventSubscriber;
import com.truzzt.extension.logginghouse.client.events.messages.CreateProcessMessageSender;
import com.truzzt.extension.logginghouse.client.events.messages.LogMessageSender;
import com.truzzt.extension.logginghouse.client.flyway.FlywayService;
import com.truzzt.extension.logginghouse.client.flyway.connection.DatasourceProperties;
import com.truzzt.extension.logginghouse.client.flyway.migration.DatabaseMigrationManager;
import com.truzzt.extension.logginghouse.client.multipart.IdsMultipartClearingRemoteMessageDispatcher;
import com.truzzt.extension.logginghouse.client.multipart.MultiContextJsonLdSerializer;
import com.truzzt.extension.logginghouse.client.multipart.ids.jsonld.JsonLd;
import com.truzzt.extension.logginghouse.client.multipart.ids.multipart.IdsMultipartSender;
import com.truzzt.extension.logginghouse.client.spi.store.LoggingHouseMessageStore;
import com.truzzt.extension.logginghouse.client.store.sql.SqlLoggingHouseMessageStore;
import com.truzzt.extension.logginghouse.client.store.sql.schema.postgres.PostgresDialectStatements;
import com.truzzt.extension.logginghouse.client.worker.LoggingHouseWorkersManager;
import com.truzzt.extension.logginghouse.client.worker.WorkersExecutor;
import de.fraunhofer.iais.eis.LogMessage;
import de.fraunhofer.iais.eis.RequestMessage;
import org.eclipse.edc.connector.contract.spi.event.contractnegotiation.ContractNegotiationFinalized;
import org.eclipse.edc.connector.contract.spi.negotiation.store.ContractNegotiationStore;
import org.eclipse.edc.connector.transfer.spi.event.TransferProcessCompleted;
import org.eclipse.edc.connector.transfer.spi.event.TransferProcessFailed;
import org.eclipse.edc.connector.transfer.spi.event.TransferProcessInitiated;
import org.eclipse.edc.connector.transfer.spi.event.TransferProcessRequested;
import org.eclipse.edc.connector.transfer.spi.event.TransferProcessStarted;
import org.eclipse.edc.connector.transfer.spi.event.TransferProcessTerminated;
import org.eclipse.edc.connector.transfer.spi.store.TransferProcessStore;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Requires;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.asset.AssetIndex;
import org.eclipse.edc.spi.event.EventRouter;
import org.eclipse.edc.spi.http.EdcHttpClient;
import org.eclipse.edc.spi.iam.IdentityService;
import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.Hostname;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.sql.QueryExecutor;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;
import org.eclipse.edc.transaction.spi.TransactionContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Map;

import static com.truzzt.extension.logginghouse.client.ConfigConstants.LOGGINGHOUSE_ENABLED_SETTING;
import static com.truzzt.extension.logginghouse.client.ConfigConstants.LOGGINGHOUSE_EXTENSION_MAX_WORKERS;
import static com.truzzt.extension.logginghouse.client.ConfigConstants.LOGGINGHOUSE_EXTENSION_WORKERS_DELAY;
import static com.truzzt.extension.logginghouse.client.ConfigConstants.LOGGINGHOUSE_EXTENSION_WORKERS_PERIOD;
import static com.truzzt.extension.logginghouse.client.ConfigConstants.LOGGINGHOUSE_FLYWAY_CLEAN_SETTING;
import static com.truzzt.extension.logginghouse.client.ConfigConstants.LOGGINGHOUSE_FLYWAY_REPAIR_SETTING;
import static com.truzzt.extension.logginghouse.client.ConfigConstants.LOGGINGHOUSE_URL_SETTING;

@Extension(value = LoggingHouseClientExtension.NAME)
@Requires(value = {
    Hostname.class,

    TypeManager.class,
    EventRouter.class,
    IdentityService.class,
    RemoteMessageDispatcherRegistry.class,

    DataSourceRegistry.class,
    TransactionContext.class,
    QueryExecutor.class,

    ContractNegotiationStore.class,
    TransferProcessStore.class,
    AssetIndex.class
})
public class LoggingHouseClientExtension implements ServiceExtension {

    public static final String NAME = "LoggingHouseClientExtension";
    private static final String TYPE_MANAGER_SERIALIZER_KEY = "ids-clearinghouse";
    private static final Map<String, String> CONTEXT_MAP = Map.of(
            "cat", "http://w3id.org/mds/data-categories#",
            "ids", "https://w3id.org/idsa/core/",
            "idsc", "https://w3id.org/idsa/code/");

    @Inject
    private Hostname hostname;

    @Inject
    private TypeManager typeManager;
    @Inject
    private EventRouter eventRouter;
    @Inject
    private IdentityService identityService;
    @Inject
    private RemoteMessageDispatcherRegistry dispatcherRegistry;

    @Inject
    private DataSourceRegistry dataSourceRegistry;
    @Inject
    private TransactionContext transactionContext;
    @Inject
    private QueryExecutor queryExecutor;

    @Inject
    private ContractNegotiationStore contractNegotiationStore;
    @Inject
    private TransferProcessStore transferProcessStore;
    @Inject
    private AssetIndex assetIndex;

    public Monitor monitor;
    private boolean enabled;
    private URL loggingHouseLogUrl;
    private LoggingHouseWorkersManager workersManager;

    @Override
    public String name() {
        return NAME;
    }

    public LoggingHouseClientExtension() {
    }

    LoggingHouseClientExtension(Hostname hostname,
                                TypeManager typeManager,
                                EventRouter eventRouter,
                                IdentityService identityService,
                                RemoteMessageDispatcherRegistry dispatcherRegistry,
                                DataSourceRegistry dataSourceRegistry,
                                TransactionContext transactionContext,
                                QueryExecutor queryExecutor,
                                ContractNegotiationStore contractNegotiationStore,
                                TransferProcessStore transferProcessStore,
                                AssetIndex assetIndex) {
        this.hostname = hostname;
        this.typeManager = typeManager;
        this.eventRouter = eventRouter;
        this.identityService = identityService;
        this.dispatcherRegistry = dispatcherRegistry;
        this.dataSourceRegistry = dataSourceRegistry;
        this.transactionContext = transactionContext;
        this.queryExecutor = queryExecutor;
        this.contractNegotiationStore = contractNegotiationStore;
        this.transferProcessStore = transferProcessStore;
        this.assetIndex = assetIndex;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        monitor = context.getMonitor();

        var extensionEnabled = context.getSetting(LOGGINGHOUSE_ENABLED_SETTING, true);
        if (!extensionEnabled) {
            enabled = false;
            monitor.info("Logginghouse client extension is disabled.");
            return;
        }
        enabled = true;
        monitor.info("Logginghouse client extension is enabled.");

        loggingHouseLogUrl = readUrlFromSettings(context);

        runFlywayMigrations(context);

        registerSerializerClearingHouseMessages(context);

        var store = initializeLoggingHouseMessageStore(typeManager);
        registerEventSubscriber(context, store);

        registerDispatcher(context);
        workersManager = initializeWorkersManager(context, store);
    }

    private URL readUrlFromSettings(ServiceExtensionContext context) {
        try {
            var urlString = context.getSetting(LOGGINGHOUSE_URL_SETTING, null);
            if (urlString == null) {
                throw new EdcException(String.format("Could not initialize LoggingHouseClientExtension: " +
                        "No url specified using setting %s", LOGGINGHOUSE_URL_SETTING));
            }

            return new URL(urlString);
        } catch (MalformedURLException e) {
            throw new EdcException(String.format("Could not parse setting %s to Url",
                    LOGGINGHOUSE_URL_SETTING), e);
        }
    }

    private void runFlywayMigrations(ServiceExtensionContext context) {
        var flywayService = new FlywayService(
                context.getMonitor(),
                context.getSetting(LOGGINGHOUSE_FLYWAY_REPAIR_SETTING, false),
                context.getSetting(LOGGINGHOUSE_FLYWAY_CLEAN_SETTING, false)
        );
        var migrationManager = new DatabaseMigrationManager(context.getConfig(), context.getMonitor(), flywayService);
        migrationManager.migrate();
    }

    private SqlLoggingHouseMessageStore initializeLoggingHouseMessageStore(TypeManager typeManager) {
        return new SqlLoggingHouseMessageStore(
                dataSourceRegistry,
                DatasourceProperties.LOGGING_HOUSE_DATASOURCE,
                transactionContext,
                typeManager.getMapper(),
                new PostgresDialectStatements(),
                queryExecutor
        );
    }

    private void registerEventSubscriber(ServiceExtensionContext context, LoggingHouseMessageStore loggingHouseMessageStore) {
        monitor.debug("Registering event subscriber for LoggingHouseClientExtension");

        var eventSubscriber = new LoggingHouseEventSubscriber(
                loggingHouseMessageStore,
                contractNegotiationStore,
                transferProcessStore,
                monitor);

        eventRouter.registerSync(ContractNegotiationFinalized.class, eventSubscriber);

        eventRouter.registerSync(TransferProcessRequested.class, eventSubscriber);
        eventRouter.registerSync(TransferProcessInitiated.class, eventSubscriber);
        eventRouter.registerSync(TransferProcessStarted.class, eventSubscriber);
        eventRouter.registerSync(TransferProcessCompleted.class, eventSubscriber);
        eventRouter.registerSync(TransferProcessFailed.class, eventSubscriber);
        eventRouter.registerSync(TransferProcessTerminated.class, eventSubscriber);
        context.registerService(LoggingHouseEventSubscriber.class, eventSubscriber);

        monitor.debug("Registered event subscriber for LoggingHouseClientExtension");
    }

    private void registerSerializerClearingHouseMessages(ServiceExtensionContext context) {
        monitor.debug("Registering serializers for LoggingHouseClientExtension");

        typeManager.registerContext(TYPE_MANAGER_SERIALIZER_KEY, JsonLd.getObjectMapper());
        registerCommonTypes(typeManager);

        monitor.debug("Registered serializers for LoggingHouseClientExtension");
    }

    private void registerCommonTypes(TypeManager typeManager) {
        monitor.debug("Registering serializers for LoggingHouseClientExtension");

        typeManager.registerSerializer(TYPE_MANAGER_SERIALIZER_KEY, LogMessage.class,
                new MultiContextJsonLdSerializer<>(LogMessage.class, CONTEXT_MAP));
        typeManager.registerSerializer(TYPE_MANAGER_SERIALIZER_KEY, RequestMessage.class,
                new MultiContextJsonLdSerializer<>(RequestMessage.class, CONTEXT_MAP));

        monitor.debug("Registered serializers for LoggingHouseClientExtension");
    }

    private LoggingHouseWorkersManager initializeWorkersManager(ServiceExtensionContext context, LoggingHouseMessageStore store) {
        var periodSeconds = context.getSetting(LOGGINGHOUSE_EXTENSION_WORKERS_DELAY, 10);
        var initialDelaySeconds = context.getSetting(LOGGINGHOUSE_EXTENSION_WORKERS_PERIOD, 30);
        var executor = new WorkersExecutor(Duration.ofSeconds(periodSeconds), Duration.ofSeconds(initialDelaySeconds), monitor);

        return new LoggingHouseWorkersManager(executor,
                monitor,
                context.getSetting(LOGGINGHOUSE_EXTENSION_MAX_WORKERS, 50),
                store,
                dispatcherRegistry,
                hostname,
                loggingHouseLogUrl
        );
    }

    private void registerDispatcher(ServiceExtensionContext context) {
        monitor.debug("Registering IDS dispatch sender for LoggingHouseClientExtension");

        var httpClient = context.getService(EdcHttpClient.class);
        var objectMapper = typeManager.getMapper(TYPE_MANAGER_SERIALIZER_KEY);

        var logMessageSender = new LogMessageSender(monitor, assetIndex, context.getConnectorId());
        var createProcessMessageSender = new CreateProcessMessageSender();

        var idsMultipartSender = new IdsMultipartSender(monitor, httpClient, identityService, objectMapper);
        var dispatcher = new IdsMultipartClearingRemoteMessageDispatcher(idsMultipartSender);
        dispatcher.register(logMessageSender);
        dispatcher.register(createProcessMessageSender);

        dispatcherRegistry.register(dispatcher);
    }

    @Override
    public void start() {
        if (!enabled) {
            monitor.info("Skipping start of Logginghouse client extension (disabled).");
        } else {
            monitor.info("Starting Logginghouse client extension.");
            workersManager.execute();
        }
    }

    @Override
    public void prepare() {
        ServiceExtension.super.prepare();
    }
}
