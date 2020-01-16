package io.mehow.squashit.report.api.adapter

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import io.mehow.squashit.report.api.IssueTypeRequest
import io.mehow.squashit.report.api.NewIssueFieldsRequest
import io.mehow.squashit.report.api.ParentKeyRequest
import io.mehow.squashit.report.api.ProjectRequest
import io.mehow.squashit.report.api.ReporterRequest
import kotlin.String

@Suppress("LongMethod", "ComplexMethod", "StringLiteralDuplication")
internal class NewIssueFieldsRequestJsonAdapter(
  private val epicFieldName: String,
  moshi: Moshi
) : JsonAdapter<NewIssueFieldsRequest>() {
  private val options: JsonReader.Options = JsonReader.Options.of(
      "project", "parent", "issuetype", "summary",
      "reporter", "description", epicFieldName
  )

  private val projectRequestAdapter: JsonAdapter<ProjectRequest> =
    moshi.adapter(ProjectRequest::class.java, emptySet(), "project")

  private val nullableParentKeyAdapter: JsonAdapter<ParentKeyRequest?> =
    moshi.adapter(ParentKeyRequest::class.java, emptySet(), "parentKey")

  private val issueTypeRequestAdapter: JsonAdapter<IssueTypeRequest> =
    moshi.adapter(IssueTypeRequest::class.java, emptySet(), "issueType")

  private val stringAdapter: JsonAdapter<String> =
    moshi.adapter(String::class.java, emptySet(), "summary")

  private val nullableReporterRequestAdapter: JsonAdapter<ReporterRequest?> =
    moshi.adapter(ReporterRequest::class.java, emptySet(), "reporter")

  private val nullableStringAdapter: JsonAdapter<String?> =
    moshi.adapter(String::class.java, emptySet(), "epic")

  override fun toString(): String = "GeneratedJsonAdapter(NewIssueFieldsRequest)"

  override fun fromJson(reader: JsonReader): NewIssueFieldsRequest {
    var project: ProjectRequest? = null
    var parentKey: ParentKeyRequest? = null
    var issueType: IssueTypeRequest? = null
    var summary: String? = null
    var reporter: ReporterRequest? = null
    var description: String? = null
    var epic: String? = null
    reader.beginObject()
    while (reader.hasNext()) {
      when (reader.selectName(options)) {
        0 -> project = projectRequestAdapter.fromJson(reader) ?: reader.unexpectedNull(
            "project",
            "project"
        )
        1 -> parentKey = nullableParentKeyAdapter.fromJson(reader)
        2 -> issueType = issueTypeRequestAdapter.fromJson(reader) ?: reader.unexpectedNull(
            "issueType",
            "issuetype"
        )
        3 -> summary = stringAdapter.fromJson(reader) ?: reader.unexpectedNull(
            "summary",
            "summary"
        )
        4 -> reporter = nullableReporterRequestAdapter.fromJson(reader)
        5 -> description = stringAdapter.fromJson(reader) ?: reader.unexpectedNull(
            "description",
            "description"
        )
        6 -> epic = nullableStringAdapter.fromJson(reader)
        -1 -> {
          // Unknown name, skip it.
          reader.skipName()
          reader.skipValue()
        }
      }
    }
    reader.endObject()
    return NewIssueFieldsRequest(
        project = project ?: reader.missingProperty("project", "project"),
        parent = parentKey,
        issueType = issueType ?: reader.missingProperty("issueType", "issuetype"),
        summary = summary ?: reader.missingProperty("summary", "summary"),
        reporter = reporter,
        description = description ?: reader.missingProperty("description", "description"),
        epic = epic
    )
  }

  override fun toJson(writer: JsonWriter, value: NewIssueFieldsRequest?) {
    if (value == null) {
      throw NullPointerException("value was null! Wrap in .nullSafe() to write nullable values.")
    }
    writer.beginObject()
    writer.name("project")
    projectRequestAdapter.toJson(writer, value.project)
    writer.name("parent")
    nullableParentKeyAdapter.toJson(writer, value.parent)
    writer.name("issuetype")
    issueTypeRequestAdapter.toJson(writer, value.issueType)
    writer.name("summary")
    stringAdapter.toJson(writer, value.summary)
    writer.name("reporter")
    nullableReporterRequestAdapter.toJson(writer, value.reporter)
    writer.name("description")
    stringAdapter.toJson(writer, value.description)
    writer.name(epicFieldName)
    nullableStringAdapter.toJson(writer, value.epic)
    writer.endObject()
  }
}
