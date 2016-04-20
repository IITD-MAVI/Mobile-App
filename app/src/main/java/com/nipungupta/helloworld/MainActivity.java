package com.nipungupta.helloworld;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends Activity {

    private Button onBtn;
    private Button offBtn;
    private Button conBtn;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_DSC = 3;
    private String message;

    private TextView tv;
    private TextView textView;
    private Handler mHandler;

    private BluetoothAdapter mAdapter;
    private TextToSpeech tts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.text);
        textView = (TextView) findViewById(R.id.textCtr);
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mAdapter==null) {
            onBtn.setEnabled(false);
            offBtn.setEnabled(false);
            conBtn.setEnabled(false);
            tv.setText("Status: not supported");
            Toast.makeText(getApplicationContext(),"Your device does not support Bluetooth",Toast.LENGTH_LONG).show();
        } else {
            onBtn = (Button) findViewById(R.id.turnOn);
            onBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    on(v);
                }
            });

            offBtn = (Button) findViewById(R.id.turnOff);
            offBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    off(v);
                }
            });

            conBtn = (Button) findViewById(R.id.connect);
            conBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    connect(v);
                }
            });

            mHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message message) {
                    String str = message.getData().getString("message");
                    if(str!=null && !str.equals("")) {
                        handle(str);
                    }
                }
            };

            tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status != TextToSpeech.ERROR) {
                        tts.setLanguage(Locale.UK);
                    }
                }
            });
        }
    }

    public void on(View view) {
        if(!mAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
            Toast.makeText(getApplicationContext(), "Bluetooth turned on", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(getApplicationContext(), "Bluetooth is already on", Toast.LENGTH_LONG).show();
        }

        Intent discover = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discover.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivityForResult(discover, REQUEST_ENABLE_DSC);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_ENABLE_BT) {
            if(mAdapter.isEnabled()) {
                tv.setText("Status: Enabled");
            } else {
                tv.setText("Status: Disabled");
            }
        }
        else if(requestCode==REQUEST_ENABLE_DSC) {
            if(mAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                tv.setText("Status: Enabled and Discoverable");
            } else {
                tv.setText("Status: Enabled but not Discoverable");
            }
        }
    }

    public void off(View view) {
        mAdapter.disable();
        tv.setText("Status: Disconnected");
        Toast.makeText(getApplicationContext(), "Bluetooth turned off", Toast.LENGTH_LONG).show();

        String toSpeak = "Bluetooth turned off";
        tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void connect(View view) {
        AcceptThread accept = new AcceptThread(mAdapter, mHandler);
        accept.start();
    }

    public void handle(String jsonMessage) {
        JsonPath jsonPath = new JsonPath(jsonMessage);

        //Face Detection
        int noOfFaces = Integer.parseInt((String) jsonPath.read("$.faceDetectionString.noOfFaces"));
        if(noOfFaces > 0) {
            message = noOfFaces + "faces are detected.\n";
            int noOfRecFaces = jsonPath.read("$.faceDetectionString.nameArray.length()");
            if(noOfRecFaces==0) {
                message += "None of them is recognized";
                displayText(message);
            }
            else {
                message += "Recognized faces are ";
                for(int i=0; i<noOfRecFaces; i++) {
                    message += jsonPath.read("$.faceDetectionString.nameArray["+i+"]") + ", ";
                    displayText(message);
                }
            }
        }


        //Texture Detection
        if(jsonPath.read("$.textureString.pothole").equals("True")) {
            message = "Pothole detected ahead. Be careful.";
            displayText(message);
//            vibratePhone(500);
        }
    }

    public static String getTextureFromCode(int code) {
        if(code==0) {
            return "not detected";
        }
        else if(code==1) {
            return "road";
        }
        else if(code==2) {
            return "pavement";
        }
        else if(code==3) {
            return "grass";
        }

        return null;
    }

    protected void displayText(String str) {
        textView.setText(str);
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
