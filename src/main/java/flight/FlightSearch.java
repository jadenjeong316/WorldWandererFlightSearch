package flight;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Set;

public class FlightSearch {
    private String  departureDate;
    private String  departureAirportCode;
    private boolean emergencyRowSeating;
    private String  returnDate;
    private String  destinationAirportCode;
    private String  seatingClass;
    private int     adultPassengerCount;
    private int     childPassengerCount;
    private int     infantPassengerCount;

    // ===== Getters (for JUnit) =====
    public String getDepartureDate() { return departureDate; }
    public String getReturnDate() { return returnDate; }
    public String getDepartureAirportCode() { return departureAirportCode; }
    public String getDestinationAirportCode() { return destinationAirportCode; }
    public boolean isEmergencyRowSeating() { return emergencyRowSeating; }
    public String getSeatingClass() { return seatingClass; }
    public int getAdultPassengerCount() { return adultPassengerCount; }
    public int getChildPassengerCount() { return childPassengerCount; }
    public int getInfantPassengerCount() { return infantPassengerCount; }

    // ===== Constants =====
    private static final Set<String> ALLOWED_AIRPORTS =
            Set.of("syd", "mel", "lax", "cdg", "del", "pvg", "doh");
    private static final Set<String> ALLOWED_CLASSES =
            Set.of("economy", "premium economy", "business", "first");
    private static final DateTimeFormatter STRICT_DMY =
            DateTimeFormatter.ofPattern("dd/MM/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    /**
     * 테스트가 사용하는 표준 시그니처로 변경:
     * departureDate, returnDate,
     * departureAirportCode, destinationAirportCode,
     * emergencyRowSeating, seatingClass,
     * adultPassengerCount, childPassengerCount, infantPassengerCount
     */
    public boolean runFlightSearch(
            String departureDate,
            String returnDate,
            boolean emergencyRowSeating,
            String departureAirportCode,
            String destinationAirportCode,
            String seatingClass,
            int adultPassengerCount,
            int childPassengerCount,
            int infantPassengerCount) {

        // trim (사양상 소문자 가정, 방어적 처리)
        String depDateStr  = safeTrim(departureDate);
        String retDateStr  = safeTrim(returnDate);
        String depAirport  = safeTrim(departureAirportCode);
        String destAirport = safeTrim(destinationAirportCode);
        String seatClass   = safeTrim(seatingClass);

        // C9: 좌석 등급
        if (!ALLOWED_CLASSES.contains(seatClass)) return false;

        // C10: 비상구 좌석은 economy만 가능
        if (emergencyRowSeating && !seatClass.equals("economy")) return false;

        // C11: 공항 코드 유효 & 출발 ≠ 도착
        if (!ALLOWED_AIRPORTS.contains(depAirport) || !ALLOWED_AIRPORTS.contains(destAirport)) return false;
        if (depAirport.equals(destAirport)) return false;

        // C7: 날짜 엄격 검증 (존재하는 날짜만, dd/MM/uuuu)
        LocalDate dep, ret;
        try {
            dep = LocalDate.parse(depDateStr, STRICT_DMY);
            ret = LocalDate.parse(retDateStr, STRICT_DMY);
        } catch (Exception e) {
            return false;
        }

        // C6: 출발일 과거 불가(오늘 OK)
        if (dep.isBefore(LocalDate.now())) return false;

        // C8: 귀국일은 출발일보다 빠를 수 없음 (같은 날 OK)
        if (ret.isBefore(dep)) return false;

        // C1: 총 승객 1..9
        int total = adultPassengerCount + childPassengerCount + infantPassengerCount;
        if (total < 1 || total > 9) return false;

        // C4: 아이 ≤ 2/성인 (아이>0이면 성인≥1)
        if (childPassengerCount > 0) {
            if (adultPassengerCount < 1) return false;
            if (childPassengerCount > 2 * adultPassengerCount) return false;
        }

        // C5: 유아 ≤ 1/성인 (유아>0이면 성인≥1)
        if (infantPassengerCount > 0) {
            if (adultPassengerCount < 1) return false;
            if (infantPassengerCount > adultPassengerCount) return false;
        }

        // C2: 아이는 비상구/1등석 불가
        if (childPassengerCount > 0) {
            if (emergencyRowSeating) return false;
            if (seatClass.equals("first")) return false;
        }

        // C3: 유아는 비상구/비즈니스석 불가
        if (infantPassengerCount > 0) {
            if (emergencyRowSeating) return false;
            if (seatClass.equals("business")) return false;
        }

        // ✅ 모든 조건 통과 → 이 시점에서만 필드 초기화 (Note 7)
        this.departureDate = depDateStr;
        this.returnDate = retDateStr;
        this.departureAirportCode = depAirport;
        this.destinationAirportCode = destAirport;
        this.emergencyRowSeating = emergencyRowSeating;
        this.seatingClass = seatClass;
        this.adultPassengerCount = adultPassengerCount;
        this.childPassengerCount = childPassengerCount;
        this.infantPassengerCount = infantPassengerCount;

        return true;
    }

    private static String safeTrim(String s) {
        return (s == null) ? "" : s.trim();
    }
}
