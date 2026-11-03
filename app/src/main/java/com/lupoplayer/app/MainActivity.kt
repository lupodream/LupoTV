
    
  
package com.lupoplayer.app

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.text.InputType
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val bg = Color.rgb(7, 9, 14)
    private val panel = Color.rgb(24, 28, 38)
    private val panel2 = Color.rgb(15, 18, 25)
    private val gold = Color.rgb(218, 177, 75)
    private val white = Color.WHITE
    private val muted = Color.rgb(174, 180, 192)
    private lateinit var content: LinearLayout
    private val executor = Executors.newSingleThreadExecutor()
    private val prefs by lazy { getSharedPreferences("lupo_player", MODE_PRIVATE) }
    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null
    private var currentChannels: List<Channel> = emptyList()
    private var currentMovies: List<MediaEntry> = emptyList()
    private var currentSeries: List<MediaEntry> = emptyList()
    private var server = ""
    private var username = ""
    private var password = ""
    private var m3uUrl = ""
    private var isM3uMode = false
    private var currentTab = "HOME"

    private data class Channel(val name: String, val url: String, val group: String = "Allgemein", val logo: String = "")
    private data class MediaEntry(val name: String, val url: String, val group: String = "Allgemein", val logo: String = "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        server = prefs.getString("server", "") ?: ""
        username = prefs.getString("username", "") ?: ""
        password = prefs.getString("password", "") ?: ""
        m3uUrl = prefs.getString("m3u", "") ?: ""
        if (server.isNotBlank() && username.isNotBlank() && password.isNotBlank()) {
            connectXtream(server, username, password, showProgress = true)
        } else {
            showLogin()
        }
    }

    override fun onDestroy() {
        releasePlayer()
        executor.shutdownNow()
        super.onDestroy()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun base(): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(dp(28), dp(14), dp(28), dp(14))
            isFocusableInTouchMode = true
        }
        val scroll = ScrollView(this).apply { isFillViewport = true; clipToPadding = false }
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL; setPadding(0, dp(6), 0, dp(18)) }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        return root
    }

