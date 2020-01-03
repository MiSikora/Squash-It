package io.mehow.squashit.report

import com.jakewharton.byteunits.DecimalByteUnit

internal data class DiskSize(val value: Long, val unit: DecimalByteUnit) {
  val displayValue = DecimalByteUnit.format(value)
}
