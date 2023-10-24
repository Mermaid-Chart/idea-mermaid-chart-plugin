package co.tula.mermaidchart.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

typealias EitherE<R> = Either<Exception, R>

sealed class Either<out E, out V> {

    companion object {
        inline fun <V> of(action: () -> V): Either<Exception, V> {
            return try {
                Right(action())
            } catch (ex: Exception) {
                ex.printStackTrace()
                Left(ex)
            }
        }
    }
}

data class Right<out R>(val v: R) : Either<Nothing, R>()
data class Left<out L>(val v: L) : Either<L, Nothing>()

suspend infix fun <L, R, V> Either<L, R>.bind(io: suspend (R) -> Either<L, V>): Either<L, V> = when (this) {
    is Right -> io(v)
    is Left -> this
}

inline infix fun <L, R, V> Either<L, R>.fmap(f: (R) -> V): Either<L, V> = when (this) {
    is Right -> Right(f(v))
    is Left -> this
}

fun <R> EitherE<R>.rightOrThrow(): R = (this as? Right)?.v ?: throw (this as Left).v
suspend fun <R> io(
    dispatcher: CoroutineContext = Dispatchers.IO,
    block: suspend CoroutineScope.() -> R
) = Either.of { withContext(dispatcher) { block() } }

fun <L, R> Either<L, R>.orNull(): R? = when (this) {
    is Right -> this.v
    else -> null
}