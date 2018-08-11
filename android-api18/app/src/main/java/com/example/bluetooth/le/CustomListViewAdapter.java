package com.example.bluetooth.le;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Owner on 8/2/2018.
 */

public class CustomListViewAdapter extends ArrayAdapter {

    List<CustomBluetoothDeviceWrapper> m_list;

    public CustomListViewAdapter(@NonNull Context context, int layoutId,
                                 List<CustomBluetoothDeviceWrapper> underLyingList) {
        super(context, layoutId, underLyingList);
        m_list = underLyingList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View customView = LayoutInflater.from(getContext()).inflate(R.layout.row, parent, false);

        CustomBluetoothDeviceWrapper device = getItem(position);
        TextView customText1 = customView.findViewById(R.id.text1);
        TextView customText2 = customView.findViewById(R.id.text2);
        TextView customText3 = customView.findViewById(R.id.text3);

        customText1.setText(device.getName());
        customText2.setText(device.getAddress());
        customText3.setText(Integer.toString(device.getRssi()));

        return customView;
    }

    public void add(@Nullable CustomBluetoothDeviceWrapper device) {
        // Need to wipe list on starting new scan
        if(device.getName().contains("DSD Tech"))
        {
            m_list.add(0,device);
            return;
        }
        m_list.add(device);
    }


    @Nullable
    @Override
    public CustomBluetoothDeviceWrapper getItem(int position) {
        return m_list.get(position);
    }
}
