import java.time.Duration;
import java.time.LocalDateTime;

public class VortioxetineCalculator {

    // Average half-life of Vortioxetine in hours
    private static final double VORTIOXETINE_HALF_LIFE_HOURS = 66.0;

    /**
     * Calculates the percentage of vortioxetine remaining in the blood.
     *
     * @param lastDoseTime The date and time of the last dose.
     * @param currentTime  The current date and time.
     * @return The estimated percentage of vortioxetine remaining, or -1 if lastDoseTime is in the future.
     */
    public static double calculatePercentageRemaining(LocalDateTime lastDoseTime, LocalDateTime currentTime) {
        if (lastDoseTime == null || currentTime == null) {
            throw new IllegalArgumentException("Dose time and current time cannot be null.");
        }

        if (lastDoseTime.isAfter(currentTime)) {
            // Cannot calculate for a future dose time relative to current
            return -1; // Or throw an exception
        }

        Duration duration = Duration.between(lastDoseTime, currentTime);
        double hoursElapsed = duration.toMillis() / (1000.0 * 60 * 60); // Convert milliseconds to hours

        // Formula: N(t) = N0 * (1/2)^(t/T)
        // We want percentage, so N0 is 100
        double percentageRemaining = 100.0 * Math.pow(0.5, hoursElapsed / VORTIOXETINE_HALF_LIFE_HOURS);

        return percentageRemaining;
    }

    public static double getHalfLifeHours() {
        return VORTIOXETINE_HALF_LIFE_HOURS;
    }
}