package speed.mobiweb.testmotion.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.pkp.PermissionHandler;
import com.github.pkp.PermissionsChecker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import speed.mobiweb.testmotion.R;
import speed.mobiweb.testmotion.utils.Speedometer;

public class ActivitySensor extends AppCompatActivity {

    private final PermissionHandler permissionHandler =
            new PermissionHandler() {
                @Override
                public void onGranted() {

                }

                @Override
                public void onDeny() {

                }
            };
    SensorManager sensorManager;
    Context ctx;
    Sensor sensor;
    TextView tvSensor;
    Button btnSwitch;
    Speedometer speedometer;
    private int _samplePeriod = 15;
    private long _lastTick = System.currentTimeMillis();
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
                    float speedKM = (float) (motion * 3.6);
                    speedometer.onSpeedChanged(speedKM);

                    if (speedKM > 50) {
                        speedometer.setBackgroundColor(getResources().getColor(R.color.red));
                        tvSensor.setText("HIGH");
                        tvSensor.setTextColor(getResources().getColor(R.color.red));
                    } else if (speedKM > 25) {
                        tvSensor.setText("AVERAGE");
                        tvSensor.setTextColor(getResources().getColor(R.color.green));
                        speedometer.setBackgroundColor(getResources().getColor(R.color.green));

                    } else {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = this;
        initialiseElements();
        clickEvents();


    }

    @Override
    protected void onResume() {
        super.onResume();
        String[] permissions = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        PermissionsChecker.check(this, 101, permissions, permissionHandler);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void initialiseElements() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(accelListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        tvSensor = (TextView) findViewById(R.id.sensorX);
        speedometer = (Speedometer) findViewById(R.id.speedometer);
        btnSwitch = (Button) findViewById(R.id.switch_activity);
        btnSwitch.setText("Show GPS speed");


    }

    private void clickEvents() {
        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(ctx, ActivityGps.class);
                startActivity(in);
                finish();
            }
        });
    }

    public void onStop() {
        super.onStop();
        sensorManager.unregisterListener(accelListener);
    }
}
