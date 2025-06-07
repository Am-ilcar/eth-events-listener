package dev.amilcar.eth.events.listener;

import dev.amilcar.eth.events.model.BeaconEvent;

/**
 * Interface for listening to Ethereum Beacon Chain events.
 * Implementations of this interface will be notified when events are received.
 *
 * @param <T> the type of event to listen for
 */
@FunctionalInterface
public interface BeaconEventListener<T extends BeaconEvent> {
    
    /**
     * Called when an event is received.
     *
     * @param event the event that was received
     */
    void onEvent(T event);
}