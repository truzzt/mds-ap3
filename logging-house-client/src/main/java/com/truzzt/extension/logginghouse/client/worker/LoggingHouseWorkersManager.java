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

import com.truzzt.extension.logginghouse.client.spi.store.LoggingHouseMessageStore;
import com.truzzt.extension.logginghouse.client.spi.types.LoggingHouseMessage;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.Hostname;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;

public class LoggingHouseWorkersManager {

    private final WorkersExecutor executor;
    private final Monitor monitor;
    private final int maxWorkers;
    private final LoggingHouseMessageStore store;
    private final RemoteMessageDispatcherRegistry dispatcherRegistry;
    private final URI connectorBaseUrl;
    private final URL loggingHouseUrl;

    public LoggingHouseWorkersManager(WorkersExecutor executor,
                                      Monitor monitor,
                                      int maxWorkers,
                                      LoggingHouseMessageStore store,
                                      RemoteMessageDispatcherRegistry dispatcherRegistry,
                                      Hostname hostname,
                                      URL loggingHouseUrl) {
        this.executor = executor;
        this.monitor = monitor;
        this.maxWorkers = maxWorkers;
        this.store = store;
        this.dispatcherRegistry = dispatcherRegistry;
        this.loggingHouseUrl = loggingHouseUrl;

        try {
            connectorBaseUrl = getConnectorBaseUrl(hostname);
        } catch (URISyntaxException e) {
            throw new EdcException("Could not create connectorBaseUrl. Hostname can be set using:" + hostname, e);
        }
    }

    public void execute() {
        executor.run(() -> {
            processPending();
        });

    }

    private void processPending() {
        List<LoggingHouseMessage> messages = store.listPending();
        if (messages.isEmpty()) {
            monitor.warning("No Messages to send, aborting execution");
            return;
        }
        monitor.debug(log("Loaded " + messages.size() + " not sent messages from store"));
        var allItems = new ArrayBlockingQueue<>(messages.size(), true, messages);

        monitor.debug(log("Instantiate workers..."));

        var actualNumWorkers = Math.min(allItems.size(), maxWorkers);
        monitor.debug(format(log("Worker parallelism is %s, based on config and number of not sent messages"), actualNumWorkers));
        var availableWorkers = createWorkers(actualNumWorkers);

        while (!allItems.isEmpty()) {
            var worker = nextAvailableWorker(availableWorkers);
            if (worker == null) {
                monitor.debug(log("No worker available, will retry later"));
                continue;
            }

            var item = allItems.poll();
            if (item == null) {
                monitor.warning(log("WorkItem queue empty, abort execution"));
                break;
            }

            worker.run(item)
                    .whenComplete((updateResponse, throwable) -> {
                        if (throwable != null) {
                            monitor.severe(log(format("Unexpected exception happened during in worker %s", worker.getId())), throwable);
                        } else {
                            monitor.info(log(format("Worker [%s] is done", worker.getId())));
                        }
                        availableWorkers.add(worker);
                    });
        }
    }

    @Nullable
    private MessageWorker nextAvailableWorker(ArrayBlockingQueue<MessageWorker> availableWorkers) {
        MessageWorker worker = null;
        try {
            monitor.debug(log("Getting next available worker"));
            worker = availableWorkers.poll(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            monitor.debug("interrupted while waiting for worker to become available");
        }
        return worker;
    }

    @NotNull
    private ArrayBlockingQueue<MessageWorker> createWorkers(int numWorkers) {

        return new ArrayBlockingQueue<>(numWorkers, true, IntStream.range(0, numWorkers)
                .mapToObj(i -> new MessageWorker(monitor, dispatcherRegistry, connectorBaseUrl, loggingHouseUrl, store))
                .collect(Collectors.toList()));
    }

    private String log(String input) {
        return "LoggingHouseWorkersManager: " + input;
    }

    private URI getConnectorBaseUrl(Hostname hostname) throws URISyntaxException {
        return new URI(String.format("https://%s/", hostname.get()));
    }
}
