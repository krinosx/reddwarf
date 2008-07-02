/*
 * This work is hereby released into the Public Domain. 
 * To view a copy of the public domain dedication, visit 
 * http://creativecommons.org/licenses/publicdomain/ or send 
 * a letter to Creative Commons, 171 Second Street, Suite 300, 
 * San Francisco, California, 94105, USA.
 */

package com.sun.sgs.example.hack.client;

import com.sun.sgs.client.ClientChannel;

import com.sun.sgs.example.hack.share.CharacterStats;
import com.sun.sgs.example.hack.share.GameMembershipDetail;

import java.io.IOException;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import java.util.Collection;
import java.util.Map;


/**
 * This class listens for all messages from the lobby.
 */
public class LobbyChannelListener extends GameChannelListener
{

    // the listener that gets notified on incoming messages
    private LobbyListener llistener;

    /**
     * Creates an instance of <code>LobbyListener</code>.
     *
     * @param lobbyListener the listener for all lobby messages
     * @param chatListener the listener for all chat messages
     */
    public LobbyChannelListener(LobbyListener lobbyListener,
                                ChatListener chatListener) {
        super(chatListener);

        this.llistener = lobbyListener;
    }

    /**
     * Notifies this listener that some data has arrived from a given
     * player. This should only be called with messages that pertain to
     * the lobby.
     *
     * @param channel the channel on which this data was received
     * @param data the data received
     */
    public void receivedMessage(ClientChannel channel, ByteBuffer data) {

	// if this is a message from the server, then it's some
	// command that we need to process, so get the command code
	int command = (int)(data.get());
	
	// NOTE: for added robustness, the list of commands should
	//       really be an enumeration
	try {
	    switch (command) {
	    case 0:
		// we got some uid to player name mapping
		addUidMappings(data);
		break;
	    case 8:
		notifyJoinOrLeave(data, true);
		break;
	    case 9:
		notifyJoinOrLeave(data, true);
		break;
	    case 11:
		// we were sent game membership updates
		@SuppressWarnings("unchecked")
                    Collection<GameMembershipDetail> details =
		    (Collection<GameMembershipDetail>)(getObject(data));
		for (GameMembershipDetail detail : details) {
		    // for each update, see if it's about the lobby
		    // or some specific dungeon
		    if (! detail.getGame().equals("game:lobby")) {
			// it's a specific dungeon, so add the game and
			// set the initial count
			llistener.gameAdded(detail.getGame());
			llistener.playerCountUpdated(detail.getGame(),
						     detail.getCount());
		    } else {
			// it's the lobby, so update the count
			llistener.playerCountUpdated(detail.getCount());
		    }
		}
		break;
	    case 12: {
		// we got a membership count update for some game
		int count = data.getInt();
		byte [] bytes = new byte[data.remaining()];
		data.get(bytes);
		String name = new String(bytes);
                    
		// see if it's the lobby or some specific dungeon, and
		// update the count appropriately
		if (name.equals("game:lobby"))
		    llistener.playerCountUpdated(count);
		else
		    llistener.playerCountUpdated(name, count);
		break; }
	    case 13: {
		// we heard about a new game
		byte [] bytes = new byte[data.remaining()];
		data.get(bytes);
		llistener.gameAdded(new String(bytes));
		break; }
	    case 14: {
		// we heard that a game was removed
		byte [] bytes = new byte[data.remaining()];
		data.get(bytes);
		llistener.gameRemoved(new String(bytes));
		break; }
	    case 15: {
		// we got updated with some character statistics...these
		// are characters that the client is allowed to play
		@SuppressWarnings("unchecked")
                    Collection<CharacterStats> characters =
		    (Collection<CharacterStats>)(getObject(data));
		llistener.setCharacters(characters);
		break; }
	    default:		    
		// someone must have sent us a chat message since
		// the first byte didn't start with a known
		// command
		notifyChatMessage(data);
	    }
	} catch (IOException ioe) {
	    // NOTE: this should probably handle the error a little more
	    // gracefully, but it's unclear what the right approach is
	    System.out.println("Failed to handle incoming Lobby object");
	    ioe.printStackTrace();
	}
    }

}
