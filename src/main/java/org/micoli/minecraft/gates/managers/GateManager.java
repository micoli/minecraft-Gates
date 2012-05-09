package org.micoli.minecraft.gates.managers;

import java.util.HashSet;
import java.util.Iterator;
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
					aGates.add(gate);
				}
			}
		}
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
		for(GateObject gate : aGates){
			if(gate.isPlayerInside(player)){
				gate.useGate(player);
				break;
			}
		}
	}
}
