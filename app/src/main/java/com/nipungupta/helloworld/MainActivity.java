package com.nipungupta.helloworld;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.GeoApiContext;
import com.google.maps.RoadsApi;
import com.google.maps.model.SnappedPoint;

import net.minidev.json.JSONArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
    private Button checkApi;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_DSC = 3;
    private static final SignBoard[] signBoardData = {
            new SignBoard(28.5451600, 77.1906900, "Cycle Stand"),
            new SignBoard(28.5448117, 77.1909533, "Use me"),
            new SignBoard(28.5442717, 77.1912483, "Hospital, Boys Hostel, Student Activity Center, West Campus"),
            new SignBoard(28.5440533, 77.1915483, "Canteen, Staff Canteen, Maintenance Unit"),
            new SignBoard(28.5440633, 77.1920300, "IDD Center, Central Workshop"),
            new SignBoard(28.5441883, 77.1920233, "Mathematics Department, Library, Dogra Hall"),
            new SignBoard(28.5443467, 77.1922350, "Canara Bank, I.I.T. Delhi"),
            new SignBoard(28.5445833, 77.1928700, "Please do not walk on grass"),
            new SignBoard(28.5443617, 77.1928617, "Administrative Block, Employees Union"),
            new SignBoard(28.54430012, 77.1932017, "Director's Office, Seminar Hall, Security Control Room, Textile Department, SBI, IDDC, Workshop"),
            new SignBoard(28.5444933, 77.1934367, "Academic Block, Administrative Block, Seminar Hall, Exhibition Hall")
    };

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

            checkApi = (Button) findViewById(R.id.googleMapsRoads);
            checkApi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkGoogleMapsRoadsAPI(v);
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
//            double a1 = SignBoard.distanceGPS(28.5451600, 77.1906900, 28.5448117, 77.1909533);
//            double a2 = SignBoard.distanceGPS(28.5448117, 77.1909533, 28.5451600, 77.1906900);
//            displayText(a1+"\n"+a2);
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

    GeoApiContext context;
    LatLng[] input_points;
    SnappedPoint[] output_points;
    AsyncTask<Void, Void, SnappedPoint[]> taskSnapToRoads = new AsyncTask<Void, Void, SnappedPoint[]>() {
        @Override
        protected SnappedPoint[] doInBackground(Void... params) {
            try {
                output_points = RoadsApi.snapToRoads(context, true, input_points).await();

                Set<String> visitedPlaceIds = new HashSet<>();
                for(SnappedPoint point : output_points) {
                    if(!visitedPlaceIds.contains(point.placeId)) {
                        visitedPlaceIds.add(point.placeId);
                        GeocodingResult[] results = GeocodingApi.newRequest(context).place(point.placeId).await();

                        if(results.length > 0)
                            Log.d("ROADNAME", results[0].placeId + "\t" + results[0].formattedAddress);
                        else
                            Log.d("ROADNAME", point.placeId + "\tNo result from Geocoding");
                    }
                }

                return output_points;
            } catch (Exception e) {
                e.printStackTrace();
                output_points = new SnappedPoint[0];
                return output_points;
            }
        }

        @Override
        protected void onPostExecute(SnappedPoint[] points) {
        //    output_points = points.clone();
            textView.setText(points.length + " snapped points returned.");

            for(SnappedPoint point : points) {
                Log.d("PLACEID", point.originalIndex + " " + point.placeId);
            }
        }
    };

    public void checkGoogleMapsRoadsAPI(View view) {
        List<LatLng> coordinates = new ArrayList<>();
        coordinates.add(new LatLng(28.5453633626302, 77.1904949982961));
        coordinates.add(new LatLng(28.5454699834188, 77.1903216679891));
        coordinates.add(new LatLng(28.5448149998983, 77.1909266630808));
        coordinates.add(new LatLng(28.5448466618856, 77.1909366607666));
        coordinates.add(new LatLng(28.541875521342, 77.1909099896749));
        coordinates.add(new LatLng(28.5449133555094, 77.1909099896749));
        coordinates.add(new LatLng(28.5449333190917, 77.1909049987793));
        coordinates.add(new LatLng(28.5449566523234, 77.1908899943034));
        coordinates.add(new LatLng(28.5450050354004, 77.1908683300018));
        coordinates.add(new LatLng(28.5450667063395, 77.1908816655477));
        coordinates.add(new LatLng(28.545188331604, 77.1908999919891));
        coordinates.add(new LatLng(28.545188331604, 77.1907716592153));
        coordinates.add(new LatLng(28.5452116648356, 77.1908583323161));
        coordinates.add(new LatLng(28.5452500025431, 77.1908333301544));
        coordinates.add(new LatLng(28.545304997762, 77.1907533327738));
        input_points = coordinates.toArray(new LatLng[coordinates.size()]);
        textView.setText("We have " + coordinates.size() + " coordinates");

        context = new GeoApiContext().setApiKey(getString(R.string.google_maps_key));
        taskSnapToRoads.execute();

        if(output_points != null) {
            for(SnappedPoint point : output_points) {
                Log.d("STATE", point.originalIndex + " " + point.placeId);
            }
        } else {
            Log.d("STATE", "output array is null");
        }
    }

    public void handle(String jsonMessage) {
     //   System.out.println(jsonMessage);
        JsonPath jsonPath = new JsonPath(jsonMessage);

        boolean isPothole = potholeDetect(jsonPath);
        if(!isPothole) {
            boolean isSignDetect = signboardDetect(jsonPath);
            if(!isSignDetect) {
                faceDetect(jsonPath);
            }
        }
    }

    public void faceDetect(JsonPath jsonPath) {
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
    }

    public boolean signboardDetect(JsonPath jsonPath) {
        JsonPath sbJson = new JsonPath((String) jsonPath.read("$.signBoardString"));
        boolean isSign = Boolean.parseBoolean((sbJson.read("$.isSignBoardDetected")).toString());
        if(isSign && !currSignboard) {
            currSignboard = true;
            message = "Sign board detected.";

            JsonPath posJson = new JsonPath((String) jsonPath.read("$.positionString"));
            double latitude = posJson.read("$.pos_x");
            double longitude = posJson.read("$.pos_y");
            int idx = SignBoard.getNextSignBoard(latitude, longitude, signBoardData);
            message = message + "\n" + signBoardData[idx].getData();

            displayText(message);
            return true;
        }
        else if(!isSign & currSignboard) {
            currSignboard = false;
            return false;
        }
        return false;
    }

    public boolean potholeDetect(JsonPath jsonPath) {
        JsonPath tdJson = new JsonPath((String) jsonPath.read("$.textureString"));

        JSONArray textureDesc = tdJson.read("$.texture");
        r1c1Btn.setText(getTextureFromCode( Integer.parseInt(((JSONArray) textureDesc.get(0)).get(0).toString()) ));
        r1c2Btn.setText(getTextureFromCode( Integer.parseInt(((JSONArray) textureDesc.get(0)).get(1).toString()) ));
        r1c3Btn.setText(getTextureFromCode( Integer.parseInt(((JSONArray) textureDesc.get(0)).get(2).toString()) ));
        r2c1Btn.setText(getTextureFromCode( Integer.parseInt(((JSONArray) textureDesc.get(1)).get(0).toString()) ));
        r2c2Btn.setText(getTextureFromCode( Integer.parseInt(((JSONArray) textureDesc.get(1)).get(1).toString()) ));
        r2c3Btn.setText(getTextureFromCode( Integer.parseInt(((JSONArray) textureDesc.get(1)).get(2).toString()) ));

        boolean isPothole = Boolean.parseBoolean((tdJson.read("$.pothole")).toString());
        if(isPothole && !currPothole) {
            currPothole = true;
            message = "Pothole detected. Be careful.";
            displayText(message);
            vibratePhone(500);
            return true;
        }
        else if(!isPothole & currPothole) {
            currPothole = false;
            return false;
        }
        return false;
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
