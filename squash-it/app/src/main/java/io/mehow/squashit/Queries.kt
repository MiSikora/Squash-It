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

internal fun MatrixCursor.add(row: List<ColumnPrimitive<*>>) {
  addRow(row.map { it.value })
}
