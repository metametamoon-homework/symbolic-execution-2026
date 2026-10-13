package com.github.metametamoon.app.expr

import com.microsoft.z3.Context
import com.microsoft.z3.Expr
import com.microsoft.z3.IntExpr

sealed interface ExprVisitor<T> {

  fun visitInt32Const(expr: IntConst): T

  fun visitEq(expr: Eq): T

  fun apply(expr: GgExpr<*>): T = expr.accept(this)
}

class Z3TranslatorVisitor(val z3ctx: Context) : ExprVisitor<Expr<*>> {
  override fun visitInt32Const(expr: IntConst): IntExpr {
    return z3ctx.mkIntConst(expr.name)
  }

  override fun visitEq(expr: Eq): Expr<*> {
    return z3ctx.mkEq(apply(expr.left), apply(expr.right))
  }
}
