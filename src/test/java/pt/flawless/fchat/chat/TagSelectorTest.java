package pt.flawless.fchat.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TagSelectorTest {
    private final TagSelector sel = new TagSelector(
            List.of(new TagRule("admin", "[Admin]"), new TagRule("vip", "[VIP]")), "[Membro]");

    @Test void firstRuleWinsCaseInsensitive() { assertEquals("[Admin]", sel.select(Set.of("VIP", "admin"))); }
    @Test void emptyGivesDefault() { assertEquals("[Membro]", sel.select(Set.of())); }
    @Test void unknownGivesDefault() { assertEquals("[Membro]", sel.select(Set.of("x"))); }
}
