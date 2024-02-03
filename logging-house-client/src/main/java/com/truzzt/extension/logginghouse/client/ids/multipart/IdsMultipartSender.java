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
 *       Microsoft Corporation - Use IDS Webhook address for JWT audience claim
 *       Fraunhofer Institute for Software and Systems Engineering - refactoring
 *
 */

package com.truzzt.extension.logginghouse.client.ids.multipart;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.TokenFormat;
import jakarta.ws.rs.core.MediaType;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.MultipartReader;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.http.EdcHttpClient;
import org.eclipse.edc.spi.iam.IdentityService;
import org.eclipse.edc.spi.iam.TokenParameters;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.response.StatusResult;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.types.domain.message.RemoteMessage;
import org.glassfish.jersey.media.multipart.ContentDisposition;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpHeaders;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.failedFuture;

public class IdsMultipartSender {

    private final Monitor monitor;
    private final EdcHttpClient httpClient;
    private final IdentityService identityService;
    private final ObjectMapper objectMapper;

    public IdsMultipartSender(Monitor monitor, EdcHttpClient httpClient,
                              IdentityService identityService,
                              ObjectMapper objectMapper) {
        this.monitor = monitor;
        this.httpClient = httpClient;
        this.identityService = identityService;
        this.objectMapper = objectMapper;
    }

    public <M extends RemoteMessage, R> CompletableFuture<StatusResult<R>> send(M request, MultipartSenderDelegate<M, R> senderDelegate) {
        var remoteConnectorAddress = request.getCounterPartyAddress();

        // Get Dynamic Attribute Token
        var tokenResult = obtainDynamicAttributeToken(remoteConnectorAddress);
        if (tokenResult.failed()) {
            String message = "Failed to obtain token: " + String.join(",", tokenResult.getFailureMessages());
            monitor.severe(message);
            return failedFuture(new EdcException(message));
        }

        var token = tokenResult.getContent();

        // Get recipient address
        var requestUrl = HttpUrl.parse(remoteConnectorAddress);
        if (requestUrl == null) {
            return failedFuture(new IllegalArgumentException("Connector address not specified"));
        }

        // Build IDS message header
        Message message;
        try {
            message = senderDelegate.buildMessageHeader(request, token);
        } catch (Exception e) {
            return failedFuture(e);
        }

        // Build multipart header part
        var headerPartHeaders = new Headers.Builder()
                .add("Content-Disposition", "form-data; name=\"header\"")
                .build();

        RequestBody headerRequestBody;
        try {
            headerRequestBody = RequestBody.create(
                    objectMapper.writeValueAsString(message),
                    okhttp3.MediaType.get(MediaType.APPLICATION_JSON));
        } catch (IOException exception) {
            return failedFuture(exception);
        }

        var headerPart = MultipartBody.Part.create(headerPartHeaders, headerRequestBody);

        // Build IDS message payload
        String payload;
        try {
            payload = senderDelegate.buildMessagePayload(request);
        } catch (Exception e) {
            return failedFuture(e);
        }

        // Build multipart payload part
        MultipartBody.Part payloadPart = null;
        if (payload != null) {
            var payloadRequestBody = RequestBody.create(payload,
                    okhttp3.MediaType.get(MediaType.APPLICATION_JSON));

            var payloadPartHeaders = new Headers.Builder()
                    .add("Content-Disposition", "form-data; name=\"payload\"")
                    .build();

            payloadPart = MultipartBody.Part.create(payloadPartHeaders, payloadRequestBody);
        }

        // Build multipart body
        var multipartBuilder = new MultipartBody.Builder()
                .setType(okhttp3.MediaType.get(MediaType.MULTIPART_FORM_DATA))
                .addPart(headerPart);

        if (payloadPart != null) {
            multipartBuilder.addPart(payloadPart);
        }

        var multipartRequestBody = multipartBuilder.build();

        // Build HTTP request
        var httpRequest = new Request.Builder()
                .url(requestUrl)
                .addHeader("Content-Type", MediaType.MULTIPART_FORM_DATA)
                .post(multipartRequestBody)
                .build();

        return httpClient.executeAsync(httpRequest, r -> {
            monitor.debug("Response received from connector. Status " + r.code());
            if (r.isSuccessful()) {
                try (var body = r.body()) {
                    if (body == null) {
                        throw new EdcException("Received an empty body response from connector");
                    } else {
                        var parts = extractResponseParts(body);
                        var response = senderDelegate.getResponseContent(parts);

                        checkResponseType(response, senderDelegate);

                        return StatusResult.success(response.payload());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new EdcException(format("Received an error from connector (%s): %s %s", requestUrl, r.code(), r.message()));
            }
        });
    }

    protected Result<DynamicAttributeToken> obtainDynamicAttributeToken(String recipientAddress) {
        var tokenParameters = TokenParameters.Builder.newInstance()
                .scope(IdsConstants.TOKEN_SCOPE)
                .audience(recipientAddress)
                .build();
        return identityService.obtainClientCredentials(tokenParameters)
                .map(credentials -> new DynamicAttributeTokenBuilder()
                        ._tokenFormat_(TokenFormat.JWT)
                        ._tokenValue_(credentials.getToken())
                        .build()
                );
    }

    protected IdsMultipartParts extractResponseParts(ResponseBody body) throws Exception {
        InputStream header = null;
        InputStream payload = null;
        try (var multipartReader = new MultipartReader(Objects.requireNonNull(body))) {
            MultipartReader.Part part;
            while ((part = multipartReader.nextPart()) != null) {
                var httpHeaders = HttpHeaders.of(
                        part.headers().toMultimap(),
                        (a, b) -> a.equalsIgnoreCase("Content-Disposition")
                );

                var value = httpHeaders.firstValue("Content-Disposition").orElse(null);
                if (value == null) {
                    continue;
                }

                var contentDisposition = new ContentDisposition(value);
                var multipartName = contentDisposition.getParameters().get("name");

                if ("header".equalsIgnoreCase(multipartName)) {
                    header = new ByteArrayInputStream(part.body().readByteArray());
                } else if ("payload".equalsIgnoreCase(multipartName)) {
                    payload = new ByteArrayInputStream(part.body().readByteArray());
                }
            }
        }

        return IdsMultipartParts.Builder.newInstance()
                .header(header)
                .payload(payload)
                .build();
    }

    public void checkResponseType(MultipartResponse<?> response, MultipartSenderDelegate<? extends RemoteMessage, ?> senderDelegate) {
        var type = senderDelegate.getAllowedResponseTypes();
        if (!type.contains(response.header().getClass())) {
            throw new EdcException(String.format("Received %s but expected %s.", response.header().getClass(), type));
        }
    }

}
