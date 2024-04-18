package com.gdxydgyhlw.heyuan.myenum;

import androidx.annotation.Nullable;

import java.util.Objects;

public enum AlarmType {
    LOW("low","低"),
    MEDIUM("medium","中"),
    SERIOUS("serious", "严重"),
    EMERGENT("emergent", "紧急");
    private final String name;
    private final String value;
    AlarmType(String name, String value){
        this.value = value;
        this.name = name;
    }
    public static @Nullable String getName(String value) {
        for (AlarmType c : AlarmType.values()) {
            if (Objects.equals(c.value, value)) {
                return c.name;
            }
        }
        return null;
    }
    public static @Nullable String getValue(String name) {
        for (AlarmType c : AlarmType.values()) {
            if (Objects.equals(c.name, name)) {
                return c.value;
            }
        }
        return null;
    }
}
