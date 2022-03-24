/*
 *    Copyright 2022 Gabrielle Guimarães de Oliveira
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

package com.gabrielleeg1.kam

import arrow.core.Either
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.jetbrains.exposed.sql.transactions.transaction

fun <T> eitherTransaction(db: Database? = null, statement: Transaction.() -> T) = Either.catch {
  transaction(db, statement)
}

fun <T> eitherTransaction(
  transactionIsolation: Int,
  repetitionAttempts: Int,
  db: Database? = null,
  statement: Transaction.() -> T,
): Either<Throwable, T> = Either.catch {
  transaction(transactionIsolation, repetitionAttempts, db, statement)
}

suspend fun <T> eitherAsyncTransaction(
  context: CoroutineDispatcher? = null,
  db: Database? = null,
  transactionIsolation: Int? = null,
  statement: suspend Transaction.() -> T,
): Either<Throwable, Deferred<T>> = Either.catch {
  suspendedTransactionAsync(context, db, transactionIsolation, statement)
}

suspend fun <T> eitherSuspendedTransaction(
  context: CoroutineDispatcher? = null,
  db: Database? = null,
  transactionIsolation: Int? = null,
  statement: suspend Transaction.() -> T
): Either<Throwable, T> = Either.catch {
  newSuspendedTransaction(context, db, transactionIsolation, statement)
}
