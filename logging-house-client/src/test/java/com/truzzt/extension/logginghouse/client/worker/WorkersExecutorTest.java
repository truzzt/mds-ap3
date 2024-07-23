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

import com.truzzt.extension.logginghouse.client.tests.BaseUnitTest;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorkersExecutorTest extends BaseUnitTest {

    @Test
    public void run_success() {
        var executor = new WorkersExecutor(Duration.ofSeconds(0), Duration.ofSeconds(0), monitor);
        var count = new AtomicInteger();
        var lock = new ReentrantLock();

        // Start the test
        var ses = Executors.newSingleThreadScheduledExecutor();
        lock.lock();
        ses.execute(executor.execute(() -> {
            count.getAndIncrement();
            lock.unlock();
        }));

        assertTrue(lock.tryLock());
        assertEquals(1, count.get());
    }
}
