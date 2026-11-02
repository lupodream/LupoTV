
package com.lupoplayer.app

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val bg = Color.rgb(9, 11, 17)
    private val panel = Color.rgb(25, 29, 42)
    private val gold = Color.rgb(218, 177, 96)
    private val white = Color.WHITE
    private val muted = Color.rgb(175, 180, 190)

    private lateinit var content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bg
        window.navigationBarColor = bg

        showHome()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

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

        scroll.addView(
            content,
            ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

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

    // STARTSEITE

    private fun showHome() {
        val root = base()

        space(12)
        text("🐺", 54, gold, true)
        text("LUPO PLAYER", 38, gold, true)
        text("PREMIUM ENTERTAINMENT", 14, white, true)

        space(28)

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(28), dp(24), dp(28))
            background = rounded(panel, 20)
        }

        hero.addView(
            TextView(this).apply {
                text = "WILLKOMMEN BEI LUPO"
                textSize = 27f
                setTextColor(white)
                gravity = Gravity.CENTER
                setTypeface(null, Typeface.BOLD)
            }
        )

        hero.addView(
            TextView(this).apply {
                text = "Dein Streaming. Dein Erlebnis."
                textSize = 16f
                setTextColor(muted)
                gravity = Gravity.CENTER
                setPadding(0, dp(12), 0, dp(8))
            }
        )

        content.addView(
            hero,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        space(25)
        text("ENTDECKEN", 20, gold, true)
        space(12)

        content.addView(
            tvButton("LIVE TV") {
                showCategory("LIVE TV")
            }
        )

        content.addView(
            tvButton("FILME") {
                showCategory("FILME")
            }
        )

        content.addView(
            tvButton("SERIEN") {
                showCategory("SERIEN")
            }
        )

        content.addView(
            tvButton("MEINE LISTE") {
                showCategory("MEINE LISTE")
            }
        )

        content.addView(
            tvButton("SUCHE") {
                showCategory("SUCHE")
            }
        )

        content.addView(
            tvButton("EINSTELLUNGEN") {
                showSettings()
            }
        )

        space(20)
        text("LUPO PLAYER  •  PREMIUM", 12, muted, true)

        setContentView(root)

        root.post {
            content.getChildAt(3)?.requestFocus()
        }
    }

    // KATEGORIE-SEITEN

    private fun showCategory(name: String) {
        val root = base()

        space(20)
        text("🐺 LUPO PLAYER", 30, gold, true)
        space(35)
        text(name, 30, white, true)
        space(20)

        text(
            "Hier erscheinen deine Inhalte.",
            18,
            muted,
            false
        )

        space(12)

        text(
            "Verbinde zuerst deinen IPTV-Anbieter in den Einstellungen.",
            15,
            muted,
            false
        )

        space(35)

        content.addView(
            tvButton("ZURÜCK ZUR STARTSEITE") {
                showHome()
            }
        )

        content.addView(
            tvButton("EINSTELLUNGEN") {
                showSettings()
            }
        )

        setContentView(root)
        root.post {
            content.getChildAt(content.childCount - 2).requestFocus()
        }
    }

    // EINSTELLUNGEN

    private fun showSettings() {
        val root = base()

        space(20)
        text("🐺 LUPO PLAYER", 30, gold, true)
        text("EINSTELLUNGEN", 24, white, true)

        space(30)

        content.addView(
            tvButton("XTREAM CODES ANMELDEN") {
                showXtream()
            }
        )

        content.addView(
            tvButton("M3U PLAYLIST") {
                showM3u()
            }
        )

        content.addView(
            tvButton("LOKALE PLAYLIST") {
                openPlaylist()
            }
        )

        space(20)

        content.addView(
            tvButton("ZURÜCK") {
                showHome()
            }
        )

        setContentView(root)
        root.post {
            content.getChildAt(0).requestFocus()
        }
    }

    // XTREAM LOGIN

    private fun showXtream() {
        val root = base()

        space(20)
        text("LUPO PLAYER", 30, gold, true)
        text("XTREAM CODES", 23, white, true)

        space(25)

        val server = input(
            "Server-URL",
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_URI
        )

        val username = input(
            "Benutzername",
            InputType.TYPE_CLASS_TEXT
        )

        val password = input(
            "Passwort",
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        )

        space(15)

        content.addView(
            tvButton("VERBINDEN") {
                if (
                    server.text.isBlank() ||
                    username.text.isBlank() ||
                    password.text.isBlank()
                ) {
                    toast("Bitte alle Felder ausfüllen.")
                } else {
                    toast("Anmeldedaten erfasst. Die Anbieter-Verbindung muss noch integriert werden.")
                }
            }
        )

        content.addView(
            tvButton("ZURÜCK") {
                showSettings()
            }
        )

        setContentView(root)
        root.post {
            server.requestFocus()
        }
    }

    // M3U PLAYLIST

    private fun showM3u() {
        val root = base()

        space(20)
        text("LUPO PLAYER", 30, gold, true)
        text("M3U PLAYLIST", 23, white, true)

        space(25)

        val url = input(
            "M3U Playlist URL",
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_URI
        )

        space(15)

        content.addView(
            tvButton("PLAYLIST LADEN") {
                if (url.text.isBlank()) {
                    toast("Bitte eine Playlist-URL eingeben.")
                } else {
                    toast("URL erfasst. Das Einlesen muss noch integriert werden.")
                }
            }
        )

        content.addView(
            tvButton("ZURÜCK") {
                showSettings()
            }
        )

        setContentView(root)
        root.post {
            url.requestFocus()
        }
    }

    // EINGABEFELDER

    private fun input(
        hintText: String,
        type: Int
    ): EditText {

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
                view.background = focusBackground(
                    panel,
                    focused
                )
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

    // TV BUTTONS MIT WEISSEM FOKUSRAHMEN

    private fun tvButton(
        label: String,
        action: () -> Unit
    ): Button {

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
                view.background = focusBackground(
                    panel,
                    focused
                )

                setTextColor(
                    if (focused) white else gold
                )
            }

            setOnClickListener {
                action()
            }
        }

        button.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(76)
            ).apply {
                topMargin = dp(7)
                bottomMargin = dp(7)
            }

        return button
    }

    // FOKUS-DESIGN

    private fun focusBackground(
        color: Int,
        focused: Boolean
    ): GradientDrawable {

        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(12).toFloat()

            if (focused) {
                setStroke(dp(3), white)
            } else {
                setStroke(
                    dp(1),
                    Color.rgb(75, 78, 88)
                )
            }
        }
    }

    private fun rounded(
        color: Int,
        radius: Int
    ): GradientDrawable {

        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    // TEXT

    private fun text(
        value: String,
        size: Int,
        color: Int,
        bold: Boolean
    ) {

        content.addView(
            TextView(this).apply {
                text = value
                textSize = size.toFloat()
                setTextColor(color)
                gravity = Gravity.CENTER
                if (bold) {
                    setTypeface(null, Typeface.BOLD)
                }
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
            LinearLayout.LayoutParams(
                1,
                dp(height)
            )
        )
    }

    // LOKALE PLAYLIST AUSWÄHLEN

    private fun openPlaylist() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }

        startActivityForResult(intent, 41)
    }

    @Deprecated("Legacy document picker callback")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            requestCode == 41 &&
            resultCode == RESULT_OK
        ) {
            toast("Datei ausgewählt. Das Einlesen muss noch integriert werden.")
        }
    }

    // ZURÜCK-TASTE DER FERNBEDIENUNG

    override fun onKeyDown(
        keyCode: Int,
        event: KeyEvent?
    ): Boolean {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showHome()
            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    private fun toast(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}
