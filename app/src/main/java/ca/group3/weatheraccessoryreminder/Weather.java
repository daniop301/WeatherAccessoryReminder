package ca.group3.weatheraccessoryreminder;

/**
 * Created by DanielP on 05-Dec-17.
 */

public class Weather {
    public boolean isRain, isWind, isSun, isSnow;

    public Weather(boolean isRain, boolean isWind, boolean isSun , boolean isSnow) {
        this.isRain = isRain;
        this.isWind = isWind;
        this.isSun = isSun;
        this.isSnow = isSnow;
    }
}
