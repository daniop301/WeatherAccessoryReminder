package ca.group3.weatheraccessoryreminder;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String FILENAME = "accelData.csv";
    private static final String TAG = "Permissions";
    private boolean started = false;
    private SensorManager sensorManager;
    private Button button;
    private Spinner sItems;
    FileOutputStream outputStream;
    private boolean outputStreamFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isStoragePermissionGranted();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        button = findViewById(R.id.buttonRecord);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!outputStreamFlag) {
                    try {
                        Toast.makeText(getApplicationContext(), "Recording data", Toast.LENGTH_LONG).show();
                        button.setText("Stop recording");
                        File sdcard = Environment.getExternalStorageDirectory();
                        File dir = new File(sdcard.getAbsolutePath() + "/accelData/");
                        dir.mkdir();
                        File file = new File(dir, FILENAME);
                        outputStream = new FileOutputStream(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Toast.makeText(getApplicationContext(), "Stopped recording data", Toast.LENGTH_LONG).show();
                        button.setText("Record");
                        outputStream.close();
                        outputStreamFlag = false;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                outputStreamFlag = !outputStreamFlag;
            }
        });

        List<String> spinnerArray = new ArrayList<>();
        spinnerArray.add("Rest");
        spinnerArray.add("Walk");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sItems = findViewById(R.id.spinnerType);
        sItems.setAdapter(adapter);

        if (!started) {
            started = true;
            Sensor accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accel,
                    SensorManager.SENSOR_DELAY_FASTEST);
            started = true;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (started) {
            double x = sensorEvent.values[0];
            double y = sensorEvent.values[1];
            double z = sensorEvent.values[2];
            //long timestamp = System.currentTimeMillis();
            int typeOfActivity = sItems.getSelectedItemPosition();

            //fs = String.format("X: %d Y: %d Z: %d",x,y,z);
            String entry = x + "," + y + "," + z + ","  + typeOfActivity + "\n";
            try {
                if (outputStreamFlag) {
                    outputStream.write(entry.getBytes());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //tvData.setText(timestamp + "," + x+"," + y + "," + z);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }
}
