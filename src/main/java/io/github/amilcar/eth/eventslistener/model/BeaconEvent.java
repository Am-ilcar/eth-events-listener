package io.github.amilcar.eth.eventslistener.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

/**
 * Class representing an Ethereum Beacon Chain event.
 * This class holds the event type and the raw JSON data.
 */
public class BeaconEvent {

    private final EventType eventType;
    /**
     * -- GETTER --
     *  Gets the raw JSON data of this event.
     *
     */
    @Getter
    private final JsonNode data;

    /**
     * Creates a new BeaconEvent with the specified type and data.
     *
     * @param eventType the type of the event
     * @param data the raw JSON data of the event
     */
    public BeaconEvent(EventType eventType, JsonNode data) {
        this.eventType = eventType;
        this.data = data;
    }

    /**
     * Gets the type of this event.
     *
     * @return the event type
     */
    @JsonIgnore
    public EventType getEventType() {
        return eventType;
    }

    /**
     * Returns a string representation of this event.
     *
     * @return a string representation of this event
     */
    @Override
    public String toString() {
        return "BeaconEvent{type=" + getEventType() + ", data=" + data + "}";
    }
}
