package com.wesync


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.wesync.connection.ConnectionManagerService
import com.wesync.metronome.MetronomeService
import com.wesync.util.service.ServiceSubscriber


class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var toolbar: ActionBar

    companion object {
        const val MTS_CON = "metronomeConnected"
        const val CNS_CON = "connectionServiceConnected"
        var metronomeIsAlive = false
        var connectionIsAlive = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSupportActionBar()
        checkForPermission()
        startServices()
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        this.lifecycle.addObserver(mainViewModel)
        mainViewModel.unpackBundle(savedInstanceState)
    }
    override fun onStart() {
        startServices()
        super.onStart()
    }
    override fun onStop() {
        if (!mainViewModel.isPlaying.value!!) {
            Log.d("onStop","stopping")
            stopServices()
        }
        super.onStop()
    }
    override fun onDestroy() {
        stopServices()
        super.onDestroy()
    }
    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(getCurrentState())
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mainViewModel.unpackBundle(savedInstanceState)
        metronomeIsAlive = savedInstanceState.getBoolean(MTS_CON)
        connectionIsAlive = savedInstanceState.getBoolean(CNS_CON)
    }
    override fun onBackPressed() {
        val id = findNavController(R.id.fragmentNavHost).currentDestination?.id
        if (id == R.id.connectionFragment) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Are you sure you want to stop finding session?")
            builder.setPositiveButton("OK") { _, _ ->
                super.onBackPressed()
            }
            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            if (id == R.id.connectionFragment) builder.show()
        } else {
            super.onBackPressed()
        }
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
        b.putBoolean(CNS_CON,mainViewModel.subscriber.connServiceConnected.value?: false)
        b.putBoolean(MTS_CON,mainViewModel.subscriber.metronomeConnected.value?: false)
        return b
    }
    private fun initSupportActionBar() {
        setContentView(R.layout.main_activity)
        setSupportActionBar(findViewById(R.id.toolbar))
        toolbar = supportActionBar!!
    }
    private fun startServices() {
        if (!metronomeIsAlive) MetronomeService.start(applicationContext)
        if (!connectionIsAlive) ConnectionManagerService.start(applicationContext)
    }
    private fun stopServices() {
        MetronomeService.stop(applicationContext)
        ConnectionManagerService.stop(applicationContext)
    }
}
