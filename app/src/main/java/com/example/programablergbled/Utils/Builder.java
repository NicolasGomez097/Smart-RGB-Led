package com.example.programablergbled.Utils;

import android.content.Context;

import com.example.programablergbled.persisntance.BluetoothConecction;
import com.example.programablergbled.persisntance.SharedPreferencesUtil;

public class Builder {
    private static Context context;

    private static SharedPreferencesUtil sharedPreferencesUtil;
    private static BluetoothUtils bluetoothUtils;
    private static BluetoothConecction bluetoothConecction;
    private static ToastUtils toastUtils;
    private static ImageUtils imageUtils;

    public static void setContext(Context c){
        context = c;
        bluetoothUtils = null;
        sharedPreferencesUtil = null;
        toastUtils = null;
    }

    public static SharedPreferencesUtil getSharedPreferencesUtil(){
        if(sharedPreferencesUtil == null)
            sharedPreferencesUtil = new SharedPreferencesUtil(context);

        return sharedPreferencesUtil;
    }

    public static BluetoothUtils getBluetoothUtils(){
        if(bluetoothUtils == null)
            bluetoothUtils = new BluetoothUtils(context);

        return bluetoothUtils;
    }

    public static BluetoothConecction getBluetoothConnection(){
        String deviceMAC = getSharedPreferencesUtil().getValue(Constants.CONF_BLUETOOTH_MAC);
        if(deviceMAC == null)
            return null;

        if(bluetoothConecction == null)
            bluetoothConecction = new BluetoothConecction(deviceMAC);
        else{
            if(!bluetoothConecction.getDeviceMAC().equals(deviceMAC)){
                bluetoothConecction.closeConecction();
                bluetoothConecction = new BluetoothConecction(deviceMAC);
            }
        }

        return bluetoothConecction;
    }


    public static ToastUtils getToastUtils(){
        if(toastUtils == null)
            toastUtils = new ToastUtils(context);

        return toastUtils;
    }

    public static ImageUtils getImageUtils(){
        if(imageUtils == null)
            imageUtils = new ImageUtils();

        return imageUtils;
    }
}
