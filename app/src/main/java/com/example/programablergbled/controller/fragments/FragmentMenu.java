package com.example.programablergbled.controller.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.programablergbled.R;
import com.example.programablergbled.Utils.Builder;
import com.example.programablergbled.persisntance.BluetoothConecction;

public class FragmentMenu extends Fragment {

    private View fragment_container;
    private FragmentMenuHandler handler;
    private Button btnDetectLight;
    private Button btnConfigBluetooth;
    private Button btnChangePattern;
    private Button connectBtn;
    private Button disconnectBtn;
    private TextView statusTxt;

    public interface FragmentMenuHandler{
        void onOpenDetectLights();
        void onOpenChangePattern();
        void onConfigBluetooth();
    }

    public FragmentMenu(FragmentMenuHandler handler){
        this.handler = handler;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment_container = inflater.inflate(R.layout.fragment_menu,container,false);
        btnDetectLight = fragment_container.findViewById(R.id.detect_light_btn);
        btnConfigBluetooth = fragment_container.findViewById(R.id.config_bluetooth_btn);
        statusTxt = fragment_container.findViewById(R.id.status_txt);
        connectBtn = fragment_container.findViewById(R.id.connect_btn);
        btnChangePattern = fragment_container.findViewById(R.id.change_lights_pattern_btn);
        disconnectBtn = fragment_container.findViewById(R.id.disconnect_btn);

        btnDetectLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.onOpenDetectLights();
            }
        });
        btnChangePattern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.onOpenChangePattern();
            }
        });
        btnConfigBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.onConfigBluetooth();
            }
        });
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToDevice();
            }
        });
        disconnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnectBluetooth();
            }
        });

        checkIfConeccted();

        return fragment_container;
    }

    private void connectToDevice() {
        BluetoothConecction bt = Builder.getBluetoothConnection();
        if(bt == null){
            Builder.getToastUtils().showToastShort(getString(R.string.pair_device));
            return;
        }

        Builder.getBluetoothUtils().checkBluetoothEnable(getActivity());
        Builder.getToastUtils().showToastShort(getString(R.string.establishing_connection));
        new ConnectBluetoothTask(bt).execute();
    }

    private class ConnectBluetoothTask extends AsyncTask<Void,Void,Boolean>{
        private BluetoothConecction bt;
        public ConnectBluetoothTask(BluetoothConecction bt){
            this.bt = bt;
        }
        @Override
        protected Boolean doInBackground(Void... voids) {
            return bt.openConnection();
        }

        @Override
        protected void onPostExecute(Boolean isTrue) {
            if(isTrue){
                Builder.getToastUtils().showToastShort(getString(R.string.connection_established));
                checkIfConeccted();
            }
            else
                Builder.getToastUtils().showToastShort(getString(R.string.connection_not_established));
            super.onPostExecute(isTrue);
        }
    }

    public void disconnectBluetooth(){
        Builder.getBluetoothConnection().closeConecction();

        //Ajustando GUI
        disconnectBtn.setVisibility(View.GONE);
        btnChangePattern.setVisibility(View.GONE);
        connectBtn.setVisibility(View.VISIBLE);

        statusTxt.setTextColor(getResources().getColor(R.color.red,null));
        statusTxt.setText(getString(R.string.disconected));
    }

    public void checkIfConeccted(){
        BluetoothConecction con = Builder.getBluetoothConnection();
        if(con == null || !con.isConnected())
            return;

        //Ajustando GUI
        statusTxt.setTextColor(getResources().getColor(R.color.green,null));
        statusTxt.setText(getString(R.string.established));
        btnChangePattern.setVisibility(View.VISIBLE);
        disconnectBtn.setVisibility(View.VISIBLE);
        connectBtn.setVisibility(View.GONE);
    }
}
