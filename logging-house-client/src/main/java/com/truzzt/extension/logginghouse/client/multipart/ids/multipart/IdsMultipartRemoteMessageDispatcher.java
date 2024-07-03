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

package com.truzzt.extension.logginghouse.client.multipart.ids.multipart;

import org.eclipse.edc.connector.transfer.spi.types.protocol.TransferCompletionMessage;
import org.eclipse.edc.connector.transfer.spi.types.protocol.TransferStartMessage;
import org.eclipse.edc.connector.transfer.spi.types.protocol.TransferTerminationMessage;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.message.RemoteMessageDispatcher;
import org.eclipse.edc.spi.response.StatusResult;
import org.eclipse.edc.spi.types.domain.message.RemoteMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class IdsMultipartRemoteMessageDispatcher implements RemoteMessageDispatcher {

    public static final String PROTOCOL = "ids-multipart";

    private final IdsMultipartSender multipartSender;
    private final Map<Class<? extends RemoteMessage>, MultipartSenderDelegate<? extends RemoteMessage, ?>> delegates = new HashMap<>();
    private final List<Class<? extends RemoteMessage>> unsupportedMessages = List.of(
            TransferStartMessage.class,
            TransferCompletionMessage.class,
            TransferTerminationMessage.class
    );

    public IdsMultipartRemoteMessageDispatcher(IdsMultipartSender idsMultipartSender) {
        this.multipartSender = idsMultipartSender;
    }

    public <M extends RemoteMessage, R> void register(MultipartSenderDelegate<M, R> delegate) {
        delegates.put(delegate.getMessageType(), delegate);
    }

    @Override
    public String protocol() {
        return PROTOCOL;
    }

    @Override
    public <T, M extends RemoteMessage> CompletableFuture<StatusResult<T>> dispatch(Class<T> responseType, M message) {
        Objects.requireNonNull(message, "Message was null");

        if (unsupportedMessages.stream().anyMatch(it -> it.isInstance(message))) { // these messages are not supposed to be sent on ids-multipart.
            return CompletableFuture.completedFuture(null);
        }

        var delegate = (MultipartSenderDelegate<M, T>) delegates.get(message.getClass());
        if (delegate == null) {
            throw new EdcException("Message sender not found for message type: " + message.getClass().getName());
        }

        return multipartSender.send(message, delegate);
    }

}