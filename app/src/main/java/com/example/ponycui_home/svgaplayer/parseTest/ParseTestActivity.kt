package com.example.ponycui_home.svgaplayer.parseTest

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.LinearLayout
import com.example.ponycui_home.svgaplayer.R
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.utils.log.SVGALogger.setSVGALogOpen
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class ParseTestActivity : AppCompatActivity() {

    lateinit var llRawOne: LinearLayout
    lateinit var llRawTwo: LinearLayout


    private var rawList = intArrayOf(
        R.raw.yylove_level1,
        R.raw.yylove_level2,
        R.raw.yylove_level3,
        R.raw.yylove_level4,
        R.raw.yylove_level5,
        R.raw.yylove_level6,
        R.raw.yylove_level7,
        R.raw.yylove_level8
    )

    var service: ScheduledExecutorService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            R.layout.activity_parse_test)
        service = Executors.newScheduledThreadPool(1)

        llRawOne = findViewById(R.id.rawOne)
        llRawTwo = findViewById(R.id.rawTwo)

        initSVGALogger()

        start()
    }

    private fun initSVGALogger() {
        setSVGALogOpen(true)
            .injectSVGALoggerImp(SvgaLog2())
    }

    private fun start() {

        val taskOne = object : TimerTask() {
            override fun run() {

                val rawRes = getRandomRawRes()
                for (i in 0 until llRawOne.childCount) {
                    val avarImage: SVGAImageView = llRawOne.getChildAt(i) as SVGAImageView
                    SvgaHelper.playAvatarSvga(avarImage, rawRes, 1, null)
                }

                for (i in 0 until llRawTwo.childCount) {
                    val avarImage: SVGAImageView = llRawTwo.getChildAt(i) as SVGAImageView
                    SvgaHelper.playAvatarSvga(avarImage, rawRes, 1, null)
                }
            }
        }
        service?.scheduleWithFixedDelay(taskOne, 1000, 2000, TimeUnit.MILLISECONDS)
    }

    fun getRandomRawRes(): Int {
        return rawList[(Math.random() * (rawList.size)).toInt()]
    }

    override fun onDestroy() {
        super.onDestroy()
        service?.shutdown()
    }
}
