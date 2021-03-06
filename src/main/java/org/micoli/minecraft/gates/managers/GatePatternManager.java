package org.micoli.minecraft.gates.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import org.micoli.minecraft.gates.Gates;
import org.micoli.minecraft.gates.entities.GatePattern;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

// TODO: Auto-generated Javadoc
/**
 * The Class GatePatternManager.
 */
public class GatePatternManager {
	
	/** The patterns. */
	private List<GatePattern> patterns;
	
	/**
	 * Gets the patterns.
	 *
	 * @return the patterns
	 */
	public List<GatePattern> getPatterns() {
		return patterns;
	}
	
	/**
	 * Sets the patterns.
	 *
	 * @param patterns the patterns to set
	 */
	public void setPatterns(List<GatePattern> patterns) {
		this.patterns = patterns;
	}
	
	/**
	 * Gets the gate pattern from name.
	 *
	 * @param name the name
	 * @return the gate pattern from name
	 */
	public GatePattern getGatePatternFromName(String name){
		for(GatePattern gatePattern:patterns){
			if(gatePattern.getName().equalsIgnoreCase(name)){
				return gatePattern;
			}
		}
		return null;
	}
	
	/**
	 * Read gates pattern.
	 *
	 * @param plugin the plugin
	 * @param fileName the file name
	 * @return the gate pattern manager
	 */
	public static GatePatternManager readGatesPattern(Gates plugin, String fileName) {
		InputStream input;
		try {
			input = new FileInputStream(new File(plugin.getDataFolder() + "/" + fileName));
			Constructor constructor = new CustomClassLoaderConstructor(GatePatternManager.class,plugin.getClass().getClassLoader());
			TypeDescription GatePatternDescription = new TypeDescription(GatePatternManager.class);
			GatePatternDescription.putListPropertyType("patterns", GatePattern.class);
			constructor.addTypeDescription(GatePatternDescription);

			Yaml yaml = new Yaml(constructor);
			GatePatternManager gatePatternManager = (GatePatternManager) yaml.load(input);
			for(GatePattern gatePattern : gatePatternManager.patterns){
				gatePattern.initPattern(plugin);
			}
			return gatePatternManager;
		} catch (FileNotFoundException e) {
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return patterns.toString();
	}
}
