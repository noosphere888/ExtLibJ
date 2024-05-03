sequenceDiagram
    autonumber
    Note over Sender,Counterparty: << Soroban >>

    Counterparty->>+Sender: Step 1: inputs, mixOutput, changeOutput
    Sender->>+Counterparty: Step 2: inputs, spendOutput, changeOutput, counterpartyChangeOutput -= fee/2
    Counterparty->>+Sender: Step 3: sign
    Sender->>+Counterparty: Step 4: sign
    