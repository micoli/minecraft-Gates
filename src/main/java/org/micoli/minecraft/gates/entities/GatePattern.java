package org.micoli.minecraft.gates.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;
import org.micoli.minecraft.gates.Gates;

// TODO: Auto-generated Javadoc
/**
 * The Class GatePattern.
 */
public class GatePattern {
	
	/** The name. */
	private String name = "";
	
	/** The blocks. */
	private ArrayList<String> blocks;
	
	/** The blocks map. */
	private HashMap<String, MaterialData> blocksMap = new HashMap<String, MaterialData>();
	
	/** The lines. */
	private ArrayList<String> lines;
	
	/** The width. */
	private int width;
	
	/** The height. */
	private int height;
	
	/** The x offset. */
	private int xOffset;

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the blocks.
	 *
	 * @return the blocks
	 */
	public ArrayList<String> getBlocks() {
		return blocks;
	}

	/**
	 * Sets the blocks.
	 *
	 * @param blocks the blocks to set
	 */
	public void setBlocks(ArrayList<String> blocks) {
		this.blocks = blocks;
	}

	/**
	 * Gets the lines.
	 *
	 * @return the lines
	 */
	public ArrayList<String> getLines() {
		return lines;
	}

	/**
	 * Sets the lines.
	 *
	 * @param lines the lines to set
	 */
	public void setLines(ArrayList<String> lines) {
		this.lines = lines;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		String result = "";
		result = result + "{name:"+name+",blocksMap:"+this.getBlocksMap().toString()+",lines:"+this.lines.toString()+"}";
		return result;
		
	}

	/**
	 * Gets the width.
	 *
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Gets the height.
	 *
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Gets the x offset.
	 *
	 * @return the xOffset
	 */
	public int getxOffset() {
		return xOffset;
	}

	/**
	 * Gets the blocks map.
	 *
	 * @return the blocksMap
	 */
	public HashMap<String, MaterialData> getBlocksMap() {
		return blocksMap;
	}

	/**
	 * Sets the blocks map.
	 *
	 * @param blocksMap the blocksMap to set
	 */
	public void setBlocksMap(HashMap<String, MaterialData> blocksMap) {
		this.blocksMap = blocksMap;
	}

	/**
	 * Inits the pattern.
	 *
	 * @param plugin the plugin
	 */
	public void initPattern(Gates plugin) {
		Pattern pattern = Pattern.compile("(.*)_(.*):(.*)");
		for (String txt : blocks) {
			Matcher matcher = pattern.matcher(txt);
			if (matcher.matches()) {
				//Material material = Material.getMaterial(Integer.parseInt(matcher.group(2)));
				MaterialData mat = new MaterialData(Integer.parseInt(matcher.group(2)),(byte)Integer.parseInt(matcher.group(3)));
				this.getBlocksMap().put(matcher.group(1), mat);
			}
		}
		
		this.width = 0;
		for (String txt : lines) {
			if(txt!=null){
				this.width = Math.max(this.width, txt.length());
			}
		}
		this.height = lines.size();
		Object[] patterns= lines.toArray();
		this.xOffset = ((String)patterns[patterns.length-1]).indexOf('!');
	}
}