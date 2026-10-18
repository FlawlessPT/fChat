package pt.flawless.fchat.prefs;

import org.junit.jupiter.api.Test;
import pt.flawless.fapi.chat.Channel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ChatPrefsTest {

    private static class FakeRepo implements ChatPrefsRepository {
        final Map<UUID, Channel> data = new HashMap<>();
        boolean failFind;
        boolean failSave;

        @Override
        public CompletableFuture<Optional<Channel>> find(UUID uuid) {
            if (failFind) return CompletableFuture.failedFuture(new RuntimeException("find"));
            return CompletableFuture.completedFuture(Optional.ofNullable(data.get(uuid)));
        }

        @Override
        public CompletableFuture<Void> save(UUID uuid, Channel channel) {
            if (failSave) return CompletableFuture.failedFuture(new RuntimeException("save"));
            data.put(uuid, channel);
            return CompletableFuture.completedFuture(null);
        }
    }

    private final UUID id = UUID.randomUUID();
    private final FakeRepo repo = new FakeRepo();
    private final AtomicReference<Throwable> error = new AtomicReference<>();
    private final ChatPrefs prefs = new ChatPrefs(repo, error::set);

    @Test
    void unknownIsLocal() {
        assertEquals(Channel.LOCAL, prefs.getDefaultChannel(id));
    }

    @Test
    void loadReadsStoredChannel() {
        repo.data.put(id, Channel.GLOBAL);
        prefs.load(id);
        assertEquals(Channel.GLOBAL, prefs.getDefaultChannel(id));
    }

    @Test
    void setSavesThenCaches() throws Exception {
        prefs.setDefaultChannel(id, Channel.GLOBAL).get();
        assertEquals(Channel.GLOBAL, prefs.getDefaultChannel(id));
        assertEquals(Channel.GLOBAL, repo.data.get(id));
    }

    @Test
    void failedSaveKeepsCache() {
        repo.failSave = true;
        assertThrows(ExecutionException.class, () -> prefs.setDefaultChannel(id, Channel.GLOBAL).get());
        assertEquals(Channel.LOCAL, prefs.getDefaultChannel(id));
    }

    @Test
    void unloadResetsToLocal() {
        repo.data.put(id, Channel.GLOBAL);
        prefs.load(id);
        prefs.unload(id);
        assertEquals(Channel.LOCAL, prefs.getDefaultChannel(id));
    }

    @Test
    void failedLoadReportsAndStaysLocal() {
        repo.failFind = true;
        prefs.load(id);
        assertNotNull(error.get());
        assertEquals(Channel.LOCAL, prefs.getDefaultChannel(id));
    }
}
