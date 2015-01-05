package io.ably.realtime;

import io.ably.transport.ConnectionManager;
import io.ably.types.ErrorInfo;

/**
 * A class representing the connection associated with an AblyRealtime instance.
 * The Connection object exposes the lifecycle and parameters of the realtime connection.
 */
public class Connection implements ConnectionStateListener {

	/**
	 * Connection states. See Ably Realtime API documentation for more details.
	 */
	public enum ConnectionState {
		initialized,
		connecting,
		connected,
		disconnected,
		suspended,
		closing,
		closed,
		failed;
	}

	/**
	 * The collection of state change listeners. Listeners can register
	 * and deregister for state change events with
	 * {@link io.ably.realtime.ConnectionStateListener.Multicaster#add}
	 * and {@link io.ably.realtime.ConnectionStateListener.Multicaster#remove}
	 */
	public final ConnectionStateListener.Multicaster listeners;

	/**
	 * The current state of this Connection.
	 */
	public ConnectionState state;

	/**
	 * Error information associated with a connection failure.
	 */
	public ErrorInfo reason;

	/**
	 * The assigned connection id.
	 */
	public String id;

	/**
	 * A public identifier for this connection, used to identify
	 * this member in presence events and message ids.
	 */
	public String memberId;

	/**
	 * The serial number of the last message to be received on this connection.
	 */
	public long serial;

	/**
	 * Causes the library to re-attempt connection, if it was previously explicitly
	 * closed by the user, or was closed as a result of an unrecoverable error.
	 */
	public void connect() {
		connectionManager.requestState(ConnectionState.connecting);
	}

	/**
	 * Send a heartbeat message to the Ably service and await a response.
	 * @param listener: a listener to be notified of the outcome of this message.
	 */
	public void ping(CompletionListener listener) {
		connectionManager.ping(listener);
	}

	/**
	 * Causes the connection to close, entering the closed state, from any state except
	 * the failed state. Once closed, the library will not attempt to re-establish the
	 * connection without a call to {@link #connect}.
	 */
	public void close() {
		id = null;
		connectionManager.requestState(ConnectionState.closing);
	}

	/*****************
	 * internal
	 *****************/

	Connection(AblyRealtime ably) {
		this.ably = ably;
		this.state = ConnectionState.initialized;
		this.listeners = new ConnectionStateListener.Multicaster();
		this.connectionManager = new ConnectionManager(ably, this);
	}

	@Override
	public void onConnectionStateChanged(ConnectionStateChange stateChange) {
		state = stateChange.current;
		reason = stateChange.reason;
		listeners.onConnectionStateChanged(stateChange);
	}

	final AblyRealtime ably;
	public final ConnectionManager connectionManager;
}
