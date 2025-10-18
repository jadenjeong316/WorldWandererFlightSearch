package flight;

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

    public boolean runFlightSearch(String departureDate,    String departureAirportCode,   boolean emergencyRowSeating,
                                   String returnDate,       String destinationAirportCode, String seatingClass,
                                   int adultPassengerCount, int childPassengerCount,       int infantPassengerCount) {
        boolean valid = true;

        //TODO: Validate all the provided parameters.
        //if the search parameters meets the given conditions,
        //   the function should initialise all the class attributes and return true.
        //else
        //   the function should return false

        return valid;
    }
}