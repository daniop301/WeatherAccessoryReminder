package ca.group3.weatheraccessoryreminder.Aggregators;

import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;

import java.io.File;

import ca.group3.weatheraccessoryreminder.MainActivity;
import ca.group3.weatheraccessoryreminder.Widgets.AccelerometerWidget;
import ca.group3.weatheraccessoryreminder.Widgets.LocationWidget;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Created by stefa on 12/5/2017.
 */

public class ActivityAggregator {
    private J48 classifier;
    private FastVector fvWekaAttributes;
    private Instance inst_co;

    private AccelerometerWidget accelerometerWidget;
    private LocationWidget locationWidget;

    private Location homeLocation;


    private MainActivity mainActivity;

    private boolean currentActivityWalking = false;

    public ActivityAggregator(SensorManager sensorManager, LocationManager locationManager, MainActivity mainActivity)
    {
        homeLocation = new Location("Home location");
        homeLocation.setLatitude(56.173);
        homeLocation.setLongitude(10.189);

        createAttributes();
        this.accelerometerWidget = new AccelerometerWidget(sensorManager, this);
        this.locationWidget = new LocationWidget(locationManager, mainActivity);

        this.mainActivity = mainActivity;


        try {
            File sdcard = Environment.getExternalStorageDirectory();
            classifier = (J48) weka.core.SerializationHelper.read(sdcard.getAbsolutePath() + "/accelData/" + "walkRestModel.model");

        } catch (Exception e) {
            e.printStackTrace();
        };
    }

    private void createAttributes() {
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

    public void accelDataReceived( double min, double max, double sd )
    {
        Instances data = new Instances("TestInstances",fvWekaAttributes,1);
        data.setClassIndex(3);

        inst_co = new DenseInstance(data.numAttributes());

        inst_co.setValue((Attribute)fvWekaAttributes.elementAt(1), min);
        inst_co.setValue((Attribute)fvWekaAttributes.elementAt(2), max);
        inst_co.setValue((Attribute)fvWekaAttributes.elementAt(3), sd);
        //inst_co.setMissing((Attribute)fvWekaAttributes.elementAt(4));
        data.add(inst_co);
        inst_co.setDataset(data);

        try {
            double result = classifier.classifyInstance(inst_co);
            double[] fDistribution = classifier.distributionForInstance(inst_co);

            if ( (fDistribution[0] > 0.5) != currentActivityWalking )
            {
                currentActivityWalking = fDistribution[0] > 0.5;

                if (currentActivityWalking == true)
                {
                    //check for the location and compare it with the saved home location
                    Location currentLocation = locationWidget.getLastBestLocation();

                    float distance = homeLocation.distanceTo(currentLocation);
                    if (distance < 1000) //1 km away from home
                    {
                        mainActivity.userLeaving(distance);
                    }

                    //check for calendar data
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
