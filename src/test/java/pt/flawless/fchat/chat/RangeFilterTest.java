package pt.flawless.fchat.chat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RangeFilterTest {
    @Test void exactRadiusIsInside() { assertTrue(RangeFilter.within("w", 0, 0, 0, "w", 50, 0, 0, 50)); }
    @Test void beyondRadiusIsOutside() { assertFalse(RangeFilter.within("w", 0, 0, 0, "w", 50.01, 0, 0, 50)); }
    @Test void otherWorldIsOutside() { assertFalse(RangeFilter.within("w", 1, 1, 1, "n", 1, 1, 1, 50)); }
}
