package com.sam.fivehundredmeters.network


import com.sam.fivehundredmeters.models.location.NearByLocationResponse
import com.sam.fivehundredmeters.models.location.Venue
import com.sam.fivehundredmeters.models.photo.PhotoItem
import com.sam.fivehundredmeters.models.photo.Photos
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function
import io.reactivex.internal.operators.observable.ObservableFromIterable

class LocationRepo(private val locationApi: LocationApi) {

    fun getLocations(
        client_id: String,
        client_secret: String,
        lnglat: String
    ): Observable<List<Venue>>? {
        return locationApi.getLocations(client_id, client_secret, lnglat).map(object :
            Function<NearByLocationResponse?, List<Venue>> {
            override fun apply(it: NearByLocationResponse): List<Venue> {
                val listOfVenues = ArrayList<Venue>()
                it.response.groups.get(0).items.forEach {
                    listOfVenues.add(it.venue)
                }
                return listOfVenues
            }
        })

    }


    fun getPhotos(
        id: String,
        client_id: String,
        client_secret: String
    ): Observable<List<PhotoItem>>? {
        return locationApi.getPhotos(id,client_id, client_secret).map { it ->
            val listOfPhotos = ArrayList<PhotoItem>()
            it.response.photos.items.forEach {
                listOfPhotos.add(it)
            }
            listOfPhotos
        }

    }


    // Injecting Photos into Venues Before Showing Them
    fun getVenues(
        client_id: String,
        client_secret: String,
        listOfVenueObs:Observable<List<Venue>>
    ): Observable<List<Venue>>? {
        val listOfVenues = arrayListOf<Venue>()
        return listOfVenueObs.doOnNext {
            it.forEach {
                val venueObs = Observable.just(it)

                val venue = Observable.zip(venueObs, venueObs.flatMap(object :
                    Function<Venue, Observable<List<PhotoItem>>> {
                    override fun apply(t: Venue): Observable<List<PhotoItem>>? {
                        return getPhotos(t.id, client_id, client_secret)
                    }
                }), object : BiFunction<Venue, List<PhotoItem>, Venue> {
                    override fun apply(t1: Venue, t2: List<PhotoItem>): Venue {
                        if (t2.size > 0)
                            t1.imageUrl = t2.get(0).prefix + "1920x1080" + t2.get(0).suffix
                        return t1
                    }

                }
                )
                venue.map {
                    listOfVenues.add(it)}
            }
        }

//
//        return Observable.zip(listOfVenueObs, venuesObs?.flatMap(object :
//            Function<Venue, Observable<List<PhotoItem>>> {
//            override fun apply(t: Venue): Observable<List<PhotoItem>> {
//                return locationApi.getPhotos(t.id, client_id, client_secret).map {
//                    it.response.photos.items
//                }
//            }
//        }), object : BiFunction<Venue, List<PhotoItem>, Venue> {
//            override fun apply(t1: Venue, t2: List<PhotoItem>): Venue {
//                if (t2.size > 0)
//                    t1.imageUrl = t2.get(0).prefix + "1920x1080" + t2.get(0).suffix
//                return t1
//            }
//
//        }
//        ).toList()
    }

}