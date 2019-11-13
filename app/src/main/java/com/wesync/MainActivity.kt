package com.wesync


import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProviders
import com.wesync.connection.ConnectionManagerService
import com.wesync.metronome.MetronomeService
import com.wesync.util.ServiceSubscriber

class MainActivity : AppCompatActivity() {

    private lateinit var sharedViewModel: SharedViewModel
    private val serviceSubscriber = ServiceSubscriber(this, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        checkForPermission()
        MetronomeService.start(applicationContext)
        ConnectionManagerService.start(applicationContext)
        /*
            TODO: REMINDER. Start first, then subscribe.
                 if you do the other way round, service will prematurely stopped unintentionally.
        */
       // serviceSubscriber.subscribe()



        sharedViewModel = ViewModelProviders.of(this,
            SavedStateViewModelFactory(this.application,this)).get(SharedViewModel::class.java)
    }

    override fun onDestroy() {
        MetronomeService.stop(applicationContext)
        ConnectionManagerService.stop(applicationContext)
        super.onDestroy()
    }

    private fun checkForPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        }
    }
}
