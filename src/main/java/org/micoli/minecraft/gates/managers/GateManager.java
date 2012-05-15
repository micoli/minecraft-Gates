package org.micoli.minecraft.gates.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.micoli.minecraft.entities.QDWorldCoord;
import org.micoli.minecraft.gates.Gates;
import org.micoli.minecraft.gates.entities.Gate;
import org.micoli.minecraft.utils.Task;

/**
 * The Class GateManager.
 */
public class GateManager {

	/** The plugin. */
	private Gates plugin;

	/** The internal array of parcels. */
	private Set<Gate> aGates;

	/** The a gates network. */
	private Map<String, LinkedList<Gate>> aGatesNetwork = new HashMap<String, LinkedList<Gate>>();

	/**
	 * Gets the a gates network.
	 * 
	 * @return the aGatesNetwork
	 */
	public final Map<String, LinkedList<Gate>> getaGatesNetwork() {
		return aGatesNetwork;
	}

	/** The non flooding blocks. */
	private HashSet<QDWorldCoord> nonFloodingBlocks = new HashSet<QDWorldCoord>();

	/** The cooldowns. */
	private HashSet<String> cooldowns = new HashSet<String>();

	/**
	 * Instantiates a new parcel manager.
	 * 
	 * @param instance
	 *            the instance
	 */
	public GateManager(Gates instance) {
		this.plugin = instance;
	}

	/**
	 * Read gates from database.
	 */
	public void readGatesFromDatabase() {
		aGates = new HashSet<Gate>();
		Iterator<Gate> gateIterator = plugin.getStaticDatabase().find(Gate.class).findList().iterator();
		if (gateIterator.hasNext()) {
			while (gateIterator.hasNext()) {
				Gate gate = gateIterator.next();
				if (gate.initFromDatabase(plugin)) {
					plugin.logger.log("loading gate %3d:%20s (%5d,%5d,%5d)", gate.getId(), gate.getNetworkID(), (int) gate.getTpX(), (int) gate.getTpY(), (int) gate.getTpZ());
					addGate(gate);
				}
			}
		}
	}

	/**
	 * Adds the gate.
	 * 
	 * @param gate
	 *            the gate
	 */
	public void addGate(Gate gate) {
		aGates.add(gate);
		String networkID = gate.getNetworkID();
		if (!aGatesNetwork.containsKey(networkID)) {
			aGatesNetwork.put(networkID, new LinkedList<Gate>());
		}
		aGatesNetwork.get(networkID).add(gate);
	}

	/**
	 * Removes the gate.
	 * 
	 * @param gateId
	 *            the gate id
	 * @return true, if successful
	 */
	public boolean removeGate(int gateId) {
		for (Gate gate : aGates) {
			if (gate.getId() == gateId) {
				aGatesNetwork.get(gate.getNetworkID()).remove(gate);
				aGates.remove(gate);
				gate.delete();
				return true;
			}
		}
		return false;
	}

	/**
	 * Player move.
	 * 
	 * @param player
	 *            the player
	 */
	public void playerMove(Player player) {
		if (isInCooldown(player.getName())) {
			return;
		}
		for (Gate gate : aGates) {
			if (gate.isPlayerInside(player)) {
				String netID = null;
				for (String key : aGatesNetwork.keySet()) {
					if (aGatesNetwork.get(key).contains(gate)) {
						netID = key;
						break;
					}
				}
				if (netID != null) {
					player.sendMessage("Using network : " + netID);
					LinkedList<Gate> network = aGatesNetwork.get(netID);
					if (network.size() < 2) {
						player.sendMessage("Not enough gates in network " + netID);
					} else {
						int idx = network.indexOf(gate);
						Gate nextGate = null;
						if (network.getLast().equals(gate)) {
							nextGate = network.getFirst();
						} else {
							nextGate = network.get(idx + 1);
						}
						gate.useGate(player, nextGate);
						addCoolDown(player.getName(), gate);
					}
				}
				break;
			}
		}
	}

	/**
	 * Adds the cool down.
	 * 
	 * @param playerName
	 *            the player name
	 * @param gate
	 *            the gate
	 */
	private void addCoolDown(String playerName, Gate gate) {
		if (!isInCooldown(playerName)) {
			Task runningTask = new Task(plugin, playerName, gate.getNetworkID()) {
				public void run() {
					removeCoolDown(getStringArg(0));
					plugin.getServer().getPlayer(getStringArg(0)).sendMessage("You can use again gates in network " + getStringArg(1));
				}
			};
			cooldowns.add(playerName);
			runningTask.startDelayed(plugin.getGateCoolDown());
		}
	}

	/**
	 * Removes the cool down.
	 * 
	 * @param playerName
	 *            the player name
	 */
	protected void removeCoolDown(String playerName) {
		if (isInCooldown(playerName)) {
			cooldowns.remove(playerName);
		}
	}

	/**
	 * Checks if is in cooldown.
	 * 
	 * @param playerName
	 *            the player name
	 * @return true, if is in cooldown
	 */
	protected boolean isInCooldown(String playerName) {
		return cooldowns.contains(playerName);
	}

	/**
	 * Adds the non flooding blocks.
	 * 
	 * @param coord
	 *            the coord
	 */
	public void addNonFloodingBlocks(QDWorldCoord coord) {
		nonFloodingBlocks.add(coord);
	}

	/**
	 * Checks if is non flooding block.
	 * 
	 * @param block
	 *            the block
	 * @return true, if is non flooding block
	 */
	public boolean isNonFloodingBlock(Block block) {
		if (!(block.getType().equals(Material.WATER) || block.getType().equals(Material.STATIONARY_WATER))) {
			return false;
		}
		QDWorldCoord coord = new QDWorldCoord(block.getLocation());
		plugin.logger.log("%d=>%s %d", nonFloodingBlocks.size(), coord.toString(), (nonFloodingBlocks.contains(coord) ? 1 : 0));
		return nonFloodingBlocks.contains(coord);
	}
}
