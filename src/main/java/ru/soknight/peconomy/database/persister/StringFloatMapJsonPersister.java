package ru.soknight.peconomy.database.persister;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.LongStringType;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;

public final class StringFloatMapJsonPersister extends LongStringType {

    private static final StringFloatMapJsonPersister singleton = new StringFloatMapJsonPersister();

    private final Gson gson;
    private final Type jsonType;

    private StringFloatMapJsonPersister() {
        super(SqlType.LONG_STRING, new Class<?>[]{LinkedHashMap.class});
        this.gson = new GsonBuilder().create();
        this.jsonType = new TypeToken<LinkedHashMap<String, Float>>(){}.getType();
    }

    public static StringFloatMapJsonPersister getSingleton() {
        return singleton;
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        LinkedHashMap<?, ?> hashMap = (LinkedHashMap<?, ?>) javaObject;
        if(hashMap == null)
            return null;

        return gson.toJson(hashMap);
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
        String json = (String) sqlArg;
        if(json == null)
            return null;

        try {
            return gson.fromJson(json, jsonType);
        } catch (JsonSyntaxException ignored) {
            return new LinkedHashMap<>();
        }
    }

}
