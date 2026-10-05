package com.github.metametamoon.app.expr

sealed interface ExprVisitor {
  fun visitInt32Value(expr: Int32Value): GgExpr<Bv32Sort>

  fun visitInt32Const(expr: Int32Const): GgExpr<Bv32Sort>
}
