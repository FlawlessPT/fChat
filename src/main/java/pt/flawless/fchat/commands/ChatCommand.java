package pt.flawless.fchat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import pt.flawless.fapi.chat.Channel;
import pt.flawless.fapi.chat.ChatApi;
import pt.flawless.fchat.chat.ChatConfig;

import java.util.function.Consumer;

/** {@code /chat [local|global]}: canal do chat normal. */
public class ChatCommand implements CommandExecutor {
    private final ChatApi prefs;
    private final ChatConfig config;
    private final Plugin plugin;
    private final Consumer<Throwable> onError;

    public ChatCommand(ChatApi prefs, ChatConfig config, Plugin plugin, Consumer<Throwable> onError) {
        this.prefs = prefs;
        this.config = config;
        this.plugin = plugin;
        this.onError = onError;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(config.onlyPlayers());
            return true;
        }
        if (args.length == 0) {
            Channel current = prefs.getDefaultChannel(player.getUniqueId());
            player.sendMessage(config.currentChannel().replace("{canal}", current == Channel.GLOBAL ? "Global" : "Local"));
            player.sendMessage(config.usageChat());
            return true;
        }
        Channel wanted = args.length == 1 ? switch (args[0].toLowerCase()) {
            case "local" -> Channel.LOCAL;
            case "global" -> Channel.GLOBAL;
            default -> null;
        } : null;
        if (wanted == null) {
            player.sendMessage(config.usageChat());
            return true;
        }
        prefs.setDefaultChannel(player.getUniqueId(), wanted).whenComplete((v, error) -> {
            if (error != null) onError.accept(error);
            String msg = error != null ? config.saveFailed()
                    : wanted == Channel.GLOBAL ? config.channelGlobal() : config.channelLocal();
            try {
                plugin.getServer().getScheduler().runTask(plugin, () -> player.sendMessage(msg));
            } catch (RuntimeException e) {
                onError.accept(e); // plugin a desligar
            }
        });
        return true;
    }
}
