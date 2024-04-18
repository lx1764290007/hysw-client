package com.gdxydgyhlw.heyuan.tools;

import com.gdxydgyhlw.heyuan.http.CollectData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.List;

public class MyValueFormatter extends ValueFormatter {
    private final List<CollectData> c;
    public String getBarLabel(BarEntry barEntry) {
        int i = (int) barEntry.getX();
        return barEntry.getY() + c.get(i).unit;
    }
    public MyValueFormatter(List<CollectData> collectData){
        this.c = collectData;
    }
}
