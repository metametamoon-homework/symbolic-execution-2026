package com.github.metametamoon.app.expr

sealed interface Expr<Sort : ExprSort> {
  val sort: ExprSort

  fun toExprString(): String

  fun accept(visitor: ExprVisitor): Expr<Sort>
}

data class Int32Value(val value: Int) : Expr<Int32Sort> {
  override val sort: ExprSort = Int32Sort()

  override fun accept(visitor: ExprVisitor): Expr<Int32Sort> {
    TODO("Not yet implemented")
  }

  override fun toExprString(): String = "$value"
}

data class Int32Const(val name: String) : Expr<Int32Sort> {
  override val sort: ExprSort = Int32Sort()

  override fun accept(visitor: ExprVisitor): Expr<Int32Sort> {
    TODO("Not yet implemented")
  }

  override fun toExprString(): String = name
}

// data class SumExpr<Sort : ExprSort>(val left: Expr<Sort>, val right: Expr<Sort>) : Expr<ExprSort>
// {
//  override val sort: ExprSort = left.sort
//
//  override fun toExprString(): String {
//    return "(add ${left.toExprString()} ${right.toExprString()})"
//  }
//
//  override fun accept(visitor: ExprVisitor): Expr<ExprSort> {
//    TODO("Not yet implemented")
//  }
// }

data class Eq<Sort : ExprSort>(val left: Expr<Sort>, val right: Expr<Sort>) : Expr<BoolSort> {
  override val sort: ExprSort = BoolSort()

  override fun toExprString(): String {
    return "(add ${left.toExprString()} ${right.toExprString()})"
  }

  override fun accept(visitor: ExprVisitor): Expr<BoolSort> {
    TODO("Not yet implemented")
  }
}
