/*
 * Copyright 2007 Sun Microsystems, Inc.
 *
 * This file is part of Project Darkstar Server.
 *
 * Project Darkstar Server is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Project Darkstar Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sun.sgs.impl.service.data.store.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The server side of an experimental network protocol, not currently used, for
 * implementing DataStoreServer using sockets instead of RMI.
 */
/*
 * XXX: Use thread pools?
 * XXX: Reap idle connections?
 */
class DataStoreServerRemote implements Runnable {

    /* XXX: 2 hours -- same as RMI default.  Make configurable? */
    /** The number of milliseconds before closing an idle connection. */
    private static final int connectionReadTimeout = 2 * 3600 * 1000;

    /** The server socket, or null if closed. */
    ServerSocket serverSocket;

    /** The data store server, for up calls. */
    private final DataStoreServer server;

    /** Creates an instance for the specified server and port. */
    DataStoreServerRemote(DataStoreServer server, int port)
	throws IOException
    {
	serverSocket = new ServerSocket(port);
	this.server = server;
	new Thread(this, "DataStoreServerRemote").start();
    }

    /** Shuts down the server. */
    synchronized void shutdown() throws IOException {
	if (serverSocket != null) {
	    serverSocket.close();
	    serverSocket = null;
	}
    }

    /** Checks if the server is shut down. */
    private synchronized boolean isShutdown() {
	return serverSocket == null;
    }

    /** Accepts and hands off new connections until shut down. */
    public void run() {
	while (!isShutdown()) {
	    try {
		new Thread(
		    new Handler(serverSocket.accept()), "Handler").start();
	    } catch (Throwable t) {
	    }
	}
    }

    /** Handles connections. */
    private class Handler implements Runnable {

	/** The accepted socket. */
	private final Socket socket;

	/** Creates an instance for an accepted socket. */
	Handler(Socket socket) {
	    this.socket = socket;
	}

	/** Handles requests until an exception occurs. */
	public void run() {
	    try {
		try {
		    socket.setTcpNoDelay(true);
		} catch (Exception e) {
		}
		try {
		    socket.setKeepAlive(true);
		} catch (Exception e) {
		}
		if (connectionReadTimeout > 0) {
		    try {
			socket.setSoTimeout(connectionReadTimeout);
		    } catch (Exception e) {
		    }
		}
		DataStoreProtocol protocol =
		    new DataStoreProtocol(
			socket.getInputStream(), socket.getOutputStream());
		while (true) {
		    protocol.dispatch(server);
		}
	    } catch (Throwable e) {
	    } finally {
		try {
		    socket.close();
		} catch (IOException e) {
		}
	    }
	}
    }
}
