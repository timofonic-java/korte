package com.soywiz.korte.tag

import com.soywiz.korte.Block
import com.soywiz.korte.Tag

val TagEmpty = Tag("", setOf(""), "") {
	Block.group(parts.map { it.body })
}