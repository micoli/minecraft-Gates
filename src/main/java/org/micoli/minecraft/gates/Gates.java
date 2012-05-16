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
import org.micoli.minecraft.bukkit.QDCommandUsageException;
import org.micoli.minecraft.gates.entities.Gate;
import org.micoli.minecraft.gates.entities.GatePattern;
import org.micoli.minecraft.gates.listeners.GatesPlayerListener;
import org.micoli.minecraft.gates.managers.GateManager;
import org.micoli.minecraft.gates.managers.GatePatternManager;
import org.micoli.minecraft.gates.managers.GatesCommandManager;
import org.micoli.minecraft.utils.ChatFormater;
import org.micoli.minecraft.utils.FileUtils;
import org.micoli.minecraft.utils.QDOrientation;
import org.micoli.minecraft.utils.QDOrientation.MultipleOrientations;

// TODO: Auto-generated Javadoc
/**
 * The Plugin Class LocalPlan.
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
	
	/** The gate cool down. */
	private int gateCoolDown = 20;

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

		configFile.set("gates.gateCoolDown", configFile.getInt("gates.gateCoolDown", getGateCoolDown()));
		setGateCoolDown( configFile.getInt("gates.gateCoolDown"));

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
	 * Checks if is with flow control.
	 *
	 * @return the withFlowControl
	 */
	public boolean isWithFlowControl() {
		return withFlowControl;
	}

	/**
	 * Sets the with flow control.
	 *
	 * @param withFlowControl the withFlowControl to set
	 */
	public void setWithFlowControl(boolean withFlowControl) {
		this.withFlowControl = withFlowControl;
	}

	/**
	 * Gets the gate cool down.
	 *
	 * @return the gateCoolDown
	 */
	public final int getGateCoolDown() {
		return gateCoolDown;
	}

	/**
	 * Sets the gate cool down.
	 *
	 * @param gateCoolDown the gateCoolDown to set
	 */
	public final void setGateCoolDown(int gateCoolDown) {
		this.gateCoolDown = gateCoolDown;
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
	public void cmdAddGate(CommandSender sender, Command command, String label, String[] args) throws Exception {
		if(args.length!=3){
			throw new QDCommandUsageException();
		}
		Player player = ((Player) sender);
		Block block = player.getTargetBlock(null, 50);
		MultipleOrientations orientations = QDOrientation.getOrientations(player);
		try {
			GatePattern gatePattern = this.getGatePatternManager().getGatePatternFromName(args[1]);

			Gate gate = new Gate(instance, block, gatePattern, orientations.getCardinalDualOrientation(), player.getWorld().getName(), args[2],player.getLocation(),(orientations.getAngle()) % 360);
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
	 * Cmd remove gate.
	 *
	 * @param sender the sender
	 * @param command the command
	 * @param label the label
	 * @param args the args
	 * @throws Exception the exception
	 */
	@QDCommand(aliases = "remove", permissions = { "gates.remove" }, usage = "<gateId>", description = "remove a gate")
	public void cmdRemoveGate(CommandSender sender, Command command, String label, String[] args) throws Exception {
		if(args.length!=2){
			throw new QDCommandUsageException();
		}
		try{
			if(this.getGateManager().removeGate(Integer.parseInt(args[1]))){
				sender.sendMessage("Gate removed, you can remove blocks now");
			}else{
				sender.sendMessage("Gate Id doesn't exists");
			}
		}catch(Exception e){
			cmdListGates(sender,command,label,args);
		}
	}

	/**
	 * Cmd list gates.
	 *
	 * @param sender the sender
	 * @param command the command
	 * @param label the label
	 * @param args the args
	 * @throws Exception the exception
	 */
	@QDCommand(aliases = "list", permissions = { "gates.list" }, usage = "", description = "list all gates")
	public void cmdListGates(CommandSender sender, Command command, String label, String[] args) throws Exception {
		for(String networkId : getGateManager().getaGatesNetwork().keySet()){
			sender.sendMessage(ChatFormater.format("{ChatColor.GOLD}%s {ChatColor.WHITE}:",networkId));
			for(Gate gate : getGateManager().getaGatesNetwork().get(networkId)){
				sender.sendMessage(ChatFormater.format("{ChatColor.WHITE}Gate Id :{ChatColor.BLUE}%3d{ChatColor.WHITE} Pos : {ChatColor.BLUE}%3d,%3d,%3d",gate.getId(),(int)gate.getX(),(int)gate.getY(),(int)gate.getZ()));
			}
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