package io.mehow.squashit.report.api

import com.squareup.moshi.Moshi
import io.mehow.squashit.report.IssueKey
import io.mehow.squashit.report.ReportConfig
import okhttp3.Call
import okhttp3.Credentials
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

internal interface JiraApi {
  @GET("rest/api/2/search")
  suspend fun getEpics(@Query("jql") epicJql: EpicJql): Response<EpicsResponse>

  @GET("rest/api/2/project/{projectKey}")
  suspend fun getProject(@Path("projectKey") projectKey: String): Response<ProjectResponse>

  @GET("rest/api/2/project/{projectKey}/role/{roleId}")
  suspend fun getUsers(
    @Path("projectKey") projectKey: String,
    @Path("roleId") roleId: String
  ): Response<RoleResponse>

  @POST("rest/api/2/issue")
  suspend fun createNewIssue(@Body request: NewIssueRequest): Response<CreateNewIssueResponse>

  @POST("rest/api/2/issue/{issueKey}/comment")
  suspend fun addComment(
    @Path("issueKey") issueKey: IssueKey,
    @Body request: AddCommentRequest
  ): Response<Unit>

  @Multipart
  @Headers("X-Atlassian-Token: no-check")
  @POST("/rest/api/2/issue/{issueKey}/attachments")
  suspend fun attachFiles(
    @Path("issueKey") issueKey: IssueKey,
    @Part files: List<MultipartBody.Part>
  ): Response<Unit>

  companion object {
    fun create(moshi: Moshi, config: ReportConfig.Valid): JiraApi {
      val credentials = Credentials.basic(config.userEmail, config.userToken)
      return Retrofit.Builder()
          .baseUrl(config.jiraUrl)
          .withAuthClient(credentials)
          .addConverterFactory(MoshiConverterFactory.create(moshi))
          .addCallAdapterFactory(ResponseAdapter.Factory)
          .build()
          .create()
    }

    private fun Retrofit.Builder.withAuthClient(credentials: String): Retrofit.Builder {
      val lazyClient = createClient(credentials)
      return callFactory(object : Call.Factory {
        override fun newCall(request: Request) = lazyClient.value.newCall(request)
      })
    }

    private fun createClient(credentials: String): Lazy<OkHttpClient> {
      return lazy {
        return@lazy OkHttpClient.Builder()
            .addInterceptor { chain ->
              val authRequest = chain.request()
                  .newBuilder()
                  .header("Authorization", credentials)
                  .build()
              return@addInterceptor chain.proceed(authRequest)
            }
            .build()
      }
    }
  }
}
