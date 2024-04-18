package com.gdxydgyhlw.heyuan.ui.dashboard;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gdxydgyhlw.heyuan.R;
import com.gdxydgyhlw.heyuan.http.DeviceData;
import com.gdxydgyhlw.heyuan.ui.sensor.Sensor;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NormalAdaptor extends RecyclerView.Adapter<NormalAdaptor.VH> {
    private List<DeviceData> mDatas;

    public NormalAdaptor(List<DeviceData> data) {
        this.mDatas = data;
    }

    //② 创建ViewHolder
    public static class VH extends RecyclerView.ViewHolder {
        public final TextView name;
        public final TextView number;
        public final TextView state;
        public final TextView delay;
        public final TextView voltage;
        public final TextView online;

        public VH(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.device_name);
            number = (TextView) v.findViewById(R.id.device_number);
            state = (TextView) v.findViewById(R.id.device_state);
            delay = (TextView) v.findViewById(R.id.device_offlineDelay);
            voltage = (TextView) v.findViewById(R.id.device_currentVoltage);
            online = (TextView) v.findViewById(R.id.device_online);
        }
    }


    //③ 在Adapter中实现3个方法
    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        try {
            holder.name.setText(mDatas.get(position).name);
            holder.online.setText(mDatas.get(position).online);
            String currentVoltage = Float.parseFloat(mDatas.get(position).currentVoltage) + "%";
            holder.voltage.setText(currentVoltage);
            holder.number.setText(mDatas.get(position).deviceNumber);
            holder.delay.setText(mDatas.get(position).offlineDelay);
            holder.state.setText(mDatas.get(position).state ? "在线" : "离线");
        } catch (NullPointerException exception) {
            Log.e("NullPointerException", String.valueOf(exception.getMessage()));
        }
        holder.itemView.setOnClickListener(v -> {
            //item 点击事件
            Intent intent = new Intent(v.getContext(), Sensor.class);
            intent.putExtra("deviceManageId", mDatas.get(position).id);
            intent.putExtra("deviceName", mDatas.get(position).name);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        //LayoutInflater.from指定写法
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_card_view, parent, false);
        return new VH(v);
    }

}
