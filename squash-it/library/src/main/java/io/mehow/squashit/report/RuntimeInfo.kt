package io.mehow.squashit.report

import android.content.Context
import android.os.Build
import android.os.Build.VERSION
import android.os.Parcelable
import android.util.DisplayMetrics
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.os.ConfigurationCompat
import kotlinx.android.parcel.Parcelize
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Parcelize
internal data class RuntimeInfo(
  val app: AppInfo,
  val device: DeviceInfo,
  val osInfo: OsInfo
) : Describable, Parcelable {
  override fun describe(): String {
    return """
      |${app.describe()}
      |
      |${device.describe()}
      |
      |${osInfo.describe()}
    """.trimMargin()
  }

  companion object {
    fun create(context: Context): RuntimeInfo {
      return RuntimeInfo(createAppInfo(context), createDeviceInfo(context), createOsInfo())
    }

    private fun createAppInfo(context: Context): AppInfo {
      val packageName = context.packageName
      val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
      val versionName = packageInfo?.versionName ?: "UNKNOWN"
      val versionCode = if (packageInfo == null) "UNKNOWN"
      else "${PackageInfoCompat.getLongVersionCode(packageInfo)}"
      return AppInfo(versionName, versionCode, packageName)
    }

    @Suppress("LongMethod")
    private fun createDeviceInfo(context: Context): DeviceInfo {
      val metrics = context.resources.displayMetrics
      val densityBucket = metrics.densityBucket
      val resolution = "${metrics.heightPixels}x${metrics.widthPixels}"
      val density = "${metrics.densityDpi}dpi ($densityBucket)"

      val locales = ConfigurationCompat.getLocales(context.resources.configuration)
      val localeList = mutableListOf<Locale>()
      for (i in 0 until locales.size()) {
        localeList.add(locales[i])
      }
      return DeviceInfo(
          manufacturer = Build.MANUFACTURER,
          model = Build.MODEL,
          resolution = resolution,
          density = density,
          locales = localeList,
          createdAt = Date(),
          timeZone = TimeZone.getDefault()
      )
    }

    private fun createOsInfo(): OsInfo {
      return OsInfo(VERSION.RELEASE, VERSION.SDK_INT)
    }

    private val DisplayMetrics.densityBucket: String
      get() {
        return when (densityDpi) {
          DisplayMetrics.DENSITY_LOW -> "ldpi"
          DisplayMetrics.DENSITY_MEDIUM -> "mdpi"
          DisplayMetrics.DENSITY_HIGH -> "hdpi"
          DisplayMetrics.DENSITY_XHIGH -> "xhdpi"
          DisplayMetrics.DENSITY_XXHIGH -> "xxhdpi"
          DisplayMetrics.DENSITY_XXXHIGH -> "xxxhdpi"
          DisplayMetrics.DENSITY_TV -> "tv"
          in 0..DisplayMetrics.DENSITY_LOW -> "below ldpi"
          in DisplayMetrics.DENSITY_LOW..DisplayMetrics.DENSITY_MEDIUM -> "ldpi–mdpi"
          in DisplayMetrics.DENSITY_MEDIUM..DisplayMetrics.DENSITY_HIGH -> "mdpi–hdpi"
          in DisplayMetrics.DENSITY_HIGH..DisplayMetrics.DENSITY_XHIGH -> "hdpi–xhdpi"
          in DisplayMetrics.DENSITY_XHIGH..DisplayMetrics.DENSITY_XXHIGH -> "xhdpi–xxhdpi"
          in DisplayMetrics.DENSITY_XXHIGH..DisplayMetrics.DENSITY_XXXHIGH -> "xxhdpi–xxxhdpi"
          else -> if (densityDpi > 0) "above xxxhdpi" else "unknown"
        }
      }
  }
}
