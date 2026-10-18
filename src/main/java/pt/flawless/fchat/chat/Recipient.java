package pt.flawless.fchat.chat;

import net.md_5.bungee.api.chat.BaseComponent;

import java.util.UUID;

/** Costura mínima sobre o jogador, para o envio se testar sem servidor. Só usar na thread principal. */
public interface Recipient {
    UUID id();

    String name();

    String world();

    double x();

    double y();

    double z();

    boolean isOnline();

    void send(BaseComponent[] message);
}
