package com.soywiz.korte.tag

import com.soywiz.korte.ExprNode
import com.soywiz.korte.Tag
import com.soywiz.korte.block.BlockFor
import com.soywiz.korte.tryRead

val TagFor = Tag("for", setOf("else"), setOf("end", "endfor")) {
	val main = chunks[0]
	val elseTag = chunks.getOrNull(1)?.body
	val tr = ExprNode.Token.tokenize(main.tag.content)
	val varnames = arrayListOf<String>()
	do {
		varnames += ExprNode.parseId(tr)
	} while (tr.tryRead(",") != null)
	ExprNode.expect(tr, "in")
	val expr = ExprNode.parseExpr(tr)
	BlockFor(varnames, expr, main.body, elseTag)
}