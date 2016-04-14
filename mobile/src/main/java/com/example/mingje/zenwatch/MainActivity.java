package com.example.mingje.zenwatch;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import org.w3c.dom.Text;

import java.io.BufferedOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;


public class MainActivity extends ActionBarActivity implements  DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LabelListFragment.OnSetCurrentLabelListener {

    private final String SENSOR_SENSOR_KEY = "SENSOR";
    private String currentLabel = "";
    private TextView mTextAcceleration;
    private EditText mEditTextIP;
    private EditText mEditTextPort;
    private Button mButtonChangeIPnPort;
    private Button mButtonStartTransmission;
    private Button mButtonStopTransmission;
    private GoogleApiClient mGoogleApiClient;
    private Handler mHandler;
    private String address = "140.112.90.184";// 連線的ip
    private int port = 54321;// 連線的port

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        setListener();

        Display display = getWindowManager().getDefaultDisplay();
        LabelListFragment labelListFragment = new LabelListFragment();
        getFragmentManager().beginTransaction().replace(R.id.label_list_activity_main, labelListFragment).commit();

        mHandler = new Handler() {

            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    mTextAcceleration.setText(Arrays.toString((String [])msg.obj));
                }
            }

        };


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void setListener() {
        View.OnClickListener changeIPnPort = new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                address = mEditTextIP.getText().toString();
                port = Integer.valueOf(mEditTextPort.getText().toString());
                //Toast.makeText(MainActivity, "Change Success", Toast.LENGTH_SHORT);
            }
        };
        mButtonChangeIPnPort.setOnClickListener(changeIPnPort);

        View.OnClickListener startOrStopTransmissionListener = new View.OnClickListener(){

            public void onClick(View view){
                switch (view.getId()){
                    case R.id.start_transmission_activity_main:
                        mGoogleApiClient.connect();
                        //Toast.makeText(MainActivity, "Start transmisson", Toast.LENGTH_SHORT);
                        break;
                    case R.id.stop_transmission_activity_main:
                        Wearable.DataApi.removeListener(mGoogleApiClient, MainActivity.this);
                        //Toast.makeText(MainActivity, "Stop transmisson", Toast.LENGTH_SHORT);
                        mGoogleApiClient.disconnect();
                        break;
                }
            }
        };
        mButtonStopTransmission.setOnClickListener(startOrStopTransmissionListener);
        mButtonStartTransmission.setOnClickListener(startOrStopTransmissionListener);
    }

    public void findViews(){
        mTextAcceleration = (TextView) findViewById(R.id.acceleration_activity_main);
        mEditTextIP = (EditText) findViewById(R.id.ip_activity_main);
        mEditTextPort = (EditText) findViewById(R.id.port_activity_main);
        mButtonChangeIPnPort = (Button) findViewById(R.id.change_socket_activity_main);
        mButtonStartTransmission = (Button) findViewById(R.id.start_transmission_activity_main);
        mButtonStopTransmission = (Button) findViewById(R.id.stop_transmission_activity_main);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    protected void onResume() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Wearable.DataApi.removeListener(mGoogleApiClient, this);
        //mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        mTextAcceleration.setText("Connected");
    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/sensor") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    //Message Test
                    socketClient((String[])dataMap.get(SENSOR_SENSOR_KEY));
                    Message msg = Message.obtain();
                    msg.what = 0;
                    msg.obj = dataMap.get(SENSOR_SENSOR_KEY);
                    mHandler.sendMessage(msg);

                    // mTextAcceleration.setText((String)dataMap.get(SENSOR_TYPE_KEY));
                    Log.d("onDataChanged","ACCELERATION");

                }else if(item.getUri().getPath().compareTo("/Acceleration") == 0){
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    //Message Test
                    socketClient((String[])dataMap.get(SENSOR_SENSOR_KEY));
                    Message msg = Message.obtain();
                    msg.what = 0;
                    msg.obj = dataMap.get(SENSOR_SENSOR_KEY);
                    mHandler.sendMessage(msg);

                    // mTextAcceleration.setText((String)dataMap.get(SENSOR_TYPE_KEY));
                    Log.d("onDataChanged","ACCELERATION");
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deletedaaaaa
            }
        }
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
    public void socketClient(String[] data){

        final String fData = toString(data);
        new Thread(new Runnable(){
            @Override
            public void run() {
                Socket client = new Socket();
                InetSocketAddress isa = new InetSocketAddress(address, port);
                try {
                    client.connect(isa, 10000);
                    BufferedOutputStream out = new BufferedOutputStream(client
                            .getOutputStream());
                    // 送出字串
                    out.write((currentLabel + "\r\n").getBytes());
                    out.write(fData.getBytes());
                    out.flush();
                    out.close();
                    out = null;
                    client.close();
                    client = null;

                } catch (java.io.IOException e) {
                    Log.d("Socket","Socket連線有問題 !");
                    Log.d("Socket","IOException :" + e.toString());
                }
            }
        }).start();


    }
    public String toString(String[] data){
        String fData = "";

        for(int i = 0; i < data.length; i++){
            fData += data[i];
        }

        return  fData;
    }


    @Override
    public void onSetCurrentLabel(String currentLabel) {

        this.currentLabel = currentLabel;
        //socketClient(data);
    }
}
