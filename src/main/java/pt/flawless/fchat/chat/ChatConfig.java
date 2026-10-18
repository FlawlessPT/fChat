package pt.flawless.fchat.chat;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Valores do config.yml já interpretados. */
public record ChatConfig(double radius, TagSelector tags, ChatFormat format, String hoverMoney,
                         String nobodyNearby, String usageLocal, String usageGlobal, String usageChat,
                         String channelLocal, String channelGlobal, String currentChannel,
                         String saveFailed, String onlyPlayers, long cooldownMs, String cooldownMessage) {

    public static ChatConfig from(ConfigurationSection c) {
        List<TagRule> rules = new ArrayList<>();
        for (Map<?, ?> m : c.getMapList("tags"))
            rules.add(new TagRule(String.valueOf(m.get("group")), String.valueOf(m.get("tag"))));
        return new ChatConfig(
                c.getDouble("radius", 50),
                new TagSelector(rules, c.getString("default-tag", "§7[Membro]")),
                new ChatFormat(c.getString("format", "{canal} {tags}{tag} {nome}: {mensagem}"),
                        c.getString("format-local-prefix", "§e[L]"), c.getString("format-global-prefix", "§a[G]"),
                        c.getString("name-color", "§f"), c.getString("message-color", "§f")),
                c.getString("hover-money", "§7Dinheiro: §f{dinheiro}"),
                c.getString("nobody-nearby", "§7Ninguém por perto para te ouvir."),
                c.getString("usage-local", "Uso: /l <mensagem>"),
                c.getString("usage-global", "Uso: /g <mensagem>"),
                c.getString("usage-chat", "Uso: /chat <local|global>"),
                c.getString("channel-local", "§7Chat por defeito: §eLocal"),
                c.getString("channel-global", "§7Chat por defeito: §aGlobal"),
                c.getString("current-channel", "§7Canal atual: §f{canal}"),
                c.getString("save-failed", "§cNão foi possível guardar a preferência."),
                c.getString("only-players", "Apenas jogadores."),
                c.getLong("cooldown-ms", 1000),
                c.getString("cooldown-message", "§cEspera um pouco antes de enviar outra mensagem."));
    }
}
