package org.micoli.minecraft.gates.entities;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;

public class GatePattern {
	public String name = "";
	public LinkedHashSet<String> blocks;
	private HashMap<String, Material> blocksMap = new HashMap<String, Material>();
	public LinkedHashSet<String> lines;
	public int width;
	public int height;
	private int xOffset;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the blocks
	 */
	public LinkedHashSet<String> getBlocks() {
		return blocks;
	}

	/**
	 * @param blocks
	 *            the blocks to set
	 */
	public void setBlocks(LinkedHashSet<String> blocks) {
		this.blocks = blocks;
	}

	/**
	 * @return the lines
	 */
	public LinkedHashSet<String> getLines() {
		return lines;
	}

	/**
	 * @param lines
	 *            the lines to set
	 */
	public void setLines(LinkedHashSet<String> lines) {
		this.lines = lines;
	}

	public String toString(){
		String result = "";
		result = result + "{name:"+name+",blocksMap:"+this.getBlocksMap().toString()+",lines:"+this.lines.toString()+"}";
		return result;
		
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	/**
	 * @return the xOffset
	 */
	public int getxOffset() {
		return xOffset;
	}

	/**
	 * @return the blocksMap
	 */
	public HashMap<String, Material> getBlocksMap() {
		return blocksMap;
	}

	/**
	 * @param blocksMap the blocksMap to set
	 */
	public void setBlocksMap(HashMap<String, Material> blocksMap) {
		this.blocksMap = blocksMap;
	}

	public void initPattern() {
		Pattern pattern = Pattern.compile("(.*)_(.*)");
		for (String txt : blocks) {
			Matcher matcher = pattern.matcher(txt);
			if (matcher.matches()) {
				this.getBlocksMap().put(matcher.group(1), Material.getMaterial(Integer.parseInt(matcher.group(2))));
			}
		}
		
		width = 0;
		for (String txt : lines) {
			width = Math.max(width, txt.length());
		}
		height = lines.size();
		Object[] patterns= lines.toArray();
		xOffset = ((String)patterns[patterns.length-1]).indexOf('!');
	}
}