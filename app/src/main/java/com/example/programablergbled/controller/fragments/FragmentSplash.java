package com.example.programablergbled.controller.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.programablergbled.R;
import com.example.programablergbled.Utils.Builder;

public class FragmentSplash extends Fragment {

    private View fragment_container;
    private FragmentSplashHandler handler;

    public interface FragmentSplashHandler{
        void onSplashFinished();
    }

    public FragmentSplash(FragmentSplashHandler handler){
        this.handler = handler;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment_container = inflater.inflate(R.layout.fragment_splash,container,false);


        handler.onSplashFinished();
        return fragment_container;
    }
}
