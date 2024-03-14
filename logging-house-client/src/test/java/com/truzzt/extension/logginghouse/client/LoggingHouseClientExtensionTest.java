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
 *
 */

package com.truzzt.extension.logginghouse.client;

import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoggingHouseClientExtensionTest {

    private LoggingHouseClientExtension extension;

    @Mock
    private ServiceExtensionContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        extension = new LoggingHouseClientExtension();
    }

    @Test
    void name_shouldReturnCorrectName() {
        assertEquals(LoggingHouseClientExtension.LOGGINGHOUSE_CLIENT_EXTENSION, extension.name());
    }
}
