package com.truzzt.extension.logginghouse.client.events;

import com.truzzt.extension.logginghouse.client.spi.store.LoggingHouseMessageStore;
import com.truzzt.extension.logginghouse.client.spi.types.LoggingHouseMessage;
import com.truzzt.extension.logginghouse.client.tests.BaseUnitTest;
import org.eclipse.edc.connector.contract.spi.event.contractnegotiation.ContractNegotiationFinalized;
import org.eclipse.edc.connector.contract.spi.negotiation.store.ContractNegotiationStore;
import org.eclipse.edc.connector.transfer.spi.event.TransferProcessInitiated;
import org.eclipse.edc.connector.transfer.spi.store.TransferProcessStore;
import org.eclipse.edc.spi.event.EventEnvelope;
import org.eclipse.edc.spi.monitor.Monitor;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.ZonedDateTime;

import static com.truzzt.extension.logginghouse.client.tests.TestDataBuilder.buildContractAgreement;
import static com.truzzt.extension.logginghouse.client.tests.TestDataBuilder.buildContractNegotiation;
import static com.truzzt.extension.logginghouse.client.tests.TestDataBuilder.buildInitialTransferProcess;
import static com.truzzt.extension.logginghouse.client.tests.TestsConstants.ASSET_ID;
import static com.truzzt.extension.logginghouse.client.tests.TestsHelper.getRandomUuid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventsHandlerTest extends BaseUnitTest {

    @Mock
    private Monitor monitor;

    @Mock
    private LoggingHouseMessageStore loggingHouseMessageStore;

    @Mock
    private ContractNegotiationStore contractNegotiationStore;

    @Mock
    private TransferProcessStore transferProcessStore;

    @Test
    public void onContractNegotiationFinalized_success() {

        var handler = new EventsHandler(loggingHouseMessageStore,
                contractNegotiationStore,
                transferProcessStore,
                monitor);

        var agreement = buildContractAgreement(ASSET_ID);
        var negotiation = buildContractNegotiation(agreement);

        var event = ContractNegotiationFinalized.Builder.newInstance()
                .contractAgreement(agreement)
                .contractNegotiationId(negotiation.getId())
                .counterPartyAddress(negotiation.getCounterPartyAddress())
                .counterPartyId(negotiation.getCounterPartyId())
                .protocol(negotiation.getProtocol())
                .build();

        var eventEnvelope = EventEnvelope.Builder.newInstance()
                .id(getRandomUuid())
                .at(ZonedDateTime.now().getNano())
                .payload(event)
                .build();

        // Mock methods calls
        when(contractNegotiationStore.findById(eq(negotiation.getId()))).thenReturn(negotiation);

        // Start the test
        handler.on(eventEnvelope);

        // Verify methods calls
        verify(contractNegotiationStore, times(1)).findById(eq(negotiation.getId()));
        verify(loggingHouseMessageStore, times(1)).save(any(LoggingHouseMessage.class));
    }

    @Test
    public void onTransferProcessEvent_success() {

        var handler = new EventsHandler(loggingHouseMessageStore,
                contractNegotiationStore,
                transferProcessStore,
                monitor);

        var transferProcess = buildInitialTransferProcess(ASSET_ID, getRandomUuid());

        var event = TransferProcessInitiated.Builder.newInstance()
                .transferProcessId(transferProcess.getId())
                .build();

        var eventEnvelope = EventEnvelope.Builder.newInstance()
                .id(getRandomUuid())
                .at(ZonedDateTime.now().getNano())
                .payload(event)
                .build();

        // Mock methods calls
        when(transferProcessStore.findById(eq(transferProcess.getId()))).thenReturn(transferProcess);

        // Start the test
        handler.on(eventEnvelope);

        // Verify methods calls
        verify(transferProcessStore, times(1)).findById(eq(transferProcess.getId()));
        verify(loggingHouseMessageStore, times(1)).save(any(LoggingHouseMessage.class));
    }

}
