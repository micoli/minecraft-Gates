package org.micoli.minecraft.gates.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.micoli.minecraft.gates.Gates;
import org.micoli.minecraft.gates.managers.GateManager;

/**
 * The listener interface for receiving QDPlayer events. The class that is
 * interested in processing a QDPlayer event implements this interface, and the
 * object created with that class is registered with a component using the
 * component's <code>addQDPlayerListener<code> method. When
 * the QDPlayer event occurs, that object's appropriate
 * method is invoked.
 * 
 * @see QDPlayerEvent
 */
public class GatesPlayerListener implements Listener {

	/** The plugin. */
	Gates plugin;
	GateManager gateManager = null;

	/**
	 * Instantiates a new gates player listener.
	 * 
	 * @param plugin
	 *            the plugin
	 */
	public GatesPlayerListener(Gates plugin) {
		this.plugin = plugin;
	}

	/**
	 * On block from to.
	 *
	 * @param event the event
	 */
	/*
	too much CPU usage 
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockFromTo(BlockFromToEvent event) {
		if(!plugin.isWithFlowControl()){
			return;
		}
		//event.setCancelled(true);
		if (event.isCancelled())
			return;

		Block block = event.getBlock();
		if (plugin.getGateManager().isNonFloodingBlock(block)) {
			event.setCancelled(true);
		}
	}
	*/

	/**
	 * On player move.
	 * 
	 * @param event
	 *            the event
	 */
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(gateManager==null){
			gateManager=plugin.getGateManager();	
		}
		gateManager.playerMove(event.getPlayer());
	}
}
