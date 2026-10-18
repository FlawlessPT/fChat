package pt.flawless.fchat.prefs;

import pt.flawless.fapi.chat.Channel;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Persistência do canal por omissão de cada jogador. */
public interface ChatPrefsRepository {

    /** @return o canal guardado, ou vazio se o jogador não tiver registo */
    CompletableFuture<Optional<Channel>> find(UUID uuid);

    CompletableFuture<Void> save(UUID uuid, Channel channel);
}
