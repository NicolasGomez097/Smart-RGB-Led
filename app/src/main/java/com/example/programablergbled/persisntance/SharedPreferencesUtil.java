package com.example.programablergbled.persisntance;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.programablergbled.R;

public class SharedPreferencesUtil {
    private SharedPreferences sharedPreferences;

    public SharedPreferencesUtil(Context context){
        this.sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.shared_preference),Context.MODE_PRIVATE);
    }

    public boolean save(String key,String value){
        return sharedPreferences.edit().putString(key,value).commit();
    }

    public String getValue(String key){
        return sharedPreferences.getString(key,null);
    }

    public boolean deletekey(String key){
        return sharedPreferences.edit().remove(key).commit();
    }
}
