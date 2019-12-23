package com.sam.fivehundredmeters.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import com.sam.fivehundredmeters.MyApplication.Companion.appContext

class LocationUtils {


     fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

     fun requestPermissionsUtil(context:Context) {
         requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_REQUEST_CODE_PERMISSION
        )
    }

    fun measure(latitude1: Double, longitude1: Double, latitude2:Double, longitude2:Double):Double{  // generally used geo measurement function
        val radius = 6378.137; // Radius of earth in KM
        val dlatitude = latitude2 * Math.PI / 180 - latitude1 * Math.PI / 180;
        val dlongitude = longitude2 * Math.PI / 180 - longitude1 * Math.PI / 180;
        val area = Math.sin(dlatitude/2) * Math.sin(dlatitude/2) +
                Math.cos(latitude1 * Math.PI / 180) * Math.cos(latitude2 * Math.PI / 180) *
                Math.sin(dlongitude/2) * Math.sin(dlongitude/2);
        val circumference = 2 * Math.atan2(Math.sqrt(area), Math.sqrt(1-area));
        val ditance = radius * circumference;
        return ditance * 1000; // meters
    }

}