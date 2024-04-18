package com.gdxydgyhlw.heyuan.myenum;

import androidx.annotation.Nullable;

import java.util.Objects;

public enum SensorType {
    CL("CL", "余氯"),
    COD("COD", "化学需氧量"),
    FLOW_LEVEL("flowvel", "流速"),
    NH3("NH3", "氨氮"),
    WATER_FLOW("waterflow", "流量"),
    WATER_LEVEL("waterlevel", "水位"),
    WATER_IMMERSION_STATUS("water_immersion_status", "水浸状态"),
    WATER_GAUGE_WATER_LEVEL("water_gauge_waterlevel", "水尺水位"),
    HYDRAULIC_PRESSURE("hydraulic_pressure", "液压"),
    TURBIDITY("turbidity", "COD浑浊度"),
    FLOW_WATER_LEVEL("flowvel_waterlevel", "流速水位"),

    RELAY_STATUS("relay_status", "继电器状态");
    private String name;
    private String value;
    SensorType(String value, String name) {
      this.value = value;
      this.name = name;
    }
    public static @Nullable String getName(String value) {
        for (SensorType c : SensorType.values()) {
            if (Objects.equals(c.value, value)) {
                return c.name;
            }
        }
        return null;
    }
    public static @Nullable String getValue(String name) {
        for (SensorType c : SensorType.values()) {
            if (Objects.equals(c.name, name)) {
                return c.value;
            }
        }
        return null;
    }
}
