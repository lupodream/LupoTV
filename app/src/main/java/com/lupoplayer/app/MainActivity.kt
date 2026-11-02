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

    private val bg = Color.rgb(12, 13, 17)
    private val panel = Color.rgb(25, 26, 32)
    private val gold = Color.rgb(218, 177, 96)
    private val white = Color.WHITE
    private lateinit var content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        showLogin()
    }

    private fun base(): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(bg)
            setPadding(40, 24, 40, 24)
            isFocusableInTouchMode = true
        }

        val scroll = ScrollView(this)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        scroll.addView(content)
        root.addView(
            scroll,
            ViewGroup.LayoutParams(-1, -1)
        )

        return root
    }

    private fun showLogin() {
        val root = base()

        space(15)

        text("🐺", 48, gold, true)
        text("LUPO PLAYER", 34, gold, true)
        text("PREMIUM ENTERTAINMENT", 13, white, true)

        space(28)

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(35, 28, 35, 28)
            background = rounded(panel, 20)
        }

        card.addView(
            TextView(this).apply {
                text = "WILLKOMMEN BEI LUPO"
                textSize = 25f
                setTextColor(white)
                gravity = Gravity.CENTER
                setTypeface(null, Typeface.BOLD)
            }
        )

        card.addView(
            TextView(this).apply {
                text = "Verbinde deinen Anbieter und starte dein Erlebnis."
                textSize = 15f
                setTextColor(Color.LTGRAY)
                gravity = Gravity.CENTER
                setPadding(0, 12, 0, 24)
            }
        )

        val xtream = tvButton("MIT XTREAM CODES ANMELDEN") {
            showXtream()
        }

        val m3u = tvButton("M3U PLAYLIST") {
            showM3u()
        }

        val local = tvButton("LOKALE PLAYLIST") {
            openPlaylist()
        }

        card.addView(xtream)
        card.addView(m3u)
        card.addView(local)

        content.addView(
            card,
            LinearLayout.LayoutParams(650, -2)
        )

        space(20)

        text(
            "DEIN STREAMING. DEIN ERLEBNIS.",
            12,
            gold,
            true
        )

        setContentView(root)

        xtream.requestFocus()
    }

    private fun showXtream() {
        val root = base()

        text("LUPO PLAYER", 30, gold, true)
        text("MIT XTREAM CODES ANMELDEN", 22, white, true)

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

        space(20)

        val connect = tvButton("VERBINDEN") {
            if (
                server.text.isBlank() ||
                username.text.isBlank() ||
                password.text.isBlank()
            ) {
                toast("Bitte alle Felder ausfüllen.")
            } else {
                toast("Anmeldedaten erfasst. Die Anbieter-Verbindung ist noch nicht integriert.")
            }
        }

        content.addView(connect)
        content.addView(
            tvButton("ZURÜCK") { showLogin() }
        )

        setContentView(root)
        server.requestFocus()
    }

    private fun showM3u() {
        val root = base()

        text("LUPO PLAYER", 30, gold, true)
        text("M3U PLAYLIST", 22, white, true)

        space(25)

        val url = input(
            "M3U Playlist URL",
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_URI
        )

        val load = tvButton("PLAYLIST LADEN") {
            if (url.text.isBlank()) {
                toast("Bitte eine Playlist-URL eingeben.")
            } else {
                toast("URL erfasst. Das Einlesen ist noch nicht integriert.")
            }
        }

        content.addView(load)
        content.addView(
            tvButton("ZURÜCK") { showLogin() }
        )

        setContentView(root)
        url.requestFocus()
    }

    private fun input(
        hintText: String,
        type: Int
    ): EditText {

        val edit = EditText(this).apply {
            hint = hintText
            textSize = 19f
            setTextColor(white)
            setHintTextColor(Color.LTGRAY)
            inputType = type
            setSingleLine(true)
            setPadding(20, 12, 20, 12)
            background = rounded(panel, 10)

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
            LinearLayout.LayoutParams(600, 65).apply {
                topMargin = 9
                bottomMargin = 9
            }
        )

        return edit
    }

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

            isFocusable = true
            isFocusableInTouchMode = true

            background = rounded(panel, 12)

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
            LinearLayout.LayoutParams(580, 68).apply {
                topMargin = 9
                bottomMargin = 9
            }

        return button
    }

    private fun focusBackground(
        color: Int,
        focused: Boolean
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = 12f

            if (focused) {
                setStroke(3, white)
            } else {
                setStroke(1, Color.rgb(75, 75, 80))
            }
        }
    }

    private fun rounded(
        color: Int,
        radius: Int
    ) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius.toFloat()
    }

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
                setPadding(8, 6, 8, 6)
            },
            LinearLayout.LayoutParams(-1, -2)
        )
    }

    private fun space(height: Int) {
        content.addView(
            View(this),
            LinearLayout.LayoutParams(1, height)
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
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 41 && resultCode == RESULT_OK) {
            toast("Datei ausgewählt. Das Einlesen folgt noch.")
        }
    }

    private fun toast(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}
