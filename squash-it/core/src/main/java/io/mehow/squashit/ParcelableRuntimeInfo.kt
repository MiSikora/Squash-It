package io.mehow.squashit

import android.content.Context
import android.os.Build
import android.os.Build.VERSION.RELEASE
import android.os.Build.VERSION.SDK_INT
import android.os.Parcelable
import android.util.DisplayMetrics
import android.util.DisplayMetrics.DENSITY_HIGH
import android.util.DisplayMetrics.DENSITY_LOW
import android.util.DisplayMetrics.DENSITY_MEDIUM
import android.util.DisplayMetrics.DENSITY_TV
import android.util.DisplayMetrics.DENSITY_XHIGH
import android.util.DisplayMetrics.DENSITY_XXHIGH
import android.util.DisplayMetrics.DENSITY_XXXHIGH
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.os.ConfigurationCompat
import kotlinx.android.parcel.Parcelize
import java.util.TimeZone

@Parcelize
internal data class ParcelableRuntimeInfo(
  val app: ParcelableAppInfo,
  val device: ParcelableDeviceInfo,
  val os: ParcelableOsInfo
) : Parcelable {
  fun toRuntimeInfo(): RuntimeInfo {
    return RuntimeInfo(app.toAppInfo(), device.toDeviceInfo(), os.toOsInfo())
  }

  companion object {
    fun create(context: Context): ParcelableRuntimeInfo {
      return ParcelableRuntimeInfo(
          createAppInfo(context),
          createDeviceInfo(context),
          createOsInfo()
      )
    }

    private fun createAppInfo(context: Context): ParcelableAppInfo {
      val packageName = context.packageName
      val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
      val versionName = packageInfo?.versionName ?: "UNKNOWN"
      val versionCode = if (packageInfo == null) "UNKNOWN"
      else "${PackageInfoCompat.getLongVersionCode(packageInfo)}"
      return ParcelableAppInfo(
          versionName,
          versionCode,
          packageName
      )
    }

    private fun createDeviceInfo(context: Context): ParcelableDeviceInfo {
      val metrics = context.resources.displayMetrics
      val densityBucket = metrics.densityBucket
      val resolution = "${metrics.heightPixels}x${metrics.widthPixels}"
      val density = "${metrics.densityDpi}dpi ($densityBucket)"

      val locales = ConfigurationCompat.getLocales(context.resources.configuration)
      val localeList = mutableListOf<ParcelableLocale>()
      for (i in 0 until locales.size()) {
        localeList.add(ParcelableLocale.fromLocale(locales[i]))
      }
      return ParcelableDeviceInfo(
          Build.MANUFACTURER,
          Build.MODEL,
          resolution,
          density,
          localeList,
          TimeZone.getDefault().id
      )
    }

    private fun createOsInfo(): ParcelableOsInfo {
      return ParcelableOsInfo(RELEASE, SDK_INT)
    }

    private val DisplayMetrics.densityBucket: String
      get() {
        return when (densityDpi) {
          DENSITY_LOW -> "ldpi"
          DENSITY_MEDIUM -> "mdpi"
          DENSITY_HIGH -> "hdpi"
          DENSITY_XHIGH -> "xhdpi"
          DENSITY_XXHIGH -> "xxhdpi"
          DENSITY_XXXHIGH -> "xxxhdpi"
          DENSITY_TV -> "tv"
          in 0..DENSITY_LOW -> "below ldpi"
          in DENSITY_LOW..DENSITY_MEDIUM -> "ldpi–mdpi"
          in DENSITY_MEDIUM..DENSITY_HIGH -> "mdpi–hdpi"
          in DENSITY_HIGH..DENSITY_XHIGH -> "hdpi–xhdpi"
          in DENSITY_XHIGH..DENSITY_XXHIGH -> "xhdpi–xxhdpi"
          in DENSITY_XXHIGH..DENSITY_XXXHIGH -> "xxhdpi–xxxhdpi"
          else -> if (densityDpi > 0) "above xxxhdpi" else "unknown"
        }
      }
  }
}
