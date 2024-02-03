/*
 *  Copyright (c) 2020 - 2022 Fraunhofer Institute for Software and Systems Engineering
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer Institute for Software and Systems Engineering - initial API and implementation
 *
 */

package com.truzzt.extension.logginghouse.client.ids.multipart;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.Message;

import java.io.IOException;

public class ResponseUtil {

    public static MultipartResponse<String> parseMultipartStringResponse(IdsMultipartParts parts, ObjectMapper objectMapper) throws IOException {
        var header = objectMapper.readValue(parts.getHeader(), Message.class);

        String payload = null;
        if (parts.getPayload() != null) {
            payload = new String(parts.getPayload().readAllBytes());
        }

        return new MultipartResponse<>(header, payload);
    }

}
