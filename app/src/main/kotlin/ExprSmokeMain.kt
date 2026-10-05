package com.github.metametamoon.app

import com.github.metametamoon.app.expr.Add
import com.github.metametamoon.app.expr.And
import com.github.metametamoon.app.expr.Gt
import com.github.metametamoon.app.expr.Int32Const
import com.github.metametamoon.app.expr.Int32Value
import com.github.metametamoon.app.expr.Lt
import com.github.metametamoon.app.expr.Z3Translator
import com.microsoft.z3.Context

fun main() {
  println("=== Symbolic Expressions Demo ===")

  val x = Int32Const("x")
  val y = Int32Const("y")
  val five = Int32Value(5)

  val sum = Add(x, y)
  val condition = Gt(sum, five)

  println("Expression: ${condition.toExprString()}")
  println("Expression sort: ${condition.sort}")

  Context().use { z3ctx ->
    val translator = Z3Translator(z3ctx)

    val z3Expr = translator.translate(condition)
    println("Z3 expression created: ${z3Expr::class.simpleName}")

    val zero = Int32Value(0)
    val ten = Int32Value(10)

    val cond1 = Gt(x, zero)
    val cond2 = Lt(y, ten)

    val andExpr = And(listOf(cond1, cond2))
    println("Compound expression: ${andExpr.toExprString()}")

    val z3AndExpr = translator.translate(andExpr)
    println("Compound Z3 expression created: ${z3AndExpr::class.simpleName}")
  }
}
