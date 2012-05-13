package org.micoli.minecraft.gates.entities;

import java.util.ArrayList;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.micoli.minecraft.entities.QDWorldCoord;
import org.micoli.minecraft.gates.Gates;
import org.micoli.minecraft.utils.ChatFormater;
import org.micoli.minecraft.utils.Json;

import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotNull;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

// TODO: Auto-generated Javadoc
/**
 * The Class GateObject.
 */
@Entity
@Table(name = "mrg_mrg_gateobject")
public class Gate {
	public Gate(){
	}
	/** The plugin. */
	@Transient
	Gates plugin;
	
	/** The block object. */
	@Transient
	Block tpBlock;

	/**
	 * The Enum GoalOrientation.
	 */
	public enum GateOrientation {
		/** The NS. */
		NS,
		/** The EW. */
		EW
	}

	@Id
	Integer id;
	
	@Length(max = 100)
	String gateName="";
	
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
	
	/** The tpX,tpY,tpZ location. */
	@NotNull
	double tpX, tpY, tpZ;

	/** The outX,outY,outZ location. */
	@NotNull
	double outX, outY, outZ;
	
	@NotNull
	int outYaw=0;
	
	/** The pattern used for creation. */
	@NotNull
	@Length(max = 100)
	String pattern="";

	/** The networkID of the gate. */
	@NotNull
	@Length(max = 100)
	String networkID="";
	
	@NotNull
	@Length(max = 4096)
	String nonFloodingCoordsStr="";
	
	@Transient
	ArrayList<QDWorldCoord> nonFloodingCoords = new ArrayList<QDWorldCoord>();
	
	/**
	 * Instantiates a new gate object.
	 *
	 * @param plugin the plugin
	 * @param centerBlock the center block
	 * @param gatePattern the gate pattern
	 * @param orientation the orientation
	 * @param worldName the world name
	 * @param iOrientation 
	 */
	public Gate(Gates plugin,Block centerBlock, GatePattern gatePattern, GateOrientation orientation,String worldName,String networkID,Location outLocation, int outYaw) {
		setPlugin(plugin);
		this.tpBlock = centerBlock;
		this.width = gatePattern.getWidth();
		this.height = gatePattern.getHeight();
		this.orientation = orientation;
		this.tpX = tpBlock.getX();
		this.tpY = tpBlock.getY();
		this.tpZ = tpBlock.getZ();
		this.outX = outLocation.getX();
		this.outY = outLocation.getY();
		this.outZ = outLocation.getZ();
		this.outYaw = outYaw;
		this.worldName = worldName;
		this.networkID = networkID;
		this.pattern = gatePattern.getName();
	}

	/**
	 * calculate a raw distance between the ball and the Location.
	 * 
	 * @param location
	 *            the location
	 * @return the int
	 */
	public int xyzDistance(Location location) {
		return (int) Math.max(Math.max(Math.abs(tpBlock.getX() - tpBlock.getX()), Math.abs(location.getY() - tpBlock.getY())), Math.abs(location.getZ() - tpBlock.getZ()));
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
			plugin.logger.log("not inside world %s %s",worldName,tpBlock.getLocation().toString());
			return false;
		}
		double X1 = playerLocation.getX(), Y1 = playerLocation.getY(), Z1 = playerLocation.getZ();
		/*if (xyzDistance(playerLocation) > Math.min(width, height)) {
			plugin.logger.log("not inside %s",blockObject.getLocation().toString());
			return false;
		}*/
		if (tpY <= Y1 && Y1 <= tpY + this.height) {
			//plugin.logger.log("Inside Y %f",Y1);
			switch (orientation) {
				case NS:
					//plugin.logger.log("NS test player(%d -> %d,%d -> %d, %d %d %d ) W(%d) H(%d)",(int)X1,(int)X,(int)Y1,(int)Y,  (int)(Z - this.width / 2),(int)Z1,(int)(Z + this.width / 2)   ,width,height);
					if ((tpZ - this.width / 2) <= Z1 && Z1 <= (tpZ + this.width / 2) && (int)tpX == (int)X1) {
						plugin.logger.log("NS inside player");
						return true;
					}
				break;
				case EW:
					if ((tpX - this.width / 2) <= X1 && X1 <= (tpX + this.width / 2) && (int)tpZ == (int)Z1) {
						return true;
					}
				break;
			}
		}
		//plugin.logger.log("not inside 2 %s",blockObject.getLocation().toString());
		return false;
	}

	private Location getOutLocation(Player player){
		Location location = new Location(plugin.getServer().getWorld(getWorldName()),getOutX(),getOutY(),getOutZ());
		location.setYaw(getOutYaw());
		location.setPitch((player!=null)?player.getLocation().getPitch():0);
		return location;
	}
	
	/**
	 * Use gate.
	 *
	 * @param player the player
	 * @param nextGate 
	 */
	public void useGate(Player player, Gate nextGate) {
		player.sendMessage(ChatFormater.format("Use gate %d",getId()));
		player.teleport(nextGate.getOutLocation(player));
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
		tpBlock = world.getBlockAt(new Location(world,tpX,tpY,tpZ));
		Gson gson = new Gson();
		nonFloodingCoords=gson.fromJson(nonFloodingCoordsStr,new TypeToken<ArrayList<QDWorldCoord>>(){}.getType());
		//if(blockObject.getTypeId()!=0){
		initGate();
		return true;
		//}
		//return false;
	}
	
	/**
	 * Inits the gate.
	 */
	public void initGate() {
		for(QDWorldCoord coord : nonFloodingCoords){
			plugin.getGateManager().addNonFloodingBlocks(coord);
		}
		
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
				QDWorldCoord coord = new QDWorldCoord(worldName,0,0,0);
				switch (orientation) {
					case EW:
						coord.setXYZ(tpX-xOffset+j,tpY+yOffset,tpZ);
					break;	
					case NS:
						coord.setXYZ(tpX,tpY+yOffset,tpZ-xOffset+j);
					break;	
				}
				location = new Location(world,coord.getX(),coord.getY(),coord.getZ());
				String blockKey = line.charAt(j)+"";
				if(gatePattern.getBlocksMap().containsKey(blockKey)){
					Material material = gatePattern.getBlocksMap().get(line.charAt(j)+"");
					if(plugin.isWithFlowControl()){
						world.getBlockAt(location).setType(material);
					}else{
						if (material.equals(Material.WATER) || material.equals(Material.STATIONARY_WATER)){
							plugin.logger.log("can't add water without enabling flow control");
						}else{
							world.getBlockAt(location).setType(material);
						}
					}
				}else{
					if(blockKey.equals("=")){
					}else if(blockKey.equals("_")){
						if(plugin.isWithFlowControl()){
							world.getBlockAt(location).setType(Material.WATER);
						}
						nonFloodingCoords.add(coord);
					}else if(blockKey.equals("-")){
						world.getBlockAt(location).setType(Material.AIR);
					}else{
						if(!blockKey.equals("!")){
							plugin.logger.log("not found "+line.charAt(j));
						}
					}
				}
				nonFloodingCoords.add(coord);
			}
		}
		this.nonFloodingCoordsStr=Json.exportObjectToJson(nonFloodingCoords);
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
	public Block getTpBlock() {
		return tpBlock;
	}

	/**
	 * @param blockObject the blockObject to set
	 */
	public void setTpBlock(Block blockObject) {
		this.tpBlock = blockObject;
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the gateName
	 */
	public String getGateName() {
		return gateName;
	}

	/**
	 * @param gateName the gateName to set
	 */
	public void setGateName(String gateName) {
		this.gateName = gateName;
	}

	/**
	 * @return the tpX
	 */
	public double getTpX() {
		return tpX;
	}

	/**
	 * @param tpX the tpX to set
	 */
	public void setTpX(double tpX) {
		this.tpX = tpX;
	}

	/**
	 * @return the tpY
	 */
	public double getTpY() {
		return tpY;
	}

	/**
	 * @param tpY the tpY to set
	 */
	public void setTpY(double tpY) {
		this.tpY = tpY;
	}

	/**
	 * @return the tpZ
	 */
	public double getTpZ() {
		return tpZ;
	}

	/**
	 * @param tpZ the tpZ to set
	 */
	public void setTpZ(double tpZ) {
		this.tpZ = tpZ;
	}

	/**
	 * @return the outX
	 */
	public double getOutX() {
		return outX;
	}

	/**
	 * @param outX the outX to set
	 */
	public void setOutX(double outX) {
		this.outX = outX;
	}

	/**
	 * @return the outY
	 */
	public double getOutY() {
		return outY;
	}

	/**
	 * @param outY the outY to set
	 */
	public void setOutY(double outY) {
		this.outY = outY;
	}

	/**
	 * @return the outZ
	 */
	public double getOutZ() {
		return outZ;
	}

	/**
	 * @param outZ the outZ to set
	 */
	public void setOutZ(double outZ) {
		this.outZ = outZ;
	}

	/**
	 * @return the outYaw
	 */
	public int getOutYaw() {
		return outYaw;
	}

	/**
	 * @param outYaw the outYaw to set
	 */
	public void setOutYaw(int outYaw) {
		this.outYaw = outYaw;
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
		return tpX;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(double x) {
		tpX = x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return tpY;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(double y) {
		tpY = y;
	}

	/**
	 * @return the z
	 */
	public double getZ() {
		return tpZ;
	}

	/**
	 * @param z the z to set
	 */
	public void setZ(double z) {
		tpZ = z;
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

	/**
	 * @return the nonFloodingCoordsStr
	 */
	public String getNonFloodingCoordsStr() {
		return nonFloodingCoordsStr;
	}

	/**
	 * @param nonFloodingCoordsStr the nonFloodingCoordsStr to set
	 */
	public void setNonFloodingCoordsStr(String nonFloodingCoordsStr) {
		this.nonFloodingCoordsStr = nonFloodingCoordsStr;
	}

	/**
	 * @param nonFloodingCoords the nonFloodingCoords to set
	 */
	public void setNonFloodingCoords(ArrayList<QDWorldCoord> nonFloodingCoords) {
		this.nonFloodingCoords = nonFloodingCoords;
	}

	/**
	 * @return the nonFloodingCoords
	 */
	public ArrayList<QDWorldCoord> getNonFloodingCoords() {
		return nonFloodingCoords;
	}
}