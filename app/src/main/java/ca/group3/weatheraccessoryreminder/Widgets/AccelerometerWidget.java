package ca.group3.weatheraccessoryreminder.Widgets;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;

import ca.group3.weatheraccessoryreminder.Aggregators.ActivityAggregator;

/**
 * Created by stefa on 12/5/2017.
 */

public class AccelerometerWidget implements SensorEventListener {
    private ArrayList<Double> window1;
    private ArrayList<Double> window2;

    private boolean started = false;

    private SensorManager sensorManager;

    private ActivityAggregator activityAggregator;

    int counter = 0;
    private long lastSensorUpdate = 0;

public AccelerometerWidget( SensorManager sensorManager, ActivityAggregator activityAggregator )
{
    window1 = new ArrayList<Double>();
    window2 = new ArrayList<Double>();

    this.sensorManager = sensorManager;
    this.activityAggregator = activityAggregator;

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
            if(System.currentTimeMillis() - lastSensorUpdate > 200) {
                //tvData.setText("No of samples" + counter);
                double x = sensorEvent.values[0];
                double y = sensorEvent.values[1];
                double z = sensorEvent.values[2];
                counter++;

                double sample = Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2));
                window1.add(sample);
                if(counter >= 64) {
                    window2.add(sample);
                }

                if(window1.size() == 128) {
                    calculateWindowData(window1);
                    window1.clear();
                }
                if(window2.size() == 128) {
                    calculateWindowData(window2);
                    window2.clear();
                }
                lastSensorUpdate = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void calculateWindowData(ArrayList<Double> window) {
        Log.d("Window", "Calculated");
        int N = 128; //window size
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double sum = 0;
        for (int i = 0; i < N; i++) {
            if(window.get(i)<min)
            {
                min = window.get(i);
            }
            if(window.get(i)>max)
            {
                max = window.get(i);
            }
            sum += window.get(i);
        }
        double mean = sum/N; //corresponds to 𝑥𝑥̅ in the formula
        double summedDifference = 0;
        for (int i = 0; i < N; i++) {
            summedDifference += Math.pow((window.get(i) - mean),2);
        }
        double sd = Math.sqrt(summedDifference / (N-1)); //standard deviation

        //Raise event with window data
        activityAggregator.accelDataReceived(min, max, sd);
    }
}
