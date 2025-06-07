package dev.amilcar.eth.events.model;

import lombok.Getter;

/**
 * Enum representing the different types of events that can be subscribed to
 * in the Ethereum Beacon Chain.
 */
@Getter
public enum EventType {
    /**
     * Sent when a new head is added to the canonical chain.
     */
    HEAD("head"),
    
    /**
     * Sent when a new block is received.
     */
    BLOCK("block"),
    
    /**
     * Sent when a new block is received via gossip.
     */
    BLOCK_GOSSIP("block_gossip"),
    
    /**
     * Sent when a new attestation is received.
     */
    ATTESTATION("attestation"),
    
    /**
     * Sent when a new attestation is received for a specific validator.
     */
    SINGLE_ATTESTATION("single_attestation"),
    
    /**
     * Sent when a new voluntary exit is received.
     */
    VOLUNTARY_EXIT("voluntary_exit"),
    
    /**
     * Sent when a new BLS to execution change is received.
     */
    BLS_TO_EXECUTION_CHANGE("bls_to_execution_change"),
    
    /**
     * Sent when a new proposer slashing is received.
     */
    PROPOSER_SLASHING("proposer_slashing"),
    
    /**
     * Sent when a new attester slashing is received.
     */
    ATTESTER_SLASHING("attester_slashing"),
    
    /**
     * Sent when a new finalized checkpoint is received.
     */
    FINALIZED_CHECKPOINT("finalized_checkpoint"),
    
    /**
     * Sent when a chain reorganization occurs.
     */
    CHAIN_REORG("chain_reorg"),
    
    /**
     * Sent when a new contribution and proof is received.
     */
    CONTRIBUTION_AND_PROOF("contribution_and_proof"),
    
    /**
     * Sent when a new light client finality update is received.
     */
    LIGHT_CLIENT_FINALITY_UPDATE("light_client_finality_update"),
    
    /**
     * Sent when a new light client optimistic update is received.
     */
    LIGHT_CLIENT_OPTIMISTIC_UPDATE("light_client_optimistic_update"),
    
    /**
     * Sent when new payload attributes are received.
     */
    PAYLOAD_ATTRIBUTES("payload_attributes"),
    
    /**
     * Sent when a new blob sidecar is received.
     */
    BLOB_SIDECAR("blob_sidecar");

    /**
     * -- GETTER --
     *  Returns the string value of the event type as expected by the Ethereum API.
     *
     */
    private final String value;
    
    EventType(String value) {
        this.value = value;
    }

    /**
     * Returns the EventType enum value for the given string value.
     *
     * @param value the string value of the event type
     * @return the corresponding EventType enum value
     * @throws IllegalArgumentException if no matching EventType is found
     */
    public static EventType fromValue(String value) {
        for (EventType eventType : EventType.values()) {
            if (eventType.getValue().equals(value)) {
                return eventType;
            }
        }
        throw new IllegalArgumentException("Unknown event type: " + value);
    }
}