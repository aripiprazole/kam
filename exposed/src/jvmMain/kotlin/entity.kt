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

@file:Suppress("UnnecessaryAbstractClass")

package co.knoten.kam

import arrow.core.Option
import arrow.core.toOption
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import java.util.UUID
import kotlin.reflect.KProperty

abstract class KamEntity<ID : Comparable<ID>>(id: EntityID<ID>) : Entity<ID>(id) {
  operator fun <R : Comparable<R>,
    RID : Comparable<RID>,
    T : Entity<RID>
    > OptionalReferenceK<R, RID, T>.getValue(o: Entity<ID>, desc: KProperty<*>): Option<T> {
    return delegate.getValue(o, desc).toOption()
  }

  operator fun <R : Comparable<R>, RID : Comparable<RID>, T : Entity<RID>> OptionalReferenceK<R, RID, T>.setValue(
    o: Entity<ID>,
    desc: KProperty<*>,
    value: Option<T>,
  ) {
    delegate.setValue(o, desc, value.orNull())
  }
}

abstract class KamIntEntity(id: EntityID<Int>) : KamEntity<Int>(id)

abstract class KamIntEntityClass<E : KamIntEntity>(
  table: IdTable<Int>,
  entityType: Class<E>? = null,
) : KamEntityClass<Int, E>(table, entityType)

abstract class KamLongEntity(id: EntityID<Long>) : KamEntity<Long>(id)

abstract class KamLongEntityClass<E : KamLongEntity>(
  table: IdTable<Long>,
  entityType: Class<E>? = null,
) : KamEntityClass<Long, E>(table, entityType)

abstract class KamUUIDEntity(id: EntityID<UUID>) : KamEntity<UUID>(id)

abstract class KamUUIDEntityClass<E : KamUUIDEntity>(
  table: IdTable<UUID>,
  entityType: Class<E>? = null,
) : KamEntityClass<UUID, E>(table, entityType)
