package com.luoyang.location_h5_vs_android

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.activity.ComponentActivity

internal class WebAppInterface(var mContext: Context, webView: WebView) {
    public val name = "NavLocation"
    private val locationManager: LocationManager =
        mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val locationListener: LocationListener
    private var isRequest = false

    /**
     * Instantiate the interface and set the context
     */
    init {
        locationListener = LocationListener { location ->
            (mContext as ComponentActivity).runOnUiThread {
                val url = ("javascript:getLocationOfNavResult([" + location.longitude
                        + "," + location.latitude + "," + location.accuracy + "])")
                webView.loadUrl(url)
            }
        }
    }

    @SuppressLint("MissingPermission")
    @JavascriptInterface
    fun watchPosition() {
        if (!isRequest) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0f,
                locationListener
            )
            isRequest = true
        }
    }

    @JavascriptInterface
    fun clearWatch() {
        locationManager.removeUpdates(locationListener)
        isRequest = false
    }
}