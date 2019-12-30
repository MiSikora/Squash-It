package io.mehow.screenshot

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.palette.graphics.Palette
import io.mehow.BaseActivity
import io.mehow.FileParceler
import io.mehow.squashit.R
import io.mehow.squashit.SquashItConfig
import io.mehow.squashit.extensions.enableEdgeToEdgeAndNightMode
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.TypeParceler
import java.io.File

internal class ScreenshotActivity : BaseActivity() {
  private lateinit var screenshotFile: File
  private lateinit var screenshotBitmap: Bitmap

  override fun onCreate(inState: Bundle?) {
    super.onCreate(inState)
    screenshotFile = intent.getParcelableExtra<Args>(ArgsKey)!!.screenshotFile
    screenshotBitmap = BitmapFactory.decodeFile(screenshotFile.path)

    window.decorView.enableEdgeToEdgeAndNightMode()
    contrastBackground()
    setContentView(R.layout.edit_screenshot)
    setUpScreenshot()

    findViewById<View>(R.id.goToReport).setOnClickListener {
      SquashItConfig.create(this).startActivity(this, screenshotFile)
      finish()
    }
  }

  private fun contrastBackground() {
    val palette = Palette.from(screenshotBitmap).clearFilters().generate()
    val isDark = palette.dominantSwatch
        ?.let { ColorUtils.calculateLuminance(it.rgb) < 0.25 } == true
    delegate.localNightMode = if (isDark) MODE_NIGHT_NO else MODE_NIGHT_YES
  }

  private fun setUpScreenshot() {
    val screenshot = findViewById<ImageView>(R.id.screenshot)
    screenshot.setImageBitmap(screenshotBitmap)
    ViewCompat.setOnApplyWindowInsetsListener(screenshot) { _, insets ->
      screenshot.updateLayoutParams<MarginLayoutParams> {
        updateMargins(top = (insets.systemWindowInsetTop * 1.5).toInt())
      }
      return@setOnApplyWindowInsetsListener insets
    }
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
  internal data class Args(val screenshotFile: File) : Parcelable
}
