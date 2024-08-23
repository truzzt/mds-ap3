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

package com.truzzt.extension.logginghouse.client.tests;

import com.truzzt.extension.logginghouse.client.worker.MessageWorker;
import org.eclipse.edc.spi.system.Hostname;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockBuilder {

    public static Hostname buildHostnameMock() {
        var hostname = mock(Hostname.class);
        when(hostname.get()).thenReturn("localhost");
        return hostname;
    }

    public static MessageWorker buildMessageWorkerMock() {
        var worker = mock(MessageWorker.class);
        when(worker.getId()).thenReturn(UUID.randomUUID().toString());
        return worker;
    }

}
