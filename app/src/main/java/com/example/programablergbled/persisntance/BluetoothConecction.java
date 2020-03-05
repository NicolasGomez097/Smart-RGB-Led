package com.example.programablergbled.persisntance;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothConecction {
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private String deviceMAC;
    private int counter;

    private int NUM_TRIES_WRITE = 1;
    private int NUM_TRIES_READ = 10;
    private int TIMEOUT = 1000;

    public BluetoothConecction(String mac){
        this.deviceMAC = mac;
        counter = 0;
    }

    public boolean openConnection(){
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceMAC);
        try {
            if(socket == null || !socket.isConnected())
                socket = device.createRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
            socket.connect();

            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();

        } catch (IOException e) {
            socket = null;
            outputStream = null;
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void closeConecction(){
        if(socket == null)
            return;
        try {outputStream.close();}catch (Exception e){}
        try {inputStream.close();}catch (Exception e){}

        try{socket.close();}catch (Exception e){}
    }

    public boolean isConnected(){
        if(socket == null)
            return false;

        return socket.isConnected();
    }

    public boolean write(byte[] msg){
        boolean flag = false;
        char stream = 0;
        int tries = 0;
        long initTime = 0;
        System.out.println("Start:"+counter);

        if(!isConnected())
            return false;
        try{
            while (tries < NUM_TRIES_WRITE){
                tries++;
                outputStream.write(msg);
                initTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - initTime < TIMEOUT){
                    while(inputStream.available() > 0){
                        stream = (char)inputStream.read();
                        flag = true;
                        Thread.sleep(20);
                    }
                    if(flag)
                        break;
                    Thread.sleep(TIMEOUT/NUM_TRIES_READ);
                }
                if(flag)
                    break;
            }
            System.out.println("End:"+counter+", stream:"+stream + ", tries: "+tries);
            counter++;
        }catch (Exception e){
            e.printStackTrace();
        }
        if(flag == false)
            return false;

        return true;
    }

    public String getDeviceMAC() {
        return deviceMAC;
    }

}
