package io.mehow.squashit.api

internal sealed class Response<out T : Any> {
  data class Success<T : Any>(val value: T) : Response<T>()

  sealed class Failure : Response<Nothing>() {
    data class Http(val httpCode: Int) : Failure()
    data class Network(val throwable: Throwable) : Failure()
  }
}
