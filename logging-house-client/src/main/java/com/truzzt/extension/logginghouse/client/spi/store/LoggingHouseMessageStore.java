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

package com.truzzt.extension.logginghouse.client.spi.store;

import com.truzzt.extension.logginghouse.client.spi.types.LoggingHouseMessage;

import java.util.List;

public interface LoggingHouseMessageStore {

    void save(LoggingHouseMessage message);

    List<LoggingHouseMessage> listPending();

    void updateSent(long id);
}
