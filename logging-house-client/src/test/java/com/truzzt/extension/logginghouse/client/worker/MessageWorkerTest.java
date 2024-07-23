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

package com.truzzt.extension.logginghouse.client.worker;

import com.truzzt.extension.logginghouse.client.events.messages.CreateProcessReceipt;
import com.truzzt.extension.logginghouse.client.events.messages.LogMessageReceipt;
import com.truzzt.extension.logginghouse.client.spi.store.LoggingHouseMessageStore;
import com.truzzt.extension.logginghouse.client.tests.BaseUnitTest;
import org.eclipse.edc.connector.contract.spi.types.agreement.ContractAgreement;
import org.eclipse.edc.connector.transfer.spi.types.TransferProcess;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.edc.spi.response.StatusResult;
import org.eclipse.edc.spi.types.domain.message.RemoteMessage;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.concurrent.CompletableFuture;

import static com.truzzt.extension.logginghouse.client.tests.ResponseBuilder.buildContractAgreement;
import static com.truzzt.extension.logginghouse.client.tests.ResponseBuilder.buildInitialTransferProcess;
import static com.truzzt.extension.logginghouse.client.tests.ResponseBuilder.buildLoggingHouseMessage;
import static com.truzzt.extension.logginghouse.client.tests.TestsConstants.ASSET_ID;
import static com.truzzt.extension.logginghouse.client.tests.TestsConstants.LOG_MESSAGE_RESPONSE_DATA;
import static com.truzzt.extension.logginghouse.client.tests.TestsHelper.getConnectorBaseUrl;
import static com.truzzt.extension.logginghouse.client.tests.TestsHelper.getLoggingHouseUrl;
import static com.truzzt.extension.logginghouse.client.tests.TestsHelper.getRandomUuid;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MessageWorkerTest extends BaseUnitTest {

    @Mock
    private LoggingHouseMessageStore store;

    @Mock
    private RemoteMessageDispatcherRegistry dispatcherRegistry;

    @Test
    public void process_successContractAgreement() {
        var worker = new MessageWorker(monitor, dispatcherRegistry, getConnectorBaseUrl(), getLoggingHouseUrl(), store);

        var agreement = buildContractAgreement(ASSET_ID);
        var message = buildLoggingHouseMessage(ContractAgreement.class, agreement, true);

        // Mock methods calls
        var createProcessReceipt = new CreateProcessReceipt(agreement.getId());
        when(dispatcherRegistry.dispatch(eq(CreateProcessReceipt.class), any(RemoteMessage.class)))
                .thenReturn(CompletableFuture.completedFuture(StatusResult.success(createProcessReceipt)));

        var logMessageReceipt = new LogMessageReceipt(LOG_MESSAGE_RESPONSE_DATA);
        when(dispatcherRegistry.dispatch(eq(LogMessageReceipt.class), any(RemoteMessage.class)))
                .thenReturn(CompletableFuture.completedFuture(StatusResult.success(logMessageReceipt)));

        // Start the test
        worker.process(message);

        // Verify methods calls
        verify(dispatcherRegistry, times(1))
                .dispatch(eq(CreateProcessReceipt.class), any(RemoteMessage.class));
        verify(dispatcherRegistry, times(1))
                .dispatch(eq(LogMessageReceipt.class), any(RemoteMessage.class));

        verify(monitor, times(1))
                .info("Process successfully created on LoggingHouse with pid " + (createProcessReceipt.pid()));
        verify(monitor, times(1))
                .info("Received receipt successfully from LoggingHouse for message with id " + message.getEventId());

        verify(store, times(1))
                .updateSent(eq(message.getId()), eq(logMessageReceipt.data()));
    }

    @Test
    public void process_successTransferProcess() {
        var worker = new MessageWorker(monitor, dispatcherRegistry, getConnectorBaseUrl(), getLoggingHouseUrl(), store);

        var transferProcess = buildInitialTransferProcess(ASSET_ID, getRandomUuid());
        var message = buildLoggingHouseMessage(TransferProcess.class, transferProcess, false);

        // Mock methods calls
        var logMessageReceipt = new LogMessageReceipt(LOG_MESSAGE_RESPONSE_DATA);
        when(dispatcherRegistry.dispatch(eq(LogMessageReceipt.class), any(RemoteMessage.class)))
                .thenReturn(CompletableFuture.completedFuture(StatusResult.success(logMessageReceipt)));

        // Start the test
        worker.process(message);

        // Verify methods calls
        verify(dispatcherRegistry, never())
                .dispatch(eq(CreateProcessReceipt.class), any(RemoteMessage.class));
        verify(dispatcherRegistry, times(1))
                .dispatch(eq(LogMessageReceipt.class), any(RemoteMessage.class));

        verify(monitor, times(1))
                .info("Received receipt successfully from LoggingHouse for message with id " + message.getEventId());

        verify(store, times(1))
                .updateSent(eq(message.getId()), eq(logMessageReceipt.data()));
    }

    @Test
    public void process_processAlreadyExists() {
        var worker = new MessageWorker(monitor, dispatcherRegistry, getConnectorBaseUrl(), getLoggingHouseUrl(), store);

        var agreement = buildContractAgreement(ASSET_ID);
        var message = buildLoggingHouseMessage(ContractAgreement.class, agreement, true);

        // Mock methods calls
        var createProcessException = new EdcException("Error");
        when(dispatcherRegistry.dispatch(eq(CreateProcessReceipt.class), any(RemoteMessage.class)))
                .thenThrow(createProcessException);

        var logMessageReceipt = new LogMessageReceipt(LOG_MESSAGE_RESPONSE_DATA);
        when(dispatcherRegistry.dispatch(eq(LogMessageReceipt.class), any(RemoteMessage.class)))
                .thenReturn(CompletableFuture.completedFuture(StatusResult.success(logMessageReceipt)));

        // Start the test
        worker.process(message);

        // Verify methods calls
        verify(dispatcherRegistry, times(1))
                .dispatch(eq(CreateProcessReceipt.class), any(RemoteMessage.class));
        verify(dispatcherRegistry, times(1))
                .dispatch(eq(LogMessageReceipt.class), any(RemoteMessage.class));

        verify(monitor, times(1))
                .warning("CreateProcess returned error (ignore it when the process already exists): " + createProcessException.getMessage());
        verify(monitor, times(1))
                .info("Received receipt successfully from LoggingHouse for message with id " + message.getEventId());

        verify(store, times(1))
                .updateSent(eq(message.getId()), eq(logMessageReceipt.data()));
    }

    @Test
    public void process_failureSendingLogMessage() {
        var worker = new MessageWorker(monitor, dispatcherRegistry, getConnectorBaseUrl(), getLoggingHouseUrl(), store);

        var agreement = buildContractAgreement(ASSET_ID);
        var message = buildLoggingHouseMessage(ContractAgreement.class, agreement, true);

        // Mock methods calls
        var createProcessReceipt = new CreateProcessReceipt(agreement.getId());
        when(dispatcherRegistry.dispatch(eq(CreateProcessReceipt.class), any(RemoteMessage.class)))
                .thenReturn(CompletableFuture.completedFuture(StatusResult.success(createProcessReceipt)));

        var logMessageException = new EdcException("Error");
        when(dispatcherRegistry.dispatch(eq(LogMessageReceipt.class), any(RemoteMessage.class)))
                .thenThrow(logMessageException);

        // Start the test
        assertThrows(EdcException.class, () -> worker.process(message));

        // Verify methods calls
        verify(dispatcherRegistry, times(1))
                .dispatch(eq(CreateProcessReceipt.class), any(RemoteMessage.class));
        verify(dispatcherRegistry, times(1))
                .dispatch(eq(LogMessageReceipt.class), any(RemoteMessage.class));

        verify(monitor, times(1))
                .info("Process successfully created on LoggingHouse with pid " + (createProcessReceipt.pid()));
    }

    @Test
    public void createProcess_success() {
        var worker = new MessageWorker(monitor, dispatcherRegistry, getConnectorBaseUrl(), getLoggingHouseUrl(), store);

        var agreement = buildContractAgreement(ASSET_ID);
        var message = buildLoggingHouseMessage(ContractAgreement.class, agreement, true);

        // Mock methods calls
        var createProcessReceipt = new CreateProcessReceipt(agreement.getId());
        when(dispatcherRegistry.dispatch(eq(CreateProcessReceipt.class), any(RemoteMessage.class)))
                .thenReturn(CompletableFuture.completedFuture(StatusResult.success(createProcessReceipt)));

        // Start the test
        var result = worker.createProcess(message, getLoggingHouseUrl()).join();
        result.onSuccess(msg -> assertEquals(createProcessReceipt.pid(), msg.pid()));

        // Verify methods calls
        verify(dispatcherRegistry, times(1))
                .dispatch(eq(CreateProcessReceipt.class), any(RemoteMessage.class));
    }

    @Test
    public void createProcess_error() {
        var worker = new MessageWorker(monitor, dispatcherRegistry, getConnectorBaseUrl(), getLoggingHouseUrl(), store);

        var agreement = buildContractAgreement(ASSET_ID);
        var message = buildLoggingHouseMessage(ContractAgreement.class, agreement, true);

        // Mock methods calls
        var createProcessException = new EdcException("Error");
        when(dispatcherRegistry.dispatch(eq(CreateProcessReceipt.class), any(RemoteMessage.class)))
                .thenThrow(createProcessException);

        // Start the test
        assertThrows(EdcException.class, () -> worker.createProcess(message, getLoggingHouseUrl()).join());

        // Verify methods calls
        verify(dispatcherRegistry, times(1))
                .dispatch(eq(CreateProcessReceipt.class), any(RemoteMessage.class));
    }

    @Test
    public void logMessage_success() {
        var worker = new MessageWorker(monitor, dispatcherRegistry, getConnectorBaseUrl(), getLoggingHouseUrl(), store);

        var agreement = buildContractAgreement(ASSET_ID);
        var message = buildLoggingHouseMessage(ContractAgreement.class, agreement, true);

        // Mock methods calls
        var logMessageReceipt = new LogMessageReceipt(LOG_MESSAGE_RESPONSE_DATA);
        when(dispatcherRegistry.dispatch(eq(LogMessageReceipt.class), any(RemoteMessage.class)))
                .thenReturn(CompletableFuture.completedFuture(StatusResult.success(logMessageReceipt)));

        // Start the test
        var result = worker.logMessage(message, getLoggingHouseUrl()).join();
        result.onSuccess(msg -> assertEquals(logMessageReceipt.data(), msg.data()));

        // Verify methods calls
        verify(dispatcherRegistry, times(1))
                .dispatch(eq(LogMessageReceipt.class), any(RemoteMessage.class));
    }

    @Test
    public void logMessage_error() {
        var worker = new MessageWorker(monitor, dispatcherRegistry, getConnectorBaseUrl(), getLoggingHouseUrl(), store);

        var agreement = buildContractAgreement(ASSET_ID);
        var message = buildLoggingHouseMessage(ContractAgreement.class, agreement, true);

        // Mock methods calls
        var logMessageException = new EdcException("Error");
        when(dispatcherRegistry.dispatch(eq(LogMessageReceipt.class), any(RemoteMessage.class)))
                .thenThrow(logMessageException);

        // Start the test
        assertThrows(EdcException.class, () -> worker.logMessage(message, getLoggingHouseUrl()).join());

        // Verify methods calls
        verify(dispatcherRegistry, times(1))
                .dispatch(eq(LogMessageReceipt.class), any(RemoteMessage.class));
    }
}
