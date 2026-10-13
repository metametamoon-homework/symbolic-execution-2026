package com.github.metametamoon.app.expr

sealed interface GgExpr<out Sort : ExprSort> {
  val sort: Sort

  fun toExprString(): String

  fun <T> accept(visitor: ExprVisitor<T>): T {
    TODO()
  }
}

data class IntValue(val value: Int) : GgExpr<GgIntSort> {
  override val sort: GgIntSort = GgIntSort()

  override fun toExprString(): String = "$value"
}

data class IntConst(val name: String) : GgExpr<GgIntSort> {
  override val sort: GgIntSort = GgIntSort()

  override fun toExprString(): String = name
}

data class Eq(val left: GgExpr<GgIntSort>, val right: GgExpr<GgIntSort>) : GgExpr<GgBoolSort> {
  override val sort: GgBoolSort = GgBoolSort()

  override fun toExprString(): String = "(add ${left.toExprString()} ${right.toExprString()})"
}

data class Add(val left: GgExpr<GgIntSort>, val right: GgExpr<GgIntSort>) : GgExpr<GgIntSort> {
  override val sort: GgIntSort = left.sort

  override fun toExprString(): String = "(add ${left.toExprString()} ${right.toExprString()})"
}

data class Gt(val left: GgExpr<GgIntSort>, val right: GgExpr<GgIntSort>) : GgExpr<GgBoolSort> {
  override val sort: GgBoolSort = GgBoolSort()

  override fun toExprString(): String = "(gt ${left.toExprString()} ${right.toExprString()})"
}

data class Lt(val left: GgExpr<GgIntSort>, val right: GgExpr<GgIntSort>) : GgExpr<GgBoolSort> {
  override val sort: GgBoolSort = GgBoolSort()

  override fun toExprString(): String = "(lt ${left.toExprString()} ${right.toExprString()})"
}

data class And(val operands: List<GgExpr<GgBoolSort>>) : GgExpr<GgBoolSort> {
  override val sort: GgBoolSort = GgBoolSort()

  override fun toExprString(): String = "(and ${operands.joinToString(" ") { it.toExprString() }})"
}
