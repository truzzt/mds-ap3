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

import org.eclipse.edc.spi.EdcException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

import static com.truzzt.extension.logginghouse.client.tests.TestsConstants.CONNECTOR_BASE_URL;
import static com.truzzt.extension.logginghouse.client.tests.TestsConstants.LOGGING_HOUSE_URL;

public class TestsHelper {

    private static final Random longRandom = new Random();

    public static URI getConnectorBaseURL() {
        try {
            return new URI(CONNECTOR_BASE_URL);
        } catch (URISyntaxException e) {
            throw new EdcException("Invalid test Connector Base URL: " + CONNECTOR_BASE_URL, e);
        }
    }

    public static URL getLoggingHouseURL() {
        try {
            return new URL(LOGGING_HOUSE_URL);
        } catch (MalformedURLException e) {
            throw new EdcException("Invalid test Logging House URL: " + LOGGING_HOUSE_URL, e);
        }
    }

    public static long getRandomLong() {
        return longRandom.nextLong();
    }

    public static String getRandomUUID() {
        return UUID.randomUUID().toString();
    }

    public static <T> ArrayBlockingQueue<T> buildQueue(List<T> items) {
        return new ArrayBlockingQueue<>(items.size(), true, items);

    }
}
