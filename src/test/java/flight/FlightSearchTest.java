package flight;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FlightSearchTest {
    @Test
    void exampleTest() {
        FlightSearch fs = new FlightSearch();
        boolean result = fs.runFlightSearch(
                "20/10/2025", "25/10/2025",
                false, "pvg",
                "mel", "economy",
                1, 0, 0
        );
        assertTrue(result);
    }
}
