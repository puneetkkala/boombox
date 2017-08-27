package com.kalapuneet.boombox

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.ListView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ui.PlaybackControlView
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.kalapuneet.boombox.objects.MediaFile

class MainActivity : AppCompatActivity() {

    var simpleExoPlayerView: SimpleExoPlayerView? = null
    var mediaList: ListView? = null
    var musicService: MusicService? = null
    var isBound: Boolean = false
    var serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder: MusicService.PlayerBinder = service as MusicService.PlayerBinder
            musicService = binder.getService()
            isBound = true
            (simpleExoPlayerView as SimpleExoPlayerView).player = musicService?.getExoplayer()
            simpleExoPlayerView?.setControlDispatcher(object : PlaybackControlView.ControlDispatcher {
                override fun dispatchSetPlayWhenReady(player: ExoPlayer?, playWhenReady: Boolean): Boolean {
                    if (playWhenReady) {
                        musicService?.resumePlayer()
                    } else {
                        musicService?.stopPlayer()
                    }
                    player?.playWhenReady = playWhenReady
                    return true
                }

                override fun dispatchSeekTo(player: ExoPlayer?, windowIndex: Int, positionMs: Long): Boolean {
                    player?.seekTo(windowIndex,positionMs)
                    return true
                }
            })
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }


    val FILE_SELECT_CODE = 0
    val MY_WAKE_LOCK_PERMISSION = 1010

    fun showFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        this.startActivityForResult(Intent.createChooser(intent, "Select a file to run"), FILE_SELECT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            FILE_SELECT_CODE -> if (resultCode == Activity.RESULT_OK) {
                val uri: Uri? = data?.data
                startMusicService(uri)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun startMusicService(uri: Uri?) {
        /*val intent = Intent(this, MusicService::class.java)
        intent.putExtra("URI", uri.toString())
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)*/
        if (isBound) {
            (simpleExoPlayerView as SimpleExoPlayerView).player = musicService?.getExoplayer()
            simpleExoPlayerView?.setControlDispatcher(object : PlaybackControlView.ControlDispatcher {
                override fun dispatchSetPlayWhenReady(player: ExoPlayer?, playWhenReady: Boolean): Boolean {
                    if (playWhenReady) {
                        musicService?.resumePlayer()
                    } else {
                        musicService?.stopPlayer()
                    }
                    player?.playWhenReady = playWhenReady
                    return true
                }

                override fun dispatchSeekTo(player: ExoPlayer?, windowIndex: Int, positionMs: Long): Boolean {
                    player?.seekTo(windowIndex,positionMs)
                    return true
                }
            })
            val intent = Intent(this, MusicService::class.java)
            intent.putExtra("URI", uri.toString())
            startService(intent)
        }

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        simpleExoPlayerView = findViewById(R.id.simple_exo_player_view) as SimpleExoPlayerView
        mediaList = findViewById(R.id.media_list) as ListView

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(Manifest.permission.WAKE_LOCK, Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions, MY_WAKE_LOCK_PERMISSION)
        } else {
            val contentResolver = contentResolver
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val cursor = contentResolver.query(uri, null, null, null, null)
            var mediaFileList: List<MediaFile> = emptyList()
            var titleList: List<String> = emptyList()
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val mediaFile: MediaFile = MediaFile()
                    mediaFile.id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    mediaFile.name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    mediaFileList = mediaFileList.plus(mediaFile)
                    titleList = titleList.plus(mediaFile.name)
                } while (cursor.moveToNext())
            }
            cursor?.close()
            mediaList?.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, titleList)
            mediaList?.setOnItemClickListener { parent, view, position, id ->
                val mediaFile: MediaFile = mediaFileList.get(position)
                val mediaUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,mediaFile.id)
                startMusicService(mediaUri)
            }
            val sharedPreferences1 = getSharedPreferences("mediaPreferences", MODE_PRIVATE)
            val editor = sharedPreferences1.edit()
            editor.putLong("lastFetchTime", 0)
            editor.apply()
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
        super.onStop()
    }
}
