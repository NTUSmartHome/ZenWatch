package com.example.mingje.zenwatch;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;


public class MainActivity extends Activity implements ConnectionCallbacks,
        OnConnectionFailedListener{
    private final float alpha = 0.8f;
    private final String SENSOR_SENSOR_KEY = "SENSOR";
    private final double DURATION_SEND = 1000;
    private final int DURATION_SENSOR = 40000;
    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    private Sensor mMagneticFieldSensor;
    private Sensor mLinearAccelerometerSensor;
    private TextView mTextIsConnect;
    private TextView mTextAcc, mTextMF, mTextZ;
    private Button mButtonShowData;
    private GoogleApiClient mGoogleApiClient;
    private String[] sensorDataStream;
    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];
    private float[] values = new float[3];
    private float[] RotationMatrix = new float[9];
    private long previousTime;
    private boolean dataVisble = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticFieldSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mLinearAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorDataStream = new String[9];
        for(int i = 0; i < sensorDataStream.length; i++){
            sensorDataStream[i] = " ";
        }


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //previousTime =  System.currentTimeMillis();
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextAcc = (TextView) stub.findViewById(R.id.accerleration_x_activity_main);
                mTextMF = (TextView) stub.findViewById(R.id.accerleration_y_activity_main);
                mTextZ = (TextView) stub.findViewById(R.id.accerleration_z_activity_main);
                mTextIsConnect = (TextView) stub.findViewById(R.id.isconnect_activity_main);
                mButtonShowData = (Button) stub.findViewById(R.id.show_data_activity_main);

                if(mGoogleApiClient.isConnected()){
                    mTextIsConnect.setText("Connected");
                }else{
                    mTextIsConnect.setText("Not yet");
                }
                View.OnClickListener oclBtnOk = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // change text of the TextView (tvOut)\
                        if(dataVisble) {
                            mTextAcc.setVisibility(View.INVISIBLE);
                            mTextMF.setVisibility((View.INVISIBLE));
                            mTextZ.setVisibility((View.INVISIBLE));
                            mTextIsConnect.setVisibility(View.INVISIBLE);
                            mButtonShowData.setVisibility(View.INVISIBLE);
                        }else{
                            mTextAcc.setVisibility(View.VISIBLE);
                            mTextMF.setVisibility((View.VISIBLE));
                            mTextZ.setVisibility((View.VISIBLE));
                            mTextIsConnect.setVisibility(View.VISIBLE);
                            mButtonShowData.setVisibility(View.VISIBLE);
                        }
                    }
                };

                mButtonShowData.setOnClickListener(oclBtnOk);

            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }


    private SensorEventListener accelerometerListener = new SensorEventListener(){
        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            Sensor sensor = event.sensor;
            if(sensor.getType() == Sensor.TYPE_ACCELEROMETER) {


                mTextAcc.setText(" X: " + String.format("%.4f", event.values[0]) + " Y: " + String.format("%.4f", event.values[1]) + " Z: " + String.format("%.4f", event.values[2]));
                accelerometerValues = event.values;

                sensorDataStream[0] += event.values[0] + ",";
                sensorDataStream[1] += event.values[1] + ",";
                sensorDataStream[2] += event.values[2] + ",";



                //sendSensorData(event, SENSOR_ACCELERATION_KEY);

            }else if(sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                mTextMF.setText("X: " + String.format("%.4f", event.values[0]) + " Y: " + String.format("%.4f", event.values[1]) + " Z: " + String.format("%.4f", event.values[2]));
                magneticFieldValues = event.values;



                //sendSensorData(event, SENSOR_GYROSCOPE_KEY);
            }else if(sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
                sensorDataStream[3] += event.values[0] + ",";
                sensorDataStream[4] += event.values[1] + ",";
                sensorDataStream[5] += event.values[2] + ",";
            }
            SensorManager.getRotationMatrix(RotationMatrix, null, accelerometerValues, magneticFieldValues);
            SensorManager.getOrientation(RotationMatrix, values);

            values[0]=(float)Math.toDegrees(values[0]);
            values[1]=(float)Math.toDegrees(values[1]);
            values[2]=(float)Math.toDegrees(values[2]);
            sensorDataStream[6] += values[0] + ",";
            sensorDataStream[7] += values[1] + ",";
            sensorDataStream[8] += values[2] + ",";
            mTextZ.setText(values[0] + " " + values[1] + " " + values[2] + "");

            if(System.currentTimeMillis() - previousTime > DURATION_SEND){
                previousTime = System.currentTimeMillis();

                for(int i = 0; i < sensorDataStream.length - 1; i++) {
                    sensorDataStream[i] = sensorDataStream[i].substring(0, sensorDataStream[i].length() - 1);
                    sensorDataStream[i] += ";";
                }
                sendSensorData(sensorDataStream);


                sensorDataStream[0] = "Ax:";
                sensorDataStream[1] = "\r\nAy:";
                sensorDataStream[2] = "\r\nAz:";
                sensorDataStream[3] = "\r\nGx:";
                sensorDataStream[4] = "\r\nGy:";
                sensorDataStream[5] = "\r\nGz:";
                sensorDataStream[6] = "\r\nY:";
                sensorDataStream[7] = "\r\nP:";
                sensorDataStream[8] = "\r\nR:";
            }



        }
    };

    protected void onStart(){
        super.onStart();
        mGoogleApiClient.connect();
    }
    @Override
    protected void onResume(){
        super.onResume();
        setSensor();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(accelerometerListener);
    }
    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    //Sensor
    private void setSensor() {
        new Thread(new Runnable() {
            public void run() {

                mSensorManager.registerListener(accelerometerListener, mAccelerometerSensor, DURATION_SENSOR);
                mSensorManager.registerListener(accelerometerListener, mMagneticFieldSensor, DURATION_SENSOR);
                mSensorManager.registerListener(accelerometerListener, mLinearAccelerometerSensor, DURATION_SENSOR);

            }
        }).start();


    }

    public void sendSensorData(String[] sensorDataStream){

        PutDataMapRequest dataMapRequest = PutDataMapRequest.create("/sensor");
        dataMapRequest.getDataMap().putStringArray(SENSOR_SENSOR_KEY, sensorDataStream);
        PutDataRequest request = dataMapRequest.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                  .putDataItem(mGoogleApiClient, request);


    }

    @Override
    public void onConnected(Bundle bundle) {
        if(mTextIsConnect != null){
            mTextIsConnect.setText("Connect succcess");
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if(mTextIsConnect != null) {
            mTextIsConnect.setText("Connect failed");
        }

    }


}

