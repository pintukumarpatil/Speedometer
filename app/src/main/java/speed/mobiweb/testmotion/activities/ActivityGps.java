package speed.mobiweb.testmotion.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import rebus.permissionutils.AskagainCallback;
import rebus.permissionutils.FullCallback;
import rebus.permissionutils.PermissionEnum;
import rebus.permissionutils.PermissionManager;
import speed.mobiweb.testmotion.R;
import speed.mobiweb.testmotion.utils.Speedometer;
import speed.mobiweb.testmotion.utils.gpsutils.Constants;
import speed.mobiweb.testmotion.utils.gpsutils.GPSCallback;
import speed.mobiweb.testmotion.utils.gpsutils.GPSManager;


public class ActivityGps extends AppCompatActivity implements GPSCallback {
    private GPSManager gpsManager = null;
    private double speed = 0.0;
    private AbsoluteSizeSpan sizeSpanLarge = null;
    private AbsoluteSizeSpan sizeSpanSmall = null;
    TextView tvSensor;
    Button btnSwitch;
    Speedometer speedometer;
    Context ctx;

    @Override
    protected void onResume() {
        super.onResume();
        PermissionManager.with(ActivityGps.this)
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx=this;
        initialiseElements();
        clickEvents();
    }

    @Override
    public void onGPSUpdate(Location location) {
        location.getLatitude();
        location.getLongitude();
        speed = location.getSpeed();

        Double speedKM=(roundDecimal(convertSpeed(speed), 2));

        speedometer.onSpeedChanged(speedKM.longValue());

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


    private void initialiseElements()
    {
        gpsManager = new GPSManager(this);
        gpsManager.startListening(getApplicationContext());
        gpsManager.setGPSCallback(this);

        ((TextView) findViewById(R.id.sensorX))
                .setText("Speed of GPS");
        tvSensor = (TextView) findViewById(R.id.sensorX);
        speedometer = (Speedometer) findViewById(R.id.speedometer);
        btnSwitch= (Button) findViewById(R.id.switch_activity);
        btnSwitch.setText("Show Sensor speed");


    }

    private  void clickEvents()
    {
        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in= new Intent(ctx, ActivitySensor.class);
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
        PermissionManager.handleResult(requestCode, permissions, grantResults);
    }

    private void showDialog(final AskagainCallback.UserResponse response) {
        new AlertDialog.Builder(ActivityGps.this)
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
}