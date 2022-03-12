package ch.repnik.quartzretry;

import org.quartz.DateBuilder;

public class RetryInterval {

    private final int number;
    private final DateBuilder.IntervalUnit unit;

    private RetryInterval(int number, DateBuilder.IntervalUnit unit){
        this.number = number;
        this.unit = unit;
    }

    public static final RetryInterval retry(int number, DateBuilder.IntervalUnit unit){
        return new RetryInterval(number, unit);
    }

    public int getNumber() {
        return number;
    }

    public DateBuilder.IntervalUnit getUnit() {
        return unit;
    }
}
