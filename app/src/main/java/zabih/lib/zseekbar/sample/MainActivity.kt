package zabih.lib.zseekbar.sample

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import zHelper.view.ZSeekBar
import zHelper.view.ZSeekBar.OnZArcSeekBarChangeListener


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val testProgress = findViewById<TextView>(R.id.test_progress2)
        val zSeekBar = findViewById<ZSeekBar>(R.id.test_zSeekBar2)

        zSeekBar.setOnZArcSeekBarChangeListener(object : OnZArcSeekBarChangeListener {
            override fun onProgressChanged(
                circleProgress: ZSeekBar,
                progress: Float,
                fromUser: Boolean
            ) {
                testProgress.text = progress.toInt().toString()
            }


            override fun onStartTrackingTouch(circleProgress: ZSeekBar) {
            }


            override fun onStopTrackingTouch(circleProgress: ZSeekBar) {
            }
        })
    }
}