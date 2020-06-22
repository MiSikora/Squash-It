@file:Suppress("LongMethod")

package io.mehow.squashit

/*
 * Copyright (C) 2018 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import io.mehow.squashit.AssertEvent.Complete
import io.mehow.squashit.AssertEvent.Error
import io.mehow.squashit.AssertEvent.Item
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

suspend fun <T> Flow<T>.test(timeoutMs: Long = 1_000L, validate: suspend FlowAssert<T>.() -> Unit) {
  coroutineScope {
    val events = Channel<AssertEvent<T>>(UNLIMITED)
    val collectJob = launch {
      val terminalEvent = try {
        collect { item ->
          events.send(Item(item))
        }
        Complete
      } catch (_: CancellationException) {
        null
      } catch (t: Throwable) {
        Error(t)
      }
      if (terminalEvent != null) {
        events.send(terminalEvent)
      }
      events.close()
    }
    val flowAssert = FlowAssert(events, collectJob, timeoutMs)
    val ensureConsumed = try {
      flowAssert.validate()
      true
    } catch (e: CancellationException) {
      if (e !== ignoreRemainingEventsException) {
        throw e
      }
      false
    }
    if (ensureConsumed) {
      flowAssert.expectNoMoreEvents()
    }
  }
}

internal val ignoreRemainingEventsException = CancellationException("Ignore remaining events")

internal sealed class AssertEvent<out T> {
  object Complete : AssertEvent<Nothing>()
  data class Error(val throwable: Throwable) : AssertEvent<Nothing>()
  data class Item<T>(val item: T) : AssertEvent<T>()
}

class FlowAssert<T> internal constructor(
  private val events: Channel<AssertEvent<T>>,
  private val collectJob: Job,
  private val timeoutMs: Long
) {
  private suspend fun <T> withTimeout(body: suspend () -> T): T {
    return if (timeoutMs == 0L) {
      body()
    } else {
      withTimeout(timeoutMs) {
        body()
      }
    }
  }

  fun cancel() {
    collectJob.cancel()
  }

  fun cancelAndIgnoreRemainingEvents(): Nothing {
    cancel()
    throw ignoreRemainingEventsException
  }

  fun expectNoEvents() {
    val event = events.poll()
    if (event != null) {
      throw AssertionError("Expected no events but found $event")
    }
  }

  suspend fun expectNoMoreEvents() {
    val event = withTimeout {
      events.receiveOrNull()
    }
    if (event != null) {
      throw AssertionError("Expected no more events but found $event")
    }
  }

  suspend fun expectItem(): T {
    val event = withTimeout {
      events.receive()
    }
    if (event !is Item<T>) {
      throw AssertionError("Expected item but was $event")
    }
    return event.item
  }

  suspend fun expectComplete() {
    val event = withTimeout {
      events.receive()
    }
    if (event != Complete) {
      throw AssertionError("Expected complete but was $event")
    }
  }

  suspend fun expectError(): Throwable {
    val event = withTimeout {
      events.receive()
    }
    if (event !is Error) {
      throw AssertionError("Expected error but was $event")
    }
    return event.throwable
  }
}
