/*
 *  Copyright (c) 2021 Fraunhofer Institute for Software and Systems Engineering
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer Institute for Software and Systems Engineering - Initial implementation
 *
 */

package com.truzzt.extension.logginghouse.client.multipart.ids.multipart;

import de.fraunhofer.iais.eis.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record MultipartResponse<T>(Message header, @Nullable T payload) {

    public MultipartResponse(@NotNull Message header, @Nullable T payload) {
        this.header = header;
        this.payload = payload;
    }
}
