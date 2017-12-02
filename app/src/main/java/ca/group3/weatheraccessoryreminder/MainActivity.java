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
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.SparseInstance;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String FILENAME = "accelData.csv";
    private static final String TAG = "Permissions";
    private boolean started = false;
    private SensorManager sensorManager;
    private Button button;
    private Spinner sItems;
    FileOutputStream outputStream;
    private boolean outputStreamFlag = false;
    private ArrayList<Double> window1;
    private ArrayList<Double> window2;
    private TextView tvData;
    int counter = 0;
    private long lastSensorUpdate = 0;
    private int counter2 = 0;
    private J48 classifier;
    private FastVector fvWekaAttributes;
    private Instance inst_co;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isStoragePermissionGranted();
        window1 = new ArrayList<Double>();
        window2 = new ArrayList<Double>();
        tvData = findViewById(R.id.textViewData);
        createAttributes();

        try {
            File sdcard = Environment.getExternalStorageDirectory();
            classifier = (J48) weka.core.SerializationHelper.read(sdcard.getAbsolutePath() + "/accelData/" + "walkRestModel.model");
            int df = classifier.getNumFolds();
            df++;
            //classifier = (J48) (new ObjectInputStream(new FileInputStream(sdcard.getAbsolutePath() + "/accelData/" + "walkRestModel.model"))).readObject();
            //classifier = (J48) SerializationHelper.read(new FileInputStream(sdcard.getAbsolutePath() + "/accelData/" + "walkRestModel.model"));

        } catch (Exception e) {
            e.printStackTrace();
        };


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

            //long timestamp = System.currentTimeMillis();
            //int typeOfActivity = sItems.getSelectedItemPosition();

            //fs = String.format("X: %d Y: %d Z: %d",x,y,z);
            //String entry = x + "," + y + "," + z + ","  + typeOfActivity + "\n";
//            try {
//                if (outputStreamFlag) {
//                    outputStream.write(entry.getBytes());
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            //tvData.setText(timestamp + "," + x+"," + y + "," + z);
        }
    }

    private void createAttributes(){
        // Declare two numeric attributes
        Attribute Attribute1 = new Attribute("min");
        Attribute Attribute2 = new Attribute("max");
        Attribute Attribute3 = new Attribute("sd");
        //Attribute Attribute4 = new Attribute("class");

        // Declare a nominal attribute along with its values
        FastVector fvNominalVal = new FastVector(2);
        fvNominalVal.addElement("walking");
        fvNominalVal.addElement("rest");
        Attribute Attribute4 = new Attribute("class", fvNominalVal);

        fvWekaAttributes = new FastVector(4);
        fvWekaAttributes.addElement(Attribute1);
        fvWekaAttributes.addElement(Attribute2);
        fvWekaAttributes.addElement(Attribute3);
        fvWekaAttributes.addElement(Attribute4);

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

        int typeOfActivity = sItems.getSelectedItemPosition();
        String entry = lastSensorUpdate + "," + min + "," + max + "," + sd + ","  + typeOfActivity + "\n";
        try {
            if (outputStreamFlag) {
                outputStream.write(entry.getBytes());
                counter2++;
                tvData.setText("No of samples: " + counter2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Instances data = new Instances("TestInstances",fvWekaAttributes,1);
        data.setClassIndex(3);

        inst_co = new DenseInstance(data.numAttributes());

        inst_co.setValue((Attribute)fvWekaAttributes.elementAt(1), min);
        inst_co.setValue((Attribute)fvWekaAttributes.elementAt(2), max);
        inst_co.setValue((Attribute)fvWekaAttributes.elementAt(3), sd);
        //inst_co.setMissing((Attribute)fvWekaAttributes.elementAt(4));
        data.add(inst_co);
        inst_co.setDataset(data);


       /* Instance iExample = new SparseInstance(4);
        iExample.setValue((Attribute)fvWekaAttributes.elementAt(0), min);
        iExample.setValue((Attribute)fvWekaAttributes.elementAt(1), max);
        iExample.setValue((Attribute)fvWekaAttributes.elementAt(2), sd);*/
        //iExample.setValue((Attribute)fvWekaAttributes.elementAt(3), "positive");

        try {
            double result = classifier.classifyInstance(inst_co);
            double[] fDistribution = classifier.distributionForInstance(inst_co);
            tvData.setText("Prob of activity 1: " + fDistribution[0] + " 2: " + fDistribution[1]);
            //double ssdd = result +1;
            //ssdd++;
        } catch (Exception e) {
            e.printStackTrace();
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
