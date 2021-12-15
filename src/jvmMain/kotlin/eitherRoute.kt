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

import arrow.core.computations.EitherEffect
import arrow.core.computations.either
import io.ktor.application.ApplicationCall
import io.ktor.routing.Route
import io.ktor.routing.RouteSelector
import io.ktor.util.pipeline.PipelineContext

/**
 * Describes a node in a routing tree with [arrow.core.Either] bindings.
 *
 * @param route is an instance of default [Route]
 */
open class EitherRoute(@PublishedApi internal val route: Route) {
  /**
   * Allows using route instance for building additional routes
   */
  fun invoke(body: EitherRoute.() -> Unit): Unit = body()

  /**
   * Installs a handler into this route which will be called when the route is selected for a call
   */
  fun handle(handler: EitherPipelineInterceptor<Unit, ApplicationCall>) {
    route.handle {
      val pipelineContextOriginal = this
      
      either<Any, Unit> {
        val context: EitherPipelineContext<Unit, ApplicationCall, *> = EitherPipelineContextImpl(
          pipelineContextOriginal, this
        )
        
        handler(context, Unit)
      }
    }
  }
  
  fun afterIntercepted() {
    route.afterIntercepted()
  }

  fun createChild(selector: RouteSelector): EitherRoute {
    return route.createChild(selector).e()
  }
}

fun Route.e(): EitherRoute {
  return EitherRoute(this)
}

typealias EitherPipelineInterceptor<TSubject, TContext> =
  suspend EitherPipelineContext<TSubject, TContext, *>.(TSubject) -> Unit

interface EitherPipelineContext<TSubject : Any, TContext : Any, V> :
  PipelineContext<TSubject, TContext>, EitherEffect<Any, V>

@PublishedApi
internal class EitherPipelineContextImpl<TSubject : Any, TContext : Any, V>(
  pipelineContext: PipelineContext<TSubject, TContext>,
  eitherEffect: EitherEffect<Any, V>,
) : EitherPipelineContext<TSubject, TContext, V>,
  PipelineContext<TSubject, TContext> by pipelineContext,
  EitherEffect<Any, V> by eitherEffect
