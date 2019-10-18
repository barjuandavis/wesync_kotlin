package com.wesync

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import androidx.databinding.DataBindingUtil
import com.wesync.ui.main.MetronomeFragment

class MainActivity : AppCompatActivity() {
    private var mBound: Boolean = false
    private lateinit var mService: MetronomeService

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MetronomeService.LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.container, MetronomeFragment.newInstance()).commitNow()
        }
    }

    override fun onStart() {
        super.onStart()
        doBindService()
    }

    override fun onResume() {
        super.onResume()
        doBindService()
    }

    override fun onPause() {
        super.onPause()
        doUnbindService()
    }

    override fun onDestroy() {
        super.onDestroy()
        doUnbindService()
    }

    private fun doBindService() {
        Intent(applicationContext, MetronomeService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun doUnbindService() {
        unbindService(connection)
        mBound = false
    }
}
