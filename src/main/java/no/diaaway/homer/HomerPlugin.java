package no.diaaway.homer;

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
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.util.Vector;

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
    private final HEntityListener entityListener = new HEntityListener(this);
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
    private iProperty homes = new iProperty("homerPlugin.properties");
//    private boolean teleport = true; // wether teleport is enabled or not
    private double homeSize = 3; // initial homeSize of the home, for block destruction/placement purposes

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
        pm.registerEvent(Event.Type.ENTITY_TARGET, entityListener, Priority.Low, this);
        pm.registerEvent(Event.Type.ENTITY_DAMAGED, entityListener, Priority.Low, this);
        pm.registerEvent(Event.Type.BLOCK_PHYSICS, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_CANBUILD, blockListener, Priority.Low, this);
        pm.registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.Low, this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Low, this);
        pm.registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, Priority.Low, this);

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
        if (split.length != 0) {
            return false; // this command is a oneliner, assume they need command description :)
        }
        Player player = (Player) sender;
        if (isTrespassing(player)) {
            player.sendMessage("You can't put your home in someone elses home!");
            return true; // returns true to avoid the command description, fugly...
        }
        homes.setString(player.getName(),
                player.getLocation().getX()
                + "," + player.getLocation().getY()
                + "," + player.getLocation().getZ()
                + "," + getHomeSize());
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
        World world = sender instanceof Player ? ((Player) sender).getWorld() : getServer().getWorlds().get(0);

        if (getServer().getPlayer(player.getName()) == null) {
            return false;
        }
        if (split.length != 0) {
            return false; // this command is a oneliner, assume they need command description :)
        }
        if (homes.keyExists(player.getName())) { // check if the player's home is set.
            String home = homes.getString(player.getName());
            StringTokenizer st = new StringTokenizer(home, ",");
            Double[] pos = new Double[4];
            int i = 0;
            while (st.hasMoreTokens()) {
                pos[i] = Double.parseDouble(st.nextToken());
                i++;
            }
            player.teleportTo(new Location(world,
                    pos[0],
                    pos[1],
                    pos[2]));
            player.sendMessage("Zwosh!");
            return true;
        }
        return false;
    }

     /**
     *
     * @param player  - the player that might be trespassing
     * @return
     */
    protected boolean isTrespassing(Player player) {
        return isTrespassing(player,
                player.getLocation().getBlockX(),
                player.getLocation().getBlockY(),
                player.getLocation().getBlockZ());
    }

    /**
     *
     * @param player  - the player that might be trespassing
     * @param x - the x coordinate of the action the player is trying to do
     * @param y - the y coordinate of the action the player is trying to do
     * @param z - the z coordinate of the action the player is trying to do
     * @return
     */
    protected boolean isTrespassing(Player player, double x, double y, double z) { // TODO fix the pos checking, right now its a bit iffy whether blocks are protected or not
        Iterator owners = null;
        try {
            owners = homes.returnMap().keySet().iterator();
        } catch (Exception ex) {
            Logger.getLogger(HBlockListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (owners != null) {
            String strHomeData = "";
            String owner = "";
            Double[] homeData = new Double[4];
            int i = 0;
            Vector min = null;
            Vector max = null;
            Vector pos = new Vector(x, y, z);
            // populate the homeData array
            while (owners.hasNext()) { // checks all the owners registered with homes
                owner = (String) owners.next();
                strHomeData = homes.getString(owner);
                StringTokenizer st = new StringTokenizer(strHomeData, ",");
                while (st.hasMoreTokens() && i < 4) { // fill the home array with data; x, y, z, size
                    homeData[i] = Double.parseDouble(st.nextToken());
                    i++;
                }
                i = 0;
                // create two vectors and check if the player is between those two vectors
                min = new Vector(homeData[0] - homeData[3], homeData[1] - homeData[3], homeData[2] - homeData[3]);
                max = new Vector(homeData[0] + homeData[3], homeData[1] + homeData[3], homeData[2] + homeData[3]);
                if (pos.isInAABB(min, max)) {
                    System.out.println("isTrespassing: Action is taking place within protected area...");
                    if (player.getName().equalsIgnoreCase(owner)) {
                        System.out.println("    The player owns this area!");
                        System.out.println("    Checking whether there's a conflict.");
                    } else {
                        System.out.println("    This area is owned by someone else!");
                        return true;
                    }
                }
            }

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
