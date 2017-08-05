package com.kalapuneet.boombox

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class MusicService : Service() {

    var simpleExoPlayer: SimpleExoPlayer? = null
    var bandwidthMeter: BandwidthMeter? = null
    var videoTrackSelectionFactory: AdaptiveTrackSelection.Factory? = null
    var trackSelector: TrackSelector? = null
    var dataSourceFactory: DefaultDataSourceFactory? = null
    var extractorsFactory: ExtractorsFactory? = null
    var binder: IBinder = PlayerBinder()
    val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action: String? = intent?.action
            if (action.equals("PAUSE")) {
                stopPlayer()
            } else {
                resumePlayer()
            }
        }
    }

   inner class PlayerBinder: Binder() {
       fun getService(): MusicService {
           return this@MusicService
       }
   }

    override fun onCreate() {
        super.onCreate()
        val filter: IntentFilter = IntentFilter()
        filter.addAction("PAUSE")
        filter.addAction("PLAY")
        registerReceiver(broadCastReceiver, filter)
        bandwidthMeter = DefaultBandwidthMeter()
        videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
        dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)), bandwidthMeter as DefaultBandwidthMeter)
        extractorsFactory = DefaultExtractorsFactory()
    }

    fun startPlayer(uri: Uri?) {
        val videoSource = ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null)
        simpleExoPlayer?.playWhenReady = true
        simpleExoPlayer?.prepare(videoSource)
    }

    fun stopPlayer() {
        simpleExoPlayer?.playWhenReady = false
        val intent = Intent("PLAY")
        val pi : PendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent,0)
        val notification = NotificationCompat.Builder(this)
                .setContentTitle("Boombox")
                .setSmallIcon(R.mipmap.ic_launcher)
                .addAction(0,"PLAY",pi)
                .build()
        startForeground(100, notification)
    }

    fun resumePlayer() {
        simpleExoPlayer?.playWhenReady = true
        val intent = Intent("PAUSE")
        val pi : PendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent,0)
        val notification = NotificationCompat.Builder(this)
                .setContentTitle("Boombox")
                .setSmallIcon(R.mipmap.ic_launcher)
                .addAction(0,"PAUSE",pi)
                .build()
        startForeground(100, notification)
    }

    fun getExoplayer(): SimpleExoPlayer? {
        return simpleExoPlayer
    }

    fun musicNotification(): Notification {
        val intent = Intent("PAUSE")
        val pi : PendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent,0)
        val notification = NotificationCompat.Builder(this)
                .setContentTitle("Boombox")
                .setSmallIcon(R.mipmap.ic_launcher)
                .addAction(0,"PAUSE",pi)
                .build()
        return notification
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startPlayer(Uri.parse(intent?.getStringExtra("URI")))
        startForeground(100, musicNotification())
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadCastReceiver)
    }
}
