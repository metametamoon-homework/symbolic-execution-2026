package com.github.metametamoon.app.expr

sealed interface GgExpr<out Sort : GgSort> {
  val sort: Sort

  fun toExprString(): String

  fun accept(visitor: ExprVisitor): GgExpr<Sort>
}

data class IntValue(val value: Int) : GgExpr<GgIntSort> {
  override val sort: GgIntSort = GgIntSort()

  override fun toExprString(): String = "$value"

  override fun accept(visitor: ExprVisitor): GgExpr<GgIntSort> = visitor.visitIntValue(this)
}

data class BoolValue(val value: Boolean) : GgExpr<GgBoolSort> {
  override val sort: GgBoolSort = GgBoolSort()

  override fun toExprString(): String = "$value"

  override fun accept(visitor: ExprVisitor): GgExpr<GgBoolSort> = visitor.visitBoolValue(this)
}

data class IntConst(val name: String) : GgExpr<GgIntSort> {
  override val sort: GgIntSort = GgIntSort()

  override fun toExprString(): String = name

  override fun accept(visitor: ExprVisitor): GgExpr<GgIntSort> = visitor.visitInt32Const(this)
}

data class Eq(val left: GgExpr<GgIntSort>, val right: GgExpr<GgIntSort>) : GgExpr<GgBoolSort> {
  override val sort: GgBoolSort = GgBoolSort()

  override fun toExprString(): String = "(add ${left.toExprString()} ${right.toExprString()})"

  override fun accept(visitor: ExprVisitor): GgExpr<GgBoolSort> = visitor.visitEq(this)
}

data class Add(val left: GgExpr<GgIntSort>, val right: GgExpr<GgIntSort>) : GgExpr<GgIntSort> {
  override val sort: GgIntSort = left.sort

  override fun toExprString(): String = "(add ${left.toExprString()} ${right.toExprString()})"

  override fun accept(visitor: ExprVisitor): GgExpr<GgIntSort> = visitor.visitAdd(this)
}

data class Mul(val left: GgExpr<GgIntSort>, val right: GgExpr<GgIntSort>) : GgExpr<GgIntSort> {
  override val sort: GgIntSort = left.sort

  override fun toExprString(): String = "(mul ${left.toExprString()} ${right.toExprString()})"

  override fun accept(visitor: ExprVisitor): GgExpr<GgIntSort> = visitor.visitMul(this)
}

data class Gt(val left: GgExpr<GgIntSort>, val right: GgExpr<GgIntSort>) : GgExpr<GgBoolSort> {
  override val sort: GgBoolSort = GgBoolSort()

  override fun toExprString(): String = "(gt ${left.toExprString()} ${right.toExprString()})"

  override fun accept(visitor: ExprVisitor): GgExpr<GgBoolSort> = visitor.visitGt(this)
}

data class Lt(val left: GgExpr<GgIntSort>, val right: GgExpr<GgIntSort>) : GgExpr<GgBoolSort> {
  override val sort: GgBoolSort = GgBoolSort()

  override fun toExprString(): String = "(lt ${left.toExprString()} ${right.toExprString()})"

  override fun accept(visitor: ExprVisitor): GgExpr<GgBoolSort> = visitor.visitLt(this)
}

data class And(val operands: List<GgExpr<GgBoolSort>>) : GgExpr<GgBoolSort> {
  override val sort: GgBoolSort = GgBoolSort()

  override fun toExprString(): String = "(and ${operands.joinToString(" ") { it.toExprString() }})"

  override fun accept(visitor: ExprVisitor): GgExpr<GgBoolSort> = visitor.visitAnd(this)
}

data class Not(val operand: GgExpr<GgBoolSort>) : GgExpr<GgBoolSort> {
  override val sort: GgBoolSort = GgBoolSort()

  override fun toExprString(): String = "(not ${operand.toExprString()})"

  override fun accept(visitor: ExprVisitor): GgExpr<GgBoolSort> = visitor.visitNot(this)
}
