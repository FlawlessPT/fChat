package pt.flawless.fchat.chat;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.flawless.fapi.chat.Channel;
import pt.flawless.fapi.economy.EconomyApi;
import pt.flawless.fapi.permissions.PermissionsApi;
import pt.flawless.fapi.tags.TagsApi;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class ChatServiceTest {

    static class Fake implements Recipient {
        final UUID id = UUID.randomUUID();
        final String name, world;
        final double x;
        boolean online = true;
        final List<BaseComponent[]> got = new ArrayList<>();

        Fake(String name, String world, double x) { this.name = name; this.world = world; this.x = x; }

        public UUID id() { return id; }
        public String name() { return name; }
        public String world() { return world; }
        public double x() { return x; }
        public double y() { return 0; }
        public double z() { return 0; }
        public boolean isOnline() { return online; }
        public void send(BaseComponent[] m) { got.add(m); }

        String last() { return plain(got.get(got.size() - 1)); }
    }

    static String plain(BaseComponent[] parts) {
        StringBuilder sb = new StringBuilder();
        for (BaseComponent p : parts) sb.append(p.toLegacyText());
        return sb.toString().replaceAll("§.", "");
    }

    Fake alice, bob, far, other;
    List<Fake> all;
    List<String> console = new ArrayList<>();
    List<Throwable> errors = new ArrayList<>();
    boolean groupsFail, balanceFail, onMain, tagsFail, tagsThrow, bypass;
    long now = 10_000;
    List<String> playerTags = List.of();
    Set<String> groups = Set.of();
    ChatConfig config;

    @BeforeEach
    void setUp() {
        alice = new Fake("Alice", "w", 0);
        bob = new Fake("Bob", "w", 50);
        far = new Fake("Far", "w", 50.01);
        other = new Fake("Other", "n", 0);
        all = List.of(alice, bob, far, other);
        config = new ChatConfig(50, new TagSelector(List.of(new TagRule("admin", "§c[ADMIN]")), "§7[Membro]"),
                new ChatFormat("{canal} {tags}{tag} {nome}: {mensagem}", "[L]", "[G]", "§f", "§f"),
                "Dinheiro: {dinheiro}", "Ninguém por perto.", "", "", "", "", "", "", "", "", 1000, "Devagar.");
    }

    ChatService service() {
        PermissionsApi perms = (PermissionsApi) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{PermissionsApi.class}, (p, m, a) -> m.getName().equals("has") ? bypass : groupsFail
                        ?CompletableFuture.failedFuture(new RuntimeException("db"))
                        : CompletableFuture.completedFuture(groups));
        EconomyApi eco = (EconomyApi) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{EconomyApi.class}, (p, m, a) -> {
                    if (m.getName().equals("format")) return "12,50€";
                    return balanceFail ? CompletableFuture.failedFuture(new RuntimeException("db"))
                            : CompletableFuture.completedFuture(1250L);
                });
        TagsApi tags = (TagsApi) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{TagsApi.class}, (p, m, a) -> {
                    if (tagsThrow) throw new RuntimeException("sync");
                    return tagsFail ? CompletableFuture.failedFuture(new RuntimeException("db"))
                            : CompletableFuture.completedFuture(playerTags);
                });
        return new ChatService(config, perms, eco, tags, Runnable::run, errors::add, () -> all, console::add, () -> onMain, () -> now);
    }

    @Test
    void secondMessageWithinCooldownIsDroppedAndSenderWarned() {
        ChatService s = service();
        s.send(alice, Channel.GLOBAL, "um");
        now += 999;
        s.send(alice, Channel.LOCAL, "dois");
        assertEquals(1, bob.got.size());
        assertEquals("Devagar.", alice.last());
        assertEquals(List.of("[G] [Membro] Alice: um"), console);
    }

    @Test
    void messageAfterCooldownIsSentAndCooldownIsPerPlayer() {
        ChatService s = service();
        s.send(alice, Channel.GLOBAL, "um");
        s.send(bob, Channel.GLOBAL, "outro");
        now += 1000;
        s.send(alice, Channel.GLOBAL, "dois");
        assertEquals("[G] [Membro] Alice: dois", far.last());
        assertEquals(3, console.size());
    }

    @Test
    void bypassPermissionSkipsCooldown() {
        bypass = true;
        ChatService s = service();
        s.send(alice, Channel.GLOBAL, "um");
        s.send(alice, Channel.GLOBAL, "dois");
        assertEquals(2, console.size());
        assertEquals("[G] [Membro] Alice: dois", bob.last());
    }

    @Test
    void blankMessageDoesNotStartCooldown() {
        ChatService s = service();
        s.send(alice, Channel.GLOBAL, "§c");
        s.send(alice, Channel.GLOBAL, "oi");
        assertEquals(List.of("[G] [Membro] Alice: oi"), console);
    }

    @Test
    void globalReachesEveryone() {
        service().send(alice, Channel.GLOBAL, "oi");
        for (Fake f : all) assertEquals("[G] [Membro] Alice: oi", f.last());
        assertEquals(List.of("[G] [Membro] Alice: oi"), console);
    }

    @Test
    void localOnlyWithinRadiusIncludingExactEdgeAndSender() {
        service().send(alice, Channel.LOCAL, "oi");
        assertEquals(1, alice.got.size());
        assertEquals(1, bob.got.size());
        assertTrue(far.got.isEmpty());
        assertTrue(other.got.isEmpty());
    }

    @Test
    void localAloneTellsNobodyNearby() {
        all = List.of(alice, far, other);
        service().send(alice, Channel.LOCAL, "oi");
        assertEquals(2, alice.got.size());
        assertEquals("Ninguém por perto.", alice.last());
    }

    @Test
    void groupsFailureFallsBackToDefaultTag() {
        groupsFail = true;
        service().send(alice, Channel.GLOBAL, "oi");
        assertEquals("[G] [Membro] Alice: oi", bob.last());
    }

    @Test
    void groupTagUsed() {
        groups = Set.of("admin");
        service().send(alice, Channel.GLOBAL, "oi");
        assertEquals("[G] [ADMIN] Alice: oi", bob.last());
    }

    @Test
    void balanceFailureMeansNoHoverButMessageSent() {
        balanceFail = true;
        service().send(alice, Channel.GLOBAL, "oi");
        assertNull(bob.got.get(0)[1].getHoverEvent());
        assertEquals("[G] [Membro] Alice: oi", bob.last());
    }

    @Test
    void hoverShowsMoney() {
        service().send(alice, Channel.GLOBAL, "oi");
        HoverEvent h = bob.got.get(0)[1].getHoverEvent();
        assertEquals("Dinheiro: 12,50€", ((TextComponent) h.getValue()[0]).getText());
    }

    @Test
    void colourOnlyMessageDropped() {
        service().send(alice, Channel.GLOBAL, "§c");
        for (Fake f : all) assertTrue(f.got.isEmpty());
        assertTrue(console.isEmpty());
    }

    @Test
    void offlineRecipientIgnored() {
        bob.online = false;
        service().send(alice, Channel.GLOBAL, "oi");
        assertTrue(bob.got.isEmpty());
        assertEquals(1, alice.got.size());
    }

    @Test
    void senderOfflineDropsMessage() {
        alice.online = false;
        service().send(alice, Channel.LOCAL, "oi");
        for (Fake f : all) assertTrue(f.got.isEmpty());
    }

    @Test
    void placeholdersInMessageNotExpanded() {
        service().send(alice, Channel.GLOBAL, "{nome} {tag} $1");
        assertEquals("[G] [Membro] Alice: {nome} {tag} $1", bob.last());
    }

    @Test
    void linhaTemTagsAntesDoGrupo() {
        playerTags = List.of("[Magnata]");
        service().send(alice, Channel.GLOBAL, "oi");
        assertEquals("[G] [Magnata] [Membro] Alice: oi", bob.last());
    }

    @Test
    void semTagsSeFalha() {
        tagsFail = true;
        service().send(alice, Channel.GLOBAL, "oi");
        assertEquals("[G] [Membro] Alice: oi", bob.last());
        assertEquals(1, errors.size());
    }

    @Test
    void tagsLancaSincrono() {
        tagsThrow = true;
        service().send(alice, Channel.GLOBAL, "oi");
        assertEquals("[G] [Membro] Alice: oi", bob.last());
        assertEquals(1, errors.size());
    }

    @Test
    void onMainThreadStillDelivers() {
        onMain = true;
        service().send(alice, Channel.GLOBAL, "oi");
        assertEquals("[G] [Membro] Alice: oi", bob.last());
    }
}
