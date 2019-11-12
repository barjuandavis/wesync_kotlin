package com.wesync

import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import com.wesync.util.UserTypes


@BindingAdapter("playState")
fun Button.setPlayState(item: LiveData<Boolean>?) {
    item.let {
        if (it!!.value!!) {
            setText(R.string.stop_button)
            setBackgroundColor(ContextCompat.getColor(context, R.color.colorStop))
        } else {
            setText(R.string.play_button)
            setBackgroundColor(ContextCompat.getColor(context, R.color.colorPlay))
        }
    }
}

@BindingAdapter("userType","isAdvertising")
fun Button.setSessionState(item: LiveData<UserTypes>,
                           isAdvertising: LiveData<Boolean>) {
    when (item.value) {
        UserTypes.SOLO -> {
            when (id) {
                R.id.new_session -> {
                    text = resources.getString(R.string.new_session)
                    setBackgroundColor(ContextCompat.getColor(context,R.color.colorPrimary))
                }
                R.id.join_session -> {
                    text = resources.getString(R.string.join_session)
                    setBackgroundColor(ContextCompat.getColor(context,R.color.colorPrimaryDark))
                }
            }
        }
        UserTypes.SESSION_HOST -> {
            when (id) {
                R.id.new_session -> {
                    text = resources.getString(R.string.dismiss_session)
                    setBackgroundColor(ContextCompat.getColor(context,R.color.colorSubtractTempo))
                }
                R.id.join_session -> {
                    if (isAdvertising.value!!) {
                        text = resources.getString(R.string.advertising)
                        setBackgroundColor(ContextCompat.getColor(context,R.color.colorStop))
                    } else {
                        text = resources.getString(R.string.not_advertising)
                        setBackgroundColor(ContextCompat.getColor(context,R.color.colorPlay))
                    }
                }
            }
        }
        UserTypes.SLAVE -> TODO("lupa mau ngapain")
    }
}


@BindingAdapter("userType","sessionName")
fun TextView.setSessionState(userType: LiveData<UserTypes>, sessionName: String?) {
    when (userType.value) {
       UserTypes.SOLO -> {
           text = resources.getString(R.string.not_connected)
       }
        UserTypes.SESSION_HOST -> {
            text = resources.getString(R.string.current_session, sessionName)
        }
        UserTypes.SLAVE -> TODO()
    }
}