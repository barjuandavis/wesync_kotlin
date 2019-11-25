package com.wesync


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.HandlerThread
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.work.*
import com.google.common.util.concurrent.ListenableFuture
import com.wesync.connection.ConnectionManagerService
import com.wesync.metronome.MetronomeService
import com.wesync.ntp.NTPClientWorker
import com.wesync.ui.ConnectionFragmentDirections
import com.wesync.util.UserTypes
import com.wesync.util.service.ServiceSubscriber

class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel
    private val serviceSubscriber = ServiceSubscriber(this, this)
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
        syncSystemClock()
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        this.lifecycle.addObserver(mainViewModel)
        mainViewModel.unpackBundle(savedInstanceState)
    }

    private fun syncSystemClock() {
        if (android.
                provider.
                Settings.Global.
                getInt(contentResolver, android.provider.Settings.Global.AUTO_TIME, 0) != 1) {
            val builder = AlertDialog.Builder(this)
            val tv = TextView(this)
            tv.text = getString(R.string.need_auto_time)
            builder.setCustomTitle(tv)
            val input = EditText(this)
            builder.setPositiveButton("OK") { _, _ ->
               val i = Intent(android.provider.Settings.ACTION_DATE_SETTINGS)
               startActivityForResult(i,0)
            }
            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            builder.show()
        } //persuade user to turn on NITZ
        //here is an attempt to connect to an NTP Service. Embrace yourselves!
        val constrains = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val otr = OneTimeWorkRequest.Builder(NTPClientWorker::class.java)
        .setConstraints(constrains).build()
        WorkManager.getInstance(this)
            .beginWith(otr)
            .enqueue()

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

    private fun initSupportActionBar() {
        setContentView(R.layout.main_activity)
        setSupportActionBar(findViewById(R.id.toolbar))
        toolbar = supportActionBar!!
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

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
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

    private fun stopServices() {
        if (metronomeIsAlive) MetronomeService.stop(applicationContext)
        if (connectionIsAlive)ConnectionManagerService.stop(applicationContext)
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
}
