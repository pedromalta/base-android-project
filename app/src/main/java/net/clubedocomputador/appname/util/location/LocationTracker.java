package net.clubedocomputador.appname.util.location;

import android.content.Context;
import android.location.Location;
import android.os.Looper;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.clubedocomputador.appname.data.model.GPSPoint;
import net.clubedocomputador.appname.injection.ApplicationContext;
import net.clubedocomputador.appname.util.AppLogger;

/**
 * * Uses Google Play API for obtaining device locations
 * * Created by alejandro.tkachuk
 * * alejandro@calculistik.com
 * * www.calculistik.com Mobile Development
 */

@Singleton
public class LocationTracker {

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 0;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 0;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private Workable<GPSPoint> workable;

    @Inject
    public LocationTracker(@ApplicationContext Context context) {
        this.locationRequest = new LocationRequest();
        this.locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        this.locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        //Check and enforce High Accuracy

        /**
         * it is possible to get the device's current location mode since API level 19 (Kitkat):

         public int getLocationMode(Context context)
         {
         return Settings.Secure.getInt(activityUnderTest.getContentResolver(), Settings.Secure.LOCATION_MODE);

         }
         These are the possible return values (see here):

         0 = LOCATION_MODE_OFF
         1 = LOCATION_MODE_SENSORS_ONLY
         2 = LOCATION_MODE_BATTERY_SAVING
         3 = LOCATION_MODE_HIGH_ACCURACY

         So you want something like

         if(getLocationMode(context) == 3)
         {
         // do stuff
         }
         Unfortunately you can't set the location mode programmatically but you can send the user directly to the settings screen where he can do that:

         startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
         */


        this.locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(this.locationRequest);
        this.locationSettingsRequest = builder.build();

        this.locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult); // why? this. is. retarded. Android.
                Location currentLocation = locationResult.getLastLocation();

                GPSPoint gpsPoint = new GPSPoint(
                        currentLocation.getLatitude(),
                        currentLocation.getLongitude(),
                        currentLocation.getSpeed(),
                        currentLocation.getAccuracy());
                AppLogger.d("GPSPoint Callback results: " + gpsPoint);
                if (null != workable)
                    workable.work(gpsPoint);
            }
        };

        this.mFusedLocationClient = LocationServices
                .getFusedLocationProviderClient(context);

        try {
            this.mFusedLocationClient.requestLocationUpdates(
                    this.locationRequest,
                    this.locationCallback, Looper.myLooper());
        } catch (SecurityException e){
            //TODO handle permission exception
            AppLogger.e("LocationTracker dont have permission!!");
        }
    }


    public void onChange(Workable<GPSPoint> workable) {
        this.workable = workable;
    }

    public LocationSettingsRequest getLocationSettingsRequest() {
        return this.locationSettingsRequest;
    }

    public void stop() {
        AppLogger.d("LocationTracker.stop() Stopping location tracking");
        this.mFusedLocationClient.removeLocationUpdates(this.locationCallback);
    }

}
