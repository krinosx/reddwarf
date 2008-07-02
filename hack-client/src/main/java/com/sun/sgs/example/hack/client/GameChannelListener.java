/*
 * This work is hereby released into the Public Domain. 
 * To view a copy of the public domain dedication, visit 
 * http://creativecommons.org/licenses/publicdomain/ or send 
 * a letter to Creative Commons, 171 Second Street, Suite 300, 
 * San Francisco, California, 94105, USA.
 */

package com.sun.sgs.example.hack.client;

import com.sun.sgs.impl.sharedutil.HexDumper;

import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.ClientChannelListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * This abstract class is the base for all game-specific listeners.
 */
public abstract class GameChannelListener implements ClientChannelListener
{

    // the chat listener that accepts all incoming chat messages
    private ChatListener chatListener;

    /**
     * Creates an instance of <code>GameChannelListener</code>.
     */
    protected GameChannelListener(ChatListener chatListener) {
        this.chatListener = chatListener;
    }

    /**
     * {@inheritDoc}
     */
    public void leftChannel(ClientChannel channel) {
        // NOTE: the current architecture does nothing when this is
        //       called but future revisions could provide addition
        //       player notifications here
    }

    /**
     * Notifies the game that a player has either joined or left.
     */
    protected void notifyJoinOrLeave(ByteBuffer data, boolean joined) {
        byte [] bytes = new byte[data.remaining()];
        data.get(bytes);
        BigInteger sessionId = new BigInteger(1, bytes);
        if (joined)
            chatListener.playerJoined(sessionId);
        else
            chatListener.playerLeft(sessionId);
    }

    /**
     * Notifies this listener that a chat message arrived from the
     * given player.
     *
     * @param data the chat message
     */
    protected void notifyChatMessage(ByteBuffer data) {
        byte [] bytes = new byte[data.remaining()];
        data.get(bytes);
        String message = new String(bytes);
        chatListener.messageArrived(message);
    }

    /**
     * Notifies this listener of new user identifier mappings.
     *
     * @param data encoded mapping from user identifier to string
     */
    protected void addUidMappings(ByteBuffer data) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String,String> map = (Map<String,String>)(getObject(data));
        HashMap<BigInteger,String> sessionMap = new HashMap<BigInteger,String>();
        for (String hexString : map.keySet()) {
            BigInteger sessionId =
                new BigInteger(1, HexDumper.fromHexString(hexString));
            sessionMap.put(sessionId, map.get(hexString));
        }
        chatListener.addUidMappings(sessionMap);
    }

    /**
     * Retrieves a serialized object from the given buffer.
     *
     * @param data the encoded object to retrieve
     */
    protected Object getObject(ByteBuffer data) throws IOException {
        try {
            byte [] bytes = new byte[data.remaining()];
            data.get(bytes);

            ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bin);
            return ois.readObject();
        } catch (ClassNotFoundException cnfe) {
            throw new IOException(cnfe.getMessage());
        }
    }

}
