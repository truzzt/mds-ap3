# LoggingHouse Client Requirements

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL
      NOT", "SHOULD", "SHOULD NOT", "RECOMMENDED",  "MAY", and
      "OPTIONAL" in this document are to be interpreted as described in
      [RFC 2119](https://datatracker.ietf.org/doc/html/rfc2119).

## Functional Requirements
- A LH client MUST report every transaction to the LH server
- A LH client MUST create a process id while negotiating a contract
  - The process id MUST be accessable (read/write) by provider and consumer.
- A Connector MUST NOT agree on any contract if it is not able to log a message to the shared process id.
- A LH client SHOULD NOT transfer any data if the transaction cannot be logged to the LH server.
- A LH client MUST buffer log messages of transfers if the LH server is unreachable.
- A LH client MUST push buffered log messages as soon as the LH server is reachable.
- A LH client SHOULD archive LH server transaction reciept.
- A LH client MAY query their transaction data from the LH server.
- A LH client MUST expose a query API to enable a connector developler to implement their use case specific functions.

## Process Words

### Transaction
A transaction is either a contract negotiation action or data transfer action.

### Process id
A process id is a unique idendifiert to group each transaction to a contract. The LH server enables a LH client to create a process id with the option to specify the owners.

### LH server
Is the logging house server that acts a central logging component.

### LH client
Is the logging house client that should be implemented in each connector.

### Log message
Is an IDS multipart "logMessage" that is immutable.

