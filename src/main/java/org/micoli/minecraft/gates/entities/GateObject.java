package org.micoli.minecraft.gates.entities;

import java.util.Arrays;
import java.util.Collections;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.micoli.minecraft.gates.Gates;

import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotNull;

// TODO: Auto-generated Javadoc
/**
 * The Class GateObject.
 */
@Entity
@Table(name = "mrg_mrg_gateobject")
public class GateObject {
	
	/** The plugin. */
	@Transient
	Gates plugin;
	
	/** The block object. */
	@Transient
	Block blockObject;

	/**
	 * The Enum GoalOrientation.
	 */
	public enum GateOrientation {
		/** The NS. */
		NS,
		/** The EW. */
		EW
	}

	/** The height. */
	@NotNull
	int height;

	/** The width. */
	@NotNull
	int width;

	/** The orientation. */
	@NotNull
	@Length(max = 20)
	GateOrientation orientation;

	/** The world name. */
	@NotNull
	@Length(max = 100)
	String worldName="";
	
	/** The X,Y,Z location */
	@NotNull
	double X, Y, Z;
	
	/** The world name. */
	@NotNull
	@Length(max = 100)
	String pattern="";

	public GateObject() {
	}
	/**
	 * Instantiates a new gate object.
	 *
	 * @param plugin the plugin
	 * @param centerBlock the center block
	 * @param width the width
	 * @param height the height
	 * @param orientation the orientation
	 */
	public GateObject(Gates plugin,Block centerBlock, GatePattern gatePattern, GateOrientation orientation,String worldName) {
		this.plugin = plugin;
		this.blockObject = centerBlock;
		this.width = gatePattern.getWidth();
		this.height = gatePattern.getHeight();
		this.orientation = orientation;
		this.X = blockObject.getX();
		this.Y = blockObject.getY();
		this.Z = blockObject.getZ();
		this.worldName=worldName;
	}

	/**
	 * Inits the gate.
	 */
	public void initGate() {
		
	}

	/**
	 * calculate a raw distance between the ball and the Location.
	 * 
	 * @param location
	 *            the location
	 * @return the int
	 */
	public int xyzDistance(Location location) {
		return (int) Math.max(Math.max(Math.abs(blockObject.getX() - blockObject.getX()), Math.abs(location.getY() - blockObject.getY())), Math.abs(location.getZ() - blockObject.getZ()));
	}

	/**
	 * Checks if player is inside the gate.
	 *
	 * @param player the player
	 * @return true, if is player inside
	 */
	public boolean isPlayerInside(Player player) {
		if (!player.getWorld().getName().equals(worldName)){
			return false;
		}
		double X1 = player.getLocation().getX(), Y1 = player.getLocation().getY(), Z1 = player.getLocation().getZ();
		if (xyzDistance(player.getLocation()) > Math.min(width, height)) {
			return false;
		}
		if (Y <= Y1 && Y1 <= Y + this.height) {
			switch (this.orientation) {
			case NS:
				if ((Z - this.width / 2) <= Z1 && Z1 <= (Z + this.width / 2) && X == X1) {
					return true;
				}
				break;
			case EW:
				if ((X - this.width / 2) <= X1 && X1 <= (X + this.width / 2) && Z == Z1) {
					return true;
				}
				break;
			}
		}
		return false;
	}

	/**
	 * Use gate.
	 *
	 * @param player the player
	 */
	public void useGate(Player player) {
		
	}

	/**
	 * Inits the from database.
	 *
	 * @param instance the instance
	 * @return true, if successful
	 */
	public boolean initFromDatabase(Gates instance) {
		plugin = instance;
		World world = plugin.getServer().getWorld(worldName);
		blockObject = world.getBlockAt(new Location(world,X,Y,Z));
		if(blockObject.getTypeId()!=0){
			initGate();
			return true;
		}
		return false;
	}
	public void draw(GatePattern gatePattern) {
		int xOffset = gatePattern.getxOffset();
		Object[] patterns= gatePattern.getLines().toArray();
		Arrays.sort(patterns, Collections.reverseOrder());
		World world = plugin.getServer().getWorld(worldName);
		Location location = new Location(world,0,0,0);
		plugin.logger.log(gatePattern.getBlocksMap().toString());
		for(int i=0;i<patterns.length;i++){
			String line = ((String)patterns[i]);
			plugin.logger.log("%d-_>%s",i,line);
			for(int j=0;j<line.length();j++){
				switch (orientation) {
					case EW:
						location = new Location(world,X-xOffset+j,Y+i+1,Z);
					break;	
					case NS:
						location = new Location(world,X,Y+i+1,Z-xOffset+j);
					break;	
				}
				if(gatePattern.getBlocksMap().containsKey(line.charAt(j)+"")){
					world.getBlockAt(location).setType(gatePattern.getBlocksMap().get(line.charAt(j)+""));
				}else{
					if((line.charAt(j)+"").equals("-")){
						world.getBlockAt(location).setType(Material.AIR);
					}else{
						plugin.logger.log("not found "+line.charAt(j));
					}
				}
			}
		}
	}
}