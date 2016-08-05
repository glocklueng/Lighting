package com.example.administrator.lighting.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.lighting.R;

import java.util.ArrayList;

public class LeDeviceListAdapter extends RecyclerView.Adapter<LeDeviceListAdapter.ViewHolder> {

    private ArrayList<BluetoothDevice> mLeDevices;
    private LayoutInflater mInflator;

    public LeDeviceListAdapter(Context context) {
        mInflator = LayoutInflater.from(context);
        mLeDevices = new ArrayList<BluetoothDevice>();
    }

    public void addDevice(BluetoothDevice device) {
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }
    }

    public BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    public void clear() {
        mLeDevices.clear();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflator.inflate(R.layout.activity_main_recycler_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BluetoothDevice device = mLeDevices.get(position);
        final String deviceName = device.getName();
        if (!deviceName.isEmpty()) {
            holder.itemTvName.setText("设备名称：" + deviceName);
        } else {
            holder.itemTvName.setText("未知设备");
        }
        holder.itemTvAddress.setText("设备地址：" + device.getAddress());
    }

    @Override
    public int getItemCount() {
        return mLeDevices.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView itemCv;
        public ImageView itemIvBlue;
        public TextView itemTvName;
        public TextView itemTvAddress;

        public ViewHolder(View view) {
            super(view);
            itemCv = (CardView) view.findViewById(R.id.id_card);
            itemIvBlue = (ImageView) view.findViewById(R.id.id_card_bluetooth);
            itemTvName = (TextView) view.findViewById(R.id.id_card_name);
            itemTvAddress = (TextView) view.findViewById(R.id.id_card_address);
        }
    }
}
