package io.mehow.squashit

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.Locale

@Parcelize
internal data class ParcelableLocale(
  val language: String,
  val country: String,
  val variant: String
) : Parcelable {
  fun toLocale(): Locale {
    return Locale(language, country, variant)
  }

  companion object {
    fun fromLocale(locale: Locale): ParcelableLocale {
      return ParcelableLocale(locale.language, locale.country, locale.variant)
    }
  }
}
