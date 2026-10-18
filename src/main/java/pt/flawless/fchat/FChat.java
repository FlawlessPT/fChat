package pt.flawless.fchat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import pt.flawless.fapi.chat.Channel;
import pt.flawless.fapi.chat.ChatApi;
import pt.flawless.fapi.database.DatabaseService;
import pt.flawless.fapi.database.Databases;
import pt.flawless.fapi.economy.Economies;
import pt.flawless.fapi.enums.EMessageTypeEnum;
import pt.flawless.fapi.logs.FConsoleLogger;
import pt.flawless.fapi.permissions.Permissions;
import pt.flawless.fapi.tags.TagSurface;
import pt.flawless.fapi.tags.Tags;
import pt.flawless.fapi.tags.TagsApi;
import pt.flawless.fapi.tags.TagsUnavailableException;
import pt.flawless.fchat.chat.ChatConfig;
import pt.flawless.fchat.chat.ChatListener;
import pt.flawless.fchat.chat.ChatService;
import pt.flawless.fchat.chat.PlayerRecipient;
import pt.flawless.fchat.commands.ChannelCommand;
import pt.flawless.fchat.commands.ChatCommand;
import pt.flawless.fchat.prefs.ChatPrefs;
import pt.flawless.fchat.prefs.PrefsListener;
import pt.flawless.fchat.prefs.SqlChatPrefsRepository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FChat extends JavaPlugin {
    private final FConsoleLogger consoleLogger = new FConsoleLogger(this.getName());

    @Override
    public void onEnable() {
        saveDefaultConfig();
        try {
            DatabaseService database = Databases.get();
            var economy = Economies.get();
            var perms = Permissions.get();
            var tags = tagsOrNoop();
            database.migrate(this, List.of("db/fchat/V001__create_prefs.sql"));

            ChatPrefs prefs = new ChatPrefs(new SqlChatPrefsRepository(database), this::logError);
            ChatConfig config = ChatConfig.from(getConfig());
            ChatService service = new ChatService(config, perms, economy, tags, this::runOnMainThread, this::logError,
                    () -> Bukkit.getOnlinePlayers().stream().map(PlayerRecipient::new).toList(),
                    line -> Bukkit.getConsoleSender().sendMessage(line), Bukkit::isPrimaryThread,
                    System::currentTimeMillis);

            getServer().getServicesManager().register(ChatApi.class, prefs, this, ServicePriority.Normal);
            Bukkit.getPluginManager().registerEvents(new PrefsListener(prefs), this);
            Bukkit.getPluginManager().registerEvents(new ChatListener(service, prefs), this);
            getCommand("l").setExecutor(new ChannelCommand(Channel.LOCAL, service, config));
            getCommand("g").setExecutor(new ChannelCommand(Channel.GLOBAL, service, config));
            getCommand("chat").setExecutor(new ChatCommand(prefs, config, this, this::logError));

            // Jogadores já online (/reload) não passam pelo PlayerJoinEvent.
            for (Player p : Bukkit.getOnlinePlayers()) prefs.load(p.getUniqueId());
        } catch (Exception e) {
            consoleLogger.sendMessage("não foi possível iniciar o chat: " + e.getMessage(), EMessageTypeEnum.ERROR);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        consoleLogger.sendEnablePluginMessage();
    }

    /** Sem fTags o chat segue sem tags (a mensagem sai com o formato atual). */
    private TagsApi tagsOrNoop() {
        try {
            return Tags.get();
        } catch (TagsUnavailableException e) {
            consoleLogger.sendMessage("fTags indisponível: o chat segue sem tags", EMessageTypeEnum.INFO);
            return new TagsApi() {
                public CompletableFuture<List<String>> getVisibleTags(UUID uuid, TagSurface surface) {
                    return CompletableFuture.completedFuture(List.of());
                }

                public CompletableFuture<Boolean> give(UUID uuid, String tagId) {
                    return CompletableFuture.completedFuture(false);
                }

                public CompletableFuture<Boolean> take(UUID uuid, String tagId) {
                    return CompletableFuture.completedFuture(false);
                }
            };
        }
    }

    private void logError(Throwable error) {
        consoleLogger.sendMessage(String.valueOf(error), EMessageTypeEnum.ERROR);
    }

    /** Com o plugin a desligar o scheduler recusa tarefas: regista e segue. */
    private void runOnMainThread(Runnable task) {
        try {
            getServer().getScheduler().runTask(this, task);
        } catch (RuntimeException e) {
            logError(e);
        }
    }

    @Override
    public void onDisable() {
        consoleLogger.sendMessage("desativado", EMessageTypeEnum.INFO);
    }
}
