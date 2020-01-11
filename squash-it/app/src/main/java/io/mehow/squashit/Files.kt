package io.mehow.squashit

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun File.toCredentialsUri(context: Context): Uri {
  return FileProvider.getUriForFile(context, "io.mehow.squashit.fileprovider", this)
}
