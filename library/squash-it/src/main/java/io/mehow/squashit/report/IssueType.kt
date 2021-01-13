package io.mehow.squashit.report

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IssueType internal constructor(val id: String, val name: String)
