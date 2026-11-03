package com.lupoplayer.app

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.text.InputType
import android.text.TextWatcher
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

    private fun logo(size: Int) {
        val image = ImageView(this).apply {
            setImageResource(R.drawable.lupo_logo)
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            contentDescription = "LUPO PLAYER Wolf-Logo"
        }
        content.addView(image, LinearLayout.LayoutParams(dp(size), dp(size)).apply { gravity = Gravity.CENTER })
    }

    private fun brandHeader() {
        logo(90)
        text("LUPO PLAYER", 30, gold, true)
        text("PREMIUM IPTV PLAYER", 12, white, true)
        val stripe = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setPadding(0, dp(5), 0, dp(8)) }
        listOf(Color.rgb(0,145,65), white, Color.rgb(220,25,45)).forEach { c ->
            stripe.addView(View(this).apply { setBackgroundColor(c) }, LinearLayout.LayoutParams(dp(34), dp(3)))
        }
        content.addView(stripe)
    }

    private fun showLogin() {
        val root = base()
        content.gravity = Gravity.CENTER
        space(10)
        val panelCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            background = rounded(Color.rgb(13, 17, 26), 22)
            setPadding(dp(34), dp(22), dp(34), dp(22))
        }
        content.addView(panelCard, LinearLayout.LayoutParams(dp(620), ViewGroup.LayoutParams.WRAP_CONTENT).apply { gravity = Gravity.CENTER })
        val oldContent = content
        content = panelCard
        logo(112)
        text("LUPO PLAYER", 31, gold, true)
        text("PREMIUM IPTV PLAYER", 12, white, true)
        val stripe = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setPadding(0, dp(4), 0, dp(10)) }
        listOf(Color.rgb(0,145,65), white, Color.rgb(220,25,45)).forEach { c -> stripe.addView(View(this).apply { setBackgroundColor(c) }, LinearLayout.LayoutParams(dp(26), dp(3))) }
        content.addView(stripe)
        space(8)
        text("WILLKOMMEN ZURÜCK", 21, white, true)
        text("Melde dich mit deinen Zugangsdaten an", 13, muted, false)
        space(12)
        val serverInput = input("Server-Adresse (z. B. http://server:port)", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI).also { it.setText(server) }
        val userInput = input("Benutzername", InputType.TYPE_CLASS_TEXT).also { it.setText(username) }
        val passInput = input("Passwort", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD).also { it.setText(password) }
        space(8)
        val login = tvButton("ANMELDEN") {
            val s = serverInput.text.toString().trim().trimEnd('/')
            val u = userInput.text.toString().trim()
            val p = passInput.text.toString()
            if (s.isBlank() || u.isBlank() || p.isBlank()) toast("Bitte Server, Benutzername und Passwort eingeben.")
            else connectXtream(s, u, p, showProgress = true)
        }
        login.background = rounded(gold, 12); login.setTextColor(Color.BLACK)
        content.addView(login)
        space(8)
        text("ODER M3U-PLAYLIST", 12, gold, true)
        val playlistInput = input("M3U-Playlist-URL", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI).also { it.setText(m3uUrl) }
        content.addView(tvButton("M3U LADEN") {
            val url = playlistInput.text.toString().trim()
            if (url.isBlank()) toast("Bitte eine M3U-Adresse eingeben.") else { m3uUrl = url; prefs.edit().putString("m3u", url).apply(); loadM3u(url) }
        })
        content = oldContent
        setContentView(root)
        serverInput.requestFocus()
    }

    private fun connectXtream(s: String, u: String, p: String, showProgress: Boolean) {
        server = s; username = u; password = p; isM3uMode = false
        if (showProgress) showLoading("Verbinde mit deinem Anbieter …")
        executor.execute {
            try {
                val query = "username=${enc(u)}&password=${enc(p)}"
                val account = JSONObject(httpGet("$s/player_api.php?$query"))
                val info = account.optJSONObject("user_info")
                if (info == null || info.optString("auth") == "0") throw IllegalStateException("Anmeldung fehlgeschlagen. Bitte Server und Zugangsdaten prüfen.")
                val live = parseXtreamStreams(httpGet("$s/player_api.php?$query&action=get_live_streams"), "live", s, u, p)
                val movies = safeXtreamList("$s/player_api.php?$query&action=get_vod_streams", "vod", s, u, p)
                val series = safeXtreamList("$s/player_api.php?$query&action=get_series", "series", s, u, p)
                prefs.edit().putString("server", s).putString("username", u).putString("password", p).apply()
                runOnUiThread {
                    currentChannels = live.map { Channel(it.name, it.url, it.group, it.logo) }
                    currentMovies = movies
                    currentSeries = series
                    currentTab = "HOME"
                    showHome()
                    toast("Verbunden: ${live.size} Live-Sender")
                }
            } catch (e: Exception) { runOnUiThread { showError(e.message ?: "Verbindung fehlgeschlagen.") } }
        }
    }

    private fun safeXtreamList(url: String, type: String, s: String, u: String, p: String): List<MediaEntry> {
        return try {
            val arr = JSONArray(httpGet(url)); val out = ArrayList<MediaEntry>()
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val id = if (type == "series") o.optString("series_id") else o.optString("stream_id")
                if (id.isBlank()) continue
                val name = o.optString("name", "Eintrag $id")
                val ext = o.optString("container_extension", if (type == "vod") "mp4" else "mp4").ifBlank { "mp4" }
                val path = if (type == "vod") "$s/movie/${encPath(u)}/${encPath(p)}/$id.$ext" else "$s/series/${encPath(u)}/${encPath(p)}/$id.$ext"
                out.add(MediaEntry(name, path, o.optString("category_name", if (type == "vod") "Filme" else "Serien"), o.optString("stream_icon", o.optString("cover", ""))))
            }
            out
        } catch (_: Exception) { emptyList() }
    }

    private fun parseXtreamStreams(source: String, type: String, s: String, u: String, p: String): List<MediaEntry> {
        val arr = JSONArray(source); val out = ArrayList<MediaEntry>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val id = o.optString("stream_id")
            if (id.isBlank()) continue
            val ext = o.optString("container_extension", "ts").ifBlank { "ts" }
            out.add(MediaEntry(o.optString("name", "Kanal $id"), "$s/live/${encPath(u)}/${encPath(p)}/$id.$ext", o.optString("category_name", "Live TV"), o.optString("stream_icon", "")))
        }
        return out
    }

    private fun loadM3u(url: String) {
        isM3uMode = true; showLoading("M3U-Playlist wird geladen …")
        executor.execute {
            try {
                val entries = parseM3u(httpGet(url))
                runOnUiThread {
                    if (entries.isEmpty()) showError("Keine abspielbaren Einträge gefunden.")
                    else { currentChannels = entries.map { Channel(it.name, it.url, it.group, it.logo) }; currentMovies = emptyList(); currentSeries = emptyList(); showHome(); toast("${entries.size} Einträge geladen.") }
                }
            } catch (e: Exception) { runOnUiThread { showError(e.message ?: "Playlist konnte nicht geladen werden.") } }
        }
    }

    private fun parseM3u(source: String): List<MediaEntry> {
        val out = ArrayList<MediaEntry>(); var name = ""; var group = "Allgemein"; var logo = ""
        source.lineSequence().forEach { raw ->
            val line = raw.trim()
            if (line.startsWith("#EXTINF", true)) {
                val comma = line.lastIndexOf(','); name = if (comma >= 0) line.substring(comma + 1).trim() else "Kanal ${out.size + 1}"
                group = Regex("""group-title=[\"']([^\"']*)[\"']""", RegexOption.IGNORE_CASE).find(line)?.groupValues?.get(1) ?: "Allgemein"
                logo = Regex("""tvg-logo=[\"']([^\"']*)[\"']""", RegexOption.IGNORE_CASE).find(line)?.groupValues?.get(1) ?: ""
            } else if ((line.startsWith("http://", true) || line.startsWith("https://", true)) && !line.startsWith("#")) {
                out.add(MediaEntry(name.ifBlank { "Kanal ${out.size + 1}" }, line, group, logo)); name = ""; group = "Allgemein"; logo = ""
            }
        }
        return out
    }

    private fun showHome() {
        currentTab = "HOME"
        val root = base()
        content.setPadding(dp(24), dp(8), dp(24), dp(24))
        val top = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val brand = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val wolf = ImageView(this).apply { setImageResource(R.drawable.lupo_logo); scaleType = ImageView.ScaleType.FIT_CENTER }
        brand.addView(wolf, LinearLayout.LayoutParams(dp(48), dp(48)))
        brand.addView(TextView(this).apply { text = "LUPO PLAYER"; textSize = 22f; setTextColor(gold); setTypeface(null, Typeface.BOLD); setPadding(dp(8),0,0,0) })
        top.addView(brand, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        top.addView(tvButton("⌕ SUCHE") { showSearch() }, LinearLayout.LayoutParams(dp(125), dp(48)))
        top.addView(tvButton("⚙") { showSettings() }, LinearLayout.LayoutParams(dp(64), dp(48)))
        content.addView(top)
        val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        listOf("HOME", "LIVE TV", "FILME", "SERIEN", "SPORT", "KIDS", "PLAYLIST").forEach { tab ->
            nav.addView(tvButton(tab) { when(tab) {
                "HOME" -> showHome()
                "LIVE TV" -> showLive()
                "FILME" -> showMedia("FILME", currentMovies)
                "SERIEN" -> showMedia("SERIEN", currentSeries)
                "SPORT" -> showMedia("SPORT", (currentMovies + currentSeries).filter { it.group.contains("sport", true) || it.name.contains("sport", true) })
                "KIDS" -> showMedia("KIDS", (currentMovies + currentSeries).filter { it.group.contains("kid", true) || it.group.contains("kind", true) || it.name.contains("kid", true) })
                else -> showPlaylist()
            } }, LinearLayout.LayoutParams(0, dp(48), 1f))
        }
        content.addView(nav)
        space(10)
        heroCard()
        section("WEITER ANSEHEN", (currentMovies + currentSeries).take(8)) { showMedia("WEITER ANSEHEN", currentMovies + currentSeries) }
        section("FILME", currentMovies) { showMedia("FILME", currentMovies) }
        section("SERIEN", currentSeries) { showMedia("SERIEN", currentSeries) }
        section("LIVE TV", currentChannels.map { MediaEntry(it.name, it.url, it.group, it.logo) }) { showLive() }
        setContentView(root)
    }

    private fun heroCard() {
        val featured = currentMovies.firstOrNull() ?: currentSeries.firstOrNull()
        val card = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.BOTTOM; background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(Color.rgb(35,39,50), Color.rgb(8,11,18))); cornerRadius = dp(18).toFloat(); setPadding(dp(30), dp(22), dp(30), dp(22)) }
        card.addView(TextView(this).apply { text = "LUPO ORIGINAL"; textSize = 12f; setTextColor(gold); setTypeface(null, Typeface.BOLD) })
        card.addView(TextView(this).apply { text = featured?.name ?: "DEIN ENTERTAINMENT"; textSize = 30f; setTextColor(white); setTypeface(null, Typeface.BOLD); setPadding(0, dp(8), 0, dp(4)) })
        card.addView(TextView(this).apply { text = if (featured != null) "Jetzt entdecken und direkt weitersehen." else "LIVE TV  •  FILME  •  SERIEN"; textSize = 15f; setTextColor(muted); setPadding(0, 0, 0, dp(10)) })
        card.addView(tvButton(if (featured != null) "▶  JETZT ABSPIELEN" else "▶  LIVE TV ÖFFNEN") { if (featured != null) play(featured.url, featured.name) else showLive() }, LinearLayout.LayoutParams(dp(230), dp(52)))
        content.addView(card, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(225)))
    }

    private fun section(title: String, items: List<MediaEntry>, action: () -> Unit) {
        space(16)
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        header.addView(TextView(this).apply { text = title; textSize = 20f; setTextColor(white); setTypeface(null, Typeface.BOLD) }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        header.addView(tvButton("ALLE ANZEIGEN  ›") { action() }, LinearLayout.LayoutParams(dp(155), dp(42)))
        content.addView(header)
        if (items.isEmpty()) { text("Noch keine Inhalte geladen", 13, muted, false); return }
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        items.take(10).forEach { item ->
            val poster = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = rounded(panel, 10); setPadding(dp(5), dp(5), dp(5), dp(5)); isFocusable = true; isFocusableInTouchMode = true; setOnClickListener { play(item.url, item.name) }; setOnFocusChangeListener { v, focused -> v.background = focusBackground(panel, focused) } }
            val art = ImageView(this).apply { scaleType = ImageView.ScaleType.CENTER_CROP; setBackgroundColor(Color.rgb(35,40,52)) }
            poster.addView(art, LinearLayout.LayoutParams(dp(142), dp(116)))
            poster.addView(TextView(this).apply { text = item.name; textSize = 12f; maxLines = 2; setTextColor(white); setPadding(dp(4), dp(6), dp(4), dp(4)) }, LinearLayout.LayoutParams(dp(142), dp(48)))
            row.addView(poster, LinearLayout.LayoutParams(dp(154), dp(180)).apply { rightMargin = dp(9) })
            if (item.logo.isNotBlank()) loadArtwork(item.logo, art)
        }
        val horizontal = android.widget.HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false; addView(row) }
        content.addView(horizontal, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(186)))
    }

    private fun loadArtwork(address: String, target: ImageView) {
        executor.execute {
            try {
                val connection = (URL(address).openConnection() as HttpURLConnection).apply { connectTimeout = 8000; readTimeout = 8000 }
                val bitmap = connection.inputStream.use { android.graphics.BitmapFactory.decodeStream(it) }
                connection.disconnect()
                if (bitmap != null) runOnUiThread { if (!isFinishing) target.setImageBitmap(bitmap) }
            } catch (_: Exception) { }
        }
    }

    private fun showLive() {
        currentTab = "LIVE TV"
        val root = base(); addNav(); text("LIVE TV", 25, gold, true)
        if (currentChannels.isEmpty()) text("Keine Sender geladen. Bitte Anbieter verbinden.", 15, muted, false)
        else currentChannels.groupBy { it.group }.toSortedMap().forEach { (group, list) ->
            space(10); text(group, 18, white, true)
            list.forEach { c -> content.addView(tvButton("▶  ${c.name}") { play(c.url, c.name) }) }
        }
        setContentView(root)
    }

    private fun showMedia(title: String, items: List<MediaEntry>) {
        currentTab = title
        val root = base(); addNav(); text(title, 25, gold, true)
        if (items.isEmpty()) text("Dein Anbieter stellt hier momentan keine Inhalte bereit.", 15, muted, false)
        else items.groupBy { it.group }.toSortedMap().forEach { (group, list) ->
            space(10); text(group, 18, white, true)
            list.forEach { item -> content.addView(tvButton("▶  ${item.name}") { play(item.url, item.name) }) }
        }
        setContentView(root)
    }

    private fun addNav() {
        val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        listOf("HOME", "LIVE TV", "FILME", "SERIEN", "SPORT", "KIDS", "PLAYLIST").forEach { tab ->
            nav.addView(tvButton(tab) { when(tab) {
                "HOME" -> showHome()
                "LIVE TV" -> showLive()
                "FILME" -> showMedia("FILME", currentMovies)
                "SERIEN" -> showMedia("SERIEN", currentSeries)
                "SPORT" -> showMedia("SPORT", (currentMovies + currentSeries).filter { it.group.contains("sport", true) || it.name.contains("sport", true) })
                "KIDS" -> showMedia("KIDS", (currentMovies + currentSeries).filter { it.group.contains("kid", true) || it.group.contains("kind", true) || it.name.contains("kid", true) })
                else -> showPlaylist()
            } }, LinearLayout.LayoutParams(0, dp(48), 1f))
        }
        content.addView(nav)
    }

    private fun showSearch() {
        currentTab = "SUCHE"
        val root = base(); addNav()
        text("SUCHEN", 25, gold, true)
        val query = input("Film, Serie oder Sender suchen …", InputType.TYPE_CLASS_TEXT)
        val results = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content.addView(results)
        fun updateResults(value: String) {
            results.removeAllViews()
            val q = value.trim()
            if (q.isBlank()) return
            val all = currentChannels.map { MediaEntry(it.name, it.url, it.group, it.logo) } + currentMovies + currentSeries
            all.filter { it.name.contains(q, true) || it.group.contains(q, true) }.take(80).forEach { item ->
                results.addView(tvButton("▶  ${item.name}") { play(item.url, item.name) })
            }
            if (results.childCount == 0) results.addView(TextView(this).apply { text = "Keine Treffer gefunden"; setTextColor(muted); textSize = 15f; setPadding(dp(12), dp(18), dp(12), dp(18)) })
        }
        query.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { updateResults(s?.toString() ?: "") }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        setContentView(root); query.requestFocus()
    }

    private fun showPlaylist() {
        currentTab = "PLAYLIST"
        val root = base(); addNav(); text("PLAYLIST", 25, gold, true)
        text(if (isM3uMode) "M3U-PLAYLIST" else "XTREAM-CODES-PLAYLIST", 13, muted, true)
        val entries = currentChannels.map { MediaEntry(it.name, it.url, it.group, it.logo) }
        if (entries.isEmpty()) text("Keine Playlist geladen.", 15, muted, false)
        else entries.groupBy { it.group }.toSortedMap().forEach { (group, list) ->
            space(10); text(group, 18, white, true)
            list.forEach { item -> content.addView(tvButton("▶  ${item.name}") { play(item.url, item.name) }) }
        }
        content.addView(tvButton("PLAYLIST / ZUGANG ÄNDERN") { showLogin() })
        setContentView(root)
    }

    private fun showSettings() {
        currentTab = "EINSTELLUNGEN"
        val root = base(); addNav(); brandHeader(); text("EINSTELLUNGEN", 23, white, true); space(12)
        content.addView(tvButton("ZUGANGSDATEN ÄNDERN") { showLogin() })
        content.addView(tvButton("M3U-PLAYLIST LADEN") { showLogin() })
        content.addView(tvButton("ZUGANGSDATEN LÖSCHEN") { prefs.edit().clear().apply(); server=""; username=""; password=""; currentChannels=emptyList(); currentMovies=emptyList(); currentSeries=emptyList(); showLogin() })
        content.addView(tvButton("ZUR STARTSEITE") { showHome() })
        setContentView(root)
    }

    private fun play(url: String, title: String) {
        releasePlayer()
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.BLACK); setPadding(dp(10), dp(10), dp(10), dp(10)) }
        val heading = TextView(this).apply { text = title; textSize = 18f; setTextColor(white); gravity = Gravity.CENTER; setPadding(0, dp(8), 0, dp(8)) }
        root.addView(heading)
        val pv = PlayerView(this).apply { useController = true; controllerShowTimeoutMs = 4000; setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING) }
        root.addView(pv, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(tvButton("ZURÜCK") { releasePlayer(); showHome() })
        setContentView(root)
        player = ExoPlayer.Builder(this).build().also { exo -> pv.player = exo; exo.setMediaItem(MediaItem.fromUri(Uri.parse(url))); exo.prepare(); exo.playWhenReady = true }
        playerView = pv
    }

    private fun releasePlayer() { playerView?.player = null; player?.release(); player = null; playerView = null }

    private fun showLoading(message: String) { val root = base(); space(40); brandHeader(); space(20); text(message, 17, white, false); setContentView(root) }
    private fun showError(message: String) { val root = base(); brandHeader(); space(20); text(message, 16, white, false); content.addView(tvButton("ZURÜCK ZUM LOGIN") { showLogin() }); content.addView(tvButton("STARTSEITE") { showHome() }); setContentView(root) }

    private fun httpGet(address: String): String {
        val conn = (URL(address).openConnection() as HttpURLConnection).apply { requestMethod = "GET"; connectTimeout = 15000; readTimeout = 30000; setRequestProperty("User-Agent", "LupoPlayer/2.0") }
        try { val code = conn.responseCode; val stream = if (code in 200..299) conn.inputStream else conn.errorStream; val body = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }; if (code !in 200..299) throw IllegalStateException("Server-Fehler HTTP $code"); return body } finally { conn.disconnect() }
    }

    private fun enc(v: String) = URLEncoder.encode(v, "UTF-8")
    private fun encPath(v: String) = enc(v).replace("+", "%20")

    private fun input(hintText: String, type: Int): EditText {
        val edit = EditText(this).apply { hint = hintText; textSize = 17f; setTextColor(white); setHintTextColor(muted); inputType = type; setSingleLine(true); setPadding(dp(18), dp(10), dp(18), dp(10)); background = focusBackground(panel2, false); isFocusable = true; isFocusableInTouchMode = true; setOnFocusChangeListener { v, focused -> v.background = focusBackground(panel2, focused) } }
        content.addView(edit, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(62)).apply { topMargin = dp(7); bottomMargin = dp(7) })
        return edit
    }

    private fun tvButton(label: String, width: Int? = null, action: () -> Unit): Button {
        val b = Button(this).apply { text = label; textSize = 15f; setTextColor(gold); isAllCaps = false; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; setPadding(dp(8), dp(5), dp(8), dp(5)); minHeight = 0; minimumHeight = 0; minWidth = 0; minimumWidth = 0; isFocusable = true; isFocusableInTouchMode = true; background = focusBackground(panel, false); setOnFocusChangeListener { v, focused -> v.background = focusBackground(panel, focused); setTextColor(if (focused) white else gold) }; setOnClickListener { action() } }
        b.layoutParams = LinearLayout.LayoutParams(width?.let { dp(it) } ?: ViewGroup.LayoutParams.MATCH_PARENT, dp(62)).apply { topMargin = dp(5); bottomMargin = dp(5) }
        return b
    }

    private fun focusBackground(color: Int, focused: Boolean) = GradientDrawable().apply { setColor(if (focused) Color.rgb(44, 39, 28) else color); cornerRadius = dp(12).toFloat(); setStroke(dp(if (focused) 3 else 1), if (focused) gold else Color.rgb(68, 73, 84)) }
    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat(); setStroke(dp(1), Color.rgb(62, 67, 78)) }
    private fun text(value: String, size: Int, color: Int, bold: Boolean) { content.addView(TextView(this).apply { text = value; textSize = size.toFloat(); setTextColor(color); gravity = Gravity.CENTER; if (bold) setTypeface(null, Typeface.BOLD); setPadding(dp(8), dp(6), dp(8), dp(6)) }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)) }
    private fun space(h: Int) { content.addView(View(this), LinearLayout.LayoutParams(1, dp(h))) }
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (player != null) { releasePlayer(); showHome() }
            else if (currentTab != "HOME" && currentChannels.isNotEmpty()) showHome()
            else if (server.isNotBlank() || isM3uMode) showHome()
            else showLogin()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
