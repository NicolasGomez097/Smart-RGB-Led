package com.example.programablergbled.Utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {
    private Context context;

    public ToastUtils(Context context){
        this.context = context;
    }

    public void showToastShort(String msg){
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
    }
}
