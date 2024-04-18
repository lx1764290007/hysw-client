package com.gdxydgyhlw.heyuan.ui.home;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.gdxydgyhlw.heyuan.http.CollectData;
import com.gdxydgyhlw.heyuan.http.DeviceData;

import java.util.List;

public class HomeViewModel extends ViewModel {
    private MutableLiveData<List<CollectData>> collectData;
    private MutableLiveData<Integer> spinnerPosition;
    private MutableLiveData<List<DeviceData>> deviceData;

    public HomeViewModel(List<CollectData> collectData, int spinnerPosition, List<DeviceData> deviceData) {
        this.collectData = new MutableLiveData<>(collectData);
        this.spinnerPosition = new MutableLiveData<>(spinnerPosition);
        this.deviceData = new MutableLiveData<>(deviceData);
    }

    @Nullable public List<CollectData> getCollectData() {
        return this.collectData.getValue();
    }

    @Nullable public List<DeviceData> getDeviceData() {
        return this.deviceData.getValue();
    }

    public int getSpinnerPosition() {
        return this.spinnerPosition.getValue() == null ? 0 : this.spinnerPosition.getValue();
    }
    public void reset(){
        this.collectData = new MutableLiveData<>(null);
        this.spinnerPosition = new MutableLiveData<>(null);
        this.deviceData = new MutableLiveData<>(null);
    }
}