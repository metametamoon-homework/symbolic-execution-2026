package com.github.metametamoon.app.expr

@Suppress("UNCHECKED_CAST")
inline fun <reified T : GgSort, reified U : GgSort> GgExpr<U>.uncheckedExprCast(): GgExpr<T> {
  require(sort is T)
  return this as GgExpr<T>
}
