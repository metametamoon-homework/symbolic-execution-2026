package com.github.metametamoon.app.expr

import com.microsoft.z3.ArithSort
import com.microsoft.z3.BoolSort
import com.microsoft.z3.Context
import com.microsoft.z3.Expr

/** Not a visitor,because the general visitor seems to be overkill for now */
class Z3Translator(private val z3ctx: Context) {
  val leafValues: MutableMap<Expr<*>, GgExpr<*>> = mutableMapOf()

  fun <Sort : GgSort> translate(expr: GgExpr<Sort>): com.microsoft.z3.Expr<*> {
    return when (expr) {
      is Eq -> z3ctx.mkEq(translate(expr.left), translate(expr.right))
      is IntConst -> z3ctx.mkIntConst(expr.name).also { leafValues[it] = expr }
      is IntValue -> z3ctx.mkInt(expr.value)
      is BoolValue -> z3ctx.mkBool(expr.value)
      is Add ->
        z3ctx.mkAdd<ArithSort>(
          translate(expr.left).uncheckedCast(),
          translate(expr.right).uncheckedCast(),
        )

      is Gt ->
        z3ctx.mkGt(translate(expr.left).uncheckedCast(), translate(expr.right).uncheckedCast())
      is Lt ->
        z3ctx.mkLt(translate(expr.left).uncheckedCast(), translate(expr.right).uncheckedCast())
      is And ->
        z3ctx.mkAnd(
          *expr.operands.map { translate(it).uncheckedCast<Expr<BoolSort>, _>() }.toTypedArray()
        )

      is Not -> z3ctx.mkNot(translate(expr.operand).uncheckedCast())
      is Mul ->
        z3ctx.mkMul<ArithSort>(
          translate(expr.left).uncheckedCast(),
          translate(expr.right).uncheckedCast(),
        )
    }
  }
}

@Suppress("UNCHECKED_CAST") fun <Dest, T> T.uncheckedCast(): Dest = this as Dest
