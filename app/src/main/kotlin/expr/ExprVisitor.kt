package com.github.metametamoon.app.expr

sealed interface ExprVisitor {
  fun visitInt32Value(expr: Int32Value): Expr<Int32Sort>

  fun visitInt32Const(expr: Int32Const): Expr<Int32Sort>
}
