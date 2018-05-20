package net.clubedocomputador.appname.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;


import com.google.gson.annotations.Expose;
import com.google.maps.model.LatLng;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by pedromalta on 13/03/18.
 */

public class GPSPoint extends BaseModel implements Parcelable, Comparable<GPSPoint> {

    @Expose
    private final BigDecimal latitude;
    @Expose
    private final BigDecimal longitude;
    @Expose
    private final Date time;
    @Expose
    private final Float speed;
    @Expose
    private final Float precision;

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public Date getTime() {
        return time;
    }

    public Float getSpeed() {
        return speed;
    }

    public Float getPrecision() {
        return precision;
    }

    public LatLng getLatLng(){
        return new LatLng(latitude.doubleValue(), longitude.doubleValue());
    }

    public GPSPoint(BigDecimal latitude, BigDecimal longitude, float speed, float precision) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = new Date();
        this.speed = speed;
        this.precision = precision;
    }

    public GPSPoint(Double latitude, Double longitude, float speed, float precision) {
        this(BigDecimal.valueOf(latitude), BigDecimal.valueOf(longitude), speed, precision);

    }


    protected GPSPoint(Parcel in) {
        latitude = (BigDecimal) in.readValue(BigDecimal.class.getClassLoader());
        longitude = (BigDecimal) in.readValue(BigDecimal.class.getClassLoader());
        speed = in.readFloat();
        precision = in.readFloat();
        long tmpTime = in.readLong();
        time = tmpTime != -1 ? new Date(tmpTime) : null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(latitude);
        dest.writeValue(longitude);
        dest.writeFloat(speed);
        dest.writeFloat(precision);
        dest.writeLong(time != null ? time.getTime() : -1L);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<GPSPoint> CREATOR = new Parcelable.Creator<GPSPoint>() {
        @Override
        public GPSPoint createFromParcel(Parcel in) {
            return new GPSPoint(in);
        }

        @Override
        public GPSPoint[] newArray(int size) {
            return new GPSPoint[size];
        }
    };

    @Override
    public int compareTo(@NonNull GPSPoint o) {
        return o.time.compareTo(this.time) ;
    }
}