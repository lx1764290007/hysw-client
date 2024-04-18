package com.gdxydgyhlw.heyuan.myenum;

import androidx.annotation.Nullable;

import java.util.Objects;

public enum EnumColor {
    Z(0, "#CC6699"),
    A(1, "#FF6666"),
    B(2, "#FF99FF"),
    C(3, "#CC00FF"),
    D(4, "#006633"),
    E(5, "#CC66FF"),
    F(6, "#CC0099"),
    G(7, "#FFCC33"),
    H(8, "#666FF"),
    I(9, "#333366"),
    J(10, "#66FFCC"),
    K(11, "#0066CC"),
    L(12, "#0066FF"),
    M(13, "#6600FF");
    private String name;
    private int value;
    EnumColor(int value, String name) {
        this.value = value;
        this.name = name;
    }
    public static @Nullable String getName(int value) {
        for (EnumColor c : EnumColor.values()) {
            if (Objects.equals(c.value, value)) {
                return c.name;
            }
        }
        return null;
    }
    public static int getValue(String name) {
        for (EnumColor c : EnumColor.values()) {
            if (Objects.equals(c.name, name)) {
                return c.value;
            }
        }
        return 0;
    }
    public static int getSize(){
        return EnumColor.values().length - 1;
    }
}
