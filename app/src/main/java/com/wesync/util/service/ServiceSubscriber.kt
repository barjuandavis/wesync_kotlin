package com.wesync.util.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wesync.connection.ConnectionManagerService
import com.wesync.metronome.MetronomeService


/**
*
*  A utility class that helps a Fragment to **subscribe** (bind) its FragmentActivity to MetronomeService and ConnectionManagerService
*
 * @param context: Context of the corresponding Fragment.
 * @param activity: FragmentActivity that the corresponding Fragment inflated to.
 *
*/

class ServiceSubscriber(private val context: Context, private val activity: FragmentActivity?) {
    private val _metronomeConnected = MutableLiveData<Boolean>()
        val metronomeConnected : LiveData<Boolean> = _metronomeConnected

    private val _connServiceConnected = MutableLiveData<Boolean>()
        val connServiceConnected : LiveData<Boolean> = _connServiceConnected

    var metronomeService : MetronomeService? = null
    var connectionService: ConnectionManagerService? = null

    private var metronomeBoundByActivity = false
    private var connBoundByActivity = false

    private val _metronomeConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            //Log.d("konek","Binding mService connected!")
            val s = (binder as MetronomeService.LocalBinder).getService()
            metronomeService = s
            _metronomeConnected.value = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            metronomeService = null
            _metronomeConnected.value = false
        }
    }
    private val _connServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            //Log.d("konek","Binding mCService connected!")
            val s = (binder as ConnectionManagerService.LocalBinder).getService()
            connectionService = s
            _connServiceConnected.value = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            connectionService= null
            _connServiceConnected.value = false
        }
    }

    init {
        _metronomeConnected.value = false
        _connServiceConnected.value = false
    }

    fun subscribe() {
        Intent(context, MetronomeService::class.java).also { i ->
            if (activity != null)
            {
                activity.bindService(i, _metronomeConnection, Context.BIND_AUTO_CREATE)
                metronomeBoundByActivity = true
            }
            else
                context.bindService(i,_metronomeConnection,Context.BIND_AUTO_CREATE)
        }
        Intent(context, ConnectionManagerService::class.java).also { i ->
            if (activity != null){
                activity.bindService(i, _connServiceConnection, Context.BIND_AUTO_CREATE)
                connBoundByActivity = true
            }
            else
                context.bindService(i,_connServiceConnection,Context.BIND_AUTO_CREATE)
         }
    }


    fun unsubscribe() {
        if (metronomeBoundByActivity)
            activity?.unbindService(_metronomeConnection)
        else
            context.unbindService(_metronomeConnection)
        if (connBoundByActivity)
            activity?.unbindService(_connServiceConnection)
        else
            context.unbindService(_connServiceConnection)
    }
}