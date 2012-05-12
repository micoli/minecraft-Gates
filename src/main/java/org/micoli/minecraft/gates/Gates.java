package org.micoli.minecraft.gates;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.micoli.minecraft.bukkit.QDBukkitPlugin;
import org.micoli.minecraft.bukkit.QDCommand;
import org.micoli.minecraft.gates.entities.Gate;
import org.micoli.minecraft.gates.entities.Gate.GateOrientation;
import org.micoli.minecraft.gates.entities.GatePattern;
import org.micoli.minecraft.gates.listeners.GatesPlayerListener;
import org.micoli.minecraft.gates.managers.GateManager;
import org.micoli.minecraft.gates.managers.GatePatternManager;
import org.micoli.minecraft.gates.managers.GatesCommandManager;
import org.micoli.minecraft.utils.FileUtils;

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

	/** The does water flood be controlled. */
	private boolean withFlowControl = false;

	/** The gate manager. */
	private GateManager gateManager;

	/** The gate pattern manager. */
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

		configFile.set("gates.flowControl", configFile.getBoolean("gates.flowControl", isWithFlowControl()));
		setWithFlowControl( configFile.getBoolean("gates.flowControl"));

		gateManager = new GateManager(instance);
		gateManager.readGatesFromDatabase();

		FileUtils.initializeFileFromRessource(this,"GatePatterns.yml",true);
		gatePatternManager = GatePatternManager.readGatesPattern(instance, "GatePatterns.yml");
		
		getPm().registerEvents(new GatesPlayerListener(this),this);
		
		logger.log(gatePatternManager.toString());
		saveConfig();

		executor = new GatesCommandManager(this, new Class[] { getClass() });
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
	 * Gets the gate pattern manager.
	 * 
	 * @return the gatePatternManager
	 */
	public GatePatternManager getGatePatternManager() {
		return gatePatternManager;
	}

	/**
	 * Sets the gate pattern manager.
	 * 
	 * @param gatePatternManager
	 *            the gatePatternManager to set
	 */
	public void setGatePatternManager(GatePatternManager gatePatternManager) {
		this.gatePatternManager = gatePatternManager;
	}

	/**
	 * @return the withFlowControl
	 */
	public boolean isWithFlowControl() {
		return withFlowControl;
	}

	/**
	 * @param withFlowControl the withFlowControl to set
	 */
	public void setWithFlowControl(boolean withFlowControl) {
		this.withFlowControl = withFlowControl;
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
		list.add(Gate.class);
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
	@QDCommand(aliases = "create", permissions = { "gates.create" }, usage = "<patternName> <gateNetworkID>", description = "create a gate")
	public void cmdGates(CommandSender sender, Command command, String label, String[] args) throws Exception {
		Player player = ((Player) sender);
		Block block = player.getTargetBlock(null, 50);
		int iOrientation = (int) (player.getLocation().getYaw() + 180) % 360;
		GateOrientation orientation = GateOrientation.NS;
		String facing = "N";
		if (iOrientation < 45 + 0 * 90) {
			facing = "N";
			orientation = GateOrientation.EW;
		} else if (iOrientation < 45 + 1 * 90) {
			facing = "E";
			orientation = GateOrientation.NS;
		} else if (iOrientation < 45 + 2 * 90) {
			facing = "S";
			orientation = GateOrientation.EW;
		} else if (iOrientation < 45 + 3 * 90) {
			facing = "W";
			orientation = GateOrientation.NS;
		}

		logger.log("Facing %s,%s", facing, orientation.toString());

		try {
			GatePattern gatePattern = this.getGatePatternManager().getGatePatternFromName(args[1]);

			Gate gate = new Gate(instance, block, gatePattern, orientation, player.getWorld().getName(), args[2],player.getLocation(),(iOrientation) % 360);
			gate.draw(gatePattern);
			gate.initGate();
			gate.save();
			this.getGateManager().addGate(gate);
			sendComments((Player) sender, "Gate created", false);
		} catch (Exception e) {
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