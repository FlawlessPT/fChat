package pt.flawless.fchat.chat;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public record PlayerRecipient(Player player) implements Recipient {
    @Override public UUID id() { return player.getUniqueId(); }
    @Override public String name() { return player.getName(); }
    @Override public String world() { return player.getWorld().getName(); }
    @Override public double x() { Location l = player.getLocation(); return l.getX(); }
    @Override public double y() { return player.getLocation().getY(); }
    @Override public double z() { return player.getLocation().getZ(); }
    @Override public boolean isOnline() { return player.isOnline(); }
    @Override public void send(BaseComponent[] message) { player.spigot().sendMessage(message); }
}
