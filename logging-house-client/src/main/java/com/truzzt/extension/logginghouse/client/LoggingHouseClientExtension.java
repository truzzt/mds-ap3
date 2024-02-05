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
 *
 */

package com.truzzt.extension.logginghouse.client;

import com.truzzt.extension.logginghouse.client.ids.jsonld.JsonLd;
import com.truzzt.extension.logginghouse.client.ids.multipart.IdsMultipartSender;
import de.fraunhofer.iais.eis.LogMessage;
import de.fraunhofer.iais.eis.RequestMessage;
import org.eclipse.edc.connector.contract.spi.event.contractnegotiation.ContractNegotiationAccepted;
import org.eclipse.edc.connector.contract.spi.event.contractnegotiation.ContractNegotiationAgreed;
import org.eclipse.edc.connector.contract.spi.event.contractnegotiation.ContractNegotiationFinalized;
import org.eclipse.edc.connector.contract.spi.negotiation.store.ContractNegotiationStore;
import org.eclipse.edc.connector.transfer.spi.event.TransferProcessCompleted;
import org.eclipse.edc.connector.transfer.spi.event.TransferProcessInitiated;
import org.eclipse.edc.connector.transfer.spi.event.TransferProcessStarted;
import org.eclipse.edc.connector.transfer.spi.event.TransferProcessTerminated;
import org.eclipse.edc.connector.transfer.spi.store.TransferProcessStore;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.event.EventRouter;
import org.eclipse.edc.spi.http.EdcHttpClient;
import org.eclipse.edc.spi.iam.IdentityService;
import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.Hostname;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class LoggingHouseClientExtension implements ServiceExtension {


    public static final String LOGGINGHOUSE_CLIENT_EXTENSION = "LoggingHouseClientExtension";
    private static final String TYPE_MANAGER_SERIALIZER_KEY = "ids-clearinghouse";

    @Inject
    private TypeManager typeManager;
    @Inject
    private EventRouter eventRouter;
    @Inject
    private IdentityService identityService;
    @Inject
    private RemoteMessageDispatcherRegistry dispatcherRegistry;

    @Inject
    private Hostname hostname;

    @Inject
    private ContractNegotiationStore contractNegotiationStore;
    @Inject
    private TransferProcessStore transferProcessStore;

    private static final Map<String, String> CONTEXT_MAP = Map.of(
            "cat", "http://w3id.org/mds/data-categories#",
            "ids", "https://w3id.org/idsa/core/",
            "idsc", "https://w3id.org/idsa/code/");
    @Setting
    public static final String LOGGINGHOUSE_LOG_URL_SETTING = "edc.logginghouse.extension.url";

    @Setting
    public static final String LOGGINGHOUSE_CLIENT_EXTENSION_ENABLED = "edc.logginghouse.extension.enabled";

    private URL loggingHouseLogUrl;
    public Monitor monitor;

    private boolean enabled;


    @Override
    public String name() {
        return LOGGINGHOUSE_CLIENT_EXTENSION;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        monitor = context.getMonitor();
        var extensionEnabled = context.getSetting(LOGGINGHOUSE_CLIENT_EXTENSION_ENABLED, true);

        if (!extensionEnabled) {
            enabled = false;
            monitor.info("Logginghouse client extension is disabled.");
            return;
        }
        enabled = true;
        monitor.info("Logginghouse client extension is enabled.");

        loggingHouseLogUrl = readUrlFromSettings(context);

        registerSerializerClearingHouseMessages(context);
        registerClearingHouseMessageSenders(context);

        registerEventSubscriber(context);
    }

    private URL readUrlFromSettings(ServiceExtensionContext context) {
        try {
            var urlString = context.getSetting(LoggingHouseClientExtension.LOGGINGHOUSE_LOG_URL_SETTING, null);
            if (urlString == null) {
                throw new EdcException(String.format("Could not initialize " +
                        "LoggingHouseClientExtension: " +
                        "No url specified using setting %s", LoggingHouseClientExtension.LOGGINGHOUSE_LOG_URL_SETTING));
            }

            return new URL(urlString);
        } catch (MalformedURLException e) {
            throw new EdcException(String.format("Could not parse setting %s to Url",
                    LoggingHouseClientExtension.LOGGINGHOUSE_LOG_URL_SETTING), e);
        }
    }

    private void registerEventSubscriber(ServiceExtensionContext context) {
        monitor.debug("Registering event subscriber for LoggingHouseClientExtension");

        var eventSubscriber = new IdsClearingHouseServiceImpl(
                dispatcherRegistry,
                hostname,
                loggingHouseLogUrl,
                contractNegotiationStore,
                transferProcessStore,
                monitor);

        eventRouter.registerSync(ContractNegotiationFinalized.class, eventSubscriber);
        eventRouter.registerSync(ContractNegotiationAgreed.class, eventSubscriber);
        eventRouter.registerSync(ContractNegotiationAccepted.class, eventSubscriber);
        eventRouter.registerSync(TransferProcessCompleted.class, eventSubscriber);
        eventRouter.registerSync(TransferProcessTerminated.class, eventSubscriber);
        eventRouter.registerSync(TransferProcessInitiated.class, eventSubscriber);
        eventRouter.registerSync(TransferProcessStarted.class, eventSubscriber);
        context.registerService(IdsClearingHouseServiceImpl.class, eventSubscriber);

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

    private void registerClearingHouseMessageSenders(ServiceExtensionContext context) {
        monitor.debug("Registering message senders for LoggingHouseClientExtension");

        var httpClient = context.getService(EdcHttpClient.class);
        var monitor = context.getMonitor();
        var objectMapper = typeManager.getMapper(TYPE_MANAGER_SERIALIZER_KEY);

        var logMessageSender = new LogMessageSender();
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
        }
    }

    @Override
    public void prepare() {
        ServiceExtension.super.prepare();
    }
}
