/*
 * This work is hereby released into the Public Domain. 
 * To view a copy of the public domain dedication, visit 
 * http://creativecommons.org/licenses/publicdomain/ or send 
 * a letter to Creative Commons, 171 Second Street, Suite 300, 
 * San Francisco, California, 94105, USA.
 */

package com.sun.sgs.example.hack.client;

import com.sun.sgs.client.simple.SimpleClient;

import com.sun.sgs.example.hack.share.CharacterStats;
import com.sun.sgs.example.hack.share.Commands;
import com.sun.sgs.example.hack.share.Commands.Command;
import com.sun.sgs.example.hack.share.CreatureInfo;
import com.sun.sgs.example.hack.share.CreatureInfo.CreatureType;

import java.nio.ByteBuffer;

import java.util.HashSet;


/**
 * This manager handles all messages from and to the creator on the
 * server.
 */
public class CreatorManager implements CreatorListener
{

    private SimpleClient client = null;

    // the listeners
    private HashSet<CreatorListener> listeners;

    /**
     * Creates a new instance of <code>CreatorManager</code>.
     */
    public CreatorManager() {
        listeners = new HashSet<CreatorListener>();
    }

    /**
     * Adds a listener that will receive any changes made by the
     * {@code CreatorManager}.
     *
     * @param listener the listener to add
     */
    public void addCreatorListener(CreatorListener listener) {
        listeners.add(listener);
    }

    /**
     * Sets the client that this class uses for all communication with
     * the game server. This method may only be called once during the
     * lifetime of the client.
     *
     * @param simpleClient the client that will use this class
     */
    public void setClient(SimpleClient simpleClient) {
        if (client == null)
            client = simpleClient;
    }

    /**
     * Requests new statistics be rolled for the given character
     *
     * @param charClass the type of character
     */
    public void rollForStats(CreatureType characterClassType) {
        ByteBuffer bb = ByteBuffer.allocate(8);

        bb.putInt(Commands.encode(Command.ROLL_FOR_STATS));
        bb.putInt(CreatureInfo.encodeCreatureType(characterClassType));
        bb.rewind();

        try {
            client.send(bb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Requests that the server create the current character as one owned
     * by the player. This ends the character creation.
     *
     * @param name the character's name
     */
    public void createCurrentCharacter(String name) {
        ByteBuffer bb = ByteBuffer.allocate(4 + name.length());

        bb.putInt(Commands.encode(Command.CREATE_CURRENT_CLIENT_CHARACTER));
        bb.put(name.getBytes());
        bb.rewind();

        try {
            client.send(bb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Requests that character creation finish without creating a character.
     */
    public void cancelCreation() {
        ByteBuffer bb = ByteBuffer.allocate(4);

        bb.putInt(Commands.encode(Command.CANCEL_CURRENT_CHARACTER_CREATION));
        bb.rewind();

        try {
            client.send(bb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Notifies the listener of new character statistics.
     *
     * @param id the character's identifier
     * @param stats the new statistics
     */
    public void changeStatistics(CreatureType characterClassType, 
				 CharacterStats stats) {
        for (CreatorListener listener : listeners)
            listener.changeStatistics(characterClassType, stats);
    }

}