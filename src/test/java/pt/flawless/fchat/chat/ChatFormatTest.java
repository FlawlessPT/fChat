package pt.flawless.fchat.chat;

import static org.junit.jupiter.api.Assertions.*;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.junit.jupiter.api.Test;
import pt.flawless.fapi.chat.Channel;

class ChatFormatTest {
    private final ChatFormat f = new ChatFormat("{canal} {tag} {nome}: {mensagem}", "§e[L]", "§6[G]", "§f", "§f");

    private static String plain(BaseComponent[] cs) {
        StringBuilder sb = new StringBuilder();
        for (BaseComponent c : cs) sb.append(((TextComponent) c).getText());
        return sb.toString();
    }

    @Test void plainText() {
        assertEquals("§e[L] §7[Membro] §fNome: §fola", plain(f.build(Channel.LOCAL, "§7[Membro]", "Nome", "ola", null)));
    }

    @Test void messageNotReExpanded() {
        String p = plain(f.build(Channel.GLOBAL, "T", "Nome", "{nome} $1 \\", null));
        assertTrue(p.endsWith("{nome} $1 \\"));
        assertEquals(1, p.split("Nome", -1).length - 1);
    }

    @Test void hover() {
        BaseComponent[] cs = f.build(Channel.LOCAL, "T", "Nome", "x", "info");
        BaseComponent name = null;
        for (BaseComponent c : cs) if (((TextComponent) c).getText().contains("Nome")) name = c;
        assertEquals(HoverEvent.Action.SHOW_TEXT, name.getHoverEvent().getAction());
        assertEquals("info", ((TextComponent) name.getHoverEvent().getValue()[0]).getText());
        for (BaseComponent c : f.build(Channel.LOCAL, "T", "Nome", "x", null)) assertNull(c.getHoverEvent());
    }

    @Test void sanitize() {
        assertEquals("ola", ChatFormat.sanitize("§cola"));
        assertEquals("", ChatFormat.sanitize("§c"));
    }
}
