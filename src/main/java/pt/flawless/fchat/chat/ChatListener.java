package pt.flawless.fchat.chat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import pt.flawless.fapi.chat.ChatApi;

/** HIGH + ignoreCancelled: o fAuth cancela em NORMAL (não logados, código 2FA), que nunca deve ser difundido. */
public class ChatListener implements Listener {
    private final ChatService service;
    private final ChatApi prefs;

    public ChatListener(ChatService service, ChatApi prefs) {
        this.service = service;
        this.prefs = prefs;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        service.send(event.getPlayer(), prefs.getDefaultChannel(event.getPlayer().getUniqueId()), event.getMessage());
    }
}
