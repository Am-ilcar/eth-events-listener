package dev.amilcar.eth.events.client;

import dev.amilcar.eth.events.listener.BeaconEventListener;
import dev.amilcar.eth.events.model.BeaconEvent;
import dev.amilcar.eth.events.model.EventType;

import java.io.Closeable;
import java.util.Set;

/**
 * Client for subscribing to Ethereum Beacon Chain events.
 * This interface defines the main API for the library.
 */
public interface BeaconEventClient extends Closeable {
    
    /**
     * Subscribes to events of the specified types.
     * 
     * @param eventTypes the types of events to subscribe to
     * @return this client instance for method chaining
     */
    BeaconEventClient subscribe(Set<EventType> eventTypes);
    
    /**
     * Subscribes to events of the specified type.
     * 
     * @param eventType the type of events to subscribe to
     * @return this client instance for method chaining
     */
    default BeaconEventClient subscribe(EventType eventType) {
        return subscribe(Set.of(eventType));
    }
    
    /**
     * Adds a listener for events of the specified type.
     * 
     * @param eventType the type of events to listen for
     * @param listener the listener to add
     * @param <T> the type of event
     * @return this client instance for method chaining
     */
    <T extends BeaconEvent> BeaconEventClient addEventListener(EventType eventType, BeaconEventListener<T> listener);
    
    /**
     * Removes a listener for events of the specified type.
     * 
     * @param eventType the type of events to stop listening for
     * @param listener the listener to remove
     * @param <T> the type of event
     * @return this client instance for method chaining
     */
    <T extends BeaconEvent> BeaconEventClient removeEventListener(EventType eventType, BeaconEventListener<T> listener);
    
    /**
     * Starts listening for events.
     * This method establishes the connection to the Ethereum node and begins receiving events.
     * 
     * @return this client instance for method chaining
     */
    BeaconEventClient start();
    
    /**
     * Stops listening for events.
     * This method closes the connection to the Ethereum node and stops receiving events.
     * 
     * @return this client instance for method chaining
     */
    BeaconEventClient stop();
    
    /**
     * Checks if the client is currently connected and listening for events.
     * 
     * @return true if the client is connected, false otherwise
     */
    boolean isConnected();
    
    /**
     * Gets the URL of the Ethereum node that this client is connected to.
     * 
     * @return the URL of the Ethereum node
     */
    String getNodeUrl();
}