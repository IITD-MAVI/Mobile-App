package com.nipungupta.helloworld;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.minidev.json.JSONArray;

import java.util.Locale;

public class MainActivity extends Activity {

    private Button onBtn;
    private Button offBtn;
    private Button conBtn;
    private Button r1c1Btn;
    private Button r1c2Btn;
    private Button r1c3Btn;
    private Button r2c1Btn;
    private Button r2c2Btn;
    private Button r2c3Btn;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_DSC = 3;
    private String message;
    private boolean currPothole = false;
    private boolean currSignboard = false;
    private int currFaces = 0;

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

            r1c1Btn = (Button) findViewById(R.id.r1c1);
            r1c1Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    speakTexture(v);
                }
            });

            r1c2Btn = (Button) findViewById(R.id.r1c2);
            r1c2Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    speakTexture(v);
                }
            });

            r1c3Btn = (Button) findViewById(R.id.r1c3);
            r1c3Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    speakTexture(v);
                }
            });

            r2c1Btn = (Button) findViewById(R.id.r2c1);
            r2c1Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    speakTexture(v);
                }
            });

            r2c2Btn = (Button) findViewById(R.id.r2c2);
            r2c2Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    speakTexture(v);
                }
            });

            r2c3Btn = (Button) findViewById(R.id.r2c3);
            r2c3Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    speakTexture(v);
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

//            String json = "{"signBoardString": "{\"isSignBoardDetected\": \"True\"}", "textureString": "{\"pothole\": \"False\", \"texture\": [[\"1\", \"0\", \"1\"], [\"1\", \"0\", \"1\"]]}", "positionString": "{\"pos_x\": 1.2314, \"pos_y\": 2.5426, \"pos_z\": 0.1243}", "faceDetectionString": "{\"noOfFaces\": \"2\", \"nameArray\": [\"Anupam\", \"Anupam\"]}"}";
//            handle(json);
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
    }

    public void connect(View view) {
        AcceptThread accept = new AcceptThread(mAdapter, mHandler);
        accept.start();
    }

    public void speakTexture(View view) {
        Button button = (Button) view;

        String toSpeak = button.getText().toString();
        if(toSpeak.equals("Not Detected")) {
            toSpeak = "Texture "+toSpeak;
        }

        switch(button.getId()) {
            case R.id.r1c1:
            case R.id.r2c1:
                toSpeak = toSpeak+" on the left";
                break;
            case R.id.r1c2:
            case R.id.r2c2:
                toSpeak = toSpeak+" ahead";
                break;
            case R.id.r1c3:
            case R.id.r2c3:
                toSpeak = toSpeak+" on the right";
                break;
        }

        tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void handle(String jsonMessage) {
        System.out.println(jsonMessage);
        JsonPath jsonPath = new JsonPath(jsonMessage);

        //Face Detection
        JsonPath fdJson = new JsonPath((String) jsonPath.read("$.faceDetectionString"));
        int noOfFaces = Integer.parseInt((fdJson.read("$.noOfFaces")).toString());
        if(noOfFaces > 0 && noOfFaces!=currFaces) {
            currFaces = noOfFaces;
            message = noOfFaces + " faces detected.\n";
            int noOfRecFaces = ((JSONArray) fdJson.read("$.nameArray")).size();
            if(noOfRecFaces==0) {
                message += "None recognized";
                displayText(message);
            }
            else {
                message += "Recognized ";
                for(int i=0; i<noOfRecFaces; i++) {
                    message += fdJson.read("$.nameArray["+i+"]") + ", ";
                    displayText(message);
                }
            }
        }
        else if(noOfFaces!=currFaces) {
            currFaces = noOfFaces;
        }

        //Signboard Detection
        JsonPath sbJson = new JsonPath((String) jsonPath.read("$.signBoardString"));
        boolean isSign = Boolean.parseBoolean((sbJson.read("$.isSignBoardDetected")).toString());
        if(isSign && !currSignboard) {
            currSignboard = true;
            message = "Sign board detected.";
            displayText(message);
        }
        else if(!isSign & currSignboard) {
            currSignboard = false;
        }

        //Texture Detection
        JsonPath tdJson = new JsonPath((String) jsonPath.read("$.textureString"));
        boolean isPothole = Boolean.parseBoolean((tdJson.read("$.pothole")).toString());
        if(isPothole && !currPothole) {
            currPothole = true;
            message = "Pothole detected. Be careful.";
            displayText(message);
            vibratePhone(500);
        }
        else if(!isPothole & currPothole) {
            currPothole = false;
        }
        JSONArray textureDesc = tdJson.read("$.texture");
        r1c1Btn.setText(getTextureFromCode( Integer.parseInt(((JSONArray) textureDesc.get(0)).get(0).toString()) ));
        r1c2Btn.setText(getTextureFromCode( Integer.parseInt(((JSONArray) textureDesc.get(0)).get(1).toString()) ));
        r1c3Btn.setText(getTextureFromCode( Integer.parseInt(((JSONArray) textureDesc.get(0)).get(2).toString()) ));
        r2c1Btn.setText(getTextureFromCode( Integer.parseInt(((JSONArray) textureDesc.get(1)).get(0).toString()) ));
        r2c2Btn.setText(getTextureFromCode( Integer.parseInt(((JSONArray) textureDesc.get(1)).get(1).toString()) ));
        r2c3Btn.setText(getTextureFromCode( Integer.parseInt(((JSONArray) textureDesc.get(1)).get(2).toString()) ));
    }

    public static String getTextureFromCode(int code) {
        if(code==0) {
            return "Road";
        }
        else if(code==1) {
            return "Pavement";
        }
        else if(code==2) {
            return "Grass";
        }
        else if(code==3) {
            return "Not Detected";
        }

        return null;
    }

    protected void displayText(String str) {
        textView.setText(str);
        tts.speak(str, TextToSpeech.QUEUE_FLUSH, null);
    }

    protected void vibratePhone(long timeInMiliSec) {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(timeInMiliSec);
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
