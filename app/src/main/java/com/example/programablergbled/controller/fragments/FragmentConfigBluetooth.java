package com.example.programablergbled.controller.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.programablergbled.R;
import com.example.programablergbled.Utils.Builder;
import com.example.programablergbled.Utils.Constants;
import com.example.programablergbled.persisntance.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FragmentConfigBluetooth extends Fragment {

    private View fragment_container;
    private FragmentConfigBluetoothHandler handler;
    private Spinner deviceSpinner;
    private Button saveBtn;
    private CheckBox turnOnStartUp;

    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothToList> deviceList;

    public interface FragmentConfigBluetoothHandler{
        public void onExitBluetoothConfig();
    }

    public FragmentConfigBluetooth(FragmentConfigBluetoothHandler handler){
        this.handler = handler;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        deviceList = new ArrayList<>();
        Set<BluetoothDevice> devices = this.bluetoothAdapter.getBondedDevices();
        for(BluetoothDevice device: devices){
            deviceList.add(new BluetoothToList(device));
        }
    }

    private class BluetoothToList{
        private BluetoothDevice device;

        public BluetoothToList(BluetoothDevice device){
            this.device = device;
        }

        @NonNull
        @Override
        public String toString() {
            return device.getName();
        }
        public String getMac(){
            return device.getAddress();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment_container = inflater.inflate(R.layout.fragment_config_bluethoot,container,false);

        deviceSpinner = fragment_container.findViewById(R.id.device_spinner);
        saveBtn = fragment_container.findViewById(R.id.save_btn);
        turnOnStartUp = fragment_container.findViewById(R.id.turn_on_on_start_up_ck);

        ArrayAdapter spinnerAdapter = new ArrayAdapter<BluetoothToList>(getContext(),android.R.layout.simple_spinner_item,deviceList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deviceSpinner.setAdapter(spinnerAdapter);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveConfig();
            }
        });

        return fragment_container;
    }

    private void saveConfig() {
        if(turnOnStartUp.isChecked()){
            Builder.getSharedPreferencesUtil().save(Constants.CONF_TURN_ON_BLUETOOTH_ON_START_UP,"true");
        }else{
            Builder.getSharedPreferencesUtil().save(Constants.CONF_TURN_ON_BLUETOOTH_ON_START_UP,"false");
        }
        if(deviceSpinner.getSelectedItem() != null){
            BluetoothToList item = (BluetoothToList) deviceSpinner.getSelectedItem();
            Builder.getSharedPreferencesUtil().save(Constants.CONF_BLUETOOTH_MAC,
                    item.getMac());
        }
        handler.onExitBluetoothConfig();
    }
}
