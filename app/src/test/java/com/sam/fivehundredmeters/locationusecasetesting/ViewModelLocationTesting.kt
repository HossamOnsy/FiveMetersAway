package com.sam.fivehundredmeters.locationusecasetesting

import android.content.Context
import com.sam.fivehundredmeters.viewmodel.MainViewModel
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.android.gms.maps.model.LatLng
import com.sam.fivehundredmeters.BuildConfig
import com.sam.fivehundredmeters.MyApplication.Companion.appContext
import com.sam.fivehundredmeters.models.location.Venue
import com.sam.fivehundredmeters.network.LocationRepo
import com.sam.fivehundredmeters.utils.LocationUtils
import com.sam.fivehundredmeters.utils.NetworkUtils
import org.junit.*
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mock

@RunWith(MockitoJUnitRunner::class)
class ViewModelLocationTesting : KoinTest {

    lateinit var mainViewModel: MainViewModel
    lateinit var mv: MainViewModel
    val locationRepo: LocationRepo by inject()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun onSetup() {
        appContext = mock(Context::class.java)
        startKoin {
            androidContext(appContext)
            loadKoinModules(module {
                single { NetworkUtils.createWebService() }
                single { LocationRepo(locationApi = get()) }
                viewModel { MainViewModel(get()) }
                single { LocationUtils() }
            })
        }
        mainViewModel = MainViewModel(locationRepo)
        mv = mock(MainViewModel::class.java)

    }

    @Test
    fun testCallBetweenViewModelAndRepository() {
        val lat = 35.0
        val long = 34.0
        `when`(mv.initiateGetLocations(LatLng(lat, long))).then {
            val arrayList = ArrayList<Venue>()
            arrayList.add(Venue(name = "Hello 1"))
            arrayList.add(Venue(name = "Hello 2"))
            arrayList.add(Venue(name = "Hello 3"))
            mainViewModel.resultMLD.value = arrayList
            null
        }

        mv.initiateGetLocations(LatLng(lat, long))
        // Has received data
        Assert.assertNotNull(mainViewModel.resultMLD.value)
        Assert.assertTrue(mainViewModel.resultMLD.value!!.size == 3)

    }

    @After
    fun stop() {
        stopKoin()
    }
}