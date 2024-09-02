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
import com.truzzt.extension.logginghouse.client.tests.TestsHelper;
import org.eclipse.edc.connector.contract.spi.types.agreement.ContractAgreement;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.Hostname;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.truzzt.extension.logginghouse.client.tests.MockBuilder.buildHostnameMock;
import static com.truzzt.extension.logginghouse.client.tests.MockBuilder.buildMessageWorkerMock;
import static com.truzzt.extension.logginghouse.client.tests.ResponseBuilder.buildContractAgreementAsJSON;
import static com.truzzt.extension.logginghouse.client.tests.ResponseBuilder.buildLoggingHouseMessage;
import static com.truzzt.extension.logginghouse.client.tests.TestsConstants.ASSET_ID;
import static com.truzzt.extension.logginghouse.client.tests.TestsHelper.buildQueue;
import static com.truzzt.extension.logginghouse.client.tests.TestsHelper.getLoggingHouseUrl;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkersManagerTest extends BaseUnitTest {

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

    private WorkersManager buildWorkersManager(int maxWorkers, Queue<MessageWorker> workers) {
        return new ManagerMockedWorkers(monitor,
                null,
                null,
                maxWorkers,
                store,
                dispatcherRegistry,
                hostname,
                getLoggingHouseUrl(),
                workers
        );
    }

    @Test
    void execute_success() {

        AtomicBoolean ran = new AtomicBoolean(false);
        var task = new Runnable() {
            @Override
            public void run() {
                ran.getAndSet(true);
            }
        };

        var manager = new ManagerMockedProcess(monitor,
                Duration.ofSeconds(1),
                Duration.ofSeconds(0),
                0,
                null,
                null,
                hostname,
                null,
                task
        );

        var scheduler = manager.execute();
        TestsHelper.sleep(5);

        assertTrue(ran.get());

        scheduler.shutdownNow();
    }
    
    @Test
    void processPending_successSingleMessage() {

        var worker = buildMessageWorkerMock();
        when(worker.run(any(LoggingHouseMessage.class))).thenReturn(CompletableFuture.completedFuture(true));
        var workers = buildQueue(List.of(worker));

        var manager = buildWorkersManager(workers.size(), workers);

        var agreement = buildContractAgreementAsJSON(ASSET_ID);
        var message = buildLoggingHouseMessage(ContractAgreement.class.getSimpleName(), agreement.toString(), true);

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

        var agreement1 = buildContractAgreementAsJSON(ASSET_ID);
        var message1 = buildLoggingHouseMessage(ContractAgreement.class.getSimpleName(), agreement1.toString(), true);

        var agreement2 = buildContractAgreementAsJSON(ASSET_ID);
        var message2 = buildLoggingHouseMessage(ContractAgreement.class.getSimpleName(), agreement2.toString(), true);

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

    @Test
    void buildMessageWorker_success() {
        var manager = new WorkersManager(monitor,
                null,
                null,
                1,
                store,
                dispatcherRegistry,
                hostname,
                getLoggingHouseUrl()
        );

        var worker = manager.buildMessageWorker();
        assertNotNull(worker);
    }

    static class ManagerMockedWorkers extends WorkersManager {

        private final Queue<MessageWorker> workers;

        ManagerMockedWorkers(Monitor monitor,
                            Duration schedule,
                            Duration initialDelay,
                            int maxWorkers,
                            LoggingHouseMessageStore store,
                            RemoteMessageDispatcherRegistry dispatcherRegistry,
                            Hostname hostname,
                            URL loggingHouseUrl,
                            Queue<MessageWorker> workers) {
            super(monitor, schedule, initialDelay, maxWorkers, store, dispatcherRegistry, hostname, loggingHouseUrl);
            this.workers = workers;
        }

        @Override
        MessageWorker buildMessageWorker() {
            return workers.peek();
        }
    }

    static class ManagerMockedProcess extends WorkersManager {

        private final Runnable task;

        ManagerMockedProcess(Monitor monitor,
                             Duration schedule,
                             Duration initialDelay,
                             int maxWorkers,
                             LoggingHouseMessageStore store,
                             RemoteMessageDispatcherRegistry dispatcherRegistry,
                             Hostname hostname,
                             URL loggingHouseUrl,
                             Runnable task) {
            super(monitor, schedule, initialDelay, maxWorkers, store, dispatcherRegistry, hostname, loggingHouseUrl);
            this.task = task;
        }

        @Override
        void processPending() {
            task.run();
        }
    }
}
