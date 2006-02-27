package com.sun.gi.logic;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import com.sun.gi.comm.routing.ChannelID;
import com.sun.gi.comm.routing.UserID;
import com.sun.gi.comm.users.server.SGSUser;
import com.sun.gi.framework.rawsocket.SimRawSocketListener;
import com.sun.gi.objectstore.ObjectStore;
import com.sun.gi.objectstore.Transaction;

/**
 * <p>Title: SimTask</p>
 * <p>Description: This interface defines a class that encapsulates
 * the exection of a Game Logic Manager task</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Sun Microsystems, TMI</p>
 * @author Jeff Kesselman
 * @version 1.0
 */

public abstract class SimTask {

    private static ThreadLocal<SimTask> current = new ThreadLocal<SimTask>();

    public enum ACCESS_TYPE {

	/** Acquire an exclusive write-lock on a GLO */
	GET,

	/** Acquire a shared read-lock on a GLO */
	PEEK,

	/** Attempt an exclusive write-lock, non-blocking */
	ATTEMPT
    };

    /**
     * Called to transfer the calling thread of control to the execution
     * of the task.
     */
    public abstract void execute();

    /**
     * A utility call used by other parts of the system.
     * It takes a Game Logic Object (GLO) ID and wraps it in a
     * GLOReference.
     *
     * @param id the GLO id
     *
     * @return a GLOReference that may be used by another GLO
     */
    public abstract GLOReference<? extends GLO> makeReference(long id);

    /**
     * Gets the transaction associated with this SimTask.  A SimTask
     * only has a transaction associated with it during execution.
     *
     * @return the associated transaction or NULL if the SimTask
     * is not currently executing.
     */
    public abstract Transaction getTransaction();


    // client functions
    // All the functions from here down are used by game application code
    // to talk to its execution context and request services from it.

    /**
     * Returns the applciation ID assigned to the game to which this
     * task belongs.
     *
     * @return the app ID
     */
    public abstract long getAppID();

    /**
     * Registers a GLO as a listener to user join/left events.
     * The listening GLO must implement the SimUserListener interface.
     *
     * @param ref A reference to the GLO to be registered.
     */
    public abstract void addUserListener(
	GLOReference<? extends SimUserListener> ref);

    /**
     * Registers a GLO as a listener to data packet arrival events.
     * It listens for data adressed to the UserID passed.
     *
     * The listening GLO must implement the SimUserDataListener interface.
     *
     * @param id The UserID that data will be adressed to to trigger
     *           this listener
     *
     * @param ref A reference to the GLO to be registered
     */
    public abstract void addUserDataListener(UserID id,
	    GLOReference<? extends SimUserDataListener> ref);

    /**
     * The game code can call this to send data to users by their IDs.
     * This actually maps to the send call down in the router layer by
     * calling the user manager created to handle this particular
     * game.
     *
     * @param cid       the channel ID
     * @param to        the list of message recipients
     * @param data      the data packet to send
     * @param reliable  true if the data should be sent reliably
     */
    public abstract void sendData(ChannelID cid, UserID[] to, ByteBuffer data,
	    boolean reliable);

    /**
     * Creates a GLO in the objectstore from the given template object.
     *
     * @param simObject the "template object" to insert into the object
     *                  store, if a GLO does not already exist with
     *                  the given name.  This object should *not* be
     *                  used after being passed to the createGLO call
     *                  -- instead, call get() on the returned
     *                  GLOReference to get the newly created object.
     *
     * @param name an optional symbolic reference to assign to the
     *             object, or null to create an anonymous GLO.
     *
     * @return A GLOReference that references the newly created GLO.
     */
    public abstract <T extends GLO> GLOReference<T> createGLO(T simObject,
	    String name);

    /**
     * Create an anonymous GLO in the objectstore.
     *
     * @param simObject the "template object" to insert into the object
     *                  store. This object should *not* be used after
     *                  being passed to the createGLO call -- instead,
     *                  call get() on the returned GLOReference to get
     *                  the newly created object.
     *
     * @return A GLOReference that references the newly created GLO
     */
    public abstract <T extends GLO> GLOReference<T> createGLO(T simObject);


    // data access functions
    // These functions are used by games to get data from the ObjectStore

    /**
     * This method is used to retrieve an GLReference based on the symbolic
     * name assigned to the GLO at the time of creation in the objectstore.
     *
     * As is everything else is the obejctstore, symbolic names are specific
     * to a game context.  (The Game's App ID is an implicit part of the key.)
     *
     * @param gloName The symbolic name to look up.
     *
     * @return A reference to the GLO if found, null if not found.
     */
    public abstract <T extends GLO> GLOReference<T> findGLO(String gloName);


    /**
     * Destroy all persistence data for the GLO referred to by ref.
     * 
     * @param ref A GLOReference to the GLO to be destroyed.
     */
    public abstract void destroyGLO(GLOReference ref);

    /**
     * This method opens a comm channel and returns an ID for it.
     * If the channel is already open, return a valid ChannelID for
     * the existing channel.  Note that channels may be referred to by
     * more than one channelID.
     *
     * @param channelName the name of the channel to create
     *
     * @return a new ChannelID for the channel created.  ChannelIDs
     *         are not unique -- a channel may be referred to by more
     *         than one channelID.
     */
    public abstract ChannelID openChannel(String channelName);

    /**
     * @param delay
     * @param repeat
     * @param ref
     *
     * @return an id for this timer event registration
     */
    public abstract long registerTimerEvent(long delay, boolean repeat,
	GLOReference<? extends SimTimerListener> ref);

    /**
     * @param access
     * @param l
     * @param b
     * @param reference
     *
     * @return an id for this timer event registration
     */
    public abstract long registerTimerEvent(ACCESS_TYPE access, long l,
	boolean b, GLOReference<? extends SimTimerListener> reference);

    /**
     * @param glo
     *
     * @return a GLOReference that points to the given GLO.
     *
     * @throws InstantiationException
     */
    public abstract <T extends GLO> GLOReference<T> makeReference(T glo)
	throws InstantiationException;

    public abstract void registerGLOID(long objID, GLO glo, ACCESS_TYPE access);

    /**
     * @param accessType
     * @param target
     * @param method
     * @param parameters
     */
    public abstract void queueTask(ACCESS_TYPE accessType,
	    GLOReference<? extends GLO> target, Method method,
	    Object[] parameters);

    /**
     * @param target
     * @param method
     * @param parameters
     */
    public abstract void queueTask(GLOReference<? extends GLO> target,
	    Method method, Object[] parameters);

    /**
     * @param accessType
     * @param glo
     */
    public abstract void access_check(ACCESS_TYPE accessType, GLO glo);


    // Hooks into the RawSocketManager, added 1/16/2006

    /**
     * Requests that a socket be opened at the given host on the given
     * port.  The returned ID can be used for future communication
     * with the socket that will be opened.  The socket ID will not be
     * valid, and therefore should not be used until the connection is
     * complete.  Connection is complete once the
     * SimRawSocketListener.socketOpened() call back is called.
     *
     * @param access	the access type (GET, PEEK, or ATTEMPT)
     * @param ref	a reference to the GLO initiating the connection
     * @param host	a String representation of the remote host
     * @param port	the remote port
     * @param reliable	if true, the connection will use a reliable protocol
     *
     * @return an identifier that can be used for future communication
     *         with the socket.
     */
    public abstract long openSocket(ACCESS_TYPE access,
	    GLOReference<? extends SimRawSocketListener> ref,
	    String host, int port, boolean reliable);

    /**
     * Sends data on the socket mapped to the given socketID.  This
     * method will not return until the entire buffer has been drained.
     *
     * @param socketID	the socket identifier.
     * @param data	the data to send.  The buffer should be in a ready
     * 			state, i.e. flipped if necessary.
     */
    public abstract void sendRawSocketData(long socketID, ByteBuffer data);

    /**
     * Requests that the socket matching the given socketID be closed.
     * The socket should not be assumed to be closed, however, until the
     * call back SimRawSocketListener.socketClosed() is called.
     *
     * @param socketID		the identifier of the socket.
     */
    public abstract void closeSocket(long socketID);

    /**
     * Joins the specified user to the Channel referenced by the
     * given ChannelID.
     *
     * @param user			the user
     * @param id			the ChannelID
     */
    public abstract void join(UserID user, ChannelID id);

    /**
     * Removes the specified user from the Channel referenced by the
     * given ChannelID.
     *
     * @param user			the user
     * @param id			the ChannelID
     */
    public abstract void leave(UserID user, ChannelID id);

    /**
     * Locks the given channel based on shouldLock.  Users cannot
     * join/leave locked channels except by way of the Router.
     *
     * @param cid         the channel ID
     * @param shouldLock  if true, will lock the channel, otherwise unlock it.
     */
    public abstract void lock(ChannelID cid, boolean shouldLock);

    /**
     * Closes the local view of the channel mapped to ChannelID.
     * Any remaining users will be notified as the channel is closing.
     *
     * @param cid  the ID of the channel to close.
     */
    public abstract void closeChannel(ChannelID cid);

    public abstract void setEvesdroppingEnabled(UserID uid, ChannelID cid,
	    boolean setting);

    /**
     * Gets the SimTask for the currently executing event
     *
     * @return the current SimTask context
     *
     * @throws ExecutionOutsideOfTaskException if called outside of
     *         the context of an SGS task dispatch.
     */
    public static SimTask getCurrent() throws ExecutionOutsideOfTaskException {

	SimTask simTask = current.get();

	if (simTask == null) {
	    throw new ExecutionOutsideOfTaskException();
	}

	return simTask;
    }

    protected void setAsCurrent(){
	current.set(this);
    }
}
