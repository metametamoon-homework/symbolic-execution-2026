package com.github.metametamoon.app.expr

import com.github.metametamoon.app.TrueType

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

data class BoolConst(val name: String) : GgExpr<GgBoolSort> {
  override val sort: GgBoolSort = GgBoolSort()

  override fun toExprString(): String = name

  override fun accept(visitor: ExprVisitor): GgExpr<GgBoolSort> = visitor.visitBoolConst(this)
}

data class Eq(val left: GgExpr<GgIntSort>, val right: GgExpr<GgIntSort>) : GgExpr<GgBoolSort> {
  override val sort: GgBoolSort = GgBoolSort()

  override fun toExprString(): String = "(eq ${left.toExprString()} ${right.toExprString()})"

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

data class Ne(val left: GgExpr<GgIntSort>, val right: GgExpr<GgIntSort>) : GgExpr<GgBoolSort> {
  override val sort: GgBoolSort = GgBoolSort()

  override fun toExprString(): String = "(ne ${left.toExprString()} ${right.toExprString()})"

  override fun accept(visitor: ExprVisitor): GgExpr<GgBoolSort> = visitor.visitNe(this)
}

data class Sub(val left: GgExpr<GgIntSort>, val right: GgExpr<GgIntSort>) : GgExpr<GgIntSort> {
  override val sort: GgIntSort = left.sort

  override fun toExprString(): String = "(sub ${left.toExprString()} ${right.toExprString()})"

  override fun accept(visitor: ExprVisitor): GgExpr<GgIntSort> = visitor.visitSub(this)
}

data class Div(val left: GgExpr<GgIntSort>, val right: GgExpr<GgIntSort>) : GgExpr<GgIntSort> {
  override val sort: GgIntSort = left.sort

  override fun toExprString(): String = "(div ${left.toExprString()} ${right.toExprString()})"

  override fun accept(visitor: ExprVisitor): GgExpr<GgIntSort> = visitor.visitDiv(this)
}

data class Mod(val left: GgExpr<GgIntSort>, val right: GgExpr<GgIntSort>) : GgExpr<GgIntSort> {
  override val sort: GgIntSort = left.sort

  override fun toExprString(): String = "(mod ${left.toExprString()} ${right.toExprString()})"

  override fun accept(visitor: ExprVisitor): GgExpr<GgIntSort> = visitor.visitMod(this)
}

data class Le(val left: GgExpr<GgIntSort>, val right: GgExpr<GgIntSort>) : GgExpr<GgBoolSort> {
  override val sort: GgBoolSort = GgBoolSort()

  override fun toExprString(): String = "(le ${left.toExprString()} ${right.toExprString()})"

  override fun accept(visitor: ExprVisitor): GgExpr<GgBoolSort> = visitor.visitLe(this)
}

data class Ge(val left: GgExpr<GgIntSort>, val right: GgExpr<GgIntSort>) : GgExpr<GgBoolSort> {
  override val sort: GgBoolSort = GgBoolSort()

  override fun toExprString(): String = "(ge ${left.toExprString()} ${right.toExprString()})"

  override fun accept(visitor: ExprVisitor): GgExpr<GgBoolSort> = visitor.visitGe(this)
}

data class Or(val operands: List<GgExpr<GgBoolSort>>) : GgExpr<GgBoolSort> {
  override val sort: GgBoolSort = GgBoolSort()

  override fun toExprString(): String = "(or ${operands.joinToString(" ") { it.toExprString() }})"

  override fun accept(visitor: ExprVisitor): GgExpr<GgBoolSort> = visitor.visitOr(this)
}

data class Implies(val left: GgExpr<GgBoolSort>, val right: GgExpr<GgBoolSort>) :
  GgExpr<GgBoolSort> {
  override val sort: GgBoolSort = GgBoolSort()

  override fun toExprString(): String = "(implies ${left.toExprString()} ${right.toExprString()})"

  override fun accept(visitor: ExprVisitor): GgExpr<GgBoolSort> = visitor.visitImplies(this)
}

sealed interface PointerExpr : GgExpr<GgIntSort>

data class AllocatedPointerExpr(val address: Int) : PointerExpr {
  override val sort: GgIntSort = GgIntSort()

  override fun toExprString(): String = "0x$address" // ksmt-style :)

  override fun accept(visitor: ExprVisitor): GgExpr<GgIntSort> =
    visitor.visitIntValue(IntValue(address)) // TODO make ctx visible
}

data class FieldPointer(
  val address: Int,
  val structType: TrueType,
  val fieldIdx: Int,
  val structPointer: GgExpr<*>,
) : PointerExpr {
  override val sort: GgIntSort = GgIntSort()

  override fun toExprString(): String = "(field-read $structType $fieldIdx)"

  override fun accept(visitor: ExprVisitor): GgExpr<GgIntSort> {
    TODO("Not yet implemented")
  }
}

data class ArrayConst<FromSort : GgSort, ToSort : GgSort>(
  val name: String,
  val fromSort: FromSort,
  val toSort: ToSort,
) : GgExpr<GgArraySort<FromSort, ToSort>> {
  override val sort: GgArraySort<FromSort, ToSort>
    get() = GgArraySort(fromSort, toSort)

  override fun toExprString(): String = name

  override fun accept(visitor: ExprVisitor): GgExpr<GgArraySort<FromSort, ToSort>> {
    TODO("Not yet implemented")
  }
}

data class Store<FromSort : GgSort, ToSort : GgSort>(
  val array: GgExpr<GgArraySort<FromSort, ToSort>>,
  val index: GgExpr<FromSort>,
  val value: GgExpr<ToSort>,
) : GgExpr<GgArraySort<FromSort, ToSort>> {
  override val sort: GgArraySort<FromSort, ToSort> = GgArraySort(index.sort, value.sort)

  override fun accept(visitor: ExprVisitor): GgExpr<GgArraySort<FromSort, ToSort>> {
    TODO("Not yet implemented")
  }

  override fun toExprString(): String {
    TODO("Not yet implemented")
  }
}

data class Select<FromSort : GgSort, ToSort : GgSort>(
  val array: GgExpr<GgArraySort<FromSort, ToSort>>,
  val index: GgExpr<FromSort>,
) : GgExpr<ToSort> {
  override val sort: ToSort = array.sort.toSort

  override fun accept(visitor: ExprVisitor): GgExpr<ToSort> =
    visitor.visitSelectExpr(this).uncheckedCast()

  override fun toExprString(): String {
    TODO("Not yet implemented")
  }
}
