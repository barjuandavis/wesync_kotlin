package com.wesync


import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.wesync.connection.ConnectionManagerService
import com.wesync.metronome.MetronomeService
import com.wesync.util.ServiceSubscriber
import com.wesync.util.Tempo
import com.wesync.util.UserTypes

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
        /*
            TODO: REMINDER. Start first, then subscribe.
                 if you do the other way round, service will prematurely stopped unintentionally.
            TODO: Another reminder. Responsibility for stopping service goes back to each service.
        */
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
        Log.d("savedinstance","onSaveInstance Called!")
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
        Log.d("savedinstance","onRestoreInstance Called!")
        sharedViewModel.unpackBundle(savedInstanceState)

    }

    override fun onBackPressed() {
        if (sharedViewModel.isPlaying.value!!)
            moveTaskToBack(true)
        else {
            val builder = AlertDialog.Builder(this)
        val t =
            "Are you sure you want to Quit?"
        builder.setTitle(t)
        builder.setPositiveButton("OK") { _, _ -> super.onBackPressed() }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
        }
    }
}
