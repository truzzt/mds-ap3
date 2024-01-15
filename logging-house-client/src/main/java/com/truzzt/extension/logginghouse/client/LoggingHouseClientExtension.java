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

/*import org.eclipse.edc.connector.contract.spi.negotiation.store.ContractNegotiationStore;
import org.eclipse.edc.connector.transfer.spi.store.TransferProcessStore;*/
//import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.EdcException;
//import org.eclipse.edc.spi.event.EventRouter;
//import org.eclipse.edc.spi.http.EdcHttpClient;
//import org.eclipse.edc.spi.iam.IdentityService;
//import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.edc.spi.monitor.Monitor;
//import org.eclipse.edc.spi.system.Hostname;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
//import org.eclipse.edc.spi.types.TypeManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class LoggingHouseClientExtension implements ServiceExtension {

    public static final String CATALOG_TRANSFER_EXTENSION = "LoggingHouseClientExtension";
    private static final String TYPE_MANAGER_SERIALIZER_KEY = "ids-clearinghouse";

    private static final Map<String, String> CONTEXT_MAP = Map.of(
            "cat", "http://w3id.org/mds/data-categories#",
            "ids", "https://w3id.org/idsa/core/",
            "idsc", "https://w3id.org/idsa/code/");
    @Setting
    public static final String CLEARINGHOUSE_LOG_URL_SETTING = "edc.clearinghouse.log.url";

    @Setting
    private static final String CLEARINGHOUSE_CLIENT_EXTENSION_ENABLED = "clearinghouse.client.extension.enabled";

    /*@Inject
    private IdsApiConfiguration idsApiConfiguration;

    @Inject
    private RemoteMessageDispatcherRegistry dispatcherRegistry;

    @Inject
    private IdentityService identityService;

    @Inject
    private IdsTransformerRegistry transformerRegistry;

    @Inject
    private ContractNegotiationStore contractNegotiationStore;

    @Inject
    private TransferProcessStore transferProcessStore;

    @Inject
    private Hostname hostname;

    @Inject
    private EdcHttpClient edcHttpClient;

    @Inject
    private DynamicAttributeTokenService dynamicAttributeTokenService;

    @Inject
    private EventRouter eventRouter;

    private IdsClearingHouseService idsClearingHouseService;*/

    private URL clearingHouseLogUrl;
    private Monitor monitor;


    @Override
    public String name() {
        return CATALOG_TRANSFER_EXTENSION;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        monitor = context.getMonitor();
        var extensionEnabled = context.getSetting(CLEARINGHOUSE_CLIENT_EXTENSION_ENABLED, false);

        if (!extensionEnabled) {
            monitor.info("Logginghouse client extension is disabled.");
            return;
        }
        monitor.info("Logginghouse client extension is enabled.");

        clearingHouseLogUrl = readUrlFromSettings(context, CLEARINGHOUSE_LOG_URL_SETTING);
    }

    private URL readUrlFromSettings(ServiceExtensionContext context, String settingsPath) {
        try {
            var urlString = context.getSetting(settingsPath, null);
            if (urlString == null) {
                throw new EdcException(String.format("Could not initialize " +
                        "LoggingHouseClientExtension: " +
                        "No url specified using setting %s", settingsPath));
            }

            return new URL(urlString);
        } catch (MalformedURLException e) {
            throw new EdcException(String.format("Could not parse setting %s to Url",
                    settingsPath), e);
        }
    }

    @Override
    public void start() {
        monitor.info("Starting Logginghouse client extension.");
    }

    @Override
    public void prepare() {
        ServiceExtension.super.prepare();
    }
}
