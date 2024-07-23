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

import com.truzzt.extension.logginghouse.client.tests.BaseUnitTest;
import org.eclipse.edc.connector.contract.spi.negotiation.store.ContractNegotiationStore;
import org.eclipse.edc.connector.transfer.spi.store.TransferProcessStore;
import org.eclipse.edc.spi.asset.AssetIndex;
import org.eclipse.edc.spi.event.EventRouter;
import org.eclipse.edc.spi.iam.IdentityService;
import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.edc.spi.system.Hostname;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.sql.QueryExecutor;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;
import org.eclipse.edc.transaction.spi.TransactionContext;
import org.mockito.Mock;

import static com.truzzt.extension.logginghouse.client.ConfigConstants.LOGGINGHOUSE_ENABLED_SETTING;
import static com.truzzt.extension.logginghouse.client.ConfigConstants.LOGGINGHOUSE_FLYWAY_CLEAN_SETTING;
import static com.truzzt.extension.logginghouse.client.ConfigConstants.LOGGINGHOUSE_FLYWAY_REPAIR_SETTING;
import static com.truzzt.extension.logginghouse.client.ConfigConstants.LOGGINGHOUSE_URL_SETTING;
import static com.truzzt.extension.logginghouse.client.tests.TestsConstants.LOGGING_HOUSE_URL;
import static org.mockito.Mockito.when;

public class LoggingHouseClientExtensionTest extends BaseUnitTest {

    @Mock
    private ServiceExtensionContext context;

    @Mock
    private Hostname hostname;

    @Mock
    private TypeManager typeManager;
    @Mock
    private EventRouter eventRouter;
    @Mock
    private IdentityService identityService;
    @Mock
    private RemoteMessageDispatcherRegistry dispatcherRegistry;

    @Mock
    private DataSourceRegistry dataSourceRegistry;
    @Mock
    private TransactionContext transactionContext;
    @Mock
    private QueryExecutor queryExecutor;

    @Mock
    private ContractNegotiationStore contractNegotiationStore;
    @Mock
    private TransferProcessStore transferProcessStore;
    @Mock
    private AssetIndex assetIndex;

    public void initialize_success() {
        var extension = new LoggingHouseClientExtension(hostname,
                typeManager,
                eventRouter,
                identityService,
                dispatcherRegistry,
                dataSourceRegistry,
                transactionContext,
                queryExecutor,
                contractNegotiationStore,
                transferProcessStore,
                assetIndex);

        // Mock methods calls
        when(context.getMonitor()).thenReturn(monitor);
        when(context.getSetting(LOGGINGHOUSE_ENABLED_SETTING, true)).thenReturn(true);
        when(context.getSetting(LOGGINGHOUSE_URL_SETTING, null)).thenReturn(LOGGING_HOUSE_URL);
        when(context.getSetting(LOGGINGHOUSE_FLYWAY_REPAIR_SETTING, false)).thenReturn(false);
        when(context.getSetting(LOGGINGHOUSE_FLYWAY_CLEAN_SETTING, false)).thenReturn(false);

        // Start the test
        extension.initialize(context);
    }

}
