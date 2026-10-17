package com.github.metametamoon.app

import com.github.metametamoon.app.expr.GgContext
import com.github.metametamoon.app.expr.Z3Translator
import com.microsoft.z3.Context

fun main() {
  println("=== Symbolic Expressions Demo ===")

  val ctx = GgContext()

  val x = ctx.mkIntConst("x")
  val y = ctx.mkIntConst("y")
  val five = ctx.mkIntValue(5)

  val sum = ctx.mkAdd(x, y)
  val condition = ctx.mkGt(sum, five)

  println("Expression: ${condition.toExprString()}")
  println("Expression sort: ${condition.sort}")

  Context().use { z3ctx ->
    val translator = Z3Translator(z3ctx)

    val z3Expr = translator.translate(condition)
    println("Z3 expression created: ${z3Expr::class.simpleName}")

    val zero = ctx.mkIntValue(0)
    val ten = ctx.mkIntValue(10)

    val cond1 = ctx.mkGt(x, zero)
    val cond2 = ctx.mkLt(y, ten)

    val andExpr = ctx.mkAnd(listOf(cond1, cond2))
    println("Compound expression: ${andExpr.toExprString()}")

    val z3AndExpr = translator.translate(andExpr)
    println("Compound Z3 expression created: ${z3AndExpr::class.simpleName}")
  }
}
