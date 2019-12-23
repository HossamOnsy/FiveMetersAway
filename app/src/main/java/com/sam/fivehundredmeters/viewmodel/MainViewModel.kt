package com.sam.fivehundredmeters.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.sam.fivehundredmeters.BuildConfig
import com.sam.fivehundredmeters.MyApplication.Companion.appContext
import com.sam.fivehundredmeters.models.location.Venue
import com.sam.fivehundredmeters.models.photo.PhotoItem
import com.sam.fivehundredmeters.network.LocationRepo
import com.sam.fivehundredmeters.utils.LocationUtils
import com.sam.fivehundredmeters.utils.SharedPrefUtils.getLatLong
import com.sam.fivehundredmeters.utils.SharedPrefUtils.setLatLong
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers


class MainViewModel(private val locationRepo: LocationRepo) : ViewModel() {
    val resultMLD = MutableLiveData<List<Venue>>()
    var listOfVenuesWithPhotosMLD = MutableLiveData<List<Venue>>()
    var photoOfVenueFetched = MutableLiveData<Venue>()
    val exception = MutableLiveData<String>()
    val loading = MutableLiveData<Int>()
    var disposable: Disposable? = null
    private val locationUtils: LocationUtils = LocationUtils()
    var mFusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(appContext)


    fun getLastLocation(context: Context) {
        if (locationUtils.checkPermissions()) {
            if (locationUtils.isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(context as Activity) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        val latestLatLng = LatLng(location.latitude, location.longitude)
                        setLatLong(latestLatLng)
                        initiateGetLocations(latestLatLng)
                    }
                }
            } else {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                context.startActivity(intent)
            }
        } else {
            locationUtils.requestPermissionsUtil(context as Activity)
        }
    }

    fun startUpdating() {
        requestNewLocationData()
    }

    fun stopUpdating() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
    }

    private fun initiateGetLocations(latLng: LatLng) {
        val latlongformat = String.format(
            "%s,%s", latLng.latitude
                .toString(), latLng.longitude
        )

        disposable =
            locationRepo.getLocations(BuildConfig.CLIENTID, BuildConfig.CLIENTSECRET, latlongformat)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.doOnSubscribe { loading.value = View.VISIBLE }
                ?.doOnComplete { loading.value = View.GONE }
                ?.subscribe(
                    { result ->

                        resultMLD.value = result
                        goCheckPhotos(result)
//                        listOfVenuesMLD = result
                        Log.v("Near me", "" + result)
//                        this@MainViewModel.result.value = result
                    },
                    { error ->
                        Log.e("ERROR", error.message)
                        exception.value = error.toString()
                    }
                )
    }

    private fun goCheckPhotos(listOfVenueObs: List<Venue>) {

        listOfVenueObs.forEach {
            val venueObs = Observable.just(it)
            val venue =
                Observable.zip(venueObs, venueObs.flatMap(object :
                    Function<Venue, Observable<List<PhotoItem>>> {
                    override fun apply(t: Venue): Observable<List<PhotoItem>>? {
                        return locationRepo.getPhotos(
                            t.id,
                            BuildConfig.CLIENTID,
                            BuildConfig.CLIENTSECRET
                        )
                    }
                }), object : BiFunction<Venue, List<PhotoItem>, Venue> {
                    override fun apply(t1: Venue, t2: List<PhotoItem>): Venue {
                        if (t2.size > 0)
                            t1.imageUrl = t2.get(0).prefix + "1920x1080" + t2.get(0).suffix
                        return t1
                    }

                }
                )
            venue
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { loading.value = View.VISIBLE }
                .doFinally {
                    loading.value = View.GONE

                }
                .subscribe(
                    { result ->
                        photoOfVenueFetched.value = result
                        Log.v("Near me", "" + result)
                    },
                    { error ->
                        Log.e("ERROR", error.message)
                        exception.value = error.toString()
                    }
                )
        }
    }


    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 2000

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }


    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            val latLong = getLatLong()
            val distance = locationUtils.measure(
                latLong.latitude,
                latLong.longitude,
                mLastLocation.latitude,
                mLastLocation.longitude
            )
            Log.v("mLocationCallback", "Entered onLocationResult")

            if (distance >= 500.0) {
                setLatLong(LatLng(mLastLocation.latitude, mLastLocation.longitude))
                initiateGetLocations(LatLng(mLastLocation.latitude, mLastLocation.longitude))
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        disposable!!.dispose()
    }


}





