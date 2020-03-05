package com.example.programablergbled.controller;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.programablergbled.R;
import com.example.programablergbled.Utils.Builder;
import com.example.programablergbled.controller.fragments.FragmentConfigBluetooth;
import com.example.programablergbled.controller.fragments.FragmentDetectLight;
import com.example.programablergbled.controller.fragments.FragmentDetectLight2;
import com.example.programablergbled.controller.fragments.FragmentMenu;
import com.example.programablergbled.controller.fragments.FragmentPattern;
import com.example.programablergbled.controller.fragments.FragmentSplash;

public class MainActivity extends AppCompatActivity
        implements FragmentSplash.FragmentSplashHandler,
        FragmentMenu.FragmentMenuHandler,
        FragmentConfigBluetooth.FragmentConfigBluetoothHandler,
        FragmentPattern.FragmentPatternHandler {

    private final FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        openSplash();

        Builder.setContext(this);
        Builder.getBluetoothUtils().checkBluetoothEnable(this);
        /*Leds l = new Leds(4);
        byte leds[] = l.turnOff();
        for(byte b: leds){
            System.out.print(Parser.getStringCharProtocol(b));
        }
        System.out.println();*/
    }

    void setFragment(Fragment fragment){
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fragment_container,fragment);
        ft.commit();
    }

    void openSplash(){
        FragmentSplash fragment = new FragmentSplash(this);
        setFragment(fragment);
    }

    void openMenu(){
        FragmentMenu fragment = new FragmentMenu(this);
        setFragment(fragment);
    }

    void openDetectLight(){
        FragmentDetectLight fragment = new FragmentDetectLight();
        //FragmentDetectLight2 fragment = new FragmentDetectLight2();
        setFragment(fragment);
    }

    void openConfigBluetooth(){
        FragmentConfigBluetooth fragment = new FragmentConfigBluetooth(this);
        setFragment(fragment);
    }

    void openChangePattern(){
        FragmentPattern fragment = new FragmentPattern(this);
        setFragment(fragment);
    }


    @Override
    public void onSplashFinished() {
        openMenu();
    }

    @Override
    public void onOpenDetectLights() {
        openDetectLight();
    }

    @Override
    public void onOpenChangePattern() {
        openChangePattern();
    }

    @Override
    public void onExitFragmentPattern() {
        openMenu();
    }

    @Override
    public void onExitBluetoothConfig() {
        openMenu();
    }

    @Override
    public void onConfigBluetooth() {
        openConfigBluetooth();
    }

    @Override
    public void onBackPressed() {
        if(fragmentManager.getFragments().get(0) instanceof FragmentMenu)
            finishAffinity();
            //super.onBackPressed();
        else
            openMenu();
    }

    @Override
    public void onDestroy() {
        Builder.getBluetoothConnection().closeConecction();
        super.onDestroy();
    }
}
