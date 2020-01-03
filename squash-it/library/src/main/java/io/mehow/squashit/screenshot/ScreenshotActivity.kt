package io.mehow.squashit.screenshot

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Parcelable
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.palette.graphics.Palette
import io.mehow.squashit.BaseActivity
import io.mehow.squashit.FileParceler
import io.mehow.squashit.R
import io.mehow.squashit.report.extensions.enableEdgeToEdgeAndNightMode
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.TypeParceler
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import java.io.File

internal class ScreenshotActivity : BaseActivity() {
  private lateinit var screenshotFile: File
  lateinit var screenshotBitmap: Bitmap
  val scope = MainScope()

  override fun onCreate(inState: Bundle?) {
    super.onCreate(inState)
    screenshotFile = intent.getParcelableExtra<Args>(ArgsKey)!!.screenshotFile
    screenshotBitmap = BitmapFactory.decodeFile(screenshotFile.path)

    window.decorView.enableEdgeToEdgeAndNightMode()
    contrastBackground(screenshotBitmap)
    setContentView(R.layout.edit_screenshot)
    setUpScreenshot(screenshotBitmap)
    val paintbox = setUpPaintbox()
    val canvas = setUpScreenshotCanvas(paintbox)
    val callback = PaintboxCanvasCallback(paintbox, canvas, this)
    paintbox.setCallback(callback)
  }

  override fun onDestroy() {
    super.onDestroy()
    scope.cancel()
  }

  private fun contrastBackground(bitmap: Bitmap) {
    val palette = Palette.from(bitmap).clearFilters().generate()
    val isDark = palette.dominantSwatch
        ?.let { ColorUtils.calculateLuminance(it.rgb) < 0.25 } == true
    delegate.localNightMode = if (isDark) MODE_NIGHT_NO else MODE_NIGHT_YES
  }

  private fun setUpScreenshot(bitmap: Bitmap) {
    val screenshot = findViewById<ImageView>(R.id.screenshot)
    screenshot.setImageBitmap(bitmap)
    ViewCompat.setOnApplyWindowInsetsListener(screenshot) { _, insets ->
      screenshot.updateLayoutParams<MarginLayoutParams> {
        updateMargins(top = insets.systemWindowInsetTop)
      }
      return@setOnApplyWindowInsetsListener insets
    }
  }

  private fun setUpPaintbox(): PaintboxView {
    val paintbox = findViewById<PaintboxView>(R.id.paintbox)
    ViewCompat.setOnApplyWindowInsetsListener(paintbox) { _, insets ->
      paintbox.updatePadding(bottom = insets.systemWindowInsetBottom)
      return@setOnApplyWindowInsetsListener insets
    }
    return paintbox
  }

  private fun setUpScreenshotCanvas(paintboxView: PaintboxView): CanvasView {
    val canvas = findViewById<CanvasView>(R.id.screenshotCanvas)
    canvas.setBrush(paintboxView.brush)
    return canvas
  }

  companion object {
    private const val ArgsKey = "ScreenshotActivity.Args"

    fun start(activity: Activity, args: Args) {
      val start = Intent(activity, ScreenshotActivity::class.java).putExtra(ArgsKey, args)
      activity.startActivity(start)
      activity.overridePendingTransition(R.anim.slide_up, R.anim.no_op)
    }
  }

  @Parcelize
  @TypeParceler<File, FileParceler>
  data class Args(val screenshotFile: File) : Parcelable
}
