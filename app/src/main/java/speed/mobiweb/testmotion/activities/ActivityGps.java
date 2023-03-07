package speed.mobiweb.testmotion.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.pkp.PermissionHandler;
import com.github.pkp.PermissionsChecker;

import java.math.BigDecimal;
import java.math.RoundingMode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import speed.mobiweb.testmotion.R;
import speed.mobiweb.testmotion.utils.Speedometer;
import speed.mobiweb.testmotion.utils.gpsutils.Constants;
import speed.mobiweb.testmotion.utils.gpsutils.GPSCallback;
import speed.mobiweb.testmotion.utils.gpsutils.GPSManager;


public class ActivityGps extends AppCompatActivity implements GPSCallback {
    private final PermissionHandler permissionHandler =
            new PermissionHandler() {
                @Override
                public void onGranted() {

                }

                @Override
                public void onDeny() {

                }
            };
    TextView tvSensor;
    Button btnSwitch;
    Speedometer speedometer;
    Context ctx;
    private GPSManager gpsManager = null;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = this;
        initialiseElements();
        clickEvents();
    }

    @Override
    public void onGPSUpdate(Location location) {
        location.getLatitude();
        location.getLongitude();
        double speed = location.getSpeed();

        double speedKM = (roundDecimal(convertSpeed(speed), 2));

        speedometer.onSpeedChanged((float) speedKM);

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


    private void initialiseElements() {
        gpsManager = new GPSManager(this);
        gpsManager.startListening(getApplicationContext());
        gpsManager.setGPSCallback(this);

        ((TextView) findViewById(R.id.sensorX))
                .setText("Speed of GPS");
        tvSensor = (TextView) findViewById(R.id.sensorX);
        speedometer = (Speedometer) findViewById(R.id.speedometer);
        btnSwitch = (Button) findViewById(R.id.switch_activity);
        btnSwitch.setText("Show Sensor speed");


    }

    private void clickEvents() {
        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(ctx, ActivitySensor.class);
                startActivity(in);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        gpsManager.stopListening();
        gpsManager.setGPSCallback(null);

        gpsManager = null;

        super.onDestroy();
    }


    private double convertSpeed(double speed) {
        return ((speed * Constants.HOUR_MULTIPLIER) * Constants.UNIT_MULTIPLIERS);
    }

    private double roundDecimal(double value, final int decimalPlace) {
        BigDecimal bd = new BigDecimal(value);

        bd = bd.setScale(decimalPlace, RoundingMode.HALF_UP);
        value = bd.doubleValue();

        return value;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}