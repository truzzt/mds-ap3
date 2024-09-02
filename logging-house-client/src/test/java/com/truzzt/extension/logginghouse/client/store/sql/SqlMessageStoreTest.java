package com.truzzt.extension.logginghouse.client.store.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.truzzt.extension.logginghouse.client.store.sql.schema.BaseSqlDialectStatements;
import com.truzzt.extension.logginghouse.client.tests.BaseUnitTest;
import org.eclipse.edc.connector.contract.spi.types.agreement.ContractAgreement;
import org.eclipse.edc.sql.QueryExecutor;
import org.eclipse.edc.sql.ResultSetMapper;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;
import org.eclipse.edc.transaction.spi.NoopTransactionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

import static com.truzzt.extension.logginghouse.client.tests.TestDataBuilder.buildContractAgreementAsJSON;
import static com.truzzt.extension.logginghouse.client.tests.TestDataBuilder.buildLoggingHouseMessage;
import static com.truzzt.extension.logginghouse.client.tests.TestsConstants.ASSET_ID;
import static com.truzzt.extension.logginghouse.client.tests.TestsConstants.LOG_MESSAGE_RESPONSE_DATA;
import static com.truzzt.extension.logginghouse.client.tests.TestsHelper.getRandomLong;
import static org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry.DEFAULT_DATASOURCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class SqlMessageStoreTest extends BaseUnitTest {

    @Mock
    private DataSourceRegistry dataSourceRegistry;

    @Mock
    private NoopTransactionContext transactionContext;

    @Mock
    private QueryExecutor queryExecutor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    @Override
    public void setup() throws Exception {
        super.setup();

        transactionContext = new NoopTransactionContext();

        var dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(mock(Connection.class));

        when(dataSourceRegistry.resolve(any(String.class))).thenReturn(dataSource);
    }

    @Test
    public void save_success() {

        var statements = new BaseSqlDialectStatements();
        var store = new SqlMessageStore(dataSourceRegistry,
                DEFAULT_DATASOURCE,
                transactionContext,
                objectMapper,
                statements,
                queryExecutor);

        var agreement = buildContractAgreementAsJSON(ASSET_ID);
        var message = buildLoggingHouseMessage(ContractAgreement.class.getSimpleName(), agreement.toString(), true);

        // Start the test
        store.save(message);

        // Verify methods calls
        verify(queryExecutor, times(1))
                .execute(any(Connection.class),
                        eq(statements.getInsertTemplate()),
                        eq(message.getEventType()),
                        eq(message.getEventId()),
                        any(String.class),
                        eq(message.getCreateProcess()),
                        eq(message.getProcessId()),
                        eq( message.getConsumerId()),
                        eq(message.getProviderId()),
                        any(Long.class));
    }

    @Test
    public void listPending_success() {

        var statements = new BaseSqlDialectStatements();
        var store = new SqlMessageStore(dataSourceRegistry,
                DEFAULT_DATASOURCE,
                transactionContext,
                objectMapper,
                statements,
                queryExecutor);

        var agreement1 = buildContractAgreementAsJSON(ASSET_ID);
        var message1 = buildLoggingHouseMessage(ContractAgreement.class.getSimpleName(), agreement1.toString(), true);

        var agreement2 = buildContractAgreementAsJSON(ASSET_ID);
        var message2 = buildLoggingHouseMessage(ContractAgreement.class.getSimpleName(), agreement2.toString(), true);

        var messages = List.of(message1, message2);

        // Mock methods calls
        when(queryExecutor.query(any(Connection.class), eq(true), any(ResultSetMapper.class), eq(statements.getSelectPendingStatement())))
                .thenReturn(messages.stream());

        // Start the test
        var result = store.listPending();

        // Verify methods calls
        verify(queryExecutor, times(1))
            .query(any(Connection.class),
                eq(true),
                any(ResultSetMapper.class),
                eq(statements.getSelectPendingStatement()));

        // Verify test result
        assertEquals(2, result.size());
        assertEquals(message1, result.get(0));
        assertEquals(message2, result.get(1));
    }

    @Test
    public void updateSent_success() {

        var statements = new BaseSqlDialectStatements();
        var store = new SqlMessageStore(dataSourceRegistry,
                DEFAULT_DATASOURCE,
                transactionContext,
                objectMapper,
                statements,
                queryExecutor);

        var id = getRandomLong();

        // Start the test
        store.updateSent(id, LOG_MESSAGE_RESPONSE_DATA);

        // Verify methods calls
        verify(queryExecutor, times(1))
                .execute(any(Connection.class),
                        eq(statements.getUpdateSentTemplate()),
                        eq(LOG_MESSAGE_RESPONSE_DATA),
                        any(Long.class),
                        eq(id));
    }
}
