package com.example.programablergbled.Utils;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.example.programablergbled.R;
import com.example.programablergbled.persisntance.BluetoothConecction;

public class BluetoothUtils {
    private Context context;

    public BluetoothUtils(Context c){
        this.context = c;
    }

    public void checkBluetoothEnable(final FragmentActivity app){
        if(BluetoothAdapter.getDefaultAdapter().isEnabled())
            return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.turn_on_Bluetooth)
                .setPositiveButton(R.string.turn_on, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BluetoothAdapter.getDefaultAdapter().enable();
                        Builder.getToastUtils().showToastShort(context.getString(R.string.bluetooth_enable));
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        app.onBackPressed();
                    }
                });
        builder.create().show();
    }

    /**
     * Manda datos sin mostrar un Toast de si fue correcto el envio.
     * */
    public void bluetoothSendData(String msg){
        new SendDataTask(Builder.getBluetoothConnection(),msg,null,null).execute();
    }

    /**Manda datos mostrando los mensajes cunado corresponda,
     * si se deja un mensaje en null, no se mostrará.
     * */
    public void bluetoothSendData(String msg, String onSuccess, String onFaild){
        new SendDataTask(Builder.getBluetoothConnection(),msg,onSuccess,onFaild).execute();
    }

    /**Manda datos mostrando los mensajes cunado corresponda,
     * si se deja un mensaje en null, no se mostrará.
     * */
    public void bluetoothSendData(byte[] msg, String onSuccess, String onFaild){
        new SendDataTask(Builder.getBluetoothConnection(),msg,onSuccess,onFaild).execute();
    }

    public class SendDataTask extends AsyncTask<Void,Void,Boolean> {
        private BluetoothConecction bt;
        private byte[] msg;
        private String onSuccess;
        private String onFaild;
        public SendDataTask(BluetoothConecction bt,String msg,
                            String onSuccess,String onFaild){
            this.bt = bt;
            this.msg = msg.getBytes();
            this.onSuccess = onSuccess;
            this.onFaild = onFaild;
        }

        public SendDataTask(BluetoothConecction bt,byte[] msg,
                            String onSuccess,String onFaild){
            this.bt = bt;
            this.msg = msg;
            this.onSuccess = onSuccess;
            this.onFaild = onFaild;
        }
        @Override
        protected Boolean doInBackground(Void... voids) {
            return bt.write(msg);
        }

        @Override
        protected void onPostExecute(Boolean isTrue) {

            if(isTrue)
                if(onSuccess != null)
                    Builder.getToastUtils().showToastShort(onSuccess);
            else
                if(onFaild != null)
                    Builder.getToastUtils().showToastShort(onFaild);

            super.onPostExecute(isTrue);
        }
    }
}
