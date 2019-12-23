package com.sam.fivehundredmeters.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.sam.fivehundredmeters.R
import com.sam.fivehundredmeters.models.enum.ModeType
import com.sam.fivehundredmeters.utils.SharedPrefUtils.getMode
import com.sam.fivehundredmeters.utils.SharedPrefUtils.setMode
import com.sam.fivehundredmeters.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject


class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by inject()

    private lateinit var locationAdapter: LocationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initiateRecyclerView()
        initMode()
        setObservers()
        setClickListeners()

    }

    private fun initiateRecyclerView() {
        // initializing catAdapter with empty list
        locationAdapter = LocationAdapter(ArrayList())
        // apply allows you to alter variables inside the object and assign them
        LocationRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        LocationRecyclerView.adapter = locationAdapter
    }


    fun initMode() {
        textChosenMode.text = getMode()
    }

    private fun setObservers() {
        mainViewModel.exception.observe(this, Observer { ExpMessage ->
            Toast.makeText(this, ExpMessage, Toast.LENGTH_SHORT).show()
        })

        mainViewModel.resultMLD.observe(this, Observer { listOfVenues ->
            locationAdapter.updatelist(listOfVenues)
        })

        mainViewModel.listOfVenuesWithPhotosMLD.observe(this, Observer { listOfVenues ->
            locationAdapter.updatelist(listOfVenues)
        })

        mainViewModel.photoOfVenueFetched.observe(this, Observer { venue ->
            locationAdapter.updateElement(venue)
        })



        mainViewModel.loading.observe(this, Observer { visibility ->
            mainProgressBar.visibility = visibility
        })

        mainViewModel.getLastLocation(this)
    }

    fun setClickListeners() {
        textChosenMode.setOnClickListener {
            when (textChosenMode.text) {
                ModeType.REALTIME.value -> {
                    updateMode(ModeType.SINGLE_UPDATE.value)
                    mainViewModel.stopUpdating()
                }
                else -> {
                    updateMode(ModeType.REALTIME.value)
                    mainViewModel.startUpdating()
                }
            }
        }
    }

    private fun updateMode(modeType: String) {
        setMode(modeType)
        textChosenMode.text = modeType
    }

}




