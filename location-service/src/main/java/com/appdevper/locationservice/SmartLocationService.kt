package  com.appdevper.locationservice

import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.LocationRequest

class SmartLocationService(private val mActivity: Activity, lifecycle: Lifecycle, callBack: LocationCallBack) :
    LifecycleObserver {

    private var mLocationPermissionGranted: Boolean = false
    // A reference to the service used to get location updates.
    private var mService: LocationService? = null
    private var locationRequest: LocationRequest? = null

    // Tracks the bound state of the service.
    private var mBound = false
    private var myReceiver: MyReceiver

    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocationService.LocalBinder
            mService = binder.service

            if (currentClass != null) {
                mService?.setCurrentClass(currentClass)
            } else {
                mService?.setCurrentClass(mActivity.javaClass)
            }

            mBound = true
            if (mLocationPermissionGranted) {
                mService!!.requestLocationUpdates(locationRequest)
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mService = null
            mBound = false
        }
    }

    init {
        lifecycle.addObserver(this)
        myReceiver = MyReceiver(callBack)
        getLocationPermission()
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(myReceiver, IntentFilter(LocationService.ACTION_BROADCAST))
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun start() {
        mActivity.bindService(Intent(mActivity, LocationService::class.java), mServiceConnection, Context.BIND_AUTO_CREATE)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun stop() {
        if (mBound) {
            mActivity.unbindService(mServiceConnection)
            mBound = false
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun destroy() {
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(myReceiver)
        val intent = Intent(mActivity, LocationService::class.java)
        intent.putExtra(LocationService.EXTRA_STARTED_FROM_NOTIFICATION, true)
        mActivity.startService(intent)
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                when {
                    grantResults.isEmpty() -> // If user interaction was interrupted, the permission request is cancelled and you
                        Log.i(TAG, "User interaction was cancelled.")
                    grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                        // Permission was granted.
                        mLocationPermissionGranted = true
                        mService!!.requestLocationUpdates(locationRequest)
                    }
                    else -> {

                    }
                }
            }
        }
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(mActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                mActivity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    fun setCurrentClass(current: Class<*>?) {
        currentClass = current
    }

    fun setLocationRequest(locationRequest: LocationRequest) {
        this.locationRequest = locationRequest
    }

    companion object {
        private val TAG = SmartLocationService::class.java.simpleName
        private var currentClass: Class<*>? = null
        const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 248
    }

    interface LocationCallBack {
        fun onLocationResult(location: Location?)
    }

    private class MyReceiver(val callBack: LocationCallBack) : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val location: Location = intent!!.getParcelableExtra(LocationService.EXTRA_LOCATION)
            callBack.onLocationResult(location)
        }
    }
}
