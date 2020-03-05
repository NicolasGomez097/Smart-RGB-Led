package com.example.programablergbled.controller.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.programablergbled.R;
import com.example.programablergbled.Utils.Builder;
import com.example.programablergbled.Utils.Parser;
import com.example.programablergbled.model.Leds;

import top.defaults.colorpicker.ColorPickerPopup;

public class FragmentPattern extends Fragment {

    private FragmentPatternHandler handler;

    private View fragment_container;
    private Button encender;
    private Button apagar;
    private EditText red,green,blue,index;
    private Button elegirColor;
    private Button reconocimientoOn;
    private Button reconocimientoOff;
    private Button next;

    public interface FragmentPatternHandler{
        void onExitFragmentPattern();
    }

    public FragmentPattern(FragmentPatternHandler handler){
        this.handler = handler;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment_container = inflater.inflate(R.layout.fragment_pattern,container,false);

        /*if(!Builder.getBluetoothConnection().isConnected()){
            handler.onExitFragmentPattern();
        }*/

        encender = fragment_container.findViewById(R.id.encender);
        apagar = fragment_container.findViewById(R.id.apagar);
        elegirColor = fragment_container.findViewById(R.id.elegirColor);
        red = fragment_container.findViewById(R.id.red);
        green = fragment_container.findViewById(R.id.green);
        blue = fragment_container.findViewById(R.id.blue);
        index = fragment_container.findViewById(R.id.index);
        reconocimientoOn = fragment_container.findViewById(R.id.encenderReconocimiento);
        reconocimientoOff = fragment_container.findViewById(R.id.apagarReconocimiento);
        next = fragment_container.findViewById(R.id.nextPatron);

        encender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                encederLeds();
            }
        });

        apagar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apagarLeds();
            }
        });

        elegirColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ColorPickerPopup.Builder(getContext())
                        .showValue(false)
                        .build()
                        .show(new ColorPickerPopup.ColorPickerObserver() {
                    @Override
                    public void onColorPicked(int color) {
                        red.setText(""+Color.red(color));
                        green.setText(""+Color.green(color));
                        blue.setText(""+Color.blue(color));
                    }
                });
            }
        });

        reconocimientoOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                encederReconocimiento();
            }
        });

        reconocimientoOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apagarReconocimiento();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextReconocimiento();
            }
        });

        return fragment_container;
    }

    private void apagarLeds() {
        Leds l = new Leds(4);
        byte leds[] = l.turnOff();
        System.out.print("Test:LedsProtocol OFF ");
        for(byte b: leds){
            System.out.print(Parser.getStringCharProtocol(b));
        }
        System.out.println();
        Builder.getBluetoothUtils().bluetoothSendData(leds,
                getString(R.string.turned_off),
                getString(R.string.could_not_turn_off));
    }

    private void encederLeds() {
        Leds l = new Leds(4);
        int i = Integer.parseInt(index.getText().toString());
        int r = Integer.parseInt(red.getText().toString());
        int g = Integer.parseInt(green.getText().toString());
        int b = Integer.parseInt(blue.getText().toString());
        if(i > 3)
            i = 3;
        if(i < 0)
            i = 0;
        l.setColor(i,new int[]{r,g,b});
        System.out.print("Test:LedsProtocol ON ");
        byte leds[] = l.getBytes();
        for(byte by: leds){
            System.out.print(Parser.getStringCharProtocol(by));
        }
        System.out.println();
        Builder.getBluetoothUtils().bluetoothSendData(leds,
                getString(R.string.turned_on),
                getString(R.string.could_not_turn_on));
    }

    private void encederReconocimiento() {
        Builder.getBluetoothUtils().bluetoothSendData("D",
                getString(R.string.turned_on),
                getString(R.string.could_not_turn_on));
    }

    private void nextReconocimiento() {
        Builder.getBluetoothUtils().bluetoothSendData("DN",
                getString(R.string.turned_on),
                getString(R.string.could_not_turn_on));
    }

    private void apagarReconocimiento() {
        Builder.getBluetoothUtils().bluetoothSendData("DS",
                getString(R.string.turned_on),
                getString(R.string.could_not_turn_on));
    }


}
