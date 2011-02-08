package com.bukkit.diaaway.homer;

import java.io.File;
import java.util.HashMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import com.nijikokun.bukkit.iProperty;
import org.bukkit.Location;

/**
 * Plugin for bukkit that enables users to:
 * * save a home location and teleport to it
 * * protect the area round its home location
 *
 * @author DiaAWAY
 */
public class HomerPlugin extends JavaPlugin {

    private final HPlayerListener playerListener = new HPlayerListener(this);
    private final HBlockListener blockListener = new HBlockListener(this);
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
    private iProperty homes = new iProperty("homerPlugin.properties");
//    private boolean teleport = true; // wether teleport is enabled or not
    private double homeSize = 10; // initial homeSize of the home, for block destruction/placement purposes

    public HomerPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);
        // TODO: Place any custom initialisation code here

        // NOTE: Event registration should be done in onEnable not here as all events are unregistered when a plugin is disabled
    }

    public void onDisable() {
        // TODO: Place any custom disable code here

        // NOTE: All registered events are automatically unregistered when a plugin is disabled

        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        System.out.println("Goodbye world!");
    }

    public void onEnable() {
        // TODO: Place any custom enable code here including the registration of any events

        // Register our events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_PHYSICS, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_CANBUILD, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.Normal, this);

        // look for the property file, if it is not there create it, if it is; load it.


        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
    }

    public boolean isDebugging(final Player player) {
        if (debugees.containsKey(player)) {
            return debugees.get(player);
        } else {
            return false;
        }
    }

    public void setDebugging(final Player player, final boolean value) {
        debugees.put(player, value);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        String[] trimmedArgs = args;
        String cmdName = cmd.getName().toLowerCase();
        if (cmdName.equals("home")) {
            return performHome(sender, trimmedArgs);
        }
        if (cmdName.equals("sethome")) {
            return performSetHome(sender, trimmedArgs);
        }
        return false;
    }
    
    /**
     *
     * @param sender
     * @param split
     * @return
     */
    private boolean performSetHome(CommandSender sender, String[] split) {
        if (split.length != 0) return false; // this command is a oneliner, assume they need command description :)
        Player player = (Player) sender;
        homes.setDouble(player.getName() + ".x", player.getLocation().getX());
        homes.setDouble(player.getName() + ".y", player.getLocation().getY());
        homes.setDouble(player.getName() + ".z", player.getLocation().getZ());
        homes.setDouble(player.getName() + ".homeSize", getHomeSize());
        player.sendMessage("Home has been set!");
        return true;
    }

    /**
     *
     * @param sender
     * @param split
     * @return
     */
    private boolean performHome(CommandSender sender, String[] split) {
        Player player = (Player) sender;
        World world = sender instanceof Player ? ((Player) sender).getWorld() : getServer().getWorlds()[0];
        
        if (getServer().getPlayer(player.getName()) == null) {
            return false;
        }
        if (split.length != 0) return false; // this command is a oneliner, assume they need command description :)
        if (homes.keyExists(player.getName() + ".x")) { // check if the player's home is set. 

            player.teleportTo(new Location(world,
                    homes.getDouble(player.getName() + ".x"),
                    homes.getDouble(player.getName() + ".y"),
                    homes.getDouble(player.getName() + ".z")));
            player.sendMessage("Zwosh!");
            return true;
        }
        return false;
    }

    /**
     * @return the homeSize of the home
     */
    protected double getHomeSize() {
        return homeSize;
    }

    /**
     * @param homeSize the homeSize to set the home to
     */
    protected void setHomeSize(double size) {
        this.homeSize = size;
    }
}
