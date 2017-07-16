package com.kalapuneet.boombox

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class MainActivity : AppCompatActivity() {

    var simpleExoPlayer: SimpleExoPlayer? = null
    var bandwidthMeter: BandwidthMeter? = null
    var videoTrackSelectionFactory: AdaptiveTrackSelection.Factory? = null
    var trackSelector: TrackSelector? = null
    var simpleExoPlayerView: SimpleExoPlayerView? = null
    var dataSourceFactory: DefaultDataSourceFactory? = null
    var extractorsFactory: ExtractorsFactory? = null

    val FILE_SELECT_CODE = 0

    fun showFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        this.startActivityForResult(Intent.createChooser(intent, "Select a file to run"),FILE_SELECT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            FILE_SELECT_CODE -> if (resultCode == Activity.RESULT_OK) {
                val uri: Uri? = data?.data
                startPlayer(uri)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun startPlayer(uri: Uri?) {
        val videoSource = ExtractorMediaSource(uri,dataSourceFactory,extractorsFactory,null,null)
        simpleExoPlayer?.prepare(videoSource)
    }

    fun stopPlayer() {
        simpleExoPlayer?.stop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bandwidthMeter = DefaultBandwidthMeter()
        videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this,trackSelector)
        simpleExoPlayerView = findViewById(R.id.simple_exo_player_view) as SimpleExoPlayerView
        (simpleExoPlayerView as SimpleExoPlayerView).player = simpleExoPlayer
        dataSourceFactory = DefaultDataSourceFactory(this,Util.getUserAgent(this,getString(R.string.app_name)), bandwidthMeter as DefaultBandwidthMeter)
        extractorsFactory = DefaultExtractorsFactory()
        showFileChooser()
    }
}
