sequenceDiagram
    autonumber
    Note over Sender,Counterparty: << Soroban >>

    Counterparty->>+Sender: Step 1: inputs, receiveOutput
    Sender->>+Counterparty: Step 2: inputs, changeOutput, counterpartyReceiveOutput=spendAmount+sum(counterpartyInputs)
    Counterparty->>+Sender: Step 3: sign
    Sender->>+Counterparty: Step 4: sign
    