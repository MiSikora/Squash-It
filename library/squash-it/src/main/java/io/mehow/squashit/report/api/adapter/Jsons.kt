package io.mehow.squashit.report.api.adapter

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader

internal fun JsonReader.unexpectedNull(
  propertyName: String,
  jsonName: String
): Nothing {
  val message = if (jsonName == propertyName) "Non-null value '$propertyName' was null at $path"
  else "Non-null value '$propertyName' (JSON name '$jsonName') was null at $path"
  throw JsonDataException(message)
}

internal fun JsonReader.missingProperty(
  propertyName: String,
  jsonName: String
): Nothing {
  val message = if (jsonName == propertyName) "Required value '$propertyName' missing at $path"
  else "Required value '$propertyName' (JSON name '$jsonName') missing at $path"
  throw JsonDataException(message)
}
