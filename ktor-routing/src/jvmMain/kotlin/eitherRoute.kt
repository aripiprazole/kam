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
@file:OptIn(ExperimentalTypeInference::class)

package co.knoten.kam

import arrow.core.Either
import arrow.core.Validated
import arrow.core.computations.EitherEffect
import arrow.core.computations.either
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.feature
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.RouteSelector
import io.ktor.routing.application
import io.ktor.util.pipeline.PipelineContext
import kotlin.experimental.ExperimentalTypeInference
import kotlin.reflect.typeOf

/**
 * Describes a node in a routing tree with [arrow.core.Either] bindings.
 *
 * @param route is an instance of default [Route]
 */
open class EitherRoute(val route: Route) {
  /**
   * Allows using route instance for building additional routes
   */
  fun invoke(body: EitherRoute.() -> Unit): Unit = body()

  /**
   * Installs a handler into this route which will be called when the route is selected for a call
   */
  fun handle(handler: EitherPipelineInterceptor<Unit, ApplicationCall, Any>) {
    route.handle {
      val pipelineContextOriginal = this

      val result = either<Any, Unit> {
        val pipelineContext: EitherPipelineContext<Unit, ApplicationCall, Any, *> =
          EitherPipelineContextImpl(pipelineContextOriginal, this)

        handler(pipelineContext, Unit)
      }
      
      when (result) {
        is Either.Right -> {}
        is Either.Left -> when (val value = result.value) {
          is Respondable -> with(value) { 
            call.respondToPipeline(null) 
          }
          else -> call.respond(HttpStatusCode.InternalServerError, value)
        }
      }
    }
  }

  /**
   * Installs a handler into this route which will be called when the route is selected for a call
   */
  @JvmName("handleLeftTyped")
  inline fun <reified E : Any> handle(crossinline handler: EitherPipelineInterceptor<Unit, ApplicationCall, E>) {
    val defaultResponder: EitherResponder<E> = { value ->
      respond(HttpStatusCode.InternalServerError, value)
    }

    route.handle {
      val routing = route.application.feature(EitherRouting)
      
      val pipelineContextOriginal = this

      val result = either<E, Unit> {
        val pipelineContext: EitherPipelineContext<Unit, ApplicationCall, E, *> =
          EitherPipelineContextImpl(pipelineContextOriginal, this)

        handler(pipelineContext, Unit)
      }

      @Suppress("UNCHECKED_CAST")
      when (result) {
        is Either.Right -> {}
        is Either.Left -> when (val value = result.value) {
          is Respondable -> with(value) { call.respondToPipeline(typeOf<E>()) }
          else -> routing.responders
            .getOrDefault(value::class, defaultResponder)
            .invoke(call, value)
        }
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

val EitherRoute.application: Application
  get() = route.application

fun Route.e(): EitherRoute {
  return EitherRoute(this)
}

typealias EitherPipelineInterceptor<TSubject, TContext, E> =
  suspend EitherPipelineContext<TSubject, TContext, E, *>.(TSubject) -> Unit

typealias AEitherPipelineContext<TSubject, TContext> =
  EitherPipelineContext<TSubject, TContext, Any, *>

interface EitherPipelineContext<TSubject : Any, TContext : Any, E, V> :
  PipelineContext<TSubject, TContext>, EitherEffect<E, V> {
  override suspend fun <B> Either<E, B>.bind(): B

  override suspend fun <B> Validated<E, B>.bind(): B

  override suspend fun <B> Result<B>.bind(transform: (Throwable) -> E): B
}

@PublishedApi
internal class EitherPipelineContextImpl<TSubject : Any, TContext : Any, E, A>(
  pipelineContext: PipelineContext<TSubject, TContext>,
  eitherEffect: EitherEffect<E, A>,
) : EitherPipelineContext<TSubject, TContext, E, A>,
  PipelineContext<TSubject, TContext> by pipelineContext,
  EitherEffect<E, A> by eitherEffect
