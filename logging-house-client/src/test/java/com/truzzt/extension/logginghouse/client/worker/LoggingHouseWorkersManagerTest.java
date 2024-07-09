package com.truzzt.extension.logginghouse.client.worker;

import com.truzzt.extension.logginghouse.client.events.messages.LogMessageReceipt;
import com.truzzt.extension.logginghouse.client.spi.store.LoggingHouseMessageStore;
import com.truzzt.extension.logginghouse.client.spi.types.LoggingHouseMessage;
import org.eclipse.edc.connector.contract.spi.event.contractnegotiation.ContractNegotiationFinalized;
import org.eclipse.edc.connector.contract.spi.types.agreement.ContractAgreement;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.protocol.dsp.spi.types.HttpMessageProtocol;
import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.response.StatusResult;
import org.eclipse.edc.spi.system.Hostname;
import org.eclipse.edc.spi.types.domain.message.RemoteMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class LoggingHouseWorkersManagerTest {

    @Mock
    private WorkersExecutor executor;

    @Mock
    private Monitor monitor;

    @Mock
    private LoggingHouseMessageStore store;

    @Mock
    private RemoteMessageDispatcherRegistry dispatcherRegistry;

    @Mock
    private Hostname hostname;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        doReturn("localhost").when(hostname).get();
    }

    @Test
    public void processPending_success_singleMessage() throws MalformedURLException {

        LoggingHouseWorkersManager manager = new LoggingHouseWorkersManager(executor,
                monitor,
                1,
                store,
                dispatcherRegistry,
                hostname,
                new URL("http://localhost:7171/api")
        );

        var policy = Policy.Builder.newInstance().build();

        var agreement = ContractAgreement.Builder.newInstance()
                .id(UUID.randomUUID().toString())
                .providerId("provider")
                .consumerId("consumer")
                .contractSigningDate(ZonedDateTime.now().getNano())
                .assetId(UUID.randomUUID().toString())
                .policy(policy)
            .build();

        var event = ContractNegotiationFinalized.Builder.newInstance()
                .contractAgreement(agreement)
                .contractNegotiationId(UUID.randomUUID().toString())
                .counterPartyAddress("http://localhost:8282/protocol")
                .counterPartyId("provider")
                .protocol(HttpMessageProtocol.DATASPACE_PROTOCOL_HTTP)
            .build();

        var message = LoggingHouseMessage.Builder.newInstance()
                .id(1L)
                .eventType(ContractNegotiationFinalized.class)
                .eventId(UUID.randomUUID().toString())
                .eventToLog(event)
                .createProcess(true)
                .processId(UUID.randomUUID().toString())
                .consumerId("consumer")
                .providerId("provider")
                .createdAt(ZonedDateTime.now())
        .build();

        doReturn( List.of(message))
                .when(store)
                .listPending();

        // Mock create process call
        doReturn(CompletableFuture.completedFuture(true))
                .when(dispatcherRegistry)
                .dispatch(eq(Object.class), any(RemoteMessage.class));

        // Mock log message call
        var logMessageReceipt = new LogMessageReceipt("{}");
        doReturn(CompletableFuture.completedFuture(StatusResult.success(logMessageReceipt)))
                .when(dispatcherRegistry)
                .dispatch(eq(LogMessageReceipt.class), any(RemoteMessage.class));

        manager.processPending();

        // Verify methods calls
        verify(store, times(1))
                .listPending();

        verify(dispatcherRegistry, times(1))
                .dispatch(eq(Object.class), any(RemoteMessage.class));
        verify(dispatcherRegistry, times(1))
                .dispatch(eq(LogMessageReceipt.class), any(RemoteMessage.class));

        verify(store, times(1))
                .updateSent(eq(message.getId()), eq(logMessageReceipt.data()));
    }
}
