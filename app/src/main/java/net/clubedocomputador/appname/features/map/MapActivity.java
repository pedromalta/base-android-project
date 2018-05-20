package net.clubedocomputador.appname.features.map;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.RoadsApi;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.SnappedPoint;
import com.google.maps.model.SpeedLimit;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.LongSparseArray;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import net.clubedocomputador.appname.R;
import net.clubedocomputador.appname.data.local.InstanceHolder;
import net.clubedocomputador.appname.features.base.BaseActivity;
import net.clubedocomputador.appname.injection.component.ActivityComponent;
import net.clubedocomputador.appname.util.AppLogger;

public class MapActivity extends BaseActivity implements OnMapReadyCallback {

    /**
     * The number of points allowed per API request. This is a fixed value.
     */
    private static final int PAGE_SIZE_LIMIT = 100;
    /**
     * Define the number of data points to re-send at the start of subsequent requests. This helps
     * to influence the API with prior data, so that paths can be inferred across multiple requests.
     * You should experiment with this value for your use-case.
     */
    private static final int PAGINATION_OVERLAP = 5;
    @Inject
    InstanceHolder instanceHolder;
    List<LatLng> mCapturedLocations;
    List<SnappedPoint> mSnappedPoints;
    Map<String, SpeedLimit> mPlaceSpeeds;
    /*
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //getSupportActionBar().setTitle(R.string.title_bar_class_started_activity);

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    */
    private GoogleMap mMap;
    private GeoApiContext mContext;
    AsyncTask<Void, Void, List<SnappedPoint>> mTaskSnapToRoads =
            new AsyncTask<Void, Void, List<SnappedPoint>>() {
                @Override
                protected void onPreExecute() {
                    showLoading();
                }

                @Override
                protected List<SnappedPoint> doInBackground(Void... params) {
                    try {
                        return snapToRoads(mContext);
                    } catch (final Exception ex) {
                        toastException(ex);
                        ex.printStackTrace();
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(List<SnappedPoint> snappedPoints) {
                    mSnappedPoints = snappedPoints;
                    hideLoading();

                    //findViewById(R.id.speed_limits).setEnabled(true);

                    com.google.android.gms.maps.model.LatLng[] mapPoints =
                            new com.google.android.gms.maps.model.LatLng[mSnappedPoints.size()];
                    int i = 0;
                    LatLngBounds.Builder bounds = new LatLngBounds.Builder();
                    for (SnappedPoint point : mSnappedPoints) {
                        mapPoints[i] = new com.google.android.gms.maps.model.LatLng(point.location.lat,
                                point.location.lng);
                        bounds.include(mapPoints[i]);
                        i += 1;
                    }

                    mMap.addPolyline(new PolylineOptions().add(mapPoints).color(Color.BLUE).width(5));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 0));
                }
            };
    /**
     * Icon cache for {@link #generateSpeedLimitMarker}.
     */
    private LongSparseArray<BitmapDescriptor> mSpeedIcons = new LongSparseArray<>();
    private IconGenerator mIconGenerator;
    AsyncTask<Void, Integer, Map<String, SpeedLimit>> mTaskSpeedLimits =
            new AsyncTask<Void, Integer, Map<String, SpeedLimit>>() {
                private List<MarkerOptions> markers;

                @Override
                protected void onPreExecute() {
                    markers = new ArrayList<>();
                    showLoading();
                }

                @Override
                protected Map<String, SpeedLimit> doInBackground(Void... params) {
                    Map<String, SpeedLimit> placeSpeeds = null;
                    try {
                        placeSpeeds = getSpeedLimits(mContext, mSnappedPoints);
                        publishProgress(0, placeSpeeds.size());

                        // Generate speed limit icons, with geocoded labels.
                        Set<String> visitedPlaceIds = new HashSet<>();
                        for (SnappedPoint point : mSnappedPoints) {
                            if (!visitedPlaceIds.contains(point.placeId)) {
                                visitedPlaceIds.add(point.placeId);

                                GeocodingResult geocode = geocodeSnappedPoint(mContext, point);
                                publishProgress(visitedPlaceIds.size());

                                // As each place has been geocoded, we'll use the name of the place
                                // as the marker title, so tapping the marker will display the address.
                                markers.add(generateSpeedLimitMarker(
                                        placeSpeeds.get(point.placeId).speedLimit, point, geocode));
                            }
                        }
                    } catch (Exception ex) {
                        toastException(ex);
                        ex.printStackTrace();
                    }

                    return placeSpeeds;
                }

                @Override
                protected void onProgressUpdate(Integer... values) {
                    //TODO use it
                    /*mProgressBar.setProgress(values[0]);
                    if (values.length > 1) {
                        mProgressBar.setIndeterminate(false);
                        mProgressBar.setMax(values[1]);
                    }*/

                }

                @Override
                protected void onPostExecute(Map<String, SpeedLimit> speeds) {
                    for (MarkerOptions marker : markers) {
                        mMap.addMarker(marker);
                    }
                    hideLoading();
                    mPlaceSpeeds = speeds;
                }
            };

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
   /* @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Report report = instanceHolder.getReport();

        PolylineOptions lineOptions = new PolylineOptions().width(5).color(Color.RED);
        GPSPoint lastPoint = null;
        LatLng start = null;
        LatLng finish = null;
        List<GPSPoint> points = report.getMap();
        Collections.sort(points, (o1, o2) -> o1.getTime().compareTo(o2.getTime()));
        for (GPSPoint point:  report.getMap()){
            if (point.getPrecision() < 22 && point.getSpeed() > 3){//10.8km/h
                continue;
            }
            if (lastPoint != null) {
                //add line with last point and this point
                lineOptions.add(lastPoint.getLatLng(), point.getLatLng());
            }else {
                //if lastPoint was null then it is the start point
                start = point.getLatLng();
                finish = point.getLatLng();
            }
            lastPoint = point;
            finish = lastPoint.getLatLng();
        }

        Polyline line = mMap.addPolyline(lineOptions);
        if (start != null && finish != null && !start.equals(finish)) {
            mMap.addMarker(new MarkerOptions().position(start).title("Inicio"));
            mMap.addMarker(new MarkerOptions().position(finish).title("Fim"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 22));
        }

    }
    */
    public static Intent getStartIntent(Context context) {
        Intent intent = new Intent(context, MapActivity.class);
        return intent;
    }

    @Override
    public int getLayout() {
        return R.layout.activity_maps;
    }

    @Override
    protected void inject(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @Override
    protected void attachView() {

    }

    @Override
    protected void detachPresenter() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mContext = new GeoApiContext.Builder()
                .apiKey(getString(R.string.google_roads_key))
                .build();

        //Data data = instanceHolder.getData();
        mCapturedLocations = new ArrayList<>();

        //for (GPSPoint points : report.getMap()) {
        //    mCapturedLocations.add(points.getLatLng());
        //}

    }


    public void plotRawData() {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        PolylineOptions polyline = new PolylineOptions();

        for (LatLng ll : mCapturedLocations) {
            com.google.android.gms.maps.model.LatLng mapPoint =
                    new com.google.android.gms.maps.model.LatLng(ll.lat, ll.lng);
            builder.include(mapPoint);
            polyline.add(mapPoint);
        }

        mMap.addPolyline(polyline.color(Color.RED).width(5));
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 0));

    }

    /**
     * Snaps the points to their most likely position on roads using the Roads API.
     */
    private List<SnappedPoint> snapToRoads(GeoApiContext context) throws Exception {
        List<SnappedPoint> snappedPoints = new ArrayList<>();

        int offset = 0;
        while (offset < mCapturedLocations.size()) {
            // Calculate which points to include in this request. We can't exceed the APIs
            // maximum and we want to ensure some overlap so the API can infer a good location for
            // the first few points in each request.
            if (offset > 0) {
                offset -= PAGINATION_OVERLAP;   // Rewind to include some previous points
            }
            int lowerBound = offset;
            int upperBound = Math.min(offset + PAGE_SIZE_LIMIT, mCapturedLocations.size());

            // Grab the data we need for this page.
            LatLng[] page = mCapturedLocations
                    .subList(lowerBound, upperBound)
                    .toArray(new LatLng[upperBound - lowerBound]);

            // Perform the request. Because we have interpolate=true, we will get extra data points
            // between our originally requested path. To ensure we can concatenate these points, we
            // only start adding once we've hit the first new point (i.e. skip the overlap).
            SnappedPoint[] points = RoadsApi.snapToRoads(context, true, page).await();
            boolean passedOverlap = false;
            for (SnappedPoint point : points) {
                if (offset == 0 || point.originalIndex >= PAGINATION_OVERLAP) {
                    passedOverlap = true;
                }
                if (passedOverlap) {
                    snappedPoints.add(point);
                }
            }

            offset = upperBound;
        }

        return snappedPoints;
    }

    public void snapRawDataToRoads() {
        AppLogger.d("Exacutando");
        mTaskSnapToRoads.execute();
    }

    /**
     * Retrieves speed limits for the previously-snapped points. This method is efficient in terms
     * of quota usage as it will only query for unique places.
     * <p>
     * Note: Speed Limit data is only available with an enabled Maps for Work API key.
     */
    private Map<String, SpeedLimit> getSpeedLimits(GeoApiContext context, List<SnappedPoint> points)
            throws Exception {
        Map<String, SpeedLimit> placeSpeeds = new HashMap<>();

        // Pro tip: save on quota by filtering to unique place IDs
        for (SnappedPoint point : points) {
            placeSpeeds.put(point.placeId, null);
        }

        String[] uniquePlaceIds =
                placeSpeeds.keySet().toArray(new String[placeSpeeds.keySet().size()]);

        // Loop through the places, one page (API request) at a time.
        for (int i = 0; i < uniquePlaceIds.length; i += PAGE_SIZE_LIMIT) {
            String[] page = Arrays.copyOfRange(uniquePlaceIds, i,
                    Math.min(i + PAGE_SIZE_LIMIT, uniquePlaceIds.length));

            // Execute!
            SpeedLimit[] placeLimits = RoadsApi.speedLimits(context, page).await();
            for (SpeedLimit sl : placeLimits) {
                placeSpeeds.put(sl.placeId, sl);
            }
        }

        return placeSpeeds;
    }

    /**
     * Geocodes a Snapped Point using the Place ID.
     */
    private GeocodingResult geocodeSnappedPoint(GeoApiContext context, SnappedPoint point) throws Exception {
        GeocodingResult[] results = GeocodingApi.newRequest(context)
                .place(point.placeId)
                .await();

        if (results.length > 0) {
            return results[0];
        }
        return null;
    }

    /**
     * Handles the Speed Limit button-click event, running the demo snippets {@link #getSpeedLimits}
     * and {@link #geocodeSnappedPoint} behind a progress dialog.
     */
    public void showSpeedLimit() {
        mTaskSpeedLimits.execute();
    }

    /**
     * Generates a marker that looks like a speed limit sign.
     */
    private MarkerOptions generateSpeedLimitMarker(double speed, SnappedPoint point,
                                                   GeocodingResult geocode) {
        if (mIconGenerator == null) {
            mIconGenerator = new IconGenerator(getApplicationContext());
            mIconGenerator
                    .setContentView(getLayoutInflater().inflate(R.layout.speed_limit_view, null));
            mIconGenerator.setBackground(null);
        }

        // Cache icons.
        long speedLabel = Math.round(speed);
        BitmapDescriptor icon = mSpeedIcons.get(speedLabel);
        if (icon == null) {
            icon = BitmapDescriptorFactory
                    .fromBitmap(mIconGenerator.makeIcon(String.valueOf(speedLabel)));
            mSpeedIcons.put(speedLabel, icon);
        }

        return new MarkerOptions()
                .icon(icon)
                .position(new com.google.android.gms.maps.model.LatLng(
                        point.location.lat, point.location.lng))
                .flat(true)
                .title(geocode != null
                        ? geocode.formattedAddress
                        : point.placeId);
    }

    /**
     * Helper for toasting exception messages on the UI thread.
     */
    private void toastException(final Exception ex) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLoadedCallback(() -> {
            plotRawData();
            snapRawDataToRoads();
        });

    }


}
