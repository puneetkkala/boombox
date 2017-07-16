package com.kalapuneet.boombox

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
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
    val MY_WAKE_LOCK_PERMISSION = 1010

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
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            var permissions = arrayOf(Manifest.permission.WAKE_LOCK,Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this,permissions,MY_WAKE_LOCK_PERMISSION)
        } else {
            var contentResolver = contentResolver
            var uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            var cursor = contentResolver.query(uri,null,null,null,null);
            if(cursor != null && cursor.moveToFirst()) {
                do {

                } while (cursor.moveToNext())
            }
            if(cursor != null)
                cursor.close();
            var sharedPreferences1 = getSharedPreferences("mediaPreferences", MODE_PRIVATE)
            var editor = sharedPreferences1.edit()
            editor.putLong("lastFetchTime",0)
            editor.apply()
        }
    }
}
