package com.appdevper.locationservice.example

import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appdevper.locationservice.SmartLocationService
import timber.log.Timber

class MainActivity : AppCompatActivity(), SmartLocationService.LocationCallBack {

    private lateinit var smartLocation: SmartLocationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        smartLocation = SmartLocationService(this, lifecycle, this)
    }

    override fun onLocationResult(location: Location?) {
        Timber.i("Location: $location")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        smartLocation.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
