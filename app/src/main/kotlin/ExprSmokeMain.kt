package com.github.metametamoon.app

import com.github.metametamoon.app.expr.Add
import com.github.metametamoon.app.expr.AndExpr
import com.github.metametamoon.app.expr.GtExpr
import com.github.metametamoon.app.expr.Int32Const
import com.github.metametamoon.app.expr.Int32Value
import com.github.metametamoon.app.expr.LtExpr
import com.github.metametamoon.app.expr.Z3Translator
import com.microsoft.z3.Context

fun main() {
  println("=== Symbolic Expressions Demo ===")

  val x = Int32Const("x")
  val y = Int32Const("y")
  val five = Int32Value(5)

  val sum = Add(x, y)
  val condition = GtExpr(sum, five)

  println("Expression: ${condition.toExprString()}")
  println("Expression sort: ${condition.sort}")

  Context().use { z3ctx ->
    val translator = Z3Translator(z3ctx)

    val z3Expr = translator.translate(condition)
    println("Z3 expression created: ${z3Expr::class.simpleName}")

    val zero = Int32Value(0)
    val ten = Int32Value(10)

    val cond1 = GtExpr(x, zero)
    val cond2 = LtExpr(y, ten)

    val andExpr = AndExpr(listOf(cond1, cond2))
    println("Compound expression: ${andExpr.toExprString()}")

    val z3AndExpr = translator.translate(andExpr)
    println("Compound Z3 expression created: ${z3AndExpr::class.simpleName}")
  }
}
