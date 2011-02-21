package no.diaaway.homer;

import org.bukkit.block.BlockDamageLevel;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * HomerPlugin block listener
 * @author DiaAWAY
 */
public class HBlockListener extends BlockListener {

    private final HomerPlugin plugin;

    public HBlockListener(final HomerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (plugin.isTrespassing(player,
                event.getBlockPlaced().getLocation().getBlockX(),
                event.getBlockPlaced().getLocation().getBlockY(),
                event.getBlockPlaced().getLocation().getBlockZ())) {
            event.setCancelled(true);
            player.sendMessage("This land belongs to someone, you can't place blocks here!");
        }
    }

    @Override
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        if (plugin.isTrespassing(player,
                event.getBlock().getLocation().getBlockX(),
                event.getBlock().getLocation().getBlockY(),
                event.getBlock().getLocation().getBlockZ())) {
            event.setCancelled(true);
            if (event.getDamageLevel().equals(BlockDamageLevel.BROKEN)) {
                player.sendMessage("This land belongs to someone, you can't break blocks here!");
            }
        }
    }

//    @Override
//    public void onBlockBreak(BlockBreakEvent event) { // Doesn't seem like this function actually works? But onBlockDamage does...
//        Player player = event.getPlayer();
//        if (plugin.isTrespassing(player,
//                event.getBlock().getX(),
//                event.getBlock().getY(),
//                event.getBlock().getZ())) {
//            event.setCancelled(true);
//            player.sendMessage("This land belongs to someone, you can't break blocks here!");
//        }
//    }
}
