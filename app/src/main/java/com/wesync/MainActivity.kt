package com.wesync


import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.wesync.connection.ConnectionManagerService
import com.wesync.metronome.MetronomeService
import com.wesync.util.service.ServiceSubscriber

class MainActivity : AppCompatActivity() {

    private lateinit var sharedViewModel: SharedViewModel
    private val serviceSubscriber = ServiceSubscriber(this, this)

    companion object {
        const val MTS_CON = "metronomeConnected"
        const val CNS_CON = "connectionServiceConnected"
        var metronomeIsAlive = false
        var connectionIsAlive = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        checkForPermission()
        sharedViewModel = ViewModelProviders.of(this)
            .get(SharedViewModel::class.java)
    }

    override fun onRestart() {
        sharedViewModel.printValues()
        super.onRestart()

    }

    override fun onStart() {
        if (!metronomeIsAlive) MetronomeService.start(applicationContext)
        if (!connectionIsAlive) ConnectionManagerService.start(applicationContext)
        serviceSubscriber.subscribe()
        super.onStart()
    }

    override fun onStop() {
        serviceSubscriber.unsubscribe()
        super.onStop()
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

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        //Log.d("savedinstance","onSaveInstance Called!")
        val currState = sharedViewModel.getConfig()
            savedInstanceState.putLong(SharedViewModel.BPM_KEY,currState.bpm)
            savedInstanceState.putBoolean(SharedViewModel.USER_TYPE_KEY,currState.isPlaying)
            savedInstanceState.putString(SharedViewModel.SESSION_KEY,currState.session)
            savedInstanceState.putString(SharedViewModel.USER_TYPE_KEY,currState.userTypeString)
            savedInstanceState.putBoolean(CNS_CON,serviceSubscriber.connServiceConnected.value?: false)
            savedInstanceState.putBoolean(MTS_CON,serviceSubscriber.metronomeConnected.value?: false)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        //Log.d("savedinstance","onRestoreInstance Called!")
        sharedViewModel.unpackBundle(savedInstanceState)
        metronomeIsAlive = savedInstanceState.getBoolean(MTS_CON)
        connectionIsAlive = savedInstanceState.getBoolean(CNS_CON)
    }

    override fun onBackPressed() {
      //  if (sharedViewModel.isPlaying.value!!)
        //    moveTaskToBack(true)
       // else
            super.onBackPressed()
    }
}
