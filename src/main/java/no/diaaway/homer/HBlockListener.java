package no.diaaway.homer;

import com.nijikokun.bukkit.iProperty;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * HomerPlugin block listener
 * @author DiaAWAY
 */
public class HBlockListener extends BlockListener {

    private final HomerPlugin plugin;
    private iProperty homes = new iProperty("homerPlugin.properties");

    public HBlockListener(final HomerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (isTrespassing(player)) {
            event.setCancelled(true);
            player.sendMessage("This land belongs to someone, you can't place blocks here!");
        }
    }

    private boolean isTrespassing(Player player) {
        boolean passX = true;
        boolean passY = true;
        boolean passZ = true;
        Iterator it = null;
        try {
            it = homes.returnMap().keySet().iterator();
        } catch (Exception ex) {
            Logger.getLogger(HBlockListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (it != null) {
            Location loc = player.getLocation();
            while (it.hasNext()) {
                System.out.println("a: " + it.next());

            }
//            int mod = 1;
//            for (int i = 0; i < values.length; i++) {
//                System.out.println("values: " + values[i]);
//                // magic numbers ahoy! ;D
//                // check x
//                if (values[(i * mod)] + values[(i * mod) + 3] >= loc.getX()
//                        && values[(i * mod)] - values[(i * mod) + 3] <= loc.getX()) {
//                    passX = false;
//                }
//                // check y
//                if (values[(i * mod) + 1] + values[(i * mod) + 3] >= loc.getX()
//                        && values[(i * mod)] - values[(i * mod) + 3] <= loc.getX()) {
//                    passY = false;
//                }
//                // check z
//                if (values[(i * mod) + 2] + values[(i * mod) + 3] >= loc.getX()
//                        && values[(i * mod)] - values[(i * mod) + 3] <= loc.getX()) {
//                    passZ = false;
//                }
//                if (i % 4 == 0) {
//                    mod += 1;
//                }
//            }
        }
        return (passX && passY && passZ);
    }
}
