package com.soywiz.korte

import com.soywiz.korio.error.noImpl
import com.soywiz.korio.util.quote
import com.soywiz.korio.util.toNumber
import kotlin.math.pow
import kotlin.reflect.KClass

object Dynamic2 {
	fun binop(l: Any?, r: Any?, op: String): Any? {
		return when (op) {
			"+" -> {
				when (l) {
					is String -> l.toString() + toString(r)
					is Iterable<*> -> toIterable(l) + toIterable(r)
					else -> toDouble(l) + toDouble(r)
				}
			}
			"-" -> toDouble(l) - toDouble(r)
			"*" -> toDouble(l) * toDouble(r)
			"/" -> toDouble(l) / toDouble(r)
			"%" -> toDouble(l) % toDouble(r)
			"**" -> toDouble(l).pow(toDouble(r))
			"&" -> toInt(l) and toInt(r)
			"or" -> toInt(l) or toInt(r)
			"^" -> toInt(l) xor toInt(r)
			"&&" -> toBool(l) && toBool(r)
			"||" -> toBool(l) || toBool(r)
			"==" -> {
				if (l is Number && r is Number) {
					l.toDouble() == r.toDouble()
				} else {
					l == r
				}
			}
			"!=" -> {
				if (l is Number && r is Number) {
					l.toDouble() != r.toDouble()
				} else {
					l != r
				}
			}
			"<" -> compare(l, r) < 0
			"<=" -> compare(l, r) <= 0
			">" -> compare(l, r) > 0
			">=" -> compare(l, r) >= 0
			"in" -> contains(r, l)
			"?:" -> if (toBool(l)) l else r
			else -> noImpl("Not implemented binary operator '$op'")
		}
	}

	fun unop(r: Any?, op: String): Any? {
		return when (op) {
			"+" -> r
			"-" -> -toDouble(r)
			"~" -> toInt(r).inv()
			"!" -> !toBool(r)
			else -> noImpl("Not implemented unary operator $op")
		}
	}

	fun contains(collection: Any?, element: Any?): Boolean = when (collection) {
		is Set<*> -> element in collection
		else -> element in toList(collection)
	}

	fun compare(l: Any?, r: Any?): Int {
		if (l is Number && r is Number) {
			return l.toDouble().compareTo(r.toDouble())
		}
		val lc = toComparable(l)
		val rc = toComparable(r)
		if (lc::class.isInstance(rc)) {
			return lc.compareTo(rc)
		} else {
			return -1
		}
	}

	@Suppress("UNCHECKED_CAST")
	fun toComparable(it: Any?): Comparable<Any?> {
		return when (it) {
			null -> 0 as Comparable<Any?>
			is Comparable<*> -> it as Comparable<Any?>
			else -> it.toString() as Comparable<Any?>
		}
	}

	fun toBool(it: Any?): Boolean = toBoolOrNull(it) ?: false

	fun toBoolOrNull(it: Any?): Boolean? {
		return when (it) {
			null -> null
			is Boolean -> it
			is String -> it.isNotEmpty() && it != "0" && it != "false"
			else -> null
		}
	}
	fun toNumber(it: Any?): Number {
		return when (it) {
			null -> 0.0
			is Number -> it
			else -> it.toString().toNumber()
		}
	}

	fun toInt(it: Any?): Int = toNumber(it).toInt()
	fun toLong(it: Any?): Long = toNumber(it).toLong()
	fun toDouble(it: Any?): Double = toNumber(it).toDouble()

	fun toString(value: Any?): String {
		return when (value) {
			null -> ""
			is String -> value
			is Double -> {
				if (value == value.toInt().toDouble()) {
					value.toInt().toString()
				} else {
					value.toString()
				}
			}
			is Iterable<*> -> "[" + value.map { toString(it) }.joinToString(", ") + "]"
			is Map<*, *> -> "{" + value.map { toString(it.key).quote() + ": " + toString(it.value) }.joinToString(", ") + "}"
			else -> value.toString()
		}
	}

	fun length(subject: Any?): Int {
		if (subject == null) return 0
		if (subject is Array<*>) return subject.size
		if (subject is List<*>) return subject.size
		if (subject is Map<*, *>) return subject.size
		if (subject is Iterable<*>) return subject.count()
		return subject.toString().length
	}

	fun toList(it: Any?): List<*> = toIterable(it).toList()

	fun toIterable(it: Any?): Iterable<*> {
		return when (it) {
			null -> listOf<Any?>()
			is Iterable<*> -> it
			is CharSequence -> it.toList()
			is Map<*, *> -> it.toList()
			else -> listOf<Any?>()
		}
	}

	suspend fun accessAny(instance: Any?, key: Any?): Any? = when (instance) {
		null -> null
		is Map<*, *> -> instance[key]
		is Iterable<*> -> instance.toList()[toInt(key)]
		else -> {
			//getField(instance, key.toString())
			null // @TODO!
		}
	}

	fun setAny(instance: Any?, key: Any?, value: Any?): Any? {
		return when (instance) {
			null -> null
			is MutableMap<*, *> -> (instance as MutableMap<Any?, Any?>).set(key, value)
			is MutableList<*> -> (instance as MutableList<Any?>)[toInt(key)] = value
			else -> {
				//setField(instance, key.toString(), value)
				Unit // @TODO!
			}
		}
	}

	//suspend fun setAny(any: Any?, key: Any?, value: Any?): Unit = TODO()


	suspend fun callAny(any: Any?, args: List<Any?>): Any? = TODO()
	suspend fun callAny(any: Any?, methodName: Any?, args: List<Any?>): Any? = TODO()

	//fun dynamicCast(any: Any?, target: KClass<*>): Any? = TODO()
}

suspend fun asyncCaptureStdout(callback: suspend () -> Unit): String = TODO()

internal fun Any?.toDynamicString() = Dynamic2.toString(this)
internal fun Any?.toDynamicBool() = Dynamic2.toBool(this)
internal fun Any?.toDynamicInt() = Dynamic2.toInt(this)
internal fun Any?.toDynamicList() = Dynamic2.toList(this)
internal fun Any?.toDynamicIterable() = Dynamic2.toIterable(this)
internal fun Any?.dynamicLength() = Dynamic2.length(this)
suspend internal fun Any?.dynamicGet(key: Any?) = Dynamic2.accessAny(this, key)
suspend internal fun Any?.dynamicSet(key: Any?, value: Any?) = Dynamic2.setAny(this, key, value)
suspend internal fun Any?.dynamicCall(vararg args: Any?) = Dynamic2.callAny(this, args.toList())
suspend internal fun Any?.dynamicCallMethod(methodName: Any?, vararg args: Any?) = Dynamic2.callAny(this, methodName, args.toList())
//suspend internal fun Any?.dynamicCastTo(target: KClass<*>) = Dynamic2.dynamicCast(this, target)