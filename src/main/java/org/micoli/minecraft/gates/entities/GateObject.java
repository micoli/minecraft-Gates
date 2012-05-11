package org.micoli.minecraft.gates.entities;

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
	
	/** The X,Y,Z location. */
	@NotNull
	double X, Y, Z;
	
	/** The pattern used for creation. */
	@NotNull
	@Length(max = 100)
	String pattern="";

	/** The networkID of the gate. */
	@NotNull
	@Length(max = 100)
	String networkID="";

	/**
	 * Instantiates a new gate object.
	 */
	public GateObject() {
	}
	
	/**
	 * Instantiates a new gate object.
	 *
	 * @param plugin the plugin
	 * @param centerBlock the center block
	 * @param gatePattern the gate pattern
	 * @param orientation the orientation
	 * @param worldName the world name
	 */
	public GateObject(Gates plugin,Block centerBlock, GatePattern gatePattern, GateOrientation orientation,String worldName,String networkID) {
		this.plugin = plugin;
		this.blockObject = centerBlock;
		this.width = gatePattern.getWidth();
		this.height = gatePattern.getHeight();
		this.orientation = orientation;
		this.X = blockObject.getX();
		this.Y = blockObject.getY();
		this.Z = blockObject.getZ();
		this.worldName = worldName;
		this.networkID = networkID;
		this.pattern = gatePattern.getName();
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
		Location playerLocation = player.getLocation();
		//plugin.logger.log("test %s %s %s",player,playerLocation.toString(),blockObject.getLocation().toString());
		if (!player.getWorld().getName().equals(worldName)){
			plugin.logger.log("not inside world %s %s",worldName,blockObject.getLocation().toString());
			return false;
		}
		double X1 = playerLocation.getX(), Y1 = playerLocation.getY(), Z1 = playerLocation.getZ();
		/*if (xyzDistance(playerLocation) > Math.min(width, height)) {
			plugin.logger.log("not inside %s",blockObject.getLocation().toString());
			return false;
		}*/
		if (Y <= Y1 && Y1 <= Y + this.height) {
			//plugin.logger.log("Inside Y %f",Y1);
			switch (orientation) {
				case NS:
					//plugin.logger.log("NS test player(%d -> %d,%d -> %d, %d %d %d ) W(%d) H(%d)",(int)X1,(int)X,(int)Y1,(int)Y,  (int)(Z - this.width / 2),(int)Z1,(int)(Z + this.width / 2)   ,width,height);
					if ((Z - this.width / 2) <= Z1 && Z1 <= (Z + this.width / 2) && (int)X == (int)X1) {
						plugin.logger.log("NS inside player");
						return true;
					}
				break;
				case EW:
					if ((X - this.width / 2) <= X1 && X1 <= (X + this.width / 2) && (int)Z == (int)Z1) {
						return true;
					}
				break;
			}
		}
		//plugin.logger.log("not inside 2 %s",blockObject.getLocation().toString());
		return false;
	}

	/**
	 * Use gate.
	 *
	 * @param player the player
	 * @param nextGate 
	 */
	public void useGate(Player player, GateObject nextGate) {
		player.sendMessage("Use gate "+this.toString());
		Location targetDestination = nextGate.getBlockObject().getLocation();
		if(nextGate.getOrientation()==GateOrientation.NS){
			targetDestination = targetDestination.add(1, 1, 0);
		}else{
			targetDestination = targetDestination.add(0, 1, 1);
		}
		targetDestination.setPitch(player.getLocation().getPitch());
		targetDestination.setYaw(player.getLocation().getYaw());
		
		player.teleport(targetDestination);
	}

	/**
	 * Init the object from the from database.
	 *
	 * @param instance the instance
	 * @return true, if successful
	 */
	public boolean initFromDatabase(Gates instance) {
		plugin = instance;
		World world = plugin.getServer().getWorld(worldName);
		blockObject = world.getBlockAt(new Location(world,X,Y,Z));
		//if(blockObject.getTypeId()!=0){
		initGate();
		return true;
		//}
		//return false;
	}
	
	/**
	 * Save.
	 */
	public void save() {
		plugin.getStaticDatabase().save(this);
	}

	/**
	 * Draw the gate
	 *
	 * @param gatePattern the gate pattern
	 */
	public void draw(GatePattern gatePattern) {
		World world = plugin.getServer().getWorld(worldName);
		int xOffset = gatePattern.getxOffset();
		int yOffset=0;
		Object[] patterns= gatePattern.getLines().toArray();
		Location location = new Location(world,0,0,0);
		plugin.logger.log(gatePattern.getName());
		for(int i=0;i<patterns.length;i++){
			yOffset = patterns.length-i-1;
			String line = ((String)patterns[i]);
			plugin.logger.log("%d -> %s",i,line);
			for(int j=0;j<line.length();j++){
				switch (orientation) {
					case EW:
						location = new Location(world,X-xOffset+j,Y+yOffset,Z);
					break;	
					case NS:
						location = new Location(world,X,Y+yOffset,Z-xOffset+j);
					break;	
				}
				String blockKey = line.charAt(j)+"";
				if(gatePattern.getBlocksMap().containsKey(blockKey)){
					world.getBlockAt(location).setType(gatePattern.getBlocksMap().get(line.charAt(j)+""));
				}else{
					if(blockKey.equals("-")){
						world.getBlockAt(location).setType(Material.AIR);
					}else{
						if(!blockKey.equals("!")){
							plugin.logger.log("not found "+line.charAt(j));
						}
					}
				}
			}
		}
	}

	/**
	 * @return the plugin
	 */
	public Gates getPlugin() {
		return plugin;
	}

	/**
	 * @param plugin the plugin to set
	 */
	public void setPlugin(Gates plugin) {
		this.plugin = plugin;
	}

	/**
	 * @return the blockObject
	 */
	public Block getBlockObject() {
		return blockObject;
	}

	/**
	 * @param blockObject the blockObject to set
	 */
	public void setBlockObject(Block blockObject) {
		this.blockObject = blockObject;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the orientation
	 */
	public GateOrientation getOrientation() {
		return orientation;
	}

	/**
	 * @param orientation the orientation to set
	 */
	public void setOrientation(GateOrientation orientation) {
		this.orientation = orientation;
	}

	/**
	 * @return the worldName
	 */
	public String getWorldName() {
		return worldName;
	}

	/**
	 * @param worldName the worldName to set
	 */
	public void setWorldName(String worldName) {
		this.worldName = worldName;
	}

	/**
	 * @return the x
	 */
	public double getX() {
		return X;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(double x) {
		X = x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return Y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(double y) {
		Y = y;
	}

	/**
	 * @return the z
	 */
	public double getZ() {
		return Z;
	}

	/**
	 * @param z the z to set
	 */
	public void setZ(double z) {
		Z = z;
	}

	/**
	 * @return the pattern
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * @param pattern the pattern to set
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * @return the networkID
	 */
	public String getNetworkID() {
		return networkID;
	}

	/**
	 * @param networkID the networkID to set
	 */
	public void setNetworkID(String networkID) {
		this.networkID = networkID;
	}
}