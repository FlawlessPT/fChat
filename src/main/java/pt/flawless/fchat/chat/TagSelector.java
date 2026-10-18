package pt.flawless.fchat.chat;

import java.util.List;
import java.util.Set;

/** Escolhe a tag do chat: a primeira regra cujo grupo o jogador tem. */
public final class TagSelector {
    private final List<TagRule> rules;
    private final String defaultTag;

    public TagSelector(List<TagRule> rules, String defaultTag) {
        this.rules = rules;
        this.defaultTag = defaultTag;
    }

    public String select(Set<String> groups) {
        for (TagRule r : rules)
            for (String g : groups)
                if (g.equalsIgnoreCase(r.group())) return r.tag();
        return defaultTag;
    }
}
