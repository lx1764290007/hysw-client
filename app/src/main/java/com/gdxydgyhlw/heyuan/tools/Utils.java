package com.gdxydgyhlw.heyuan.tools;

import android.text.InputFilter;
import android.text.Spanned;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {
    public static InputFilter filter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
            // 判断输入的字符是否合法，这里只允许输入英文和数字
            if (charSequence.toString().matches("[a-zA-Z0-9]+")) {
                return charSequence;
            } else {
                return "";
            }
        }
    };

    public static String todayStartTime() {
        LocalDate today = LocalDate.now();
         // 获取当前日期的开始时间
        LocalDateTime startTime = today.atStartOfDay();
        DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return dtf2.format(startTime);
    }
    public static String todayEndTime() {
        LocalDate today = LocalDate.now();
        // 获取当前日期的开始时间
        LocalDateTime startTime = today.atStartOfDay();
        // 获取当前日期的结束时间
        LocalDateTime endTime = today.atTime(23, 59, 59).plusDays(1);
        DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dtf2.format(endTime);

    }
}