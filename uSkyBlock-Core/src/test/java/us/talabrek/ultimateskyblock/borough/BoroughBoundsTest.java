package us.talabrek.ultimateskyblock.borough;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BoroughBoundsTest {
    @Test void includesWholeHomeAndBufferNotOnlyItsCenter() {
        var city = new BoroughBounds(900, -300, 1100, -100);
        assertTrue(city.intersects(1150, -200, 64, 32));
        assertTrue(city.intersects(1196, -200, 64, 32));
        assertFalse(city.intersects(1197, -200, 64, 32));
        assertTrue(city.intersects(805, -200, 64, 32));
        assertFalse(city.intersects(804, -200, 64, 32));
    }

    @Test void centerIsOnExistingIslandGridEvenForNegativeCoordinates() {
        var city = new BoroughBounds(900, -300, 1100, -100);
        assertEquals(1024, city.centerX(128));
        assertEquals(-256, city.centerZ(128));
    }

    @Test void rejectsInvertedSelectionBounds() {
        assertThrows(IllegalArgumentException.class, () -> new BoroughBounds(10, 0, -10, 0));
    }
}
