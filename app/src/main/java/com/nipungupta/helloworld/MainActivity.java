package com.nipungupta.helloworld;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {

    private Button onBtn;
    private Button offBtn;
    private Button listBtn;
    private Button findBtn;

    private static UUID MY_UUID = UUID.fromString("446118f0-8b1e-11e2-9e96-0800200c9a66");
    private static final int REQUEST_ENABLE_BT = 1;

    private TextView tv;
    private ListView listView;
    private ArrayAdapter<String> BTArrayAdapter;
    private TextToSpeech textToSpeech;
    private String message;

    private BluetoothServerSocket mmServerSocket;
    private BluetoothAdapter mAdapter;
    private Set<BluetoothDevice> pairedDevices;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.text);
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mAdapter==null) {
            onBtn.setEnabled(false);
            offBtn.setEnabled(false);
            listBtn.setEnabled(false);
            findBtn.setEnabled(false);
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

            listBtn = (Button) findViewById(R.id.paired);
            listBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    list(v);
                }
            });

            findBtn = (Button) findViewById(R.id.search);
            findBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    find(v);
                }
            });

            listView = (ListView) findViewById(R.id.listView1);
            BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        }

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });

   //     turnOnBluetooth();
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
    }

    public void list(View view) {
        pairedDevices = mAdapter.getBondedDevices();
        tv.setText("Status: "+pairedDevices.size()+" paired devices found");
        for(BluetoothDevice device : pairedDevices)
            BTArrayAdapter.add(device.getName()+"\n"+device.getAddress());

        Toast.makeText(getApplicationContext(), "Show Paired Devices", Toast.LENGTH_LONG).show();
    }

    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                BTArrayAdapter.add(device.getName()+"\n"+device.getAddress());
                BTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    public void find(View view) {
        if(mAdapter.isDiscovering()) {
            mAdapter.cancelDiscovery();
        }
        else {
            BTArrayAdapter.clear();
            mAdapter.startDiscovery();
            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    public void off(View view) {
        mAdapter.disable();
        tv.setText("Status: Disconnected");
        Toast.makeText(getApplicationContext(), "Bluetooth turned off", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bReceiver);
    }

    protected void turnOnBluetooth() {
        if(mAdapter == null) {
            message = "Device does not support bluetooth.";
            displayText(message);
            return;
        }

        if(!mAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 0);
            message = "Bluetooth is now turned on";
            displayText(message);
        }
        else {
            message = "Bluetooth is already on";
            displayText(message);
        }

        BluetoothSocket socket = null;
        try {
            mmServerSocket = mAdapter.listenUsingRfcommWithServiceRecord("MyService", MY_UUID);
            socket = mmServerSocket.accept();
        } catch(IOException e) {
            message = "Problem with Bluetooth connection";
            displayText(message);
        }

        DataInputStream mmInStream;
        DataOutputStream mmOutStream;
        try {
            mmServerSocket.close();

            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();

            mmInStream = new DataInputStream(tmpIn);
            mmOutStream = new DataOutputStream(tmpOut);
        } catch(IOException e) {
            message = "Problem with input/output stream.";
            displayText(message);
            return;
        }

        byte[] buffer = new byte[1024];
        int bytes;
        while(true) {
            try {
                bytes = mmInStream.read(buffer);
                String jsonString = new String(buffer,0,bytes);
                displayText(jsonString);
            } catch(IOException e) {
                break;
            }
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
