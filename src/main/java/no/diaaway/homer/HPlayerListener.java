package no.diaaway.homer;

import org.bukkit.event.player.PlayerListener;

/**
 * Handle events for all Player related events
 * @author DiaAWAY
 */
public class HPlayerListener extends PlayerListener {

    private final HomerPlugin plugin;

    public HPlayerListener(HomerPlugin instance) {
        plugin = instance;
    }
}
