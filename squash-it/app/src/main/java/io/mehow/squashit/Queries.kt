package io.mehow.squashit

import android.database.Cursor
import android.database.MatrixCursor
import com.squareup.sqldelight.Query

fun <T : Any> Query<T>.asCursor(
  vararg columnNames: String,
  mapper: (T) -> List<ColumnPrimitive<*>>
): Cursor {
  val elements = executeAsList().map(mapper)
  return MatrixCursor(columnNames, elements.size).apply {
    for (element in elements) {
      add(element)
    }
  }
}

class ColumnPrimitive<T> private constructor(val value: T?) {
  companion object {
    operator fun invoke(value: Short?): ColumnPrimitive<Short> = ColumnPrimitive(value)
    operator fun invoke(value: Int?): ColumnPrimitive<Int> = ColumnPrimitive(value)
    operator fun invoke(value: Long?): ColumnPrimitive<Long> = ColumnPrimitive(value)
    operator fun invoke(value: Float?): ColumnPrimitive<Float> = ColumnPrimitive(value)
    operator fun invoke(value: Double?): ColumnPrimitive<Double> = ColumnPrimitive(value)
    operator fun invoke(value: String?): ColumnPrimitive<String> = ColumnPrimitive(value)
    operator fun invoke(value: ByteArray?): ColumnPrimitive<ByteArray> = ColumnPrimitive(value)
  }
}

internal fun MatrixCursor.add(row: List<ColumnPrimitive<*>>) {
  addRow(row.map { it.value })
}
