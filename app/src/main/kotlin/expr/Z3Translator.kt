package com.github.metametamoon.app.expr

import com.microsoft.z3.Context

/** Not a visitor,because the general visitor seems to be overkill for now */
class Z3Translator(val z3ctx: Context) {
  fun <Sort : ExprSort> translate(expr: Expr<Sort>): com.microsoft.z3.Expr<*> {
    return when (expr) {
      is Eq<*> -> z3ctx.mkEq(translate(expr.left), translate(expr.right))
      is Int32Const -> z3ctx.mkIntConst(expr.name)
      is Int32Value -> z3ctx.mkInt(expr.value)
    }
  }
}
