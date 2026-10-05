package com.github.metametamoon.app.expr

sealed interface Expr<out Sort : ExprSort> {
  val sort: Sort

  fun toExprString(): String

  fun accept(visitor: ExprVisitor): Expr<Sort>
}

data class Int32Value(val value: Int) : Expr<Int32Sort> {
  override val sort: Int32Sort = Int32Sort()

  override fun accept(visitor: ExprVisitor): Expr<Int32Sort> {
    TODO("Not yet implemented")
  }

  override fun toExprString(): String = "$value"
}

data class Int32Const(val name: String) : Expr<Int32Sort> {
  override val sort: Int32Sort = Int32Sort()

  override fun accept(visitor: ExprVisitor): Expr<Int32Sort> {
    TODO("Not yet implemented")
  }

  override fun toExprString(): String = name
}

data class Eq<Sort : ExprSort>(val left: Expr<Sort>, val right: Expr<Sort>) : Expr<BoolSort> {
  override val sort: BoolSort = BoolSort()

  override fun toExprString(): String = "(add ${left.toExprString()} ${right.toExprString()})"

  override fun accept(visitor: ExprVisitor): Expr<BoolSort> {
    TODO("Not yet implemented")
  }
}

data class Add(val left: Expr<Int32Sort>, val right: Expr<Int32Sort>) : Expr<Int32Sort> {
  override val sort: Int32Sort = left.sort

  override fun toExprString(): String = "(add ${left.toExprString()} ${right.toExprString()})"

  override fun accept(visitor: ExprVisitor): Expr<Int32Sort> {
    TODO("Not yet implemented")
  }
}

data class GtExpr(val left: Expr<Int32Sort>, val right: Expr<Int32Sort>) : Expr<BoolSort> {
  override val sort: BoolSort = BoolSort()

  override fun toExprString(): String = "(gt ${left.toExprString()} ${right.toExprString()})"

  override fun accept(visitor: ExprVisitor): Expr<BoolSort> {
    TODO("Not yet implemented")
  }
}

data class LtExpr(val left: Expr<Int32Sort>, val right: Expr<Int32Sort>) : Expr<BoolSort> {
  override val sort: BoolSort = BoolSort()

  override fun toExprString(): String = "(lt ${left.toExprString()} ${right.toExprString()})"

  override fun accept(visitor: ExprVisitor): Expr<BoolSort> {
    TODO("Not yet implemented")
  }
}

data class AndExpr(val operands: List<Expr<BoolSort>>) : Expr<BoolSort> {
  override val sort: BoolSort = BoolSort()

  override fun toExprString(): String = "(and ${operands.joinToString(" ") { it.toExprString() }})"

  override fun accept(visitor: ExprVisitor): Expr<BoolSort> {
    TODO("Not yet implemented")
  }
}
