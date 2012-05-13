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

/**
 * The Class GateManager.
 */
public class GateManager {

	/** The plugin. */
	private Gates plugin;
	
	/** The internal array of parcels. */
	private Set<Gate> aGates;
	private Map<String,LinkedList<Gate>> aGatesNetwork = new HashMap<String,LinkedList<Gate>>();
	private HashSet<QDWorldCoord> nonFloodingBlocks = new HashSet<QDWorldCoord>();

	/**
	 * Instantiates a new parcel manager.
	 *
	 * @param instance the instance
	 */
	public GateManager(Gates instance) {
		this.plugin = instance;
	}
	public void readGatesFromDatabase(){
		aGates = new HashSet<Gate>();
		Iterator<Gate> gateIterator = plugin.getStaticDatabase().find(Gate.class).findList().iterator();
		if (gateIterator.hasNext()) {
			while (gateIterator.hasNext()) {
				Gate gate = gateIterator.next();
				if(gate.initFromDatabase(plugin)){
					plugin.logger.log("loading gate %3d:%20s (%5d,%5d,%5d)",gate.getId(),gate.getNetworkID(),(int)gate.getTpX(),(int)gate.getTpY(),(int)gate.getTpZ());
					addGate(gate);
				}
			}
		}
	}
	
	public void addGate(Gate gate){
		aGates.add(gate);
		String networkID = gate.getNetworkID();
		if(!aGatesNetwork.containsKey(networkID)){
			aGatesNetwork.put(networkID,new LinkedList<Gate>());
		}
		aGatesNetwork.get(networkID).add(gate);
	}
	
	/**
	 * Player move.
	 *
	 * @param player the player
	 */
	public void playerMove(Player player) {
		for(Gate gate : aGates){
			if(gate.isPlayerInside(player)){
				String netID=null;
				for(String key:aGatesNetwork.keySet()){
					if(aGatesNetwork.get(key).contains(gate)){
						netID = key;
						break;
					}
				}
				if(netID!=null){
					player.sendMessage("Using network : "+netID);
					LinkedList<Gate> network = aGatesNetwork.get(netID);
					if (network.size()<2){
						player.sendMessage("Not enough gates in network "+netID);
					}else{
						int idx = network.indexOf(gate);
						Gate nextGate = null;
						if(network.getLast().equals(gate)){
							nextGate = network.getFirst();
						}else{
							nextGate = network.get(idx+1);
						}
						gate.useGate(player,nextGate);
						
					}
				}
				break;
			}
		}
	}
	
	public void addNonFloodingBlocks(QDWorldCoord coord){
		nonFloodingBlocks.add(coord);
	}

	public boolean isNonFloodingBlock(Block block) {
		if (!(block.getType().equals(Material.WATER)||block.getType().equals(Material.STATIONARY_WATER))){
			return false;
		}
		QDWorldCoord coord = new QDWorldCoord(block.getLocation());
		plugin.logger.log("%d=>%s %d",nonFloodingBlocks.size(),coord.toString(),(nonFloodingBlocks.contains(coord)?1:0));
		return nonFloodingBlocks.contains(coord);
	}
}
