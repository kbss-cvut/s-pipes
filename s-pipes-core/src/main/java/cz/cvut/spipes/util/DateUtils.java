package cz.cvut.spipes.util;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Objects;

public class DateUtils {

    public static Date toDate(Object objectDate){
        Objects.requireNonNull(objectDate);

        if(objectDate instanceof OffsetDateTime)
            return toDate((OffsetDateTime)objectDate);
        return (Date)objectDate;

    }

    public static Date toDate(OffsetDateTime objectDate) {
        Objects.requireNonNull(objectDate);
        return Date.from(objectDate.toInstant());
    }
}
