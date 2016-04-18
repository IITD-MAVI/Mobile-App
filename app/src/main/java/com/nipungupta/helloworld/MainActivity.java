package com.nipungupta.helloworld;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity {

    private static TextView tv;
    private static BluetoothAdapter bluetoothAdapter;
    private static TextToSpeech textToSpeech;
    private static String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      //  setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        tv = (TextView) findViewById(R.id.textView);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });

        turnOnBluetooth();
    }

    protected void turnOnBluetooth() {
    //    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            message = "Device does not support bluetooth.";
            displayText(message);
            return;
        }

        if(!bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 0);
            message = "Bluetooth is now turned on";
            displayText(message);
        }
        else {
            message = "Bluetooth is already on";
            displayText(message);
        }
    }

 //   @SuppressWarnings("deprecation")
 //   @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void displayText(String text) {
        tv.setText(text);
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, this.hashCode()+"");
//        }
//        else {
//            HashMap<String, String> map = new HashMap<>();
//            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
//            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, map);
//        }

        return;
    }

    protected void vibratePhone(long timeInMiliSec) {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(timeInMiliSec);
        return;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
