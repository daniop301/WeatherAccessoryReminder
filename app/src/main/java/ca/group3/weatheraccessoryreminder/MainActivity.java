package ca.group3.weatheraccessoryreminder;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import ca.group3.weatheraccessoryreminder.Aggregators.ActivityAggregator;
import ca.group3.weatheraccessoryreminder.Widgets.WeatherWidget;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Permissions";
    private Button button;
    private TextView tvData;
    private ActivityAggregator activityAggregator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isStoragePermissionGranted();
        isFineLocationPermissionGranted();
        isCoarseLocationPermissionGranted();
        tvData = findViewById(R.id.textViewData);

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        activityAggregator = new ActivityAggregator(sensorManager, locationManager, this, (float)56.173, (float)10.189);

        button = findViewById(R.id.buttonRecord);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activityAggregator.setCurrentLocationAsHome();
            }
        });
    }

    public void userLeaving(float distance)
    {
        Log.d("Window", "User leaving");
        tvData.setText("Distance: " + distance);
        WeatherWidget ww = new WeatherWidget(this, activityAggregator.homeLocation.toString());
        ww.execute();
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

    public boolean isFineLocationPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    public boolean isCoarseLocationPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    public void weatherDataReceived(Weather weather) {
        String notificationText = "Undefined weather status";

        if (weather.isRain && weather.isWind)
        {
            notificationText = "Expect rain and wind. Take a raincoat with you!";
        }
        else if (weather.isRain)
        {
            notificationText = "Rain is coming. Take an umbrella with you!";
        }
        else if (weather.isSnow)
        {
            notificationText = "Snowing outside today. Remember your boots!";
        }
        else if (weather.isSun)
        {
            notificationText = "Sky will clear up! Sunglasses are a must!";
        }

        Drawable drawable = ContextCompat.getDrawable(this,R.drawable.rainsun);

        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setLargeIcon(bitmap)
                        .setSmallIcon(R.drawable.rainsun)
                        .setContentTitle("Weather alert")
                        .setContentText(notificationText)
                        .setDefaults(Notification.DEFAULT_SOUND);


        int mNotificationId = 001;
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}
