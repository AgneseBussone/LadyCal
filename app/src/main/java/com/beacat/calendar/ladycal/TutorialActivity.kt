package com.beacat.calendar.ladycal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ViewFlipper

/**
 * Activity that shows a one time tutorial with the base functionality of the app
 */

class TutorialActivity : AppCompatActivity(), View.OnClickListener {

    private var flipper: ViewFlipper? = null
    internal var btn: Button? = null
    internal var f1: FrameLayout? = null
    internal var f2: FrameLayout? = null
    internal var f3: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.tutorial)

        flipper = findViewById(R.id.view_flipper)
        btn = findViewById(R.id.tutorial_btn)
        btn!!.setOnClickListener(this)

        f1 = findViewById(R.id.b1_frame)
        f2 = findViewById(R.id.b2_frame)
        f3 = findViewById(R.id.b3_frame)
        f2!!.visibility = View.VISIBLE // start period image
    }

    override fun onClick(v: View) {
        when (flipper!!.displayedChild) {
            0 -> {
                f2!!.visibility = View.INVISIBLE
                flipper!!.showNext()
                f3!!.visibility = View.VISIBLE // med image
            }
            1 -> {
                f3!!.visibility = View.INVISIBLE
                flipper!!.showNext()
                f1!!.visibility = View.VISIBLE // refresh image
                btn!!.text = "DONE"
            }
            2 -> finish()
        }
    }
}
