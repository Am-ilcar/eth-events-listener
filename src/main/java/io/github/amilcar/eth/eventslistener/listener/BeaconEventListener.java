package io.github.amilcar.eth.eventslistener.listener;

import io.github.amilcar.eth.eventslistener.model.BeaconEvent;

/**
 * Interface for listening to Ethereum Beacon Chain eventslistener.
 * Implementations of this interface will be notified when eventslistener are received.
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