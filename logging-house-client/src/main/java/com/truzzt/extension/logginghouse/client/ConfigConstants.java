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

package com.truzzt.extension.logginghouse.client;

public class ConfigConstants {

    static final String LOGGINGHOUSE_ENABLED = "edc.logginghouse.client.enabled";

    static final String LOGGINGHOUSE_SERVER_URL_SETTING = "edc.logginghouse.client.server.url";

    static final String LOGGINGHOUSE_FLYWAY_REPAIR_SETTING = "edc.logginghouse.client.flyway.repair";

    static final String LOGGINGHOUSE_FLYWAY_CLEAN_SETTING = "edc.logginghouse.client.flyway.clean";

    static final String LOGGINGHOUSE_EXTENSION_MAX_WORKERS = "edc.logginghouse.client.workers.max";

    static final String LOGGINGHOUSE_EXTENSION_WORKERS_DELAY = "edc.logginghouse.client.workers.delay";

    static final String LOGGINGHOUSE_EXTENSION_WORKERS_PERIOD = "edc.logginghouse.client.workers.period";
}
