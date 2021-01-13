package io.mehow.squashit.report.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.broadcastIn
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// TODO: Remove this custom operator once it is provided by the library.
//   https://github.com/Kotlin/kotlinx.coroutines/issues/1261
internal fun <T> Flow<T>.shareIn(scope: CoroutineScope): Flow<T> {
  lateinit var sharedChannel: BroadcastChannel<T>
  var refCounter = 0
  val mutex = Mutex()

  suspend fun addCollector() = mutex.withLock {
    refCounter++
    if (refCounter == 1) sharedChannel = broadcastIn(scope)
  }

  suspend fun removeCollector() = mutex.withLock {
    refCounter--
    if (refCounter == 0) sharedChannel.cancel()
  }

  return flow {
    addCollector()
    try {
      emitAll(sharedChannel.asFlow())
    } finally {
      removeCollector()
    }
  }
}
