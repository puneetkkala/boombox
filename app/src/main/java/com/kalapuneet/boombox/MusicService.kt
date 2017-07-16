package com.kalapuneet.boombox

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.support.v4.app.NotificationCompat
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

    override fun onCreate() {
        super.onCreate()

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
        simpleExoPlayer?.stop()
    }

    fun musicNotification(): Notification {
        val notification = NotificationCompat.Builder(this)
                .setContentTitle("Boombox")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build()
        return notification
    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startPlayer(Uri.parse(intent?.getStringExtra("URI")))
        startForeground(100, musicNotification())
        return START_STICKY
    }
}
