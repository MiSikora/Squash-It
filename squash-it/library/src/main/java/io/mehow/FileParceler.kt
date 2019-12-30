package io.mehow

import android.os.Parcel
import kotlinx.android.parcel.Parceler
import java.io.File

internal object FileParceler : Parceler<File?> {
  override fun create(parcel: Parcel) = parcel.readString()?.let { File(it) }
  override fun File?.write(parcel: Parcel, flags: Int) = parcel.writeString(this?.path)
}
