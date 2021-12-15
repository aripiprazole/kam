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
import io.ktor.routing.Routing
import io.ktor.routing.RoutingResolveTrace
import io.ktor.routing.routing
import io.ktor.util.pipeline.ContextDsl
import io.ktor.util.pipeline.PipelineContext

/**
 * Root routing node with [arrow.core.Either] bindings for an [Application]
 * @param routing is an instance of default [Routing]
 */
class EitherRouting(private val routing: Routing) : EitherRoute(routing) {
  /**
   * Register a route resolution trace function.
   * See https://ktor.io/servers/features/routing.html#tracing for details
   */
  fun trace(block: (RoutingResolveTrace) -> Unit): Unit = routing.trace(block)

  suspend fun interceptor(context: PipelineContext<Unit, ApplicationCall>): Unit =
    routing.interceptor(context)
}

@ContextDsl
fun Application.routingE(configuration: EitherRouting.() -> Unit) {
  routing {
    configuration(EitherRouting(this))
  }
}
