package org.micoli.minecraft.gates.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;
import org.micoli.minecraft.gates.Gates;
import org.micoli.minecraft.gates.entities.GateObject;

// TODO: Auto-generated Javadoc
/**
 * The Class GateManager.
 */
public class GateManager {

	/** The plugin. */
	private Gates plugin;
	
	/** The internal array of parcels. */
	private Set<GateObject> aGates;
	private Map<String,Set<GateObject>> aGatesNetwork = new HashMap<String,Set<GateObject>>();

	/**
	 * Instantiates a new parcel manager.
	 *
	 * @param instance the instance
	 */
	public GateManager(Gates instance) {
		this.plugin = instance;
		aGates = new HashSet<GateObject>();
		Iterator<GateObject> gateIterator = plugin.getStaticDatabase().find(GateObject.class).findList().iterator();
		instance.logger.log("eeeee %s",gateIterator.toString());
		if (gateIterator.hasNext()) {
			instance.logger.log("eeeee 1 %s",gateIterator.toString());
			while (gateIterator.hasNext()) {
				instance.logger.log("eeeee 2 %s",gateIterator.toString());
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
			aGatesNetwork.put(networkID,new HashSet<GateObject>());
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
	 * Sets the a parcel.
	 *
	 * @param aGates the new a parcel
	 */
	public void setaParcel(Set<GateObject> aGates) {
		this.aGates = aGates;
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
				gate.useGate(player);
				break;
			}
		}
	}
}
