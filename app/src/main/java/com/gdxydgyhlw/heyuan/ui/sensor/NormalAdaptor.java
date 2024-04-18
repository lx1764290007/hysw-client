package com.gdxydgyhlw.heyuan.ui.sensor;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gdxydgyhlw.heyuan.R;
import com.gdxydgyhlw.heyuan.http.SensorData;
import com.gdxydgyhlw.heyuan.myenum.SensorType;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NormalAdaptor extends RecyclerView.Adapter<NormalAdaptor.VH> {
    private List<SensorData> mDatas;

    public NormalAdaptor(List<SensorData> data) {
        this.mDatas = data;
    }

    //② 创建ViewHolder
    public static class VH extends RecyclerView.ViewHolder {
        public final TextView name;
        public final TextView number;
        public final TextView online;
        public final TextView typeName;
        public final TextView state;

        public VH(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.sensor_name);
            number = (TextView) v.findViewById(R.id.sensor_number);
            online = (TextView) v.findViewById(R.id.sensor_online);

            typeName = (TextView) v.findViewById(R.id.sensor_type);
            state = (TextView) v.findViewById(R.id.sensor_state);

        }
    }


    //③ 在Adapter中实现3个方法
    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        try {
            SensorData sensorData = mDatas.get(position);
            holder.name.setText(sensorData.name);
            holder.number.setText(sensorData.sensorNumber);
            holder.state.setText(sensorData.state? "在线":"离线");
            holder.typeName.setText(SensorType.getName(sensorData.sensorType));
            holder.online.setText(sensorData.online);


        } catch (NullPointerException exception) {
            Log.e("NullPointerException", String.valueOf(exception.getMessage()));
        }
        holder.itemView.setOnClickListener(v -> {

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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.sensor_card_view, parent, false);
        return new VH(v);
    }

}
