/*
 *    Copyright 2021 Knoten
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * *        http://www.apache.org/licenses/LICENSE-2.0 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package co.knoten.kam

import arrow.core.Either
import arrow.core.Option
import arrow.core.toOption
import org.jetbrains.exposed.dao.ColumnWithTransform
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.OptionalReferrers
import org.jetbrains.exposed.dao.Reference
import org.jetbrains.exposed.dao.Referrers
import org.jetbrains.exposed.dao.View
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Alias
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnSet
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.QueryAlias
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions

// TODO: add immutable entity class
@Suppress("UnnecessaryAbstractClass", "TooManyFunctions")
abstract class KamEntityClass<ID : Comparable<ID>, T : Entity<ID>>
  (val table: IdTable<ID>, entityType: Class<T>? = null) {
  @PublishedApi
  internal val delegate = object : EntityClass<ID, T>(table, entityType) {}

  /**
   * Get an entity by its [id].
   *
   * @param id The id of the entity
   *
   * @return The entity that has this id or null if no entity was found.
   */
  open fun findById(id: ID): Option<T> = delegate.findById(id).toOption()

  /**
   * Get an entity by its [id].
   *
   * @param id The id of the entity
   *
   * @return The entity that has this id or null if no entity was found.
   */
  open fun findById(id: EntityID<ID>): Option<T> = delegate.findById(id).toOption()

  /**
   * Reloads entity fields from database as new object.
   * @param flush whether pending entity changes should be flushed previously
   */
  fun reload(entity: Entity<ID>, flush: Boolean = true): Option<T> =
    delegate.reload(entity, flush).toOption()

  fun testCache(id: EntityID<ID>): Option<T> =
    delegate.testCache(id).toOption()

  fun testCache(cacheCheckCondition: T.() -> Boolean): Sequence<T> =
    delegate.testCache(cacheCheckCondition)

  fun removeFromCache(entity: Entity<ID>): Either<Throwable, Unit> =
    Either.catch { delegate.removeFromCache(entity) }

  fun forEntityIds(ids: List<EntityID<ID>>): SizedIterable<T> =
    delegate.forEntityIds(ids)

  fun forIds(ids: List<ID>): SizedIterable<T> =
    delegate.forIds(ids)

  fun wrapRows(rows: SizedIterable<ResultRow>): SizedIterable<T> =
    delegate.wrapRows(rows)

  fun wrapRows(rows: SizedIterable<ResultRow>, alias: Alias<IdTable<*>>): SizedIterable<T> =
    delegate.wrapRows(rows, alias)

  fun wrapRows(rows: SizedIterable<ResultRow>, alias: QueryAlias): SizedIterable<T> =
    delegate.wrapRows(rows, alias)

  fun wrapRow(row: ResultRow): T =
    delegate.wrapRow(row)

  fun wrapRow(row: ResultRow, alias: Alias<IdTable<*>>): T =
    delegate.wrapRow(row, alias)

  fun wrapRow(row: ResultRow, alias: QueryAlias): T =
    delegate.wrapRow(row, alias)

  open fun all(): SizedIterable<T> = delegate.all()

  /**
   * Get all the entities that conform to the [op] statement.
   *
   * @param op The statement to select the entities for. The statement must be of boolean type.
   *
   * @return All the entities that conform to the [op] statement.
   */
  fun find(op: Op<Boolean>): SizedIterable<T> = delegate.find(op)

  /**
   * Get all the entities that conform to the [op] statement.
   *
   * @param op The statement to select the entities for. The statement must be of boolean type.
   *
   * @return All the entities that conform to the [op] statement.
   */
  fun find(op: SqlExpressionBuilder.() -> Op<Boolean>): SizedIterable<T> =
    delegate.find(op)

  fun findWithCacheCondition(
    cacheCheckCondition: T.() -> Boolean,
    op: SqlExpressionBuilder.() -> Op<Boolean>,
  ): Sequence<T> = delegate.findWithCacheCondition(cacheCheckCondition, op)

  open val dependsOnTables: ColumnSet = delegate.dependsOnTables
  open val dependsOnColumns: List<Column<out Any?>> = delegate.dependsOnColumns

  open fun searchQuery(op: Op<Boolean>): Query = delegate.searchQuery(op)

  /**
   * Count the amount of entities that conform to the [op] statement.
   *
   * @param op The statement to count the entities for. The statement must be of boolean type.
   *
   * @return The amount of entities that conform to the [op] statement.
   */
  fun count(op: Op<Boolean>): Long = delegate.count(op)

  private val createInstance =
    delegate::class.declaredFunctions
      .filterIsInstance<KFunction<T>>()
      .find { it.name == "createInstance" }
      ?: error("Could not find createInstance")

  protected open fun createInstance(entityId: EntityID<ID>, row: Option<ResultRow>): T =
    createInstance.call(delegate, entityId, row.orNull())

  fun wrap(id: EntityID<ID>, row: Option<ResultRow>): T = delegate.wrap(id, row.orNull())

  /**
   * Create a new entity with the fields that are set in the [init] block. The id will be automatically set.
   *
   * @param init The block where the entities' fields can be set.
   *
   * @return The entity that has been created.
   */
  open fun new(init: T.() -> Unit) = delegate.new(init)

  /**
   * Create a new entity with the fields that are set in the [init] block and with a set [id].
   *
   * @param id The id of the entity. Set this to null if it should be automatically generated.
   * @param init The block where the entities' fields can be set.
   *
   * @return The entity that has been created.
   */
  open fun new(id: ID, init: T.() -> Unit): T = delegate.new(id, init)

  inline fun view(op: SqlExpressionBuilder.() -> Op<Boolean>): View<T> = delegate.view(op)

  infix fun <R : Comparable<R>> referencedOn(column: Column<R>): Reference<R, ID, T> =
    delegate.referencedOn(column)

  infix fun <R : Comparable<R>> optionalReferencedOn(column: Column<R?>): OptionalReferenceK<R, ID, T> {
    return delegate.optionalReferencedOn(column).k()
  }

  infix fun <TID : Comparable<TID>, T : Entity<TID>, R : Comparable<R>> EntityClass<TID, T>.backReferencedOn(
    column: Column<R>,
  ): ReadOnlyProperty<Entity<ID>, T> = with(delegate) {
    backReferencedOn<TID, T, R>(column)
  }

  @JvmName("backReferencedOnOpt")
  infix fun <TID : Comparable<TID>, T : Entity<TID>, R : Comparable<R>> EntityClass<TID, T>.backReferencedOn(
    column: Column<R?>,
  ): ReadOnlyProperty<Entity<ID>, T> = with(delegate) {
    backReferencedOn<TID, T, R>(column)
  }

  infix fun <TID : Comparable<TID>, T : Entity<TID>, R : Comparable<R>> EntityClass<TID, T>.referrersOn(
    column: Column<R>,
  ): Referrers<ID, Entity<ID>, TID, T, R> = with(delegate) {
    referrersOn<TID, T, R>(column)
  }

  fun <TID : Comparable<TID>, T : Entity<TID>, R : Comparable<R>> EntityClass<TID, T>.referrersOn(
    column: Column<R>,
    cache: Boolean,
  ): Referrers<ID, Entity<ID>, TID, T, R> = with(delegate) {
    referrersOn<TID, T, R>(column, cache)
  }

  infix fun <TID : Comparable<TID>, T : Entity<TID>, R : Comparable<R>> EntityClass<TID, T>.optionalReferrersOn(
    column: Column<R?>,
  ): OptionalReferrers<ID, Entity<ID>, TID, T, R> = with(delegate) {
    optionalReferrersOn<TID, T, R>(column)
  }

  fun <TID : Comparable<TID>, T : Entity<TID>, R : Comparable<R>> EntityClass<TID, T>.optionalReferrersOn(
    column: Column<R?>,
    cache: Boolean = false,
  ): OptionalReferrers<ID, Entity<ID>, TID, T, R> = with(delegate) {
    optionalReferrersOn<TID, T, R>(column, cache)
  }

  fun <TC : Any?, TR : Any?> Column<TC>.transform(
    toColumn: (TR) -> TC,
    toReal: (TC) -> TR,
  ): ColumnWithTransform<TC, TR> = with(delegate) {
    transform(toColumn, toReal)
  }

  fun <SID> warmUpOptReferences(
    references: List<SID>,
    refColumn: Column<SID?>,
    forUpdate: Boolean? = null
  ): List<T> = delegate.warmUpOptReferences(references, refColumn, forUpdate)

  fun <SID> warmUpReferences(
    references: List<SID>,
    refColumn: Column<SID>,
    forUpdate: Boolean? = null,
  ): List<T> = delegate.warmUpReferences(references, refColumn, forUpdate)

  fun warmUpLinkedReferences(
    references: List<EntityID<*>>,
    linkTable: Table,
    forUpdate: Boolean? = null,
  ): List<T> = delegate.warmUpLinkedReferences(references, linkTable, forUpdate)
}
