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
import com.sam.fivehundredmeters.models.location.NearByLocationResponse
import com.sam.fivehundredmeters.models.location.Venue
import com.sam.fivehundredmeters.network.LocationRepo
import com.sam.fivehundredmeters.utils.LocationUtils
import com.sam.fivehundredmeters.utils.SharedPrefUtils.getLatLong
import com.sam.fivehundredmeters.utils.SharedPrefUtils.setLatLong
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*


class MainViewModel(private val locationRepo: LocationRepo) : ViewModel() {
    val resultMLD = MutableLiveData<List<Venue>>()
    var listOfVenuesMLD = mutableListOf<Venue>()
    val exception = MutableLiveData<String>()
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

    fun startUpdating(){
        requestNewLocationData()
    }

    fun stopUpdating(){
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
    }

    private fun initiateGetLocations(latLng: LatLng) {
        val latlongformat = String.format(
            "%s,%s", latLng.latitude
                .toString(), latLng.longitude
        )

        disposable =
            locationRepo.getVenues(BuildConfig.CLIENTID, BuildConfig.CLIENTSECRET, latlongformat)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(
                    { result ->

                        resultMLD.value = result
                        listOfVenuesMLD = result
                        Log.v("Near me", "" + result)
//                        this@MainViewModel.result.value = result
                    },
                    { error ->
                        Log.e("ERROR", error.message)
                        exception.value = error.toString()
                    }
                )
    }

//    fun getUsersWithTheirTweets(): Observable<List<User>?>? {
//        val usersObs: Observable<User> =
//            briteDb.createQuery("user", "SELECT * FROM user")
//                .map(object : Func1<SqlBrite.Query?, List<User?>?>() {
//                    fun call(query: SqlBrite.Query): List<User>? {
//                        val cursor: Cursor = query.run()
//                        val result: MutableList<User> =
//                            ArrayList(cursor.getCount())
//                        while (cursor.moveToNext()) {
//                            val user: User = UserTable.parseCursor(cursor)
//                            result.add(user)
//                        }
//                        cursor.close()
//                        return result
//                    }
//                }) // transform Observable<List<User>> into Observable<User>
//                .flatMap(object :
//                    Func1<List<User?>?, Observable<User?>?>() {
//                    fun call(users: List<User?>?): Observable<User?>? {
//                        return Observable.from(users)
//                    }
//                })
//    return Observable.zip(usersObs, usersObs.flatMap(new Func1<User, Observable<List<Tweet>>>() {
//        @Override
//        public Observable<List<Tweet>> call(User user) {
//            return tweetDao.getTweetsByUser(user);
//        }
//    }), new Func2<User, List<Tweet>, User>() {
//        @Override
//        public User call(User user, List<Tweet> tweets) {
//            user.tweets = tweets;
//            return user;
//        }
//    }).toList();
//    }


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

//            val toast = Toast.makeText(appContext, distance.toString(), Toast.LENGTH_SHORT)
//            toast.show()

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





