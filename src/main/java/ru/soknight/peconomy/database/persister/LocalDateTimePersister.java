package ru.soknight.peconomy.database.persister;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.DateTimeType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class LocalDateTimePersister extends DateTimeType {

    private static final LocalDateTimePersister singleton = new LocalDateTimePersister();

    private LocalDateTimePersister() {
        super(SqlType.LONG, new Class<?>[] { LocalDateTime.class });
    }

    public static LocalDateTimePersister getSingleton() {
        return singleton;
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        LocalDateTime dateTime = (LocalDateTime) javaObject;
        if(dateTime == null)
            return null;

        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
        Long epochMillis = (Long) sqlArg;
        if(epochMillis == null)
            return null;

        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault()).toLocalDateTime();
    }

}
