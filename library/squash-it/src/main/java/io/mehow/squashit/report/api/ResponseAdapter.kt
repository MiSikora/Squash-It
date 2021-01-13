package io.mehow.squashit.report.api

import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

internal class ResponseAdapter<T : Any>(
  private val type: Type
) : CallAdapter<T, Call<Response<T>>> {
  override fun responseType(): Type {
    return type
  }

  override fun adapt(call: Call<T>): Call<Response<T>> {
    return ResponseCall(call)
  }

  object Factory : CallAdapter.Factory() {
    override fun get(
      returnType: Type,
      annotations: Array<Annotation>,
      retrofit: Retrofit
    ): CallAdapter<*, *>? {
      val rawType = getRawType(returnType)
      if (rawType != Call::class.java) return null

      val callType = getParameterUpperBound(0, returnType as ParameterizedType)
      if (getRawType(callType) != Response::class.java) return null

      val responseType = getParameterUpperBound(0, callType as ParameterizedType)
      return ResponseAdapter<Any>(responseType)
    }
  }
}
