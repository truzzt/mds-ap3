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

import com.truzzt.extension.logginghouse.client.spi.store.LoggingHouseMessageStore;
import com.truzzt.extension.logginghouse.client.spi.types.LoggingHouseMessage;
import com.truzzt.extension.logginghouse.client.tests.BaseUnitTest;
import org.eclipse.edc.connector.contract.spi.types.agreement.ContractAgreement;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.Hostname;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import static com.truzzt.extension.logginghouse.client.tests.MockBuilder.buildHostnameMock;
import static com.truzzt.extension.logginghouse.client.tests.MockBuilder.buildMessageWorkerMock;
import static com.truzzt.extension.logginghouse.client.tests.ResponseBuilder.buildContractAgreement;
import static com.truzzt.extension.logginghouse.client.tests.ResponseBuilder.buildLoggingHouseMessage;
import static com.truzzt.extension.logginghouse.client.tests.TestsConstants.ASSET_ID;
import static com.truzzt.extension.logginghouse.client.tests.TestsHelper.buildQueue;
import static com.truzzt.extension.logginghouse.client.tests.TestsHelper.getLoggingHouseUrl;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoggingHouseWorkersManagerTest extends BaseUnitTest {

    @Mock
    private WorkersExecutor executor;

    @Mock
    private LoggingHouseMessageStore store;

    @Mock
    private RemoteMessageDispatcherRegistry dispatcherRegistry;

    private Hostname hostname;

    @BeforeEach
    @Override
    public void setup() {
        super.setup();
        hostname = buildHostnameMock();
    }

    private LoggingHouseWorkersManagerWrapper buildWorkersManager(int maxWorkers, Queue<MessageWorker> workers) {
        return new LoggingHouseWorkersManagerWrapper(executor,
                monitor,
                maxWorkers,
                store,
                dispatcherRegistry,
                hostname,
                getLoggingHouseUrl(),
                workers
        );
    }

    @Test
    void processPending_successSingleMessage() {

        var worker = buildMessageWorkerMock();
        when(worker.run(any(LoggingHouseMessage.class))).thenReturn(CompletableFuture.completedFuture(true));
        var workers = buildQueue(List.of(worker));

        var manager = buildWorkersManager(workers.size(), workers);

        var agreement = buildContractAgreement(ASSET_ID);
        var message = buildLoggingHouseMessage(ContractAgreement.class, agreement, true);

        // Mock methods calls
        when(store.listPending()).thenReturn(List.of(message));

        // Start the test
        manager.processPending();

        // Verify methods calls
        verify(store, times(1)).listPending();
        verify(worker, times(1)).run(message);
        verify(monitor, times(1)).info(format("LoggingHouseWorkersManager: Worker [%s] is done", worker.getId()));
    }

    @Test
    void processPending_successMultipleMessages() {

        var worker = buildMessageWorkerMock();
        when(worker.run(any(LoggingHouseMessage.class))).thenReturn(CompletableFuture.completedFuture(true));
        var workers = buildQueue(List.of(worker));

        var manager = buildWorkersManager(workers.size(), workers);

        var agreement1 = buildContractAgreement(ASSET_ID);
        var message1 = buildLoggingHouseMessage(ContractAgreement.class, agreement1, true);

        var agreement2 = buildContractAgreement(ASSET_ID);
        var message2 = buildLoggingHouseMessage(ContractAgreement.class, agreement2, true);

        // Mock methods calls
        when(store.listPending()).thenReturn(List.of(message1, message2));

        // Start the test
        manager.processPending();

        // Verify methods calls
        verify(store, times(1)).listPending();
        verify(worker, times(1)).run(message1);
        verify(worker, times(1)).run(message2);
        verify(monitor, times(2)).info(format("LoggingHouseWorkersManager: Worker [%s] is done", worker.getId()));
    }

    @Test
    void processPending_emptyPendingMessages() {

        var worker = buildMessageWorkerMock();
        when(worker.run(any(LoggingHouseMessage.class))).thenReturn(CompletableFuture.completedFuture(true));
        var workers = buildQueue(List.of(worker));

        var manager = buildWorkersManager(workers.size(), workers);

        // Mock methods calls
        when(store.listPending()).thenReturn(List.of());

        // Start the test
        manager.processPending();

        // Verify methods calls
        verify(store, times(1)).listPending();
        verify(monitor, times(1)).debug("No Messages to send, aborting execution");
        verify(worker, never()).run(any(LoggingHouseMessage.class));
    }

    @Test
    void nextAvailableWorker_success() {

        var worker = buildMessageWorkerMock();
        var workers = buildQueue(List.of(worker));
        var manager = buildWorkersManager(workers.size(), workers);

        // Start the test
        var availableWorker = manager.nextAvailableWorker(workers);

        // Assert test results
        assertEquals(availableWorker.getId(), worker.getId());
    }

    @Test
    void getConnectorBaseUrl_success() {

        var worker = buildMessageWorkerMock();
        var workers = buildQueue(List.of(worker));
        var manager = buildWorkersManager(workers.size(), workers);

        // Start the test
        var connectorBaseUrl = manager.getConnectorBaseUrl(hostname);

        // Assert test results
        assertEquals("https://localhost/", connectorBaseUrl.toString());
    }

    @Test
    void getConnectorBaseUrl_error() {

        var worker = buildMessageWorkerMock();
        var workers = buildQueue(List.of(worker));
        var manager = buildWorkersManager(workers.size(), workers);

        // Mock methods calls
        var errorHostname = mock(Hostname.class);
        when(errorHostname.get()).thenReturn("%$#@&");

        // Start the test
        assertThrows(EdcException.class, () -> manager.getConnectorBaseUrl(errorHostname));
    }

    static class LoggingHouseWorkersManagerWrapper extends LoggingHouseWorkersManager {

        private final Queue<MessageWorker> workers;

        LoggingHouseWorkersManagerWrapper(WorkersExecutor executor,
                                                 Monitor monitor,
                                                 int maxWorkers,
                                                 LoggingHouseMessageStore store,
                                                 RemoteMessageDispatcherRegistry dispatcherRegistry,
                                                 Hostname hostname,
                                                 URL loggingHouseUrl,
                                                 Queue<MessageWorker> workers) {
            super(executor, monitor, maxWorkers, store, dispatcherRegistry, hostname, loggingHouseUrl);
            this.workers = workers;
        }

        @Override
        MessageWorker buildMessageWorker(Monitor monitor,
                                         RemoteMessageDispatcherRegistry dispatcherRegistry,
                                         URI connectorBaseUrl,
                                         URL loggingHouseUrl,
                                         LoggingHouseMessageStore store) {
            return workers.peek();
        }
    }
}
