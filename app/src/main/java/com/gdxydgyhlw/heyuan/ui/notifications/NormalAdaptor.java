package com.gdxydgyhlw.heyuan.ui.notifications;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.gdxydgyhlw.heyuan.R;
import com.gdxydgyhlw.heyuan.http.AlarmData;
import com.gdxydgyhlw.heyuan.myenum.AlarmType;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NormalAdaptor extends RecyclerView.Adapter<NormalAdaptor.VH> {
    private List<AlarmData> mDatas;

    public NormalAdaptor(List<AlarmData> data) {
        this.mDatas = data;
    }

    //② 创建ViewHolder
    public static class VH extends RecyclerView.ViewHolder {
        public final TextView alarmGrade;
        public final TextView alarmValue;
        public final TextView createTime;
        public final TextView deviceName;
        public final TextView handler;
        public final TextView triggerRemark;
        public final TextView unit;
        public final TextView sensorType;
        public VH(View v) {
            super(v);
            alarmGrade = (TextView) v.findViewById(R.id.alarm_grade);
            alarmValue = (TextView) v.findViewById(R.id.alarm_value);
            createTime = (TextView) v.findViewById(R.id.alarm_create_time);
            deviceName = (TextView) v.findViewById(R.id.alarm_device_name);
            handler = (TextView) v.findViewById(R.id.alarm_handle);
            unit = (TextView) v.findViewById(R.id.alarm_unit);
            triggerRemark = (TextView) v.findViewById(R.id.alarm_trigger_remark);
            sensorType = (TextView) v.findViewById(R.id.alarm_sensor_type);
        }
    }


    //③ 在Adapter中实现3个方法
    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        try {
            AlarmData alarmData = mDatas.get(position);
            holder.alarmGrade.setText(AlarmType.getValue(alarmData.alarmGrade));
            holder.alarmValue.setText(alarmData.alarmValue);
            holder.createTime.setText(alarmData.createTime);
            holder.deviceName.setText("测试");
            holder.unit.setText(alarmData.unit);
            holder.handler.setText(alarmData.handler ? "是" : "否");
            holder.triggerRemark.setText(alarmData.triggerRemark);
            holder.deviceName.setText(alarmData.getDeviceName());
            holder.sensorType.setText(alarmData.getSensorName());
        } catch (NullPointerException exception) {
            Log.e("NullPointerException", String.valueOf(exception.getMessage()));
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //item 点击事件
            }
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.notify_card_view, parent, false);
        return new VH(v);
    }

}
