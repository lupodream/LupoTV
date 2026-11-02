package com.lupoplayer.app

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val bg = Color.rgb(16, 17, 20)
    private val gold = Color.rgb(218, 177, 96)
    private val white = Color.rgb(245, 242, 234)

    private lateinit var content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun base(): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(42, 22, 42, 28)
            setBackgroundColor(bg)
            isFocusableInTouchMode = true
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
        }

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        scroll.addView(content)
        root.addView(scroll, ViewGroup.LayoutParams(-1, -1))

        return root
    }

    private fun showHome() {
        val root = base()

        val logo = ImageView(this).apply {
            setImageResource(R.drawable.lupo_logo)
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
        }

        content.addView(
            logo,
            LinearLayout.LayoutParams(230, 185).apply {
                bottomMargin = 2
            }
        )

        text("LUPO PLAYER", 30, gold, true)
        text("PREMIUM IPTV PLAYER", 13, Color.LTGRAY, false)

        space(24)

        goldButton("ANMELDEN MIT XTREAM CODES") {
            showXtream()
        }

        goldButton("M3U PLAYLIST") {
            showM3u()
        }

        goldButton("LOKALE PLAYLIST ÖFFNEN") {
            openPlaylist()
        }

        space(16)

        text(
            "Für eigene oder rechtmäßig lizenzierte Streams",
            12,
            Color.GRAY,
            false
        )

        setContentView(root)
        content.getChildAt(2)?.requestFocus()
    }

    private fun showXtream() {
        val root = base()

        heading("XTREAM CODES")

        val server = field(
            "Server-URL",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
        )

        val user = field(
            "Benutzername",
            InputType.TYPE_CLASS_TEXT
        )

        val pass = field(
            "Passwort",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        )

        goldButton("VERBINDEN") {
            if (server.text.isBlank() ||
                user.text.isBlank() ||
                pass.text.isBlank()
            ) {
                toast("Bitte alle drei Felder ausfüllen.")
            } else {
                toast(
                    "Eingaben erfasst. Die Anbieter-Verbindung wird im nächsten Entwicklungsschritt ergänzt."
                )
            }
        }

        secondaryButton("ZURÜCK") {
            showHome()
        }

        setContentView(root)
        server.requestFocus()
    }

    private fun showM3u() {
        val root = base()

        heading("M3U PLAYLIST")

        val url = field(
            "M3U-Playlist-URL",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
        )

        goldButton("PLAYLIST LADEN") {
            if (url.text.isBlank()) {
                toast("Bitte eine Playlist-URL eingeben.")
            } else {
                toast(
                    "URL erfasst. Das Laden und Abspielen wird im nächsten Entwicklungsschritt ergänzt."
                )
            }
        }

        secondaryButton("ZURÜCK") {
            showHome()
        }

        setContentView(root)
        url.requestFocus()
    }

    private fun openPlaylist() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }

        startActivityForResult(intent, 41)
    }

    @Deprecated("Kept compatible with older Fire TV devices")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 41 &&
            resultCode == RESULT_OK &&
            data?.data != null
        ) {
            toast(
                "Datei ausgewählt. Das Einlesen der Playlist folgt im nächsten Entwicklungsschritt."
            )
        }
    }

    private fun heading(value: String) {
        space(28)
        text("LUPO PLAYER", 27, gold, true)
        space(18)
        text(value, 19, white, true)
        space(18)
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
                    setTypeface(typeface, Typeface.BOLD)
                }

                setPadding(8, 5, 8, 5)
            },
            LinearLayout.LayoutParams(-1, -2)
        )
    }

    private fun field(
        hintText: String,
        type: Int
    ): EditText {
        val edit = EditText(this).apply {
            hint = hintText
            setHintTextColor(Color.LTGRAY)
            setTextColor(white)
            textSize = 17f
            inputType = type
            setSingleLine(true)
            setPadding(18, 12, 18, 12)
            background = rounded(Color.rgb(38, 39, 43), 10)
        }

        content.addView(
            edit,
            LinearLayout.LayoutParams(520, 58).apply {
                topMargin = 8
                bottomMargin = 8
            }
        )

        return edit
    }

    private fun goldButton(
        label: String,
        action: () -> Unit
    ) {
        val button = Button(this).apply {
            text = label
            textSize = 15f
            setTextColor(Color.rgb(20, 20, 20))
            isAllCaps = false
            typeface = Typeface.DEFAULT_BOLD
            background = rounded(gold, 12)
            isFocusable = true
            isFocusableInTouchMode = true
            setOnClickListener {
                action()
            }
        }

        content.addView(
            button,
            LinearLayout.LayoutParams(520, 62).apply {
                topMargin = 9
                bottomMargin = 9
            }
        )
    }

    private fun secondaryButton(
        label: String,
        action: () -> Unit
    ) {
        val button = Button(this).apply {
            text = label
            textSize = 14f
            setTextColor(gold)
            isAllCaps = false
            background = rounded(Color.rgb(38, 39, 43), 12)
            isFocusable = true
            setOnClickListener {
                action()
            }
        }

        content.addView(
            button,
            LinearLayout.LayoutParams(520, 58).apply {
                topMargin = 12
            }
        )
    }

    private fun rounded(
        color: Int,
        radius: Int
    ) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius.toFloat()
    }

    private fun space(height: Int) {
        content.addView(
            View(this),
            LinearLayout.LayoutParams(1, height)
        )
    }

    private fun toast(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}
