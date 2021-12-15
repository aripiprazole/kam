/*
 *    Copyright 2021 Knoten
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

@file:Suppress("unused")

package co.knoten.kam

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationFeature
import io.ktor.application.featureOrNull
import io.ktor.application.install
import io.ktor.routing.Routing
import io.ktor.routing.RoutingResolveTrace
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.ContextDsl
import io.ktor.util.pipeline.PipelineContext
import kotlin.reflect.KClass

/**
 * Root routing node with [arrow.core.Either] bindings for an [Application]
 * @param routing is an instance of default [Routing]
 */
class EitherRouting(private val routing: Routing) : EitherRoute(routing) {
  @PublishedApi
  internal val responders = mutableMapOf<KClass<*>, EitherResponder<Any>>()

  /**
   * Register a responder for the given type [T]
   */
  inline fun <reified T : Any> responder(noinline function: EitherResponder<T>) {
    responders[T::class] = { function(it as T) }
  }

  /**
   * Register a route resolution trace function.
   * See https://ktor.io/servers/features/routing.html#tracing for details
   */
  fun trace(block: (RoutingResolveTrace) -> Unit): Unit = routing.trace(block)

  suspend fun interceptor(context: PipelineContext<Unit, ApplicationCall>): Unit =
    routing.interceptor(context)

  companion object Feature : ApplicationFeature<Application, EitherRouting, EitherRouting> {
    override val key: AttributeKey<EitherRouting> = AttributeKey("EitherRouting")

    override fun install(
      pipeline: Application,
      configure: EitherRouting.() -> Unit,
    ): EitherRouting {
      return EitherRouting(pipeline.install(Routing)).apply(configure)
    }
  }
}

/**
 * Gets or installs a [EitherRouting] feature for the this [Application] and runs a [configuration] script on it
 */
@ContextDsl
fun Application.routingE(configuration: EitherRouting.() -> Unit): EitherRouting {
  return featureOrNull(EitherRouting)?.apply(configuration) ?: install(EitherRouting, configuration)
}
