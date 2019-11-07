package com.wesync.ui.connection

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wesync.connection.Endpoint

class ConnectionViewModel : ViewModel() {
    /*
           TODO: Create a ViewModel.Factory for this ViewModel that accept MyEndpointCallback as parameter.
            Shared reference between the service and this VM will AB-SO-LUTE-LY USEFUL.
            In short: use the Callback as a data source for this VM.
     */

    private val _ConnectedSession = MutableLiveData<Endpoint>()

    fun getAllSessions(): LiveData<List<String>> {
        val ld =  MutableLiveData<List<String>>()
        ld.postValue(listOf("Ayam","Bebek","Kucing","Ikan","Tikus"))
        return ld
    }

    fun onSessionClicked(it: String) {
        Log.d("henlo","henlo")
    }

}
