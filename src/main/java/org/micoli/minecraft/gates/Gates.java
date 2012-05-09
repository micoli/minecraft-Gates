package org.micoli.minecraft.gates;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.micoli.minecraft.bukkit.QDBukkitPlugin;
import org.micoli.minecraft.bukkit.QDCommand;
import org.micoli.minecraft.gates.entities.GateObject;
import org.micoli.minecraft.gates.entities.GateObject.GateOrientation;
import org.micoli.minecraft.gates.entities.GatePattern;
import org.micoli.minecraft.gates.managers.GateManager;
import org.micoli.minecraft.gates.managers.GatePatternManager;
import org.micoli.minecraft.gates.managers.GatesCommandManager;

// TODO: Auto-generated Javadoc
/**
 * The Class LocalPlan.
 */
public class Gates extends QDBukkitPlugin implements ActionListener {

	/** The my executor. */
	protected GatesCommandManager executor;

	/** The instance. */
	private static Gates instance;

	/** The max gate height. */
	private int maxGateHeight = 15;

	/** The gate manager. */
	private GateManager gateManager;
	
	GatePatternManager gatePatternManager;
	/**
	 * Gets the single instance of LocalPlan.
	 * 
	 * @return the instance
	 */
	public static Gates getInstance() {
		return instance;
	}

	/**
	 * On enable.
	 * 
	 * @see org.micoli.minecraft.bukkit.QDBukkitPlugin#onEnable()
	 */
	@Override
	public void onEnable() {
		instance = this;
		withDatabase = true;

		commandString = "mg";
		super.onEnable();
		logger.log("%s version enabled", this.pdfFile.getName(), this.pdfFile.getVersion());

		configFile.set("gates.maxHeight", configFile.getDouble("gates.maxHeight", getMaxGateHeight()));
		setMaxGateHeight((int) configFile.getDouble("gates.maxHeight"));
		gateManager = new GateManager(instance);

		initializeFileFromRessource("GatePatterns.yml");
		gatePatternManager = GatePatternManager.readGatesPattern(instance,"GatePatterns.yml");
		
		logger.log(gatePatternManager.toString());
		saveConfig();

		executor = new GatesCommandManager(this, new Class[] { getClass() });
	}

	private void initializeFileFromRessource(String fileName) {
		File patternFile = new File(getDataFolder(), fileName);
		if (!patternFile.exists()|| true) {
			try {
				InputStream isr = getClass().getClassLoader().getResourceAsStream(fileName);
				File fileOut = new File(getDataFolder().getAbsolutePath() + "/" + fileName);
				FileOutputStream fop = new FileOutputStream(fileOut);
				if (!fileOut.exists()) {
					fileOut.createNewFile();
				}
				try {
					byte[] buf = new byte[512];
					int len;
					while ((len = isr.read(buf)) > 0) {
						fop.write(buf, 0, len);
					}
				} finally {
					isr.close();
				}
			} catch (IOException e) {
				logger.dumpStackTrace(e);
			}
		}

	}

	/**
	 * Gets the max gate height.
	 * 
	 * @return the maxGateHeight
	 */
	public int getMaxGateHeight() {
		return maxGateHeight;
	}

	/**
	 * Sets the max gate height.
	 * 
	 * @param maxGateHeight
	 *            the maxGateHeight to set
	 */
	public void setMaxGateHeight(int maxGateHeight) {
		this.maxGateHeight = maxGateHeight;
	}

	/**
	 * Gets the gate manager.
	 * 
	 * @return the gateManager
	 */
	public GateManager getGateManager() {
		return gateManager;
	}

	/**
	 * Sets the gate manager.
	 * 
	 * @param gateManager
	 *            the gateManager to set
	 */
	public void setGateManager(GateManager gateManager) {
		this.gateManager = gateManager;
	}

	/**
	 * @return the gatePatternManager
	 */
	public GatePatternManager getGatePatternManager() {
		return gatePatternManager;
	}

	/**
	 * @param gatePatternManager the gatePatternManager to set
	 */
	public void setGatePatternManager(GatePatternManager gatePatternManager) {
		this.gatePatternManager = gatePatternManager;
	}

	/*
	 * 
	 * @see org.micoli.minecraft.bukkit.QDBukkitPlugin#getDatabaseORMClasses()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.micoli.minecraft.bukkit.QDBukkitPlugin#getDatabaseORMClasses()
	 */
	protected java.util.List<Class<?>> getDatabaseORMClasses() {
		List<Class<?>> list = new ArrayList<Class<?>>();
		list.add(GateObject.class);
		return list;
	};

	/**
	 * CmdCreate.
	 * 
	 * @param sender
	 *            the sender
	 * @param command
	 *            the command
	 * @param label
	 *            the label
	 * @param args
	 *            the args
	 * @throws Exception
	 *             the exception
	 */
	@QDCommand(aliases = "create", permissions = { "gates.create" }, usage = "", description = "create a gate")
	public void cmdGates(CommandSender sender, Command command, String label, String[] args) throws Exception {
		Block block = ((Player)sender).getTargetBlock(null, 50);
		int iOrientation = (int) (((Player)sender).getLocation().getYaw() + 180) % 360;
		GateOrientation orientation = GateOrientation.NS;
		String dir = "N";
		if (iOrientation < 45 + 0 * 90) {
			dir = "N";
			orientation = GateOrientation.EW;
		} else if (iOrientation < 45 + 1 * 90) {
			dir = "E";
			orientation = GateOrientation.NS;
		} else if (iOrientation < 45 + 2 * 90) {
			dir = "S";
			orientation = GateOrientation.EW;
		} else if (iOrientation < 45 + 3 * 90) {
			dir = "W";
			orientation = GateOrientation.NS;
		}

		try{
			GatePattern gatePattern = this.getGatePatternManager().getGatePatternFromName(args[1]);
			
			GateObject gate = new GateObject(instance,block,gatePattern,orientation,((Player)sender).getWorld().getName());
			gate.draw(gatePattern);
			sendComments((Player) sender, "create", false);
		}catch(Exception e){
			logger.dumpStackTrace(e);
		}
	}

	/**
	 * Player move.
	 * 
	 * @param player
	 *            the player
	 */
	public void playerMove(Player player) {

	}
}