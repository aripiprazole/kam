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

import arrow.core.Either
import arrow.core.computations.either
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.response.respond
import io.ktor.routing.ConstantParameterRouteSelector
import io.ktor.routing.HttpAcceptRouteSelector
import io.ktor.routing.HttpHeaderRouteSelector
import io.ktor.routing.HttpMethodRouteSelector
import io.ktor.routing.OptionalParameterRouteSelector
import io.ktor.routing.ParameterRouteSelector
import io.ktor.routing.Route
import io.ktor.routing.createRouteFromPath
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.head
import io.ktor.routing.options
import io.ktor.routing.patch
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.util.pipeline.ContextDsl
import io.ktor.util.pipeline.PipelineContext
import co.knoten.kam.EitherRoute as RouteE

/**
 * Builds a route to match specified [path]
 */
@ContextDsl
fun RouteE.route(path: String, build: RouteE.() -> Unit): RouteE {
  return createRouteFromPath(path).apply(build)
}

/**
 * Builds a route to match specified [method] and [path]
 */
@ContextDsl
fun RouteE.route(path: String, method: HttpMethod, build: RouteE.() -> Unit): RouteE {
  return createRouteFromPath(path)
    .createChild(HttpMethodRouteSelector(method))
    .apply(build)
}

/**
 * Builds a route to match specified [method]
 */
@ContextDsl
fun RouteE.method(method: HttpMethod, build: RouteE.() -> Unit): RouteE {
  return createChild(HttpMethodRouteSelector(method)).apply(build)
}

/**
 * Builds a route to match parameter with specified [name] and [value]
 */
@ContextDsl
fun RouteE.param(name: String, value: String, build: RouteE.() -> Unit): RouteE {
  return createChild(ConstantParameterRouteSelector(name, value)).apply(build)
}

/**
 * Builds a route to match parameter with specified [name] and capture its value
 */
@ContextDsl
fun RouteE.param(name: String, build: RouteE.() -> Unit): RouteE {
  return createChild(ParameterRouteSelector(name)).apply(build)
}

/**
 * Builds a route to optionally capture parameter with specified [name], if it exists
 */
@ContextDsl
fun RouteE.optionalParam(name: String, build: RouteE.() -> Unit): RouteE {
  return createChild(OptionalParameterRouteSelector(name)).apply(build)
}

/**
 * Builds a route to match header with specified [name] and [value]
 */
@ContextDsl
fun RouteE.header(name: String, value: String, build: RouteE.() -> Unit): RouteE {
  return createChild(HttpHeaderRouteSelector(name, value)).apply(build)
}

/**
 * Builds a route to match requests with [HttpHeaders.Accept] header matching specified [contentType]
 */
@ContextDsl
fun RouteE.accept(contentType: ContentType, build: RouteE.() -> Unit): RouteE {
  return createChild(HttpAcceptRouteSelector(contentType)).apply(build)
}

/**
 * Builds a route to match requests with [HttpHeaders.ContentType] header matching specified [contentType]
 */
@ContextDsl
fun RouteE.contentType(contentType: ContentType, build: RouteE.() -> Unit): RouteE {
  return header(
    HttpHeaders.ContentType,
    "${contentType.contentType}/${contentType.contentSubtype}",
    build,
  )
}

/**
 * Builds a route to match `GET` requests with specified [path]
 */
@ContextDsl
fun RouteE.get(path: String, body: EitherPipelineInterceptor<Unit, ApplicationCall>): RouteE =
  bindRoute(body) { route.get(path, it) }

/**
 * Builds a route to match `GET` requests
 */
@ContextDsl
fun RouteE.get(body: EitherPipelineInterceptor<Unit, ApplicationCall>): RouteE =
  bindRoute(body) { route.get(it) }

/**
 * Builds a route to match `POST` requests with specified [path]
 */
@ContextDsl
fun RouteE.post(path: String, body: EitherPipelineInterceptor<Unit, ApplicationCall>): RouteE =
  bindRoute(body) { route.post(path, it) }

/**
 * Builds a route to match `POST` requests
 */
@ContextDsl
fun RouteE.post(body: EitherPipelineInterceptor<Unit, ApplicationCall>): RouteE =
  bindRoute(body) { route.post(it) }

/**
 * Builds a route to match `POST` requests with specified [path] receiving request body content of type [R]
 */
@ContextDsl
@JvmName("eitherPostTypedPath")
inline fun <reified R : Any> RouteE.post(
  path: String,
  crossinline body: suspend EitherPipelineContext<Unit, ApplicationCall, *>.(R) -> Unit
): RouteE = bindRoute(body) { route.post(path, it) }

/**
 * Builds a route to match `POST` requests receiving request body content of type [R]
 */
@ContextDsl
@JvmName("eitherPostTyped")
inline fun <reified R : Any> RouteE.post(
  crossinline body: suspend EitherPipelineContext<Unit, ApplicationCall, *>.(R) -> Unit
): RouteE {
  return bindRoute(body) { route.post(it) }
}

/**
 * Builds a route to match `HEAD` requests with specified [path]
 */
@ContextDsl
fun RouteE.head(path: String, body: EitherPipelineInterceptor<Unit, ApplicationCall>): RouteE =
  bindRoute(body) { route.head(path, it) }

/**
 * Builds a route to match `HEAD` requests
 */
@ContextDsl
fun RouteE.head(body: EitherPipelineInterceptor<Unit, ApplicationCall>): RouteE =
  bindRoute(body) { route.head(it) }

/**
 * Builds a route to match `PUT` requests with specified [path]
 */
@ContextDsl
fun RouteE.put(path: String, body: EitherPipelineInterceptor<Unit, ApplicationCall>): RouteE =
  bindRoute(body) { route.put(path, it) }

/**
 * Builds a route to match `PUT` requests
 */
@ContextDsl
fun RouteE.put(body: EitherPipelineInterceptor<Unit, ApplicationCall>): RouteE =
  bindRoute(body) { route.put(it) }

/**
 * Builds a route to match `PUT` requests with specified [path] receiving request body content of type [R]
 */
@ContextDsl
@JvmName("eitherPutTypedPath")
inline fun <reified R : Any> RouteE.put(
  path: String,
  crossinline body: suspend EitherPipelineContext<Unit, ApplicationCall, *>.(R) -> Unit
): RouteE = bindRoute(body) { route.put(path, it) }

/**
 * Builds a route to match `PUT` requests receiving request body content of type [R]
 */
@ContextDsl
@JvmName("eitherPutTyped")
inline fun <reified R : Any> RouteE.put(
  crossinline body: suspend EitherPipelineContext<Unit, ApplicationCall, *>.(R) -> Unit
): RouteE {
  return bindRoute(body) { route.put(it) }
}

/**
 * Builds a route to match `PATCH` requests with specified [path]
 */
@ContextDsl
fun RouteE.patch(path: String, body: EitherPipelineInterceptor<Unit, ApplicationCall>): RouteE =
  bindRoute(body) { route.patch(path, it) }

/**
 * Builds a route to match `PUT` requests
 */
@ContextDsl
fun RouteE.patch(body: EitherPipelineInterceptor<Unit, ApplicationCall>): RouteE =
  bindRoute(body) { route.patch(it) }

/**
 * Builds a route to match `PATCH` requests with specified [path] receiving request body content of type [R]
 */
@ContextDsl
@JvmName("eitherPatchTypedPath")
inline fun <reified R : Any> RouteE.patch(
  path: String,
  crossinline body: suspend EitherPipelineContext<Unit, ApplicationCall, *>.(R) -> Unit
): RouteE = bindRoute(body) { route.patch(path, it) }

/**
 * Builds a route to match `PATCH` requests receiving request body content of type [R]
 */
@ContextDsl
@JvmName("eitherPatchTyped")
inline fun <reified R : Any> RouteE.patch(
  crossinline body: suspend EitherPipelineContext<Unit, ApplicationCall, *>.(R) -> Unit
): RouteE {
  return bindRoute(body) { route.patch(it) }
}

/**
 * Builds a route to match `DELETE` requests with specified [path]
 */
@ContextDsl
fun RouteE.delete(path: String, body: EitherPipelineInterceptor<Unit, ApplicationCall>): RouteE =
  bindRoute(body) { route.delete(path, it) }

/**
 * Builds a route to match `DELETE` requests
 */
@ContextDsl
fun RouteE.delete(body: EitherPipelineInterceptor<Unit, ApplicationCall>): RouteE =
  bindRoute(body) { route.delete(it) }

/**
 * Builds a route to match `OPTIONS` requests with specified [path]
 */
@ContextDsl
fun RouteE.options(path: String, body: EitherPipelineInterceptor<Unit, ApplicationCall>): RouteE =
  bindRoute(body) { route.options(path, it) }

/**
 * Builds a route to match `OPTIONS` requests
 */
@ContextDsl
fun RouteE.options(body: EitherPipelineInterceptor<Unit, ApplicationCall>): RouteE =
  bindRoute(body) { route.options(it) }

/**
 * Create a routing entry for specified path
 */
fun RouteE.createRouteFromPath(path: String): RouteE {
  return route.createRouteFromPath(path).e()
}

@PublishedApi
internal inline fun <R> bindRoute(
  crossinline body: suspend EitherPipelineContext<Unit, ApplicationCall, *>.(R) -> Unit,
  original: (suspend PipelineContext<Unit, ApplicationCall>.(R) -> Unit) -> Route,
): RouteE {
  return RouteE(original { parameter ->
    val result = either<Any, Unit> {
      body(EitherPipelineContextImpl(this@original, this@either), parameter)
    }
    
    when (result) {
      is Either.Right -> {}
      is Either.Left -> call.respond(result.value)
    }
  })
}
