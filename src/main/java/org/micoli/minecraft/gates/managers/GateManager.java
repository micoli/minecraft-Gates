package org.micoli.minecraft.gates.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;
import org.micoli.minecraft.gates.Gates;
import org.micoli.minecraft.gates.entities.GateObject;

/**
 * The Class GateManager.
 */
public class GateManager {

	/** The plugin. */
	private Gates plugin;
	
	/** The internal array of parcels. */
	private Set<GateObject> aGates;
	private Map<String,LinkedList<GateObject>> aGatesNetwork = new HashMap<String,LinkedList<GateObject>>();

	/**
	 * Instantiates a new parcel manager.
	 *
	 * @param instance the instance
	 */
	public GateManager(Gates instance) {
		this.plugin = instance;
		aGates = new HashSet<GateObject>();
		Iterator<GateObject> gateIterator = plugin.getStaticDatabase().find(GateObject.class).findList().iterator();
		if (gateIterator.hasNext()) {
			while (gateIterator.hasNext()) {
				GateObject gate = gateIterator.next();
				if(gate.initFromDatabase(plugin)){
					plugin.logger.log(gate.toString());
					addGate(gate);
				}
			}
		}
	}
	
	public void addGate(GateObject gateObject){
		aGates.add(gateObject);
		String networkID = gateObject.getNetworkID();
		if(!aGatesNetwork.containsKey(networkID)){
			aGatesNetwork.put(networkID,new LinkedList<GateObject>());
		}
		aGatesNetwork.get(networkID).add(gateObject);
	}
	
	/**
	 * Gets the a gates.
	 *
	 * @return the aParcel
	 */
	public Set<GateObject> getaGates() {
		return aGates;
	}

	/**
	 * Player move.
	 *
	 * @param player the player
	 */
	public void playerMove(Player player) {
		//plugin.logger.log("%d",aGates.size());
		for(GateObject gate : aGates){
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
					LinkedList<GateObject> network = aGatesNetwork.get(netID);
					if (network.size()<2){
						player.sendMessage("Not enough gates in network "+netID);
					}else{
						int idx = network.indexOf(gate);
						GateObject nextGate = null;
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
}
