package pt.flawless.fchat.prefs;

import pt.flawless.fapi.chat.Channel;
import pt.flawless.fapi.chat.ChatApi;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/** {@link ChatApi} com cache dos jogadores online sobre um {@link ChatPrefsRepository}. */
public class ChatPrefs implements ChatApi {

    private final ChatPrefsRepository repo;
    private final Consumer<Throwable> onError;
    private final Map<UUID, Channel> cache = new ConcurrentHashMap<>();

    public ChatPrefs(ChatPrefsRepository repo, Consumer<Throwable> onError) {
        this.repo = repo;
        this.onError = onError;
    }

    /** Carrega a preferência para a cache; se falhar, regista o erro e o jogador fica em LOCAL. */
    public void load(UUID uuid) {
        repo.find(uuid).whenComplete((found, error) -> {
            if (error != null) {
                onError.accept(error);
            } else {
                found.ifPresent(channel -> cache.put(uuid, channel));
            }
        });
    }

    public void unload(UUID uuid) {
        cache.remove(uuid);
    }

    @Override
    public Channel getDefaultChannel(UUID uuid) {
        return cache.getOrDefault(uuid, Channel.LOCAL);
    }

    /** Grava primeiro; a cache só muda se a gravação correr bem. */
    @Override
    public CompletableFuture<Void> setDefaultChannel(UUID uuid, Channel channel) {
        return repo.save(uuid, channel).thenRun(() -> cache.put(uuid, channel));
    }
}
