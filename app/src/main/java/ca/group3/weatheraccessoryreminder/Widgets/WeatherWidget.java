package ca.group3.weatheraccessoryreminder.Widgets;

import android.os.AsyncTask;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.util.JsonReader;
import android.util.Log;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ca.group3.weatheraccessoryreminder.MainActivity;
import ca.group3.weatheraccessoryreminder.Weather;

public class WeatherWidget extends AsyncTask<String, Void, String> {
    private String serverAddress =
            "http://api.openweathermap.org/data/2.5/forecast?q=Arhus,dk&cnt=1&units=metric&appid=ed10a84bb5fc776cbe29475746b5352c";
    private String location;
    private boolean isRain, isSun;
    private boolean isSuccessful = true;
    private URL containerAPIEndpoint;
    private MainActivity mainActivity;
    private ProgressDialog progressDialog;
    private Weather weather;

    public WeatherWidget(MainActivity mainActivity, String location) {
        this.mainActivity = mainActivity;
        this.location = location;
        this.progressDialog = new ProgressDialog(mainActivity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.setMessage("Connecting to server");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        //progressDialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        getWeather();
        return null;
    }

    private void getWeather() {
        try {
            containerAPIEndpoint = new URL(serverAddress);
            HttpURLConnection myConnection = (HttpURLConnection) containerAPIEndpoint.openConnection();
            myConnection.setConnectTimeout(3000);
            myConnection.setReadTimeout(3000);
            myConnection.setRequestProperty("Content-Type", "application/json");

            if (myConnection.getResponseCode() == 200) {
                InputStream responseBody = myConnection.getInputStream();
                InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                JsonReader jsonReader = new JsonReader(responseBodyReader);

                jsonReader.beginObject();
                while (jsonReader.hasNext()) {
                    String key = jsonReader.nextName();
                    if (key.equals("list")) {
                        isRain = true;
                        String list = "";
                        jsonReader.beginArray();
                        jsonReader.beginObject();
                        //Build weather object
                        weather = new Weather(true,true,false,false);
                        break;
//                        while (jsonReader.hasNext()) {
//                            String key2 = jsonReader.nextName();
//                            if (key.equals("rain")) {
//                                list = list + jsonReader.nextString() ;
//                            }
//                        }
                    } else if (key.equals("sun")) {
                        //alarmDescription = jsonReader.nextString();
                        isSun = true;
                        break;
                    } else {
                        jsonReader.skipValue();
                    }
                }
                jsonReader.close();
            } else {
                Log.e("Connection Error", myConnection.getResponseCode() + "");
                isSuccessful = false;
            }
            myConnection.disconnect();
        } catch (Exception e) {
            Log.e("ERROR getWeather", e.toString());
            isSuccessful = false;
            progressDialog.dismiss();
            final Exception ex = e;
            Handler handler =  new Handler(mainActivity.getMainLooper());
            handler.post( new Runnable(){
                public void run(){
                    Toast.makeText(mainActivity, ex.getMessage(),Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onPostExecute(String result) {
        progressDialog.dismiss();
        if (isSuccessful) {
            Toast.makeText(mainActivity, "Success",Toast.LENGTH_LONG).show();
            mainActivity.weatherDataReceived(weather);
        }
    }
}
