package com.truzzt.extension.logginghouse.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.truzzt.extension.logginghouse.client.flyway.migration.DatabaseMigrationManager;
import com.truzzt.extension.logginghouse.client.tests.BaseUnitTest;
import com.truzzt.extension.logginghouse.client.worker.WorkersManager;
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
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static com.truzzt.extension.logginghouse.client.ConfigConstants.LOGGINGHOUSE_ENABLED_SETTING;
import static com.truzzt.extension.logginghouse.client.ConfigConstants.LOGGINGHOUSE_EXTENSION_MAX_WORKERS;
import static com.truzzt.extension.logginghouse.client.ConfigConstants.LOGGINGHOUSE_EXTENSION_WORKERS_DELAY;
import static com.truzzt.extension.logginghouse.client.ConfigConstants.LOGGINGHOUSE_EXTENSION_WORKERS_PERIOD;
import static com.truzzt.extension.logginghouse.client.ConfigConstants.LOGGINGHOUSE_FLYWAY_CLEAN_SETTING;
import static com.truzzt.extension.logginghouse.client.ConfigConstants.LOGGINGHOUSE_FLYWAY_REPAIR_SETTING;
import static com.truzzt.extension.logginghouse.client.ConfigConstants.LOGGINGHOUSE_URL_SETTING;
import static com.truzzt.extension.logginghouse.client.tests.TestsConstants.LOGGING_HOUSE_URL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoggingHouseClientExtensionTest extends BaseUnitTest {

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

    @Mock
    private DatabaseMigrationManager flywayMigrationManager;
    @Mock
    private WorkersManager workersManager;

    @Mock
    private ServiceExtensionContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
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

        when(context.getSetting(eq(LOGGINGHOUSE_ENABLED_SETTING), any(Boolean.class))).thenReturn(true);
        when(context.getSetting(eq(LOGGINGHOUSE_URL_SETTING), isNull())).thenReturn(LOGGING_HOUSE_URL);
        when(context.getSetting(eq(LOGGINGHOUSE_FLYWAY_REPAIR_SETTING), any(Boolean.class))).thenReturn(false);
        when(context.getSetting(eq(LOGGINGHOUSE_FLYWAY_CLEAN_SETTING), any(Boolean.class))).thenReturn(false);
        when(context.getSetting(eq(LOGGINGHOUSE_EXTENSION_WORKERS_DELAY), any(Integer.class))).thenReturn(0);
        when(context.getSetting(eq(LOGGINGHOUSE_EXTENSION_WORKERS_PERIOD), any(Integer.class))).thenReturn(10);
        when(context.getSetting(eq(LOGGINGHOUSE_EXTENSION_MAX_WORKERS), any(Integer.class))).thenReturn(1);

        when(typeManager.getMapper()).thenReturn(objectMapper);
        when(typeManager.getMapper(any(String.class))).thenReturn(objectMapper);

        // Start the test
        extension.initialize(context);
    }

    @Test
    public void start_enabled() {

        var extension = new LoggingHouseClientExtension(monitor, true, eventRouter, flywayMigrationManager, workersManager);

        // Start the test
        extension.start();

        // Verify methods calls
        verify(flywayMigrationManager, times(1)).migrate();
        verify(workersManager, times(1)).execute();
    }

    @Test
    public void start_disabled() {

        var extension = new LoggingHouseClientExtension(monitor, false, eventRouter, flywayMigrationManager, workersManager);

        // Start the test
        extension.start();

        // Verify methods calls
        verify(flywayMigrationManager, never()).migrate();
        verify(workersManager, never()).execute();
    }
}
