package io.mehow.squashit.api.adapter

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import io.mehow.squashit.api.EpicFieldsResponse
import kotlin.String

@Suppress("LongMethod", "ComplexMethod", "StringLiteralDuplication")
internal class EpicFieldsResponseJsonAdapter(
  private val fieldName: String,
  moshi: Moshi
) : JsonAdapter<EpicFieldsResponse>() {
  private val options: JsonReader.Options = JsonReader.Options.of(fieldName)

  private val stringAdapter: JsonAdapter<String> =
    moshi.adapter(String::class.java, emptySet(), "epicName")

  override fun toString(): String = "GeneratedJsonAdapter(EpicFieldsResponse)"

  override fun fromJson(reader: JsonReader): EpicFieldsResponse {
    var epicName: String? = null
    reader.beginObject()
    while (reader.hasNext()) {
      when (reader.selectName(options)) {
        0 -> epicName = stringAdapter.fromJson(reader) ?: reader.unexpectedNull(
            "epicName",
            fieldName
        )
        -1 -> {
          // Unknown name, skip it.
          reader.skipName()
          reader.skipValue()
        }
      }
    }
    reader.endObject()
    return EpicFieldsResponse(epicName = epicName ?: reader.missingProperty("epicName", fieldName))
  }

  override fun toJson(writer: JsonWriter, value: EpicFieldsResponse?) {
    if (value == null) {
      throw NullPointerException("value was null! Wrap in .nullSafe() to write nullable values.")
    }
    writer.beginObject()
    writer.name(fieldName)
    stringAdapter.toJson(writer, value.epicName)
    writer.endObject()
  }
}
