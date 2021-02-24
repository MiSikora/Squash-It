package io.mehow.squashit

import android.os.Parcel
import kotlinx.parcelize.Parceler
import java.io.File

internal object FileParceler : Parceler<File?> {
  override fun create(parcel: Parcel) = parcel.readString()?.let { File(it) }
  override fun File?.write(parcel: Parcel, flags: Int) = parcel.writeString(this?.path)
}
