package com.sam.fivehundredmeters.di

import com.sam.fivehundredmeters.network.LocationRepo
import com.sam.fivehundredmeters.utils.LocationUtils
import com.sam.fivehundredmeters.utils.NetworkUtils.createWebService
import com.sam.fivehundredmeters.viewmodel.MainViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val koinModule = module {

    single { createWebService() }
    single { LocationRepo(locationApi = get()) }
    viewModel { MainViewModel( get()) }
    single { LocationUtils() }

}
