package ch.repnik.quartzretry;

import org.quartz.DateBuilder;

/**
 * This class holds all data to define the timeout between retries.
 */
public class RetryTimeout {

    private final int number;
    private final DateBuilder.IntervalUnit unit;

    private RetryTimeout(int number, DateBuilder.IntervalUnit unit){
        this.number = number;
        this.unit = unit;
    }

    /**
     * Creates an instance of RetryTimeout with the defined timeout.
     * @param number number to use with the unit
     * @param unit IntervalUnit to use
     * @return instance of RetryTimeout with the specified timeout data.
     */
    public static RetryTimeout timeout(int number, DateBuilder.IntervalUnit unit){
        return new RetryTimeout(number, unit);
    }

    /**
     * Gets the numeric part of the timeout
     * @return numeric part of the timeout
     */
    public int getNumber() {
        return number;
    }

    /**
     * Gets the unit part of the timeout
     * @return unit part of the timeout
     */
    public DateBuilder.IntervalUnit getUnit() {
        return unit;
    }
}
