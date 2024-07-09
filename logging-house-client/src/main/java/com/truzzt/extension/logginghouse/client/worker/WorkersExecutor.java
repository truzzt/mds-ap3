/*
 *  Copyright (c) 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - Initial implementation
 *
 */

package com.truzzt.extension.logginghouse.client.worker;

import org.eclipse.edc.spi.monitor.Monitor;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WorkersExecutor {
    private final Duration schedule;
    private final Duration withInitialDelay;
    private final Monitor monitor;

    public WorkersExecutor(Duration schedule, Duration initialDelay, Monitor monitor) {
        this.schedule = schedule;
        withInitialDelay = initialDelay;
        this.monitor = monitor;
    }

    public void run(Runnable task) {
        var ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate(execute(task), withInitialDelay.toMillis(), schedule.toMillis(), TimeUnit.MILLISECONDS);
    }

    private Runnable execute(Runnable original) {
        return () -> {
            try {
                original.run();
            } catch (Throwable thr) {
                monitor.severe("Unexpected error during plan execution", thr);
            }
        };
    }
}
