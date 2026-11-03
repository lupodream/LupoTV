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
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors

class MainActivity : Activity() {

    private val bg = Color.rgb(9, 11, 17)
    private val panel = Color.rgb(25, 29, 42)
    private val gold = Color.rgb(218, 177, 96)
    private val white = Color.WHITE
    private val muted = Color.rgb(175, 180, 190)

    private lateinit var content: LinearLayout
    private val executor = Executors.newSingleThreadExecutor()
    private var currentChannels: List<Channel> = emptyList()
    private var currentTitle = "LIVE TV"

    data class Channel(val name: String, val url: String, val group: String = "Allgemein")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        showHome()
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun base(): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(bg)
            setPadding(dp(28), dp(20), dp(28), dp(20))
            isFocusableInTouchMode = true
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
        }

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(0, dp(10), 0, dp(20))
        }

        scroll.addView(content)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )
        return root
    }

    private fun addLupoLogo(size: Int) {
        val logo = ImageView(this).apply {
            setImageResource(R.drawable.lupo_logo)
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            contentDescription = "LUPO PLAYER Logo"
        }
        content.addView(
            logo,
            LinearLayout.LayoutParams(dp(size), dp(size)).apply {
                gravity = Gravity.CENTER
            }
        )
    }

    private fun showHome() {
        val root = base()
        space(8)
        addLupoLogo(145)
        text("LUPO PLAYER", 34, gold, true)
        text("PREMIUM ENTERTAINMENT", 14, white, true)
        space(24)
        text("WILLKOMMEN BEI LUPO", 25, white, true)
        text("Wähle deine Anmeldemethode", 16, muted, false)
        space(18)

        val xtream = tvButton("XTREAM CODES") { showXtream() }
        content.addView(xtream)
        content.addView(tvButton("M3U PLAYLIST") { showM3u() })
        space(20)
        text("Dein Streaming. Dein Erlebnis.", 12, gold, true)
        setContentView(root)
        xtream.requestFocus()
    }

    private fun showSettings() {
        val root = base()
        space(10)
        addLupoLogo(110)
        text("EINSTELLUNGEN", 26, white, true)
        space(25)
        val xtream = tvButton("XTREAM CODES ANMELDEN") { showXtream() }
        content.addView(xtream)
        content.addView(tvButton("M3U PLAYLIST") { showM3u() })
        content.addView(tvButton("LOKALE PLAYLIST") { openPlaylist() })
        space(20)
        content.addView(tvButton("ZURÜCK") { showHome() })
        setContentView(root)
        xtream.requestFocus()
    }

    private fun showXtream() {
        val root = base()
        space(10)
        addLupoLogo(100)
        text("XTREAM CODES", 24, white, true)
        space(20)

        val server = input("Server-URL (z. B. http://server:port)", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI)
        val username = input("Benutzername", InputType.TYPE_CLASS_TEXT)
        val password = input("Passwort", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        space(15)

        val connect = tvButton("VERBINDEN") {
            val serverValue = server.text.toString().trim().trimEnd('/')
            val userValue = username.text.toString().trim()
            val passValue = password.text.toString()
            if (serverValue.isBlank() || userValue.isBlank() || passValue.isBlank()) {
                toast("Bitte alle Felder ausfüllen.")
            } else {
                connectXtream(serverValue, userValue, passValue)
            }
        }
        content.addView(connect)
        content.addView(tvButton("ZURÜCK") { showSettings() })
        setContentView(root)
        server.requestFocus()
    }

    private fun connectXtream(server: String, username: String, password: String) {
        showLoading("Verbinde mit deinem Anbieter …")
        executor.execute {
            try {
                val query = "username=${enc(username)}&password=${enc(password)}"
                val apiUrl = "$server/player_api.php?$query"
                val response = httpGet(apiUrl)
                val json = JSONObject(response)
                val userInfo = json.optJSONObject("user_info")
                if (userInfo == null || userInfo.optString("auth") == "0") {
                    throw IllegalStateException("Anmeldung fehlgeschlagen. Bitte Server und Zugangsdaten prüfen.")
                }

                val streamsUrl = "$server/player_api.php?$query&action=get_live_streams"
                val streamsJson = JSONArray(httpGet(streamsUrl))
                val channels = ArrayList<Channel>()
                for (i in 0 until streamsJson.length()) {
                    val item = streamsJson.optJSONObject(i) ?: continue
                    val id = item.optString("stream_id")
                    val name = item.optString("name", "Kanal $id")
                    val extension = item.optString("container_extension", "ts").ifBlank { "ts" }
                    if (id.isNotBlank()) {
                        val streamUrl = "$server/live/${encPath(username)}/${encPath(password)}/$id.$extension"
                        channels.add(Channel(name, streamUrl, item.optString("category_name", "Live TV")))
                    }
                }
                runOnUiThread {
                    if (channels.isEmpty()) {
                        showMessage("Keine Live-Kanäle gefunden. Prüfe, ob dein Anbieter Live-TV freigeschaltet hat.")
                    } else {
                        currentChannels = channels
                        currentTitle = "LIVE TV"
                        showChannels(channels, "LIVE TV")
                        toast("${channels.size} Kanäle geladen.")
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showMessage(e.message ?: "Verbindung fehlgeschlagen.")
                }
            }
        }
    }

    private fun showM3u() {
        val root = base()
        space(10)
        addLupoLogo(100)
        text("M3U PLAYLIST", 24, white, true)
        space(20)
        val url = input("M3U Playlist URL", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI)
        space(15)

        val load = tvButton("PLAYLIST LADEN") {
            val value = url.text.toString().trim()
            if (value.isBlank()) {
                toast("Bitte eine Playlist-URL eingeben.")
            } else {
                loadM3uFromUrl(value)
            }
        }
        content.addView(load)
        content.addView(tvButton("LOKALE PLAYLIST ÖFFNEN") { openPlaylist() })
        content.addView(tvButton("ZURÜCK") { showSettings() })
        setContentView(root)
        url.requestFocus()
    }

    private fun loadM3uFromUrl(value: String) {
        showLoading("Playlist wird geladen …")
        executor.execute {
            try {
                val playlistText = httpGet(value)
                val channels = parseM3u(playlistText)
                runOnUiThread {
                    if (channels.isEmpty()) {
                        showMessage("Keine abspielbaren Einträge in der M3U-Playlist gefunden.")
                    } else {
                        currentChannels = channels
                        currentTitle = "M3U PLAYLIST"
                        showChannels(channels, currentTitle)
                        toast("${channels.size} Einträge geladen.")
                    }
                }
            } catch (e: Exception) {
                runOnUiThread { showMessage(e.message ?: "Playlist konnte nicht geladen werden.") }
            }
        }
    }

    private fun parseM3u(source: String): List<Channel> {
        val result = ArrayList<Channel>()
        var pendingName = ""
        var pendingGroup = "Allgemein"

        source.lineSequence().forEach { raw ->
            val line = raw.trim()
            if (line.startsWith("#EXTINF", ignoreCase = true)) {
                val comma = line.lastIndexOf(',')
                pendingName = if (comma >= 0 && comma < line.length - 1) {
                    line.substring(comma + 1).trim()
                } else {
                    "Unbenannter Kanal"
                }
                val groupMatch = Regex("""group-title=["']([^"']*)["']""", RegexOption.IGNORE_CASE).find(line)
                pendingGroup = groupMatch?.groupValues?.get(1)?.ifBlank { "Allgemein" } ?: "Allgemein"
            } else if (line.isNotBlank() && !line.startsWith("#") &&
                (line.startsWith("http://", true) || line.startsWith("https://", true))) {
                result.add(Channel(pendingName.ifBlank { "Kanal ${result.size + 1}" }, line, pendingGroup))
                pendingName = ""
                pendingGroup = "Allgemein"
            }
        }
        return result
    }

    private fun showChannels(channels: List<Channel>, title: String) {
        val root = base()
        space(6)
        addLupoLogo(75)
        text(title, 25, gold, true)
        text("${channels.size} Einträge", 13, muted, false)
        space(12)

        val groups = channels.groupBy { it.group }.toSortedMap()
        groups.forEach { (groupName, items) ->
            text(groupName, 18, white, true)
            items.forEach { channel ->
                content.addView(tvButton(channel.name) { play(channel) })
            }
        }
        space(16)
        content.addView(tvButton("NEUE PLAYLIST / ANMELDUNG") { showHome() })
        setContentView(root)
    }

    private fun play(channel: Channel) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(channel.url), "video/*")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (_: Exception) {
            toast("Kein kompatibler Videoplayer installiert.")
        }
    }

    private fun showLoading(message: String) {
        val root = base()
        space(30)
        addLupoLogo(100)
        text("LUPO PLAYER", 28, gold, true)
        space(20)
        text(message, 17, white, false)
        setContentView(root)
    }

    private fun showMessage(message: String) {
        val root = base()
        space(20)
        addLupoLogo(90)
        text("LUPO PLAYER", 26, gold, true)
        space(18)
        text(message, 16, white, false)
        space(20)
        content.addView(tvButton("ZURÜCK") { showHome() })
        setContentView(root)
    }

    private fun httpGet(address: String): String {
        val connection = (URL(address).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15000
            readTimeout = 30000
            setRequestProperty("User-Agent", "LupoPlayer/1.0")
        }
        try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
            if (code !in 200..299) {
                throw IllegalStateException("Server-Fehler HTTP $code")
            }
            return body
        } finally {
            connection.disconnect()
        }
    }

    private fun enc(value: String): String =
        URLEncoder.encode(value, "UTF-8")

    private fun encPath(value: String): String =
        enc(value).replace("+", "%20")

    private fun input(hintText: String, type: Int): EditText {
        val edit = EditText(this).apply {
            hint = hintText
            textSize = 18f
            setTextColor(white)
            setHintTextColor(muted)
            inputType = type
            setSingleLine(true)
            setPadding(dp(18), dp(10), dp(18), dp(10))
            background = rounded(panel, 12)
            isFocusable = true
            isFocusableInTouchMode = true
            setOnFocusChangeListener { view, focused ->
                view.background = focusBackground(panel, focused)
            }
        }
        content.addView(
            edit,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(65)
            ).apply {
                topMargin = dp(8)
                bottomMargin = dp(8)
            }
        )
        return edit
    }

    private fun tvButton(label: String, action: () -> Unit): Button {
        val button = Button(this).apply {
            text = label
            textSize = 17f
            setTextColor(gold)
            isAllCaps = false
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(8), dp(12), dp(8))
            minHeight = 0
            minimumHeight = 0
            minWidth = 0
            minimumWidth = 0
            isFocusable = true
            isFocusableInTouchMode = true
            background = focusBackground(panel, false)
            setOnFocusChangeListener { view, focused ->
                view.background = focusBackground(panel, focused)
                setTextColor(if (focused) white else gold)
            }
            setOnClickListener { action() }
        }
        button.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(76)
        ).apply {
            topMargin = dp(7)
            bottomMargin = dp(7)
        }
        return button
    }

    private fun focusBackground(color: Int, focused: Boolean): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(12).toFloat()
            if (focused) setStroke(dp(3), white)
            else setStroke(dp(1), Color.rgb(75, 78, 88))
        }

    private fun rounded(color: Int, radius: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }

    private fun text(value: String, size: Int, color: Int, bold: Boolean) {
        content.addView(
            TextView(this).apply {
                text = value
                textSize = size.toFloat()
                setTextColor(color)
                gravity = Gravity.CENTER
                if (bold) setTypeface(null, Typeface.BOLD)
                setPadding(dp(8), dp(6), dp(8), dp(6))
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun space(height: Int) {
        content.addView(
            View(this),
            LinearLayout.LayoutParams(1, dp(height))
        )
    }

    private fun openPlaylist() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, 41)
    }

    @Deprecated("Legacy document picker callback")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 41 && resultCode == RESULT_OK && data?.data != null) {
            val uri = data.data!!
            showLoading("Lokale Playlist wird eingelesen …")
            executor.execute {
                try {
                    val input = contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("Datei konnte nicht geöffnet werden.")
                    val source = input.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    val channels = parseM3u(source)
                    runOnUiThread {
                        if (channels.isEmpty()) {
                            showMessage("In dieser Datei wurden keine M3U-Einträge gefunden.")
                        } else {
                            currentChannels = channels
                            currentTitle = "LOKALE PLAYLIST"
                            showChannels(channels, currentTitle)
                            toast("${channels.size} Einträge geladen.")
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread { showMessage(e.message ?: "Datei konnte nicht gelesen werden.") }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showHome()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
