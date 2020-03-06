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

    private int LED_COUNT = 10;

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

        if(!Builder.getBluetoothConnection().isConnected()){
            handler.onExitFragmentPattern();
        }

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
                        float hsv[] = new float[3];
                        Color.colorToHSV(color,hsv);
                        red.setText(""+hsv[0]);
                        green.setText(""+hsv[1]);
                        blue.setText(""+hsv[2]);
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
        Leds l = new Leds(LED_COUNT);
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
        int auxH, auxS, auxV;

        Leds l = new Leds(LED_COUNT);
        int i = Integer.parseInt(index.getText().toString());

        if(i > LED_COUNT)
            i = LED_COUNT;
        if(i < 0)
            i = 0;

        auxH = Parser.parseRange(Float.parseFloat(red.getText().toString()),
                0,360,0,255);
        auxS = Parser.parseRange(Float.parseFloat(green.getText().toString()),
                0,1,1,255);
        auxV = Parser.parseRange(Float.parseFloat(blue.getText().toString()),
                0,1,0,255);

        byte h = (byte) Math.round(auxH);
        byte s = (byte) Math.round(auxS);
        byte v = (byte) Math.round(auxV);

        l.setColor(i,new byte[]{h,s,v});
        System.out.print("Test:LedsProtocol ON ");
        byte leds[] = l.getBytes();
        for(byte by: leds){
            System.out.print(Parser.getStringCharProtocol(by)+" ");
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
