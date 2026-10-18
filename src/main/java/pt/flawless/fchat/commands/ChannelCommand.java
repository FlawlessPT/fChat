package pt.flawless.fchat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pt.flawless.fapi.chat.Channel;
import pt.flawless.fchat.chat.ChatConfig;
import pt.flawless.fchat.chat.ChatService;

/** {@code /l} e {@code /g}. O fAuth já cancela comandos a quem não fez login. */
public class ChannelCommand implements CommandExecutor {
    private final Channel channel;
    private final ChatService service;
    private final ChatConfig config;

    public ChannelCommand(Channel channel, ChatService service, ChatConfig config) {
        this.channel = channel;
        this.service = service;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(config.onlyPlayers());
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(channel == Channel.LOCAL ? config.usageLocal() : config.usageGlobal());
            return true;
        }
        service.send(player, channel, String.join(" ", args));
        return true;
    }
}
