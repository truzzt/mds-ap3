/*
 *  Copyright (c) 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package com.truzzt.extension.logginghouse.client.worker;

import com.truzzt.extension.logginghouse.client.events.messages.CreateProcessMessage;
import com.truzzt.extension.logginghouse.client.events.messages.LogMessage;
import com.truzzt.extension.logginghouse.client.spi.store.LoggingHouseMessageStore;
import com.truzzt.extension.logginghouse.client.spi.types.LoggingHouseMessage;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.response.StatusResult;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MessageWorker {
    private final Monitor monitor;
    private final RemoteMessageDispatcherRegistry dispatcherRegistry;
    private final URI connectorBaseUrl;
    private final URL loggingHouseUrl;
    private final LoggingHouseMessageStore store;
    private final String workerId;

    public MessageWorker(Monitor monitor, RemoteMessageDispatcherRegistry dispatcherRegistry, URI connectorBaseUrl, URL loggingHouseUrl,
                         LoggingHouseMessageStore store) {
        this.monitor = monitor;
        this.dispatcherRegistry = dispatcherRegistry;
        this.connectorBaseUrl = connectorBaseUrl;
        this.loggingHouseUrl = loggingHouseUrl;
        this.store = store;

        workerId = "Worker-" + UUID.randomUUID();
    }

    public String getId() {
        return workerId;
    }

    public CompletableFuture<Boolean> run(LoggingHouseMessage message) {
        try {
            monitor.debug("Worker " + workerId + " processing message with event of type " + message.getEventType() + " and id " + message.getEventId());
            process(message);

            return CompletableFuture.completedFuture(true);

        } catch (Exception e) {
            monitor.severe(e.getMessage());
            return CompletableFuture.failedFuture(new EdcException(e));
        }
    }

    public void process(LoggingHouseMessage message) {
        try {
            var pid = message.getProcessId();

            // Create Process
            if (message.getCreateProcess()) {
                var extendedProcessUrl = new URL(loggingHouseUrl + "/process/" + pid);
                try {
                    createProcess(message, extendedProcessUrl).join();
                } catch (Exception e) {
                    // TODO: Not fail when process already exists
                    monitor.severe("CreateProcess returned error: " + e.getMessage());
                    //throw new EdcException("Could not create process in LoggingHouse", e);
                }
            }

            // Log Message
            var extendedLogUrl = new URL(loggingHouseUrl + "/messages/log/" + pid);
            try {
                var response = logMessage(message, extendedLogUrl).join();
                response.onSuccess(msg -> {
                    monitor.info("Received receipt successfully from LoggingHouse for message with id " + message.getEventId());
                    store.updateSent(message.getId(), msg);
                });
            } catch (Exception e) {
                throw new EdcException("Could not log message to LoggingHouse", e);
            }
        } catch (MalformedURLException e) {
            throw new EdcException("Could not create extended clearinghouse url.");
        }
    }

    public CompletableFuture<StatusResult<Object>> createProcess(LoggingHouseMessage message, URL loggingHouseUrl) {

        List<String> processOwners = new ArrayList<>();
        processOwners.add(message.getConsumerId());
        processOwners.add(message.getProviderId());

        monitor.info("Creating process in LoggingHouse with id: " + message.getProcessId());
        var logMessage = new CreateProcessMessage(loggingHouseUrl, connectorBaseUrl, message.getProcessId(), processOwners);

        return dispatcherRegistry.dispatch(Object.class, logMessage);
    }

    public CompletableFuture<StatusResult<String>> logMessage(LoggingHouseMessage message, URL clearingHouseLogUrl) {

        monitor.info("Logging message to LoggingHouse with type " + message.getEventType() + " and id " + message.getEventId());
        var logMessage = new LogMessage(clearingHouseLogUrl, connectorBaseUrl, message.getEventToLog());

        return dispatcherRegistry.dispatch(String.class, logMessage);
    }

}
