package speed.mobiweb.testmotion.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import rebus.permissionutils.AskagainCallback;
import rebus.permissionutils.FullCallback;
import rebus.permissionutils.PermissionEnum;
import rebus.permissionutils.PermissionManager;
import speed.mobiweb.testmotion.R;
import speed.mobiweb.testmotion.utils.Speedometer;

public class ActivitySensor extends AppCompatActivity {

    SensorManager sensorManager;
    Context ctx;
    Sensor sensor;
    TextView tvSensor;
    Button btnSwitch;
    Speedometer speedometer;
    private int _samplePeriod = 15;
    private long _lastTick = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx=this;
        initialiseElements();
        clickEvents();



    }
    @Override
    protected void onResume() {
        super.onResume();
        PermissionManager.with(ActivitySensor.this)
                .permission(PermissionEnum.ACCESS_COARSE_LOCATION, PermissionEnum.ACCESS_FINE_LOCATION)
                .askagain(true)
                .askagainCallback(new AskagainCallback() {
                    @Override
                    public void showRequestPermission(UserResponse response) {
                        showDialog(response);
                    }
                })
                .callback(new FullCallback() {
                    @Override
                    public void result(ArrayList<PermissionEnum> permissionsGranted, ArrayList<PermissionEnum> permissionsDenied, ArrayList<PermissionEnum> permissionsDeniedForever, ArrayList<PermissionEnum> permissionsAsked) {
                    }
                })
                .ask();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.handleResult(requestCode, permissions, grantResults);
    }

    private void showDialog(final AskagainCallback.UserResponse response) {
        new AlertDialog.Builder(ActivitySensor.this)
                .setTitle("Permission needed")
                .setMessage("This app realy need to use this permission, you wont to authorize it?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        response.result(true);
                    }
                })
                .setNegativeButton("NOT NOW", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        response.result(false);
                    }
                })
                .show();
    }
    private void initialiseElements()
    {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(accelListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        tvSensor = (TextView) findViewById(R.id.sensorX);
        speedometer = (Speedometer) findViewById(R.id.speedometer);
        btnSwitch= (Button) findViewById(R.id.switch_activity);
        btnSwitch.setText("Show GPS speed");


    }

    private  void clickEvents()
    {
        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in= new Intent(ctx, ActivityGps.class);
                startActivity(in);
                finish();
            }
        });
    }
    public void onStop() {
        super.onStop();
        sensorManager.unregisterListener(accelListener);
    }

    SensorEventListener accelListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            String vendor = event.sensor.getVendor();
           // Toast.makeText(ActivitySensor.this, vendor, Toast.LENGTH_SHORT).show();
            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                long tick = System.currentTimeMillis();
                long localPeriod = tick - _lastTick;

                if (localPeriod > _samplePeriod) {
                    _lastTick = tick;
                    double motion = Math.sqrt(Math.pow(event.values[0], 2) +
                            Math.pow(event.values[1], 2) +
                            Math.pow(event.values[2], 2));

                    // Warn the activity that we sampled a new value.
                    tvSensor.setText(motion + "");
                    float speedKM=(float) (motion * 3.6);
                    speedometer.onSpeedChanged(speedKM);

                    if (speedKM>50)
                    {
                        speedometer.setBackgroundColor(getResources().getColor(R.color.red));
                        tvSensor.setText("HIGH");
                        tvSensor.setTextColor(getResources().getColor(R.color.red));
                    }
                    else if (speedKM>25)
                    {
                        tvSensor.setText("AVERAGE");
                        tvSensor.setTextColor(getResources().getColor(R.color.green));
                        speedometer.setBackgroundColor(getResources().getColor(R.color.green));

                    }
                    else
                    {
                        tvSensor.setText("LOW");
                        tvSensor.setTextColor(getResources().getColor(R.color.blue));
                        speedometer.setBackgroundColor(getResources().getColor(R.color.blue));

                    }


                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

    };
}
