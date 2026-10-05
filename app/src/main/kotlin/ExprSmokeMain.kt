package com.github.metametamoon.app

import com.github.metametamoon.app.expr.Add
import com.github.metametamoon.app.expr.And
import com.github.metametamoon.app.expr.Gt
import com.github.metametamoon.app.expr.IntConst
import com.github.metametamoon.app.expr.IntValue
import com.github.metametamoon.app.expr.Lt
import com.github.metametamoon.app.expr.Z3Translator
import com.microsoft.z3.Context

fun main() {
  println("=== Symbolic Expressions Demo ===")

  val x = IntConst("x")
  val y = IntConst("y")
  val five = IntValue(5)

  val sum = Add(x, y)
  val condition = Gt(sum, five)

  println("Expression: ${condition.toExprString()}")
  println("Expression sort: ${condition.sort}")

  Context().use { z3ctx ->
    val translator = Z3Translator(z3ctx)

    val z3Expr = translator.translate(condition)
    println("Z3 expression created: ${z3Expr::class.simpleName}")

    val zero = IntValue(0)
    val ten = IntValue(10)

    val cond1 = Gt(x, zero)
    val cond2 = Lt(y, ten)

    val andExpr = And(listOf(cond1, cond2))
    println("Compound expression: ${andExpr.toExprString()}")

    val z3AndExpr = translator.translate(andExpr)
    println("Compound Z3 expression created: ${z3AndExpr::class.simpleName}")
  }
}
