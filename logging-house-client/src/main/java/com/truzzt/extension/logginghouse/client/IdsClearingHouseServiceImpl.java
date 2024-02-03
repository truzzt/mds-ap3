/*
 *  Copyright (c) 2022 sovity GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       sovity GmbH - initial API and implementation
 *
 */

package com.truzzt.extension.logginghouse.client;

import org.eclipse.edc.connector.contract.spi.event.contractnegotiation.ContractNegotiationFinalized;
import org.eclipse.edc.connector.contract.spi.negotiation.store.ContractNegotiationStore;
import org.eclipse.edc.connector.contract.spi.types.agreement.ContractAgreement;
import org.eclipse.edc.connector.transfer.spi.event.TransferProcessTerminated;
import org.eclipse.edc.connector.transfer.spi.store.TransferProcessStore;
import org.eclipse.edc.connector.transfer.spi.types.TransferProcess;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.event.Event;
import org.eclipse.edc.spi.event.EventEnvelope;
import org.eclipse.edc.spi.event.EventSubscriber;
import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.Hostname;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IdsClearingHouseServiceImpl implements EventSubscriber {

    private final RemoteMessageDispatcherRegistry dispatcherRegistry;
    private final URI connectorBaseUrl;
    private final URL clearingHouseLogUrl;
    private final ContractNegotiationStore contractNegotiationStore;
    private final TransferProcessStore transferProcessStore;
    private final Monitor monitor;

    public IdsClearingHouseServiceImpl(
            RemoteMessageDispatcherRegistry dispatcherRegistry,
            Hostname hostname,
            URL clearingHouseLogUrl,
            ContractNegotiationStore contractNegotiationStore,
            TransferProcessStore transferProcessStore,
            Monitor monitor) {
        this.dispatcherRegistry = dispatcherRegistry;
        this.clearingHouseLogUrl = clearingHouseLogUrl;
        this.contractNegotiationStore = contractNegotiationStore;
        this.transferProcessStore = transferProcessStore;
        this.monitor = monitor;

        try {
            connectorBaseUrl = getConnectorBaseUrl(hostname);
        } catch (URISyntaxException e) {
            throw new EdcException("Could not create connectorBaseUrl. Hostname can be set using:" +
                    " edc.hostname", e);
        }
    }

    public void createProcess(ContractAgreement contractAgreement, URL clearingHouseLogUrl) {
        // Create PID
        List<String> processOwners = new ArrayList<>();
        processOwners.add(contractAgreement.getConsumerId());
        processOwners.add(contractAgreement.getProviderId());

        monitor.info("Creating Process to ClearingHouse");
        var logMessage = new CreateProcessMessage(clearingHouseLogUrl, connectorBaseUrl, contractAgreement.getId(), processOwners);
        dispatcherRegistry.dispatch(Object.class, logMessage);
    }

    public void logContractAgreement(ContractAgreement contractAgreement, URL clearingHouseLogUrl) {
        monitor.info("Logging contract agreement to ClearingHouse");
        var logMessage = new LogMessage(clearingHouseLogUrl, connectorBaseUrl, contractAgreement);
        dispatcherRegistry.dispatch(Object.class, logMessage);
    }

    public void logTransferProcess(TransferProcess transferProcess, URL clearingHouseLogUrl) {
        monitor.info("Logging transferprocess to ClearingHouse");
        var logMessage = new LogMessage(clearingHouseLogUrl, connectorBaseUrl, transferProcess);
        dispatcherRegistry.dispatch(Object.class, logMessage);
    }

    @Override
    public <E extends Event> void on(EventEnvelope<E> event) {
        try {
            if (event.getPayload() instanceof ContractNegotiationFinalized contractNegotiationFinalized) {
                var contractAgreement = resolveContractAgreement(contractNegotiationFinalized);
                var pid = contractAgreement.getId();
                var extendedUrl = new URL(clearingHouseLogUrl + "/" + pid);

                createProcess(contractAgreement, clearingHouseLogUrl);
                logContractAgreement(contractAgreement, extendedUrl);
            } else if (event.getPayload() instanceof TransferProcessTerminated transferProcessTerminated) {
                var transferProcess = resolveTransferProcess(transferProcessTerminated);
                var pid = transferProcess.getContractId();
                var extendedUrl = new URL(clearingHouseLogUrl + "/" + pid);
                logTransferProcess(transferProcess, extendedUrl);
            }
        } catch (Exception e) {
            throw new EdcException("Could not create extended clearinghouse url.");
        }
    }

    private ContractAgreement resolveContractAgreement(ContractNegotiationFinalized contractNegotiationFinalized) throws NullPointerException {
        var contractNegotiationId = contractNegotiationFinalized.getContractNegotiationId();
        var contractNegotiation = contractNegotiationStore.findById(contractNegotiationId);
        return Objects.requireNonNull(contractNegotiation).getContractAgreement();
    }

    private TransferProcess resolveTransferProcess(TransferProcessTerminated transferProcessTerminated) {
        var transferProcessId = transferProcessTerminated.getTransferProcessId();
        return transferProcessStore.findById(transferProcessId);
    }

    private URI getConnectorBaseUrl(Hostname hostname) throws URISyntaxException {
        return new URI(String.format("https://%s/", hostname.get()));
    }
}