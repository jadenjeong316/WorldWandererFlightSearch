package flight;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class FlightSearchTest {

    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    private String today() { return LocalDate.now().format(DMY); }
    private String tomorrow() { return LocalDate.now().plusDays(1).format(DMY); }
    private String nextWeek() { return LocalDate.now().plusDays(7).format(DMY); }

    /** 항상 유효한 기준 상태를 한 번 만들어 두는 헬퍼 (Note 7: 상태 비교용) */
    private boolean validBaseline(FlightSearch fs) {
        return fs.runFlightSearch(
                tomorrow(), nextWeek(),  // departure, return
                false, "pvg",            // airports
                "mel",                   // emergency row
                "economy",               // class
                1, 0, 0                  // adults, children, infants
        );
    }

    // =========================
    // TC01 – Condition 1: 총 승객 수 1~9
    // =========================
    @Test
    void condition1_totalPassengers_between1and9() {
        FlightSearch fs = new FlightSearch();
        assertTrue(validBaseline(fs)); // 상태 시드

        // A) 0명 → false, 상태 유지
        boolean r1 = fs.runFlightSearch(tomorrow(), nextWeek(), false, "pvg", "mel", "economy", 0, 0, 0);
        assertFalse(r1);
        assertEquals("economy", fs.getSeatingClass()); // unchanged

        // B) 10명 → false, 상태 유지
        boolean r2 = fs.runFlightSearch(tomorrow(), nextWeek(), false, "pvg", "mel", "economy", 5, 5, 0);
        assertFalse(r2);
        assertEquals("economy", fs.getSeatingClass()); // unchanged
    }

    // =========================
    // TC02 – Condition 2: 어린이(2–11) 비상구/1등석 불가
    // =========================
    @Test
    void condition2_children_notEmergency_or_First() {
        FlightSearch fs = new FlightSearch();
        assertTrue(validBaseline(fs));

        // A) child>0 + emergencyRow=true → false
        boolean r1 = fs.runFlightSearch(tomorrow(), nextWeek(), true, "pvg", "mel", "economy", 1, 1, 0);
        assertFalse(r1);
        assertEquals(0, fs.getChildPassengerCount()); // unchanged

        // B) child>0 + class=first → false
        boolean r2 = fs.runFlightSearch(tomorrow(), nextWeek(), false, "pvg", "mel", "first", 1, 1, 0);
        assertFalse(r2);
        assertEquals("economy", fs.getSeatingClass()); // unchanged
    }

    // =========================
    // TC03 – Condition 3: 유아(<2) 비상구/비즈니스석 불가
    // =========================
    @Test
    void condition3_infants_notEmergency_or_Business() {
        FlightSearch fs = new FlightSearch();
        assertTrue(validBaseline(fs));

        // A) infant>0 + emergencyRow=true → false
        boolean r1 = fs.runFlightSearch(tomorrow(), nextWeek(), true, "pvg", "mel", "economy", 1, 0, 1);
        assertFalse(r1);
        assertEquals(0, fs.getInfantPassengerCount()); // unchanged

        // B) infant>0 + class=business → false
        boolean r2 = fs.runFlightSearch(tomorrow(), nextWeek(), false, "pvg", "mel", "business", 1, 0, 1);
        assertFalse(r2);
        assertEquals("economy", fs.getSeatingClass()); // unchanged
    }

    // =========================
    // TC04 – Condition 4: 아이 ≤ 2명/성인 1명 (경계: 2 OK, 3 NO)
    // =========================
    @Test
    void condition4_childrenAdjacency_ratioBoundaries() {
        FlightSearch fs = new FlightSearch();

        // A) 1 adult, 2 children → true
        boolean ok = fs.runFlightSearch(tomorrow(), nextWeek(), false, "pvg", "mel", "economy", 1, 2, 0);
        assertTrue(ok);
        assertEquals(2, fs.getChildPassengerCount());

        // B) 1 adult, 3 children → false (상태 유지)
        boolean bad = fs.runFlightSearch(tomorrow(), nextWeek(), false, "pvg", "mel", "economy", 1, 3, 0);
        assertFalse(bad);
        assertEquals(2, fs.getChildPassengerCount()); // unchanged
    }

    // =========================
    // TC05 – Condition 5: 유아 ≤ 1명/성인 1명 (경계: = OK, > NO)
    // =========================
    @Test
    void condition5_infantLap_ratioBoundaries() {
        FlightSearch fs = new FlightSearch();

        // A) 2 adults, 2 infants → true
        boolean ok = fs.runFlightSearch(tomorrow(), nextWeek(), false, "pvg", "mel", "economy", 2, 0, 2);
        assertTrue(ok);
        assertEquals(2, fs.getInfantPassengerCount());

        // B) 1 adult, 2 infants → false (상태 유지)
        boolean bad = fs.runFlightSearch(tomorrow(), nextWeek(), false, "pvg", "mel", "economy", 1, 0, 2);
        assertFalse(bad);
        assertEquals(2, fs.getInfantPassengerCount()); // unchanged
    }

    // =========================
    // TC06 – Condition 6: 출발일 과거 불가 (today OK)
    // =========================
    @Test
    void condition6_departure_notInPast() {
        FlightSearch fs = new FlightSearch();
        assertTrue(validBaseline(fs));

        // A) 어제 → false
        String yesterday = LocalDate.now().minusDays(1).format(DMY);
        boolean r1 = fs.runFlightSearch(yesterday, nextWeek(), false, "pvg", "mel", "economy", 1, 0, 0);
        assertFalse(r1);

        // B) 오늘 → true (귀국≥오늘)
        boolean r2 = fs.runFlightSearch(today(), nextWeek(), false, "pvg", "mel", "economy", 1, 0, 0);
        assertTrue(r2);
        assertEquals(today(), fs.getDepartureDate());
    }

    // =========================
    // TC07 – Condition 7: 날짜 형식/존재 엄격 검증 (윤년 포함)
    // =========================
    @Test
    void condition7_strictDateValidation() {
        FlightSearch fs = new FlightSearch();

        // A) 잘못된 날짜: 29/02/2025 (윤년 아님) → false
        boolean bad = fs.runFlightSearch("28/02/2025", "29/02/2025", false, "pvg", "mel", "economy", 1, 0, 0);
        assertFalse(bad);

        // B) 윤년: 2028년 2월 29일 → true
        boolean ok = fs.runFlightSearch("28/02/2028", "29/02/2028", false, "pvg", "mel", "economy", 1, 0, 0);
        assertTrue(ok);
        assertEquals("29/02/2028", fs.getReturnDate());
    }

    // =========================
    // TC08 – Condition 8: 왕복, 귀국일은 출발일보다 빠를 수 없음
    // =========================
    @Test
    void condition8_returnNotBeforeDeparture() {
        FlightSearch fs = new FlightSearch();

        // A) 같은 날 왕복 → true
        boolean ok = fs.runFlightSearch(tomorrow(), tomorrow(), false, "pvg", "mel", "economy", 1, 0, 0);
        assertTrue(ok);

        // B) 귀국 < 출발 → false (상태 유지)
        String day2 = LocalDate.now().plusDays(2).format(DMY);
        String day1 = LocalDate.now().plusDays(1).format(DMY);
        boolean bad = fs.runFlightSearch(day2, day1, false, "pvg", "mel", "economy", 1, 0, 0);
        assertFalse(bad);
        assertEquals(tomorrow(), fs.getDepartureDate()); // unchanged from A
    }

    // =========================
    // TC09 – Condition 9: 좌석 등급 유효성
    // =========================
    @Test
    void condition9_seatingClass_allowedSetOnly() {
        FlightSearch fs = new FlightSearch();
        assertTrue(validBaseline(fs));

        // A) 무효 등급 → false
        boolean bad = fs.runFlightSearch(tomorrow(), nextWeek(), false, "pvg", "mel", "ultra", 1, 0, 0);
        assertFalse(bad);

        // B) 유효 등급 (premium economy) → true
        boolean ok = fs.runFlightSearch(tomorrow(), nextWeek(), false, "pvg", "mel", "premium economy", 1, 0, 0);
        assertTrue(ok);
        assertEquals("premium economy", fs.getSeatingClass());
    }

    // =========================
    // TC10 – Condition 10: 비상구 좌석은 economy만 가능
    // =========================
    @Test
    void condition10_emergencyRow_onlyEconomy() {
        FlightSearch fs = new FlightSearch();

        // A) economy + emergencyRow=true → true
        boolean ok = fs.runFlightSearch(tomorrow(), nextWeek(), true, "pvg", "mel", "economy", 1, 0, 0);
        assertTrue(ok);
        assertTrue(fs.isEmergencyRowSeating());

        // B) business + emergencyRow=true → false (상태 유지)
        boolean bad = fs.runFlightSearch(tomorrow(), nextWeek(), true, "pvg", "mel", "business", 1, 0, 0);
        assertFalse(bad);
        assertEquals("economy", fs.getSeatingClass());
        assertTrue(fs.isEmergencyRowSeating()); // unchanged
    }

    // =========================
    // TC11 – Condition 11: 허용 공항 코드 & 출발≠도착
    // =========================
    @Test
    void condition11_airports_allowedAndDifferent() {
        FlightSearch fs = new FlightSearch();

        // A) 무효 공항 코드 → false
        boolean bad1 = fs.runFlightSearch(tomorrow(), nextWeek(), false, "pvg", "xyz", "economy", 1, 0, 0);
        assertFalse(bad1);

        // B) 출발 = 도착 → false
        boolean bad2 = fs.runFlightSearch(tomorrow(), nextWeek(), false, "mel", "mel", "economy", 1, 0, 0);
        assertFalse(bad2);
    }

    // =========================
    // TC12 – All Valid: 모든 입력이 유효한 조합 4개
    // =========================
    @Test
    void allValid_fourCombinations() {
        FlightSearch fs = new FlightSearch();

        // A) economy, non-emergency
        assertTrue(fs.runFlightSearch(tomorrow(), nextWeek(), false, "pvg", "mel", "economy", 1, 0, 0));

        // B) economy, emergency
        assertTrue(fs.runFlightSearch(tomorrow(), nextWeek(), true, "lax", "syd", "economy", 2, 0, 0));

        // C) premium economy, non-emergency
        assertTrue(fs.runFlightSearch(tomorrow(), nextWeek(), false, "doh", "cdg", "premium economy", 3, 0, 0));

        // D) 가족 조합(비율 충족): adult=2, child=4, infant=2 (economy, non-emergency)
        assertTrue(fs.runFlightSearch(tomorrow(), nextWeek(), false, "pvg", "del", "economy", 2, 4, 2));
        assertEquals(2, fs.getAdultPassengerCount());
        assertEquals(4, fs.getChildPassengerCount());
        assertEquals(2, fs.getInfantPassengerCount());
    }
}
