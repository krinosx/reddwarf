/*
 * Copyright 2007-2009 Sun Microsystems, Inc.
 *
 * This file is part of Project Darkstar Server.
 *
 * Project Darkstar Server is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation and
 * distributed hereunder to you.
 *
 * Project Darkstar Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sun.sgs.impl.service.data.store.cache;

import static com.sun.sgs.impl.sharedutil.Objects.checkNull;
import java.io.IOException;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.net.InetAddress;
import java.rmi.Remote;

/** Defines the network interface for the caching data store server. */
public interface CachingDataStoreServer extends Remote {

    /**
     * Registers a node with the data server, supplying a callback server that
     * the data server can use to make callback requests.  Returns the ID for
     * the new node and the socket port that the node should connect to for
     * sending updates to the data server.
     *
     * @param	callbackServer the callback server
     * @return	the node ID and socket port
     * @throws	IllegalArgumentException if {@code nodeId} has already been
     *		registered
     * @throws	IOException if a network problem occurs
     */
    RegisterNodeResult registerNode(CallbackServer callbackServer)
	throws IOException;

    /** The results of a call to {@link #registerNode}. */
    public static class RegisterNodeResult implements Serializable {

	/** The version of the serialized form. */
	private static final long serialVersionUID = 1;

	/** The node ID for the new node. */
	public final long nodeId;

	/** The socket port. */
	public final int socketPort;

	/**
	 * Creates an instance of this class.
	 *
	 * @param	nodeId the node ID for the new node
	 * @param	socketPort the socket port for the update queue server
	 */
	public RegisterNodeResult(long nodeId, int socketPort) {
	    this.nodeId = nodeId;
	    this.socketPort = socketPort;
	}
    }

    /* -- Object methods -- */

    /**
     * Reserves a block of object IDs to use for allocating new objects.
     *
     * @param	numIds the number of object IDs to reserve
     * @return	the first new object ID
     * @throws	IllegalArgumentException if {@code numIds} is less than
     *		{@code 1}
     * @throws	IOException if a network problem occurs
     */
    long newObjectIds(int numIds) throws IOException;

    /**
     * Obtains read access to an object.
     *
     * @param	nodeId the ID of the requesting node
     * @param	oid the object ID
     * @return	information about the object, or {@code null} if the object is
     *		not found
     * @throws	IllegalArgumentException if {@code nodeId} has not been
     *		registered or if {@code oid} is negative
     * @throws	IOException if a network problem occurs
     */
    GetObjectResults getObject(long nodeId, long oid) throws IOException;

    /** The results of a call to {@link #getObject}. */
    class GetObjectResults implements Serializable {

	/** The version of the serialized form. */
	private static final long serialVersionUID = 1;

	/** The object data. */
	public final byte[] data;

	/** Whether there is a conflicting write request. */
	public final boolean callbackEvict;

	/**
	 * Creates an instance of this class.
	 *
	 * @param	data the object data
	 * @param	callbackEvict whether this is a conflicting write
	 *		request
	 */
	public GetObjectResults(byte[] data, boolean callbackEvict) {
	    checkNull("data", data);
	    this.data = data;
	    this.callbackEvict = callbackEvict;
	}
    }

    /**
     * Obtains write access to an object.
     *
     * @param	nodeId the ID of the requesting node
     * @param	oid the object ID
     * @return	information about the object, or {@code null} if the object is
     *		not found
     * @throws	IllegalArgumentException if {@code nodeId} has not been
     *		registered or if {@code oid} is negative
     * @throws	IOException if a network problem occurs
     */
    GetObjectForUpdateResults getObjectForUpdate(long nodeId, long oid)
	throws IOException;

    /** The results of a call to {@link #getObjectForUpdate}. */
    final class GetObjectForUpdateResults extends GetObjectResults {

	/** The version of the serialized form. */
	private static final long serialVersionUID = 1;

	/** Whether there is a conflicting read request. */
	public final boolean callbackDowngrade;

	/**
	 * Creates an instance of this class.
	 *
	 * @param	data the object data
	 * @param	callbackEvict whether there is a conflicting write
	 *		request
	 * @param	callbackDowngrade whether there is a conflicting read
	 *		request
	 */
	public GetObjectForUpdateResults(
	    byte[] data, boolean callbackEvict, boolean callbackDowngrade)
	{
	    super(data, callbackEvict);
	    this.callbackDowngrade = callbackDowngrade;
	}
    }

    /**
     * Upgrades access to an object from read to write access.
     *
     * @param	nodeId the ID of the requesting node
     * @param	oid the object ID
     * @return	whether there is a conflicting read request
     * @throws	CacheConsistencyException if the requesting node does not have
     *		read access for the object ID
     * @throws	IllegalArgumentException if {@code nodeId} has not been
     *		registered or if {@code oid} is negative
     * @throws	IOException if a network problem occurs
     */
    boolean upgradeObject(long nodeId, long oid)
	throws CacheConsistencyException, IOException;

    /**
     * Returns information about the next object after the object with the
     * specified ID.  If {@code objectId} is {@code -1}, then returns
     * information about the first object.  The results returned by this method
     * will not refer to objects that have already been removed, and may not
     * refer to objects created after an iteration has begun.  It is not an
     * error to specify the identifier for an object that has already been
     * removed.
     *
     * @param	nodeId the ID of the requesting node
     * @param	oid the identifier of the object to search after, or
     *		{@code -1} to request the first object
     * @return	information about the next object, or {@code null} if there are
     *		no more objects
     * @throws	IllegalArgumentException if {@code nodeId} has not been
     *		registered or if {@code oid} is negative
     * @throws	IOException if a network problem occurs
     */
    NextObjectResults nextObjectId(long nodeId, long oid) throws IOException;

    /** The results of a call to {@link #nextObjectId}. */
    final class NextObjectResults implements Serializable {

	/** The version of the serialized form. */
	private static final long serialVersionUID = 1;

	/** The ID of the next object. */
	public final long oid;

	/** The object data associated with the next object. */
	public final byte[] data;

	/** Whether there is a conflicting write request. */
	public final boolean callbackEvict;

	/**
	 * Creates an instance of this class.
	 *
	 * @param	oid the next object ID
	 * @param	data the object data
	 * @param	callbackEvict whether there is a conflicting write
	 *		request
	 */
	public NextObjectResults(
	    long oid, byte[] data, boolean callbackEvict) {
	    if (oid < 0) {
		throw new IllegalArgumentException(
		    "The oid argument must not be negative");
	    }
	    checkNull("data", data);
	    this.oid = oid;
	    this.data = data;
	    this.callbackEvict = callbackEvict;
	}
    }

    /* -- Name binding methods -- */

    /**
     * Obtains read access to a name binding.  If the name is not bound,
     * obtains read access to the next name binding instead.
     *
     * @param	nodeId the ID of the requesting node
     * @param	name the name
     * @return	information about the name binding
     * @throws	IllegalArgumentException if {@code nodeId} has not been
     *		registered
     * @throws	IOException if a network problem occurs
     */
    GetBindingResults getBinding(long nodeId, String name) throws IOException;

    /**
     * The results of a call to {@link #getBinding}. <p>
     *
     * If {@link #found} is {@code true}, then the name is bound.  In that
     * case, {@link #nextName} is {@code null}, {@link #oid} is the object ID
     * that is bound to the name, and {@link #callbackEvict} records whether
     * there is a conflicting write request for the name. <p>
     *
     * If {@code found} is {@code false}, then the name was not bound.  In that
     * case, {@code nextName} is the next bound name following the name, or
     * {@code null} if there is no next bound name, {@code oid} is the object
     * ID that is bound to that next name, or {@code -1} if {@code nextName} is
     * {@code null}, and {@code callbackEvict} records whether there is a
     * conflicting write request for the next name.
     */
    class GetBindingResults implements Serializable {

	/** The version of the serialized form. */
	private static final long serialVersionUID = 1;

	/** Whether the requested name is bound. */
	public final boolean found;

	/**
	 * The next bound name after the requested name, or {@code null} if
	 * either there is no next bound name or {@code found} is {@code true}.
	 */
	public final String nextName;

	/**
	 * The object ID associated with the requested name, if {@code found}
	 * is {@code true}, else the object ID of the next name, if {@code
	 * nextName} is not {@code null}, else {@code -1}.
	 */
	public final long oid;

	/**
	 * Whether there is a conflicting write request for the requested name,
	 * if {@code found} is {@code true}, else of the next name, if {@code
	 * found} is {@code false}.
	 */
	public final boolean callbackEvict;

	/**
	 * Creates an instance of this class.
	 *
	 * @param	found whether the requested name was found
	 * @param	nextName the next bound name or {@code null}
	 * @param	oid the object ID or {@code -1}
	 * @param	callbackEvict whether there is a conflicting write
	 *		request
	 */
	public GetBindingResults(
	    boolean found, String nextName, long oid, boolean callbackEvict)
	{
	    this.found = found;
	    this.nextName = nextName;
	    this.oid = oid;
	    this.callbackEvict = callbackEvict;
	}	    
    }

    /**
     * Obtains write access to a name binding.  If the name is not bound,
     * obtains write access to the next name binding instead.
     *
     * @param	nodeId the ID of the requesting node
     * @param	name the name
     * @return	information about the name binding
     * @throws	IllegalArgumentException if {@code nodeId} has not been
     *		registered
     * @throws	IOException if a network problem occurs
     */
    GetBindingForUpdateResults getBindingForUpdate(long nodeId, String name)
	throws IOException;

    /** The results of a call to {@link #getBindingForUpdate}. */
    final class GetBindingForUpdateResults extends GetBindingResults {

	/** The version of the serialized form. */
	private static final long serialVersionUID = 1;
	
	/** Whether there is a conflicting read request. */
	public final boolean callbackDowngrade;

	/**
	 * Creates an instance of this class.
	 *
	 * @param	found whether the requested name was found
	 * @param	nextName the next bound name
	 * @param	oid the object ID
	 * @param	callbackEvict whether there is a conflicting write
	 *		request
	 * @param	callbackDowngrade whether there is a conflicting read
	 *		request
	 */
	public GetBindingForUpdateResults(boolean found,
					  String nextName,
					  long oid,
					  boolean callbackEvict,
					  boolean callbackDowngrade)
	{
	    super(found, nextName, oid, callbackEvict);
	    this.callbackDowngrade = callbackDowngrade;
	}	    
    }

    /**
     * Obtains access to a name binding needed for removing the binding,
     * including write access to the name binding if it is bound.  If the name
     * is bound, obtains write access to the next bound name; if it is not
     * bound, obtains read access to the next bound name.
     *
     * @param	nodeId the ID of the requesting node
     * @param	name the name
     * @return	information about the object ID and the next name
     * @throws	IllegalArgumentException if {@code nodeId} has not been
     *		registered
     * @throws	IOException if a network problem occurs
     */
    GetBindingForRemoveResults getBindingForRemove(long nodeId, String name)
	throws IOException;

    /**
     * The results of a call to {@link #getBindingForRemove}. <p>
     *
     * In all cases, {@link #nextName} is the next bound name following the
     * requested name, or {@code null} if there is no next bound name, {@link
     * #nextOid} is the object ID that is bound to that next name, or {@code
     * -1} if {@code nextName} is {@code null}, and {@link #nextCallbackEvict}
     * records whether there is a conflicting write request for the next
     * name. <p>
     *
     * If {@link #found} is {@code true}, then the name is bound.  In that
     * case, {@link #oid} is the object ID that is bound to the requested name,
     * {@link #callbackEvict} records whether there is a conflicting write
     * request for the requested name, {@link #callbackDowngrade} records
     * whether there is a conflicting read request for the name, and {@link
     * #nextCallbackDowngrade} records whether there is a conflicting read
     * request for the next name. <p>
     *
     * If {@code found} is {@code false}, then the name is not bound.  In that
     * case, {@code oid} is {@code -1}, and {@code callbackEvict}, {@code
     * callbackDowngrade}, and {@code nextCallbackDowngrade} are {@code false}.
     */
    class GetBindingForRemoveResults implements Serializable {

	/** The version of the serialized form. */
	private static final long serialVersionUID = 1;

	/** Whether the requested name is bound. */
	public final boolean found;

	/**
	 * The next bound name after the requested one, or {@code null} if
	 * there is no next bound name.
	 */
	public final String nextName;

	/**
	 * The object ID associated with the requested name, if {@code found}
	 * is {@code true}, else {@code -1}.
	 */
	public final long oid;

	/**
	 * Whether there is a conflicting write request for the requested name.
	 */
	public final boolean callbackEvict;

	/**
	 * Whether there is a conflicting read request for the requested name.
	 */
	public final boolean callbackDowngrade;

	/**
	 * The object ID associated with the next bound name or {@code -1} if
	 * there is no next bound name.
	 */
	public final long nextOid;

	/**
	 * Whether there is a conflicting write request for the next bound
	 * name.
	 */
	public final boolean nextCallbackEvict;

	/**
	 * Whether there is a conflicting read request for the next bound name.
	 */
	public final boolean nextCallbackDowngrade;

	/**
	 * Creates an instance of this class.
	 *
	 * @param	found whether the name is bound
	 * @param	nextName the next bound name
	 * @param	oid the object ID
	 * @param	callbackEvict whether there is a conflicting write
	 *		request for the name
	 * @param	callbackDowngrade whether there is a conflicting
	 *		read request for the name
	 * @param	nextOid the object ID for the next bound name
	 * @param	nextCallbackEvict whether there is a conflicting
	 *		write request for the next name
	 * @param	nextCallbackDowngrade whether there is a conflicting
	 *		read request for the next name
	 */
	public GetBindingForRemoveResults(boolean found,
					  String nextName,
					  long oid,
					  boolean callbackEvict,
					  boolean callbackDowngrade,
					  long nextOid,
					  boolean nextCallbackEvict,
					  boolean nextCallbackDowngrade)
	{
	    this.found = found;
	    this.nextName = nextName;
	    this.oid = oid;
	    this.callbackEvict = callbackEvict;
	    this.callbackDowngrade = callbackDowngrade;
	    this.nextOid = nextOid;
	    this.nextCallbackEvict = nextCallbackEvict;
	    this.nextCallbackDowngrade = nextCallbackDowngrade;
	}
    }

    /**
     * Returns information about the next name after the specified name that
     * has a binding, or {@code null} if there are no more bound names.  If
     * {@code name} is {@code null}, then the search starts at the beginning.
     *
     * @param	nodeId the ID of the requesting node
     * @param	name the name
     * @return	information about the next bound name
     * @throws	IllegalArgumentException if {@code nodeId} has not been
     *		registered
     * @throws	IOException if a network problem occurs
     */
    NextBoundNameResults nextBoundName(long nodeId, String name)
	throws IOException;

    /**
     * The results of a call to {@link #nextBoundName}. <p>
     *
     * The {@link #nextName} field is the next bound name following the
     * requested name, or {@code null} if there is no next bound name.  The
     * {@link #oid} field is the object ID that is bound to that next name, or
     * {@code -1} if {@code nextName} is {@code null}.  The {@link
     * #callbackEvict} field records whether there is a conflicting write
     * request for the next name.
     */
    final class NextBoundNameResults implements Serializable {

	/** The version of the serialized form. */
	private static final long serialVersionUID = 1;

	/**
	 * The next bound name after the requested one, or {@code null} if
	 * there is no next bound name.
	 */
	public final String nextName;

	/**
	 * The object ID associated with the next name, or {@code -1} if {@code
	 * nextName} is {@code null}.
	 */
	public final long oid;

	/** Whether there is a conflicting write request for the next name. */
	public final boolean callbackEvict;

	/**
	 * Creates an instance of this class.
	 *
	 * @param	nextName the next name or {@code null}
	 * @param	oid the object ID or {@code -1}
	 * @param	callback whether there is a conflicting write request
	 */
	public NextBoundNameResults(
	    String nextName, long oid, boolean callbackEvict)
	{
	    this.nextName = nextName;
	    this.oid = oid;
	    this.callbackEvict = callbackEvict;
	}
    }

    /* -- Class info methods -- */

    /**
     * Returns the class ID to represent classes with the specified class
     * information.  Obtains an existing ID for the class information if
     * present; otherwise, stores the information and returns the new ID
     * associated with it.  Class IDs are always greater than {@code 0}.  The
     * class information is the serialized form of the {@link
     * ObjectStreamClass} instance that serialization uses to represent the
     * class.
     *
     * @param	classInfo the class information
     * @return	the associated class ID
     * @throws	IOException if a network problem occurs
     */
    int getClassId(byte[] classInfo) throws IOException;

    /**
     * Returns the class information associated with the specified class ID.
     * The class information is the serialized form of the {@link
     * ObjectStreamClass} instance that serialization uses to represent the
     * class.
     *
     * @param	classId the class ID
     * @return	the associated class information or {@code null} if the ID is
     *		not found
     * @throws	IllegalArgumentException if {@code classId} is not greater
     *		than {@code 0}
     * @throws	IOException if a network problem occurs
     */
    byte[] getClassInfo(int classId) throws IOException;
}