package app.harshit.pokdex.activity

import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.app.AppCompatActivity
import android.view.View
import app.harshit.pokdex.R
import com.otaliastudios.cameraview.Gesture
import com.otaliastudios.cameraview.GestureAction
import kotlinx.android.synthetic.main.activity_main.*

abstract class BaseCameraActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var sheetBehavior: BottomSheetBehavior<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sheetBehavior = BottomSheetBehavior.from(bottomLayout)
        sheetBehavior.peekHeight = 224
        cameraView.mapGesture(Gesture.PINCH, GestureAction.ZOOM)
        cameraFrame.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        cameraView.start()
    }

    override fun onPause() {
        cameraView.stop()
        super.onPause()
    }
}