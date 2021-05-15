package com.example.soundboard

import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var soundPool: SoundPool
    private var isLoaded: Boolean = false
    var soundMap: HashMap<Int, Int> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.setUpUi()
        this.loadFamousCalls()
    }
    private fun setUpUi() {
        topLeftImage.setOnClickListener(this)
        topRightImage.setOnClickListener(this)
        bottomLeftImage.setOnClickListener(this)
        bottomRightImage.setOnClickListener(this)
    }
    private fun loadFamousCalls() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.soundPool = SoundPool.Builder().setMaxStreams(10).build()
        } else {
            this.soundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 0)
        }

        this.soundPool.setOnLoadCompleteListener { _, _, _ -> isLoaded = true }
        this.soundMap[R.raw.edin_avdic_teletovic] = this.soundPool.load(this, R.raw.edin_avdic_teletovic, 1)
        this.soundMap[R.raw.kevin_harlan_no_regard] = this.soundPool.load(this, R.raw.kevin_harlan_no_regard, 1)
        this.soundMap[R.raw.michael_buffer_rumble] = this.soundPool.load(this, R.raw.michael_buffer_rumble, 1)
        this.soundMap[R.raw.martin_tyler_aguero] = this.soundPool.load(this, R.raw.martin_tyler_aguero, 1)
    }
    override fun onClick(v: View) {
        if (this.isLoaded == false) return

        when (v.getId()) {
            R.id.topLeftImage -> playSound(R.raw.edin_avdic_teletovic)
            R.id.topRightImage-> playSound(R.raw.kevin_harlan_no_regard)
            R.id.bottomLeftImage-> playSound(R.raw.michael_buffer_rumble)
            R.id.bottomRightImage-> playSound(R.raw.martin_tyler_aguero)
        }
    }
    fun playSound(selectedSound: Int) {
        val soundID = this.soundMap[selectedSound] ?: 0
        this.soundPool.play(soundID, 1f, 1f, 1, 0, 1f)
    }
}