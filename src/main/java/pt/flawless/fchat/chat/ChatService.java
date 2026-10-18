package pt.flawless.fchat.chat;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import pt.flawless.fapi.chat.Channel;
import pt.flawless.fapi.economy.EconomyApi;
import pt.flawless.fapi.permissions.PermissionsApi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Formata e entrega mensagens de chat. {@code send} pode ser chamado de qualquer thread e nunca bloqueia a principal. */
public class ChatService {
    private final ChatConfig config;
    private final PermissionsApi perms;
    private final EconomyApi economy;
    private final Executor mainThread;
    private final Consumer<Throwable> onError;
    private final Supplier<? extends Collection<? extends Recipient>> online;
    private final Consumer<String> console;
    private final BooleanSupplier onMainThread;

    public ChatService(ChatConfig config, PermissionsApi perms, EconomyApi economy, Executor mainThread,
                       Consumer<Throwable> onError, Supplier<? extends Collection<? extends Recipient>> online,
                       Consumer<String> console, BooleanSupplier onMainThread) {
        this.config = config;
        this.perms = perms;
        this.economy = economy;
        this.mainThread = mainThread;
        this.onError = onError;
        this.online = online;
        this.console = console;
        this.onMainThread = onMainThread;
    }

    public void send(Player sender, Channel channel, String rawMessage) {
        send(new PlayerRecipient(sender), channel, rawMessage);
    }

    public void send(Recipient sender, Channel channel, String rawMessage) {
        String message = ChatFormat.sanitize(rawMessage);
        if (message.isEmpty()) return;

        CompletableFuture<Set<String>> groups = safe(() -> perms.getGroups(sender.id()), Set.of());
        CompletableFuture<Long> balance = safe(() -> economy.getBalance(sender.id()), null);
        CompletableFuture<Void> both = groups.thenCombine(balance, (g, b) -> {
            deliverLater(sender, channel, message, g, b);
            return null;
        });
        // Fora da thread principal esperamos (mantém a ordem das mensagens); na principal só encadeamos.
        if (!onMainThread.getAsBoolean()) both.join();
    }

    private <T> CompletableFuture<T> safe(Supplier<CompletableFuture<T>> call, T fallback) {
        try {
            return call.get().exceptionally(e -> {
                onError.accept(e);
                return fallback;
            });
        } catch (RuntimeException e) {
            onError.accept(e);
            return CompletableFuture.completedFuture(fallback);
        }
    }

    private void deliverLater(Recipient sender, Channel channel, String message, Set<String> groups, Long cents) {
        mainThread.execute(() -> {
            try {
                deliver(sender, channel, message, groups, cents);
            } catch (RuntimeException e) {
                onError.accept(e);
            }
        });
    }

    private void deliver(Recipient sender, Channel channel, String message, Set<String> groups, Long cents) {
        if (!sender.isOnline()) return;
        String hover = cents == null ? null : config.hoverMoney().replace("{dinheiro}", economy.format(cents));
        BaseComponent[] line = config.format().build(channel, config.tags().select(groups), sender.name(), message, hover);

        List<Recipient> targets = new ArrayList<>();
        int others = 0;
        for (Recipient r : online.get()) {
            if (!r.isOnline()) continue;
            boolean self = r.id().equals(sender.id());
            if (channel == Channel.LOCAL && !self && !RangeFilter.within(sender.world(), sender.x(), sender.y(),
                    sender.z(), r.world(), r.x(), r.y(), r.z(), config.radius())) continue;
            if (!self) others++;
            targets.add(r);
        }
        for (Recipient r : targets) r.send(line);

        StringBuilder plain = new StringBuilder();
        for (BaseComponent c : line) plain.append(c.toLegacyText());
        console.accept(plain.toString().replaceAll("§.", ""));

        if (channel == Channel.LOCAL && others == 0)
            sender.send(new BaseComponent[]{new TextComponent(config.nobodyNearby())});
    }
}
