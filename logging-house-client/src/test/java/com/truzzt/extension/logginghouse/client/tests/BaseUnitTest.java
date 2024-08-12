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

import org.eclipse.edc.spi.monitor.Monitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public abstract class BaseUnitTest {

    @Mock
    protected Monitor monitor;

    protected AutoCloseable mocks;

    @BeforeEach
    public void setup() {
        this.mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void clean() throws Exception {
        mocks.close();
    }
}
