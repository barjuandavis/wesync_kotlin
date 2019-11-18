package com.wesync


import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.wesync.connection.ConnectionManagerService
import com.wesync.metronome.MetronomeService
import com.wesync.util.service.ServiceSubscriber

class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel
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
        startServices()
        mainViewModel = ViewModelProvider.AndroidViewModelFactory.
            getInstance(this.application).create(MainViewModel::class.java)
        mainViewModel.unpackBundle(savedInstanceState)
    }

    override fun onStart() {
        startServices()
        serviceSubscriber.subscribe()
        super.onStart()
    }

    override fun onPause() {
        super.onPause()
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

    private fun getCurrentState(): Bundle {
        val b = Bundle()
        val currState = mainViewModel.getConfig()
            b.putLong(MainViewModel.BPM_KEY,currState.bpm)
            b.putBoolean(MainViewModel.USER_TYPE_KEY,currState.isPlaying)
            b.putString(MainViewModel.SESSION_KEY,currState.session)
            b.putString(MainViewModel.USER_TYPE_KEY,currState.userTypeString)
            b.putBoolean(CNS_CON,serviceSubscriber.connServiceConnected.value?: false)
            b.putBoolean(MTS_CON,serviceSubscriber.metronomeConnected.value?: false)
        return b
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        //Log.d("savedinstance","onSaveInstance Called!")
        super.onSaveInstanceState(getCurrentState())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mainViewModel.unpackBundle(savedInstanceState)
        metronomeIsAlive = savedInstanceState.getBoolean(MTS_CON)
        connectionIsAlive = savedInstanceState.getBoolean(CNS_CON)
    }

    private fun startServices() {
        if (!metronomeIsAlive) MetronomeService.start(applicationContext)
        if (!connectionIsAlive) ConnectionManagerService.start(applicationContext)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
