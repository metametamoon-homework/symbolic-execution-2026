package com.github.metametamoon.app.expr

class GgContext {
  fun mkIntValue(value: Int): GgExpr<GgIntSort> = IntValue(value)

  fun mkBoolValue(value: Boolean): GgExpr<GgBoolSort> = BoolValue(value)

  fun mkIntConst(name: String): GgExpr<GgIntSort> = IntConst(name)

  fun mkEq(left: GgExpr<GgIntSort>, right: GgExpr<GgIntSort>): GgExpr<GgBoolSort> =
    if (left is IntValue && right is IntValue) mkBoolValue(left.value == right.value)
    else Eq(left, right)

  fun mkAdd(left: GgExpr<GgIntSort>, right: GgExpr<GgIntSort>): GgExpr<GgIntSort> =
    if (left is IntValue && right is IntValue) mkIntValue(left.value + right.value)
    else Add(left, right)

  fun mkMul(left: GgExpr<GgIntSort>, right: GgExpr<GgIntSort>): GgExpr<GgIntSort> =
    if (left is IntValue && right is IntValue) mkIntValue(left.value * right.value)
    else Mul(left, right)

  fun mkGt(left: GgExpr<GgIntSort>, right: GgExpr<GgIntSort>): GgExpr<GgBoolSort> =
    if (left is IntValue && right is IntValue) mkBoolValue(left.value > right.value)
    else Gt(left, right)

  fun mkLt(left: GgExpr<GgIntSort>, right: GgExpr<GgIntSort>): GgExpr<GgBoolSort> =
    if (left is IntValue && right is IntValue) mkBoolValue(left.value < right.value)
    else Lt(left, right)

  fun mkAnd(operands: List<GgExpr<GgBoolSort>>): GgExpr<GgBoolSort> {
    if (operands.any { it is BoolValue && !it.value }) return mkBoolValue(false)
    val nonTrivialOperands = operands.filterNot { it is BoolValue }
    return when (nonTrivialOperands.size) {
      0 -> mkBoolValue(true)
      1 -> nonTrivialOperands.single()
      else -> And(nonTrivialOperands)
    }
  }

  fun mkNot(arg: GgExpr<GgBoolSort>): GgExpr<GgBoolSort> =
    if (arg is BoolValue) mkBoolValue(!arg.value) else Not(arg)
}
