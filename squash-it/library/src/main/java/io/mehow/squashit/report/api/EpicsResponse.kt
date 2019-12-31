package io.mehow.squashit.report.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class EpicsResponse(val issues: List<EpicIssueResponse>)
