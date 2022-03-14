package ch.repnik.quartzretry;

import org.quartz.DateBuilder;

/**
 * This class holds all data to define the timeout between retries.
 */
public class RetryInterval {

    private final int number;
    private final DateBuilder.IntervalUnit unit;

    private RetryInterval(int number, DateBuilder.IntervalUnit unit){
        this.number = number;
        this.unit = unit;
    }

    /**
     * Creates an instance of RetyInterval with the defined timeout.
     * @param number number to use with the unit
     * @param unit IntervalUnit to use
     * @return instance of RetryInterval with the specified timeout data.
     */
    public static RetryInterval retry(int number, DateBuilder.IntervalUnit unit){
        return new RetryInterval(number, unit);
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
