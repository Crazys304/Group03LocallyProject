package com.example.localliy.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PlaybackBroadcastReceiver : BroadcastReceiver() {

    companion object {
        var onPlayPause: (() -> Unit)? = null
        var onPrevious: (() -> Unit)? = null
        var onNext: (() -> Unit)? = null
        var onStop: (() -> Unit)? = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            MediaNotificationManager.ACTION_PLAY_PAUSE -> onPlayPause?.invoke()
            MediaNotificationManager.ACTION_PREVIOUS -> onPrevious?.invoke()
            MediaNotificationManager.ACTION_NEXT -> onNext?.invoke()
            MediaNotificationManager.ACTION_STOP -> onStop?.invoke()
        }
    }
}
