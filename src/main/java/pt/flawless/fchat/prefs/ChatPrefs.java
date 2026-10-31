package pt.flawless.fchat.prefs;

import pt.flawless.fapi.chat.Channel;
import pt.flawless.fapi.chat.ChatApi;
import pt.flawless.fapi.prefs.Pref;
import pt.flawless.fapi.prefs.PrefOption;
import pt.flawless.fapi.prefs.PrefsApi;

import org.bukkit.Material;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** {@link ChatApi} sobre a preferência {@code chat-channel} do fPrefs. */
public class ChatPrefs implements ChatApi {

    private static final String ID = "chat-channel";

    private final PrefsApi prefs;

    public ChatPrefs(PrefsApi prefs) {
        this.prefs = prefs;
        prefs.register(new Pref(ID, "§eChat padrão", Material.WRITABLE_BOOK,
                List.of("§7Canal em que falas sem usar /l ou /g."),
                List.of(new PrefOption("local", "§aLocal"), new PrefOption("global", "§bGlobal")),
                "local"));
    }

    @Override
    public Channel getDefaultChannel(UUID uuid) {
        return "global".equals(prefs.get(uuid, ID)) ? Channel.GLOBAL : Channel.LOCAL;
    }

    @Override
    public CompletableFuture<Void> setDefaultChannel(UUID uuid, Channel channel) {
        return prefs.set(uuid, ID, channel == Channel.GLOBAL ? "global" : "local");
    }
}
