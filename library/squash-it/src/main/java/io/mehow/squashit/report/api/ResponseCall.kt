package io.mehow.squashit.report.api

import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response as RetrofitResponse

internal class ResponseCall<T : Any>(private val delegate: Call<T>) : Call<Response<T>> {
  override fun execute(): RetrofitResponse<Response<T>> {
    val response = try {
      val retrofitResponse = delegate.execute()
      retrofitResponse.toResponse()
    } catch (throwable: Throwable) {
      Response.Failure.Network(throwable)
    }
    return RetrofitResponse.success(response)
  }

  override fun enqueue(callback: Callback<Response<T>>) {
    delegate.enqueue(object : Callback<T> {
      override fun onFailure(call: Call<T>, throwable: Throwable) {
        val response = Response.Failure.Network(throwable)
        callback.onResponse(this@ResponseCall, RetrofitResponse.success(response))
      }

      override fun onResponse(call: Call<T>, retrofitResponse: RetrofitResponse<T>) {
        val response = retrofitResponse.toResponse()
        callback.onResponse(this@ResponseCall, RetrofitResponse.success(response))
      }
    })
  }

  override fun clone(): Call<Response<T>> {
    return ResponseCall(delegate.clone())
  }

  override fun request(): Request {
    return delegate.request()
  }

  override fun cancel() {
    delegate.cancel()
  }

  override fun isCanceled(): Boolean {
    return delegate.isCanceled
  }

  override fun isExecuted(): Boolean {
    return delegate.isExecuted
  }

  override fun timeout(): Timeout {
    return delegate.timeout()
  }

  fun RetrofitResponse<T>.toResponse(): Response<T> {
    return if (isSuccessful) Response.Success(body()!!) else Response.Failure.Http(code())
  }
}
