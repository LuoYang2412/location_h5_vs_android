package com.luoyang.locationtest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

class WebAppInterface {
    Context mContext;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location mLocation;
    private Boolean isRequest = false;

    /**
     * Instantiate the interface and set the context
     */
    WebAppInterface(Context c, WebView webView) {
        mContext = c;
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                ((AppCompatActivity) mContext).runOnUiThread(() -> {
                    String url = "javascript:getLocationOfNavResult([" + location.getLongitude()
                            + "," + location.getLatitude() + "," + location.getAccuracy() + "])";
                    webView.loadUrl(url);
                });
            }
        };
    }

    /**
     * Show a toast from the web page
     */
    @SuppressLint("MissingPermission")
    @JavascriptInterface
    public void watchPosition() {
        if (!isRequest) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            isRequest = true;
        }
    }

    @JavascriptInterface
    public void clearWatch() {
        locationManager.removeUpdates(locationListener);
        isRequest = false;
    }
}
