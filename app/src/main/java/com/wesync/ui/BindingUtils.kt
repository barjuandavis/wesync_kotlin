package com.wesync.ui

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wesync.R
import com.wesync.util.ConnectionStatus
import com.wesync.util.UserTypes


@BindingAdapter("userType","playState")
fun Button.setPlayState(userType: LiveData<String>, isPlaying: LiveData<Boolean>) {
    if (this.id == R.id.play_button) {
        isEnabled = userType.value!! != UserTypes.SLAVE
    }
    when (userType.value!!){
        UserTypes.SLAVE -> {
            setBackgroundColor(ContextCompat.getColor(context,
                R.color.colorDisabledButton
            ))
            setTextColor(ContextCompat.getColor(context, R.color.colorDisabledButtonText))
        }
        else -> {
            setTextColor(ContextCompat.getColor(context, R.color.colorEnabledButtonText))
            if (isPlaying.value!!) {
                setBackgroundColor(ContextCompat.getColor(context, R.color.colorStop))
            } else {
                setBackgroundColor(ContextCompat.getColor(context, R.color.colorPlay))
            }
        }
    }
    if (isPlaying.value!!) {
        setText(R.string.stop_button)
    } else {
        setText(R.string.play_button)
    }

}

@BindingAdapter("userType","isAdvertising","playState","connectionStatus")
fun Button.setSessionState(item: LiveData<String>,
                           isAdvertising: LiveData<Boolean>,
                           isPlaying: LiveData<Boolean>,
                           connectionStatus: Int) {
    when (item.value) {
        UserTypes.SOLO -> {
            when (id) {
                R.id.new_session -> {
                    text = resources.getString(R.string.new_session)
                    setBackgroundColor(ContextCompat.getColor(context,
                        R.color.colorPrimary
                    ))
                }
                R.id.join_session -> {
                    visibility = View.VISIBLE
                    text = resources.getString(R.string.join_session)
                    setBackgroundColor(ContextCompat.getColor(context,
                        R.color.colorPrimaryDark
                    ))
                }
            }
        }
        UserTypes.SESSION_HOST -> {
            when (id) {
                R.id.new_session -> {
                    text = resources.getString(R.string.dismiss_session)
                    setBackgroundColor(ContextCompat.getColor(context,
                        R.color.colorSubtractTempo
                    ))
                }
                R.id.join_session -> {
                    visibility = View.VISIBLE
                    isEnabled = !isPlaying.value!!
                    if (!isEnabled) {
                        text = resources.getString(R.string.not_advertising)
                        setBackgroundColor(ContextCompat.getColor(context, R.color.colorDisabledButton))
                        setTextColor(ContextCompat.getColor(context,R.color.colorDisabledButtonText))
                    }
                    if (isAdvertising.value!!) {
                        text = resources.getString(R.string.advertising)
                        setBackgroundColor(ContextCompat.getColor(context,
                            R.color.colorStop
                        ))
                        setTextColor(ContextCompat.getColor(context,R.color.colorEnabledButtonText))
                    } else {
                        text = resources.getString(R.string.not_advertising)
                        setBackgroundColor(ContextCompat.getColor(context,
                            R.color.colorPlay
                        ))
                        setTextColor(ContextCompat.getColor(context,R.color.colorEnabledButtonText))
                    }
                }
            }
        }
        UserTypes.SLAVE -> when (id) {
            R.id.new_session -> {
                if (connectionStatus == ConnectionStatus.CONNECTING) {
                    isEnabled = false
                    setBackgroundColor(ContextCompat.getColor(context, R.color.colorDisabledButton))
                    setTextColor(ContextCompat.getColor(context,R.color.colorDisabledButtonText))
                } else  {
                    isEnabled = true
                    setTextColor(ContextCompat.getColor(context,R.color.colorEnabledButtonText))
                }
                text = resources.getString(R.string.quit_session)
                setBackgroundColor(ContextCompat.getColor(context,
                    R.color.colorSubtractTempo
                ))
            }
            R.id.join_session -> {
                visibility = View.GONE
            }
        }
    }
}

@BindingAdapter("userType","sessionName","connecting","connectedEndpointId")
fun TextView.setSessionState(userType: LiveData<String>, sessionName: String?, connectionStatus: Int?,connectedEndpointId: LiveData<String>) {
    when (userType.value) {
       UserTypes.SOLO -> {
           text = resources.getString(R.string.not_connected)
       }
        UserTypes.SESSION_HOST -> {
            text = resources.getString(R.string.current_session, sessionName)
        }
        UserTypes.SLAVE -> {
            text = if (connectionStatus == ConnectionStatus.CONNECTING) {
                resources.getString(R.string.connecting, connectedEndpointId.value)
            } else {
                resources.getString(R.string.current_joined_session, connectedEndpointId.value)
            }
        }
    }
}

@BindingAdapter("joinedSession")
fun Button.setButtonEnabled(userType: LiveData<String>) {
    isEnabled = (userType.value!! != UserTypes.SLAVE)
    if (!isEnabled) {
        setBackgroundColor(ContextCompat.getColor(context, R.color.colorDisabledButton))
        setTextColor(ContextCompat.getColor(context, R.color.colorDisabledButtonText))
    } else {
        when (id) {
            R.id.plus_1 -> setBackgroundColor(ContextCompat.getColor(context, R.color.colorAddTempo))
            R.id.plus_10 -> setBackgroundColor(ContextCompat.getColor(context, R.color.colorAddTempo))
            R.id.plus_50 -> setBackgroundColor(ContextCompat.getColor(context, R.color.colorAddTempo))
            R.id.minus_1 -> setBackgroundColor(ContextCompat.getColor(context, R.color.colorSubtractTempo))
            R.id.minus_10 -> setBackgroundColor(ContextCompat.getColor(context, R.color.colorSubtractTempo))
            R.id.minus_50 -> setBackgroundColor(ContextCompat.getColor(context, R.color.colorSubtractTempo))
        }
        setTextColor(ContextCompat.getColor(context, R.color.colorEnabledButtonText))
    }
}

@BindingAdapter("offset")
fun TextView.setOffset(offset: LiveData<Long>) {
    text = resources.getString(R.string.offset,offset.value)
}
@BindingAdapter("isDiscovering")
fun View.setIsDiscovering(isDiscovering: LiveData<Boolean>) {
    when(id) {
        R.id.progressBar2 -> {
            visibility =
                if (isDiscovering.value!!) {
                View.VISIBLE
            } else View.GONE
        }
        R.id.floatingActionButton -> {
            visibility = if (isDiscovering.value!!) {
                View.GONE
            } else View.VISIBLE
        }
    }
}




