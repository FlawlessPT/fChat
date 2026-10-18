package pt.flawless.fchat.chat;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import pt.flawless.fapi.chat.Channel;

/** Monta a linha de chat a partir do formato; o nome é um componente com hover. */
public final class ChatFormat {
    private static final String NAME = "{nome}", MSG = "{mensagem}";

    private final String format, localPrefix, globalPrefix, nameColor, messageColor;

    public ChatFormat(String format, String localPrefix, String globalPrefix, String nameColor, String messageColor) {
        this.format = format;
        this.localPrefix = localPrefix;
        this.globalPrefix = globalPrefix;
        this.nameColor = nameColor;
        this.messageColor = messageColor;
    }

    public BaseComponent[] build(Channel channel, String tag, String name, String message, String hoverText) {
        String canal = channel == Channel.LOCAL ? localPrefix : globalPrefix;
        int i = format.indexOf(NAME);
        String before = i < 0 ? format : format.substring(0, i);
        String after = i < 0 ? "" : format.substring(i + NAME.length());
        TextComponent nameC = new TextComponent(nameColor + name);
        if (hoverText != null)
            nameC.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new BaseComponent[]{new TextComponent(hoverText)}));
        // A mensagem entra só no fim, numa passagem, para nunca ser re-expandida.
        return new BaseComponent[]{
                new TextComponent(withMessage(vars(before, canal, tag), message)),
                nameC,
                new TextComponent(withMessage(vars(after, canal, tag), message))};
    }

    private String vars(String s, String canal, String tag) {
        return s.replace("{canal}", canal).replace("{tag}", tag);
    }

    private String withMessage(String s, String message) {
        int j = s.indexOf(MSG);
        return j < 0 ? s : s.substring(0, j) + messageColor + message + s.substring(j + MSG.length());
    }

    public static String sanitize(String message) {
        return message.replaceAll("§.?", "").trim();
    }
}
