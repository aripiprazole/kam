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

@file:Suppress("unused", "TooManyFunctions")
@file:OptIn(ExperimentalTypeInference::class)

package co.knoten.kam

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.request.receive
import io.ktor.routing.ConstantParameterRouteSelector
import io.ktor.routing.HttpAcceptRouteSelector
import io.ktor.routing.HttpHeaderRouteSelector
import io.ktor.routing.HttpMethodRouteSelector
import io.ktor.routing.OptionalParameterRouteSelector
import io.ktor.routing.ParameterRouteSelector
import io.ktor.routing.createRouteFromPath
import io.ktor.util.pipeline.ContextDsl
import kotlin.experimental.ExperimentalTypeInference
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
fun RouteE.get(path: String, body: EitherPipelineInterceptor<Unit, ApplicationCall, Any>): RouteE {
  return route(path, HttpMethod.Get) {
    handle(body)
  }
}

/**
 * Builds a route to match `GET` requests with specified [path]
 */
@ContextDsl
@JvmName("eitherLeftTypedGet")
inline fun <reified E : Any> RouteE.get(
  path: String,
  @BuilderInference noinline body: EitherPipelineInterceptor<Unit, ApplicationCall, E>,
): RouteE {
  return route(path, HttpMethod.Get) {
    handle(body)
  }
}

/**
 * Builds a route to match `GET` requests
 */
@ContextDsl
fun RouteE.get(body: EitherPipelineInterceptor<Unit, ApplicationCall, Any>): RouteE {
  return method(HttpMethod.Get) {
    handle(body)
  }
}

/**
 * Builds a route to match `GET` requests
 */
@ContextDsl
@JvmName("eitherLeftTypedGet")
inline fun <reified E : Any> RouteE.get(
  @BuilderInference
  noinline body: EitherPipelineInterceptor<Unit, ApplicationCall, E>,
): RouteE {
  return method(HttpMethod.Get) {
    handle(body)
  }
}

/**
 * Builds a route to match `POST` requests with specified [path]
 */
@ContextDsl
fun RouteE.post(path: String, body: EitherPipelineInterceptor<Unit, ApplicationCall, Any>): RouteE {
  return route(path, HttpMethod.Post) {
    handle(body)
  }
}

/**
 * Builds a route to match `POST` requests with specified [path]
 */
@ContextDsl
@JvmName("eitherLeftTypedPost")
inline fun <reified E : Any> RouteE.post(
  path: String,
  @BuilderInference
  noinline body: EitherPipelineInterceptor<Unit, ApplicationCall, E>,
): RouteE {
  return route(path, HttpMethod.Post) {
    handle(body)
  }
}

/**
 * Builds a route to match `POST` requests
 */
@ContextDsl
fun RouteE.post(body: EitherPipelineInterceptor<Unit, ApplicationCall, Any>): RouteE {
  return method(HttpMethod.Post) {
    handle(body)
  }
}

/**
 * Builds a route to match `POST` requests
 */
@ContextDsl
@JvmName("eitherLeftTypedPost")
inline fun <reified E : Any> RouteE.post(
  @BuilderInference
  noinline body: EitherPipelineInterceptor<Unit, ApplicationCall, E>,
): RouteE {
  return method(HttpMethod.Post) {
    handle(body)
  }
}

/**
 * Builds a route to match `POST` requests with specified [path] receiving request body content of type [R]
 */
@ContextDsl
@JvmName("eitherPostTypedPath")
inline fun <reified R : Any> RouteE.post(
  path: String,
  crossinline body: suspend AEitherPipelineContext<Unit, ApplicationCall>.(R) -> Unit,
): RouteE {
  return route(path, HttpMethod.Post) {
    handle {
      body(call.receive())
    }
  }
}

/**
 * Builds a route to match `POST` requests with specified [path] receiving request body content of type [R]
 */
@ContextDsl
@JvmName("eitherLeftPostTypedPath")
inline fun <reified E : Any, reified R : Any> RouteE.postE(
  path: String,
  @BuilderInference
  crossinline body: suspend EitherPipelineContext<Unit, ApplicationCall, E, *>.(R) -> Unit,
): RouteE {
  return route(path, HttpMethod.Post) {
    handle<E> {
      body(call.receive())
    }
  }
}

/**
 * Builds a route to match `POST` requests receiving request body content of type [R]
 */
@ContextDsl
@JvmName("eitherPostTyped")
inline fun <reified R : Any> RouteE.post(
  crossinline body: suspend AEitherPipelineContext<Unit, ApplicationCall>.(R) -> Unit
): RouteE {
  return method(HttpMethod.Post) {
    handle {
      body(call.receive())
    }
  }
}

/**
 * Builds a route to match `POST` requests receiving request body content of type [R]
 */
@ContextDsl
@JvmName("eitherLeftPostTyped")
inline fun <reified E : Any, reified R : Any> RouteE.postE(
  @BuilderInference
  crossinline body: suspend EitherPipelineContext<Unit, ApplicationCall, E, *>.(R) -> Unit
): RouteE {
  return method(HttpMethod.Post) {
    handle<E> {
      body(call.receive())
    }
  }
}

/**
 * Builds a route to match `HEAD` requests with specified [path]
 */
@ContextDsl
fun RouteE.head(path: String, body: EitherPipelineInterceptor<Unit, ApplicationCall, Any>): RouteE {
  return route(path, HttpMethod.Head) {
    handle(body)
  }
}

/**
 * Builds a route to match `HEAD` requests with specified [path]
 */
@ContextDsl
@JvmName("eitherLeftTypedHead")
inline fun <reified E : Any> RouteE.head(
  path: String,
  @BuilderInference
  noinline body: EitherPipelineInterceptor<Unit, ApplicationCall, E>,
): RouteE {
  return route(path, HttpMethod.Head) {
    handle(body)
  }
}

/**
 * Builds a route to match `HEAD` requests
 */
@ContextDsl
fun RouteE.head(body: EitherPipelineInterceptor<Unit, ApplicationCall, Any>): RouteE {
  return method(HttpMethod.Head) {
    handle(body)
  }
}

/**
 * Builds a route to match `HEAD` requests
 */
@ContextDsl
@JvmName("eitherLeftTypedHead")
inline fun <reified E : Any> RouteE.head(
  @BuilderInference
  noinline body: EitherPipelineInterceptor<Unit, ApplicationCall, E>,
): RouteE {
  return method(HttpMethod.Head) {
    handle(body)
  }
}

/**
 * Builds a route to match `PUT` requests with specified [path]
 */
@ContextDsl
fun RouteE.put(path: String, body: EitherPipelineInterceptor<Unit, ApplicationCall, Any>): RouteE {
  return route(path, HttpMethod.Put) {
    handle(body)
  }
}

/**
 * Builds a route to match `PUT` requests with specified [path]
 */
@ContextDsl
@JvmName("eitherLeftTypedPut")
inline fun <reified E : Any> RouteE.put(
  path: String,
  @BuilderInference
  noinline body: EitherPipelineInterceptor<Unit, ApplicationCall, E>,
): RouteE {
  return route(path, HttpMethod.Put) {
    handle(body)
  }
}

/**
 * Builds a route to match `PUT` requests
 */
@ContextDsl
fun RouteE.put(body: EitherPipelineInterceptor<Unit, ApplicationCall, Any>): RouteE {
  return method(HttpMethod.Put) {
    handle(body)
  }
}

/**
 * Builds a route to match `PUT` requests
 */
@ContextDsl
@JvmName("eitherLeftTypedPut")
inline fun <reified E : Any> RouteE.put(
  @BuilderInference
  noinline body: EitherPipelineInterceptor<Unit, ApplicationCall, E>,
): RouteE {
  return method(HttpMethod.Put) {
    handle(body)
  }
}

/**
 * Builds a route to match `PUT` requests with specified [path] receiving request body content of type [R]
 */
@ContextDsl
@JvmName("eitherPutTyped")
inline fun <reified R : Any> RouteE.put(
  path: String,
  crossinline body: suspend AEitherPipelineContext<Unit, ApplicationCall>.(R) -> Unit,
): RouteE {
  return route(path, HttpMethod.Put) {
    handle {
      body(call.receive())
    }
  }
}

/**
 * Builds a route to match `PUT` requests with specified [path] receiving request body content of type [R]
 */
@ContextDsl
@JvmName("eitherLeftPutTyped")
inline fun <reified E : Any, reified R : Any> RouteE.put(
  path: String,
  @BuilderInference
  crossinline body: suspend EitherPipelineContext<Unit, ApplicationCall, E, *>.(R) -> Unit,
): RouteE {
  return route(path, HttpMethod.Put) {
    handle<E> {
      body(call.receive())
    }
  }
}

/**
 * Builds a route to match `PUT` requests receiving request body content of type [R]
 */
@ContextDsl
@JvmName("eitherPutTyped")
inline fun <reified E : Any, reified R : Any> RouteE.put(
  @BuilderInference
  crossinline body: suspend EitherPipelineContext<Unit, ApplicationCall, E, *>.(R) -> Unit
): RouteE {
  return method(HttpMethod.Put) {
    handle<E> {
      body(call.receive())
    }
  }
}

/**
 * Builds a route to match `PUT` requests receiving request body content of type [R]
 */
@ContextDsl
@JvmName("eitherLeftPutTyped")
inline fun <reified E : Any, reified R : Any> RouteE.putE(
  @BuilderInference
  crossinline body: suspend EitherPipelineContext<Unit, ApplicationCall, E, *>.(R) -> Unit
): RouteE {
  return method(HttpMethod.Put) {
    handle<E> {
      body(call.receive())
    }
  }
}

/**
 * Builds a route to match `PATCH` requests with specified [path]
 */
@ContextDsl
fun RouteE.patch(path: String, body: EitherPipelineInterceptor<Unit, ApplicationCall, Any>): RouteE {
  return route(path, HttpMethod.Patch) {
    handle(body)
  }
}

/**
 * Builds a route to match `PATCH` requests with specified [path]
 */
@ContextDsl
@JvmName("eitherLeftTypedPatch")
inline fun <reified E : Any> RouteE.patch(
  path: String,
  @BuilderInference
  noinline body: EitherPipelineInterceptor<Unit, ApplicationCall, E>,
): RouteE {
  return route(path, HttpMethod.Patch) {
    handle(body)
  }
}

/**
 * Builds a route to match `PATCH` requests
 */
@ContextDsl
fun RouteE.patch(body: EitherPipelineInterceptor<Unit, ApplicationCall, Any>): RouteE {
  return method(HttpMethod.Patch) {
    handle(body)
  }
}

/**
 * Builds a route to match `PATCH` requests
 */
@ContextDsl
@JvmName("eitherLeftTypedPatch")
inline fun <reified E : Any> RouteE.patch(
  @BuilderInference
  noinline body: EitherPipelineInterceptor<Unit, ApplicationCall, E>,
): RouteE {
  return method(HttpMethod.Patch) {
    handle(body)
  }
}

/**
 * Builds a route to match `PATCH` requests with specified [path] receiving request body content of type [R]
 */
@ContextDsl
@JvmName("eitherPatchTypedPath")
inline fun <reified R : Any> RouteE.patch(
  path: String,
  crossinline body: suspend AEitherPipelineContext<Unit, ApplicationCall>.(R) -> Unit,
): RouteE {
  return route(path, HttpMethod.Patch) {
    handle {
      body(call.receive())
    }
  }
}

/**
 * Builds a route to match `PATCH` requests with specified [path] receiving request body content of type [R]
 */
@ContextDsl
@JvmName("eitherLeftPatchTyped")
inline fun <reified E : Any, reified R : Any> RouteE.patchE(
  path: String,
  @BuilderInference
  crossinline body: suspend EitherPipelineContext<Unit, ApplicationCall, E, *>.(R) -> Unit,
): RouteE {
  return route(path, HttpMethod.Patch) {
    handle<E> {
      body(call.receive())
    }
  }
}

/**
 * Builds a route to match `PATCH` requests receiving request body content of type [R]
 */
@ContextDsl
@JvmName("eitherPatchTyped")
inline fun <reified R : Any> RouteE.patch(
  crossinline body: suspend AEitherPipelineContext<Unit, ApplicationCall>.(R) -> Unit,
): RouteE {
  return method(HttpMethod.Patch) {
    handle {
      body(call.receive())
    }
  }
}

/**
 * Builds a route to match `PATCH` requests receiving request body content of type [R]
 */
@ContextDsl
@JvmName("eitherLeftPatchTyped")
inline fun <reified E : Any, reified R : Any> RouteE.patchE(
  @BuilderInference
  crossinline body: suspend EitherPipelineContext<Unit, ApplicationCall, E, *>.(R) -> Unit,
): RouteE {
  return method(HttpMethod.Patch) {
    handle<E> {
      body(call.receive())
    }
  }
}

/**
 * Builds a route to match `DELETE` requests with specified [path]
 */
@ContextDsl
fun RouteE.delete(path: String, body: EitherPipelineInterceptor<Unit, ApplicationCall, Any>): RouteE {
  return route(path, HttpMethod.Delete) {
    handle(body)
  }
}

/**
 * Builds a route to match `DELETE` requests with specified [path]
 */
@ContextDsl
@JvmName("eitherDeleteLeftTyped")
inline fun <reified E : Any> RouteE.delete(
  path: String,
  @BuilderInference
  noinline body: EitherPipelineInterceptor<Unit, ApplicationCall, E>,
): RouteE {
  return route(path, HttpMethod.Delete) {
    handle(body)
  }
}

/**
 * Builds a route to match `DELETE` requests
 */
@ContextDsl
fun RouteE.delete(body: EitherPipelineInterceptor<Unit, ApplicationCall, Any>): RouteE {
  return method(HttpMethod.Delete) {
    handle(body)
  }
}

/**
 * Builds a route to match `DELETE` requests
 */
@ContextDsl
@JvmName("eitherDeleteLeftTyped")
inline fun <reified E : Any> RouteE.delete(
  @BuilderInference
  noinline body: EitherPipelineInterceptor<Unit, ApplicationCall, E>,
): RouteE {
  return method(HttpMethod.Delete) {
    handle(body)
  }
}

/**
 * Builds a route to match `OPTIONS` requests with specified [path]
 */
@ContextDsl
fun RouteE.options(
  path: String,
  body: EitherPipelineInterceptor<Unit, ApplicationCall, Any>,
): RouteE {
  return route(path, HttpMethod.Options) {
    handle(body)
  }
}

/**
 * Builds a route to match `OPTIONS` requests with specified [path]
 */
@ContextDsl
@JvmName("eitherOptionsLeftTyped")
inline fun <reified E : Any> RouteE.options(
  path: String,
  @BuilderInference
  noinline body: EitherPipelineInterceptor<Unit, ApplicationCall, E>,
): RouteE {
  return route(path, HttpMethod.Options) {
    handle(body)
  }
}

/**
 * Builds a route to match `OPTIONS` requests
 */
@ContextDsl
fun RouteE.options(body: EitherPipelineInterceptor<Unit, ApplicationCall, Any>): RouteE {
  return method(HttpMethod.Options) {
    handle(body)
  }
}

/**
 * Builds a route to match `OPTIONS` requests
 */
@ContextDsl
@JvmName("eitherOptionsLeftTyped")
inline fun <reified E : Any> RouteE.options(
  @BuilderInference
  noinline body: EitherPipelineInterceptor<Unit, ApplicationCall, E>,
): RouteE {
  return method(HttpMethod.Options) {
    handle(body)
  }
}

/**
 * Create a routing entry for specified path
 */
fun RouteE.createRouteFromPath(path: String): RouteE {
  return route.createRouteFromPath(path).e()
}
