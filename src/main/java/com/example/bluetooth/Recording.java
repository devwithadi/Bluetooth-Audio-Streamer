package com.example.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.CountDownTimer;
import android.widget.Toast;

public class Recording {
    static int count = 0;
    static String Shared;
    static String bFlag;
    public static int TIMEOUT = 5000;
    public static int COUNTDOWN_INTERVAL = 1000;
    static Context context;

    public void checkAndRecord(Context context,OnBluetoothRecording BluetoothRecording, boolean resume) {


        if (getBluetoothFlag(context) && isBluetoothON()) {


            startBluetoothRecording(BluetoothRecording, resume, context);

        } else {


            if (getBluetoothFlag(context) && !isBluetoothON()) {

                Toast.makeText(context,
                        "Bluetooth is OFF. Recording from Phone MIC.",
                        Toast.LENGTH_SHORT).show();
                BluetoothRecording.onStartRecording(resume, false);
            } else {
                // false because recording not started
                BluetoothRecording.onStartRecording(resume, false);
            }
        }

    }

  public   void startBluetoothRecording(
            final OnBluetoothRecording BluetoothRecording,
            final boolean resume, Context context) {


        final int MAX_ATTEPTS_TO_CONNECT = 3;
        final AudioManager audioManager = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);

        final CountDownTimer timer = getTimer(BluetoothRecording, audioManager,
                resume);

        context.registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                int state = intent.getIntExtra(
                        AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
                if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {

                    timer.cancel();
                    context.unregisterReceiver(this);


                    BluetoothRecording.onStartRecording(resume, true);

                } else if (AudioManager.SCO_AUDIO_STATE_DISCONNECTED == state) {
                    if (count > MAX_ATTEPTS_TO_CONNECT) {
                        context.unregisterReceiver(this);

                        audioManager.stopBluetoothSco();

                        count = 0;

                        timer.cancel();

                        BluetoothRecording.onStartRecording(resume, false);
                    } else {

                        count++;

                    }
                }

            }
        }, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));


        timer.start();
        audioManager.startBluetoothSco();

    }


   public   CountDownTimer getTimer(
            final OnBluetoothRecording BluetoothRecording,
            final AudioManager audioManager, final boolean resume) {
        // TODO Auto-generated method stub
        return new CountDownTimer(TIMEOUT, COUNTDOWN_INTERVAL) {

            @Override
            public void onTick(long millisUntilFinished) {
                // Do Nothing

            }

            @Override
            public void onFinish() {


                audioManager.stopBluetoothSco();


                BluetoothRecording.onStartRecording(resume, false);
            }
        };
    }


    public  boolean isBluetoothON() {

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();
        return bluetoothAdapter.isEnabled();
    }


    public  boolean getBluetoothFlag(Context context) {


        SharedPreferences sp = context.getSharedPreferences(Shared,
                Context.MODE_PRIVATE);
        return sp.getBoolean(bFlag, false);

    }

}

