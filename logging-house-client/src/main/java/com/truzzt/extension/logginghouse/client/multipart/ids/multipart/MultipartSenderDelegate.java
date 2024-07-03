/*
 *  Copyright (c) 2022 Fraunhofer Institute for Software and Systems Engineering
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

package com.truzzt.extension.logginghouse.client.multipart.ids.multipart;

import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.Message;
import org.eclipse.edc.spi.types.domain.message.RemoteMessage;

import java.util.List;

public interface MultipartSenderDelegate<M extends RemoteMessage, R> {

    Message buildMessageHeader(M request, DynamicAttributeToken token) throws Exception;

    String buildMessagePayload(M request) throws Exception;

    MultipartResponse<R> getResponseContent(IdsMultipartParts parts) throws Exception;

    List<Class<? extends Message>> getAllowedResponseTypes();

    Class<M> getMessageType();
}
