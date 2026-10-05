package com.github.metametamoon.app.expr

import com.microsoft.z3.ArithSort
import com.microsoft.z3.BoolSort
import com.microsoft.z3.Context

/** Not a visitor,because the general visitor seems to be overkill for now */
class Z3Translator(private val z3ctx: Context) {
  fun <Sort : ExprSort> translate(expr: Expr<Sort>): com.microsoft.z3.Expr<*> {
    return when (expr) {
      is Eq<*> -> z3ctx.mkEq(translate(expr.left), translate(expr.right))
      is Int32Const -> z3ctx.mkIntConst(expr.name)
      is Int32Value -> z3ctx.mkInt(expr.value)
      is Add ->
        z3ctx.mkAdd<ArithSort>(
          translate(expr.left).uncheckedCast(),
          translate(expr.right).uncheckedCast(),
        )

      is GtExpr ->
        z3ctx.mkGt(translate(expr.left).uncheckedCast(), translate(expr.right).uncheckedCast())
      is LtExpr ->
        z3ctx.mkLt(translate(expr.left).uncheckedCast(), translate(expr.right).uncheckedCast())
      is AndExpr ->
        z3ctx.mkAnd(
          *expr.operands
            .map { translate(it).uncheckedCast<com.microsoft.z3.Expr<BoolSort>, _>() }
            .toTypedArray()
        )
    }
  }
}

@Suppress("UNCHECKED_CAST") private fun <Dest, T> T.uncheckedCast(): Dest = this as Dest
