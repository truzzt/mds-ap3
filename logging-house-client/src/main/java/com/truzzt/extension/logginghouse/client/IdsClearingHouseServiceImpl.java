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

import com.truzzt.extension.logginghouse.client.messages.CreateProcessMessage;
import com.truzzt.extension.logginghouse.client.messages.LogMessage;
import org.eclipse.edc.connector.contract.spi.event.contractnegotiation.ContractNegotiationFinalized;
import org.eclipse.edc.connector.contract.spi.negotiation.store.ContractNegotiationStore;
import org.eclipse.edc.connector.contract.spi.types.agreement.ContractAgreement;
import org.eclipse.edc.connector.transfer.spi.event.TransferProcessEvent;
import org.eclipse.edc.connector.transfer.spi.store.TransferProcessStore;
import org.eclipse.edc.connector.transfer.spi.types.TransferProcess;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.event.Event;
import org.eclipse.edc.spi.event.EventEnvelope;
import org.eclipse.edc.spi.event.EventSubscriber;
import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.Hostname;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
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

        monitor.info("Creating Process in LoggingHouse");
        var logMessage = new CreateProcessMessage(clearingHouseLogUrl, connectorBaseUrl, contractAgreement.getId(), processOwners);

        try {
            dispatcherRegistry.dispatch(Object.class, logMessage);
        } catch (EdcException e) {
            if (e.getMessage().startsWith("No provider dispatcher registered for protocol")) {
                throw e;
            } else {
                monitor.severe("Unhandled exception while creating process in LoggingHouse. " + e.getMessage());
                // Print stack trace
                String errorStr;
                try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
                    e.printStackTrace(pw);
                    errorStr = sw.toString();
                } catch (IOException ex) {
                    throw new RuntimeException("Error while converting the stacktrace");
                }
                monitor.severe(errorStr);
            }
        }
    }

    public void logContractAgreement(ContractAgreement contractAgreement, URL clearingHouseLogUrl) {
        monitor.info("Logging ContractAgreement to LoggingHouse with contract id: " + contractAgreement.getId());
        var logMessage = new LogMessage(clearingHouseLogUrl, connectorBaseUrl, contractAgreement);
        dispatcherRegistry.dispatch(Object.class, logMessage);
    }

    public void logTransferProcess(TransferProcess transferProcess, URL clearingHouseLogUrl) {
        monitor.info("Logging TransferProcess to LoggingHouse");
        var logMessage = new LogMessage(clearingHouseLogUrl, connectorBaseUrl, transferProcess);
        dispatcherRegistry.dispatch(Object.class, logMessage);
    }

    @Override
    public <E extends Event> void on(EventEnvelope<E> event) {
        try {
            if (event.getPayload() instanceof ContractNegotiationFinalized contractNegotiationFinalized) {
                var contractAgreement = resolveContractAgreement(contractNegotiationFinalized);
                var pid = contractAgreement.getId();

                // Create Process
                var extendedProcessUrl = new URL(clearingHouseLogUrl + "/process/" + pid);
                try {
                    createProcess(contractAgreement, extendedProcessUrl);
                } catch (Exception e) {
                    monitor.warning("Could not create process in LoggingHouse: " + e.getMessage());
                }

                // Log Contract Agreement
                var extendedLogUrl = new URL(clearingHouseLogUrl + "/messages/log/" + pid);
                try {
                    logContractAgreement(contractAgreement, extendedLogUrl);
                } catch (Exception e) {
                    monitor.warning("Could not log ContractNegotiation to LoggingHouse: " + e.getMessage());
                }
            } else if (event.getPayload() instanceof TransferProcessEvent transferProcessEvent) {
                monitor.debug("Logging transfer event with id " + event.getId());

                var transferProcess = resolveTransferProcess(transferProcessEvent);
                var pid = transferProcess.getContractId();
                var extendedUrl = new URL(clearingHouseLogUrl + "/messages/log/" + pid);
                try {
                    logTransferProcess(transferProcess, extendedUrl);
                } catch (Exception e) {
                    monitor.warning("Could not log TransferProcess to LoggingHouse: " + e.getMessage());
                }
            }
        } catch (MalformedURLException e) {
            throw new EdcException("Could not create extended clearinghouse url.");
        }
    }

    private ContractAgreement resolveContractAgreement(ContractNegotiationFinalized contractNegotiationFinalized) throws NullPointerException {
        var contractNegotiationId = contractNegotiationFinalized.getContractNegotiationId();
        var contractNegotiation = contractNegotiationStore.findById(contractNegotiationId);
        return Objects.requireNonNull(contractNegotiation).getContractAgreement();
    }

    private TransferProcess resolveTransferProcess(TransferProcessEvent transferProcessEvent) {
        var transferProcessId = transferProcessEvent.getTransferProcessId();
        return transferProcessStore.findById(transferProcessId);
    }

    private URI getConnectorBaseUrl(Hostname hostname) throws URISyntaxException {
        return new URI(String.format("https://%s/", hostname.get()));
    }
}