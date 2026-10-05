package com.github.metametamoon.app.expr

sealed interface GgExpr<out Sort : ExprSort> {
  val sort: Sort

  fun toExprString(): String

  fun accept(visitor: ExprVisitor): GgExpr<Sort>
}

data class Int32Value(val value: Int) : GgExpr<Bv32Sort> {
  override val sort: Bv32Sort = Bv32Sort()

  override fun accept(visitor: ExprVisitor): GgExpr<Bv32Sort> {
    TODO("Not yet implemented")
  }

  override fun toExprString(): String = "$value"
}

data class Int32Const(val name: String) : GgExpr<Bv32Sort> {
  override val sort: Bv32Sort = Bv32Sort()

  override fun accept(visitor: ExprVisitor): GgExpr<Bv32Sort> {
    TODO("Not yet implemented")
  }

  override fun toExprString(): String = name
}

data class Eq(val left: GgExpr<Bv32Sort>, val right: GgExpr<Bv32Sort>) : GgExpr<BoolSort> {
  override val sort: BoolSort = BoolSort()

  override fun toExprString(): String = "(add ${left.toExprString()} ${right.toExprString()})"

  override fun accept(visitor: ExprVisitor): GgExpr<BoolSort> {
    TODO("Not yet implemented")
  }
}

data class Add(val left: GgExpr<Bv32Sort>, val right: GgExpr<Bv32Sort>) : GgExpr<Bv32Sort> {
  override val sort: Bv32Sort = left.sort

  override fun toExprString(): String = "(add ${left.toExprString()} ${right.toExprString()})"

  override fun accept(visitor: ExprVisitor): GgExpr<Bv32Sort> {
    TODO("Not yet implemented")
  }
}

data class Gt(val left: GgExpr<Bv32Sort>, val right: GgExpr<Bv32Sort>) : GgExpr<BoolSort> {
  override val sort: BoolSort = BoolSort()

  override fun toExprString(): String = "(gt ${left.toExprString()} ${right.toExprString()})"

  override fun accept(visitor: ExprVisitor): GgExpr<BoolSort> {
    TODO("Not yet implemented")
  }
}

data class Lt(val left: GgExpr<Bv32Sort>, val right: GgExpr<Bv32Sort>) : GgExpr<BoolSort> {
  override val sort: BoolSort = BoolSort()

  override fun toExprString(): String = "(lt ${left.toExprString()} ${right.toExprString()})"

  override fun accept(visitor: ExprVisitor): GgExpr<BoolSort> {
    TODO("Not yet implemented")
  }
}

data class And(val operands: List<GgExpr<BoolSort>>) : GgExpr<BoolSort> {
  override val sort: BoolSort = BoolSort()

  override fun toExprString(): String = "(and ${operands.joinToString(" ") { it.toExprString() }})"

  override fun accept(visitor: ExprVisitor): GgExpr<BoolSort> {
    TODO("Not yet implemented")
  }
}
