package pt.flawless.fchat.prefs;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PrefsListener implements Listener {

    private final ChatPrefs prefs;

    public PrefsListener(ChatPrefs prefs) {
        this.prefs = prefs;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        prefs.load(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        prefs.unload(event.getPlayer().getUniqueId());
    }
}
