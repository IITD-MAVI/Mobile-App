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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {

    private Button onBtn;
    private Button offBtn;
    private Button listBtn;
    private Button findBtn;
    private Button conBtn;

    private static UUID MY_UUID = UUID.fromString("446118f0-8b1e-11e2-9e96-0800200c9a66");
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_DSC = 3;

    private TextView tv;
    private Handler mHandler;
    private ListView listView;
    private ArrayAdapter<String> BTArrayAdapter;

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
                        tv.setText(str);
                    }
                }
            };

            listView = (ListView) findViewById(R.id.listView1);
            BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
            listView.setAdapter(BTArrayAdapter);
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

    public void connect(View view) {
        AcceptThread accept = new AcceptThread(mAdapter, mHandler);
        accept.start();

//        Intent newActIntent = new Intent(this, DisplayActivity.class);
//        newActIntent.putExtra("bluetooth", mAdapter);
//        startActivity(newActIntent);
    }

    protected void displayText(String str) {
        tv.setText(str);
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
