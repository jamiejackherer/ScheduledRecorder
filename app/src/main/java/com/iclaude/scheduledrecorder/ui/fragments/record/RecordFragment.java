/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.ui.fragments.record;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.databinding.Observable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableInt;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.SimpleColorFilter;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.value.LottieValueCallback;
import com.iclaude.scheduledrecorder.R;
import com.iclaude.scheduledrecorder.databinding.FragmentRecordBinding;
import com.iclaude.scheduledrecorder.utils.PermissionsManager;

import java.util.Objects;


// TODO implement pause recording
public class RecordFragment extends Fragment {
    private static final String TAG = "SCHEDULED_RECORDER_TAG";

    private static final int REQUEST_DANGEROUS_PERMISSIONS = 0;
    private static final String ARG_POSITION = "position";
    private final boolean marshmallow = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

    private RecordViewModel recordViewModel;
    private AudioLevelView audioView;
    private LottieAnimationView lottieView;

    private boolean firstCallback = true;
    private Observable.OnPropertyChangedCallback secsCallback;


    public static RecordFragment newInstance(int position) {
        RecordFragment f = new RecordFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);

        return f;
    }

    public RecordFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        recordViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(RecordViewModel.class);
        recordViewModel.getToastMsg().observe(this, msgId ->
                Toast.makeText(getActivity(), getString(msgId), Toast.LENGTH_SHORT).show());
        recordViewModel.getAmplitudeLive().observe(this, integer ->
                    audioView.addAmplitude(integer));

        recordViewModel.serviceRecording.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                boolean isRecording =  ((ObservableBoolean) sender).get();
                if(isRecording) {
                    lottieView.playAnimation();
                } else {
                    lottieView.setProgress(0f);
                    lottieView.pauseAnimation();
                }
            }
        });

    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentRecordBinding binding = FragmentRecordBinding.inflate(inflater, container, false);
        binding.setViewModel(recordViewModel);
        View rootView = binding.getRoot();

        FloatingActionButton fab = rootView.findViewById(R.id.btnRecord);
        fab.setOnClickListener(v -> checkPermissionsAndRecord());

        audioView = rootView.findViewById(R.id.audio_view);

        // Lottie animation view.
        lottieView = rootView.findViewById(R.id.lottie_animation_view);
        lottieView.setRepeatCount(LottieDrawable.INFINITE);
        // change the color to match app's theme
        int color = ContextCompat.getColor(getActivity(), R.color.primary_light);
        SimpleColorFilter filter = new SimpleColorFilter(color);
        KeyPath keyPath = new KeyPath("**");
        LottieValueCallback<ColorFilter> callback = new LottieValueCallback<ColorFilter>(filter);
        lottieView.addValueCallback(keyPath, LottieProperty.COLOR_FILTER, callback);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        firstCallback = true;
        // When receiving the first second, adjust the line of times.
        secsCallback = new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if(firstCallback) {
                    firstCallback = false;

                    int secs = ((ObservableInt) sender).get();
                    getActivity().runOnUiThread(() -> audioView.startRecording(secs));
                }
            }
        };
        recordViewModel.secondsElapsed.addOnPropertyChangedCallback(secsCallback);

        if(recordViewModel.serviceRecording.get()) {
            lottieView.playAnimation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        recordViewModel.secondsElapsed.removeOnPropertyChangedCallback(secsCallback);
        lottieView.pauseAnimation();
    }

    // Check dangerous permissions for Android Marshmallow+.
    private void checkPermissionsAndRecord() {
        if(!marshmallow) {
            startStopRecording();
            return;
        }

        String[] permissionsToAsk = PermissionsManager.checkPermissions(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO);
        if(permissionsToAsk.length > 0)
            requestPermissions(permissionsToAsk, REQUEST_DANGEROUS_PERMISSIONS);
        else
            startStopRecording();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean granted = true;
        for (int grantResult : grantResults) { // we nee all permissions granted
            if (grantResult != PackageManager.PERMISSION_GRANTED)
                granted = false;
        }

        if (granted)
            startStopRecording();
        else
            Toast.makeText(getActivity(), getString(R.string.toast_permissions_denied), Toast.LENGTH_LONG).show();
    }

    private void startStopRecording() {
        firstCallback = false;
        if (!recordViewModel.serviceRecording.get()) { // start recording
            recordViewModel.startRecording();
            Objects.requireNonNull(getActivity()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //keep screen on while recording
            audioView.startRecording(0);
        } else { //stop recording
            recordViewModel.stopRecording();
            Objects.requireNonNull(getActivity()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //allow the screen to turn off again once recording is finished
            audioView.stopRecording();
        }
    }
}