package com.github.metametamoon.app.expr

import com.github.metametamoon.app.TrueType
import kotlin.math.absoluteValue

class GgContext {
  fun mkIntValue(value: Int): GgExpr<GgIntSort> = IntValue(value)

  fun mkBoolValue(value: Boolean): GgExpr<GgBoolSort> = BoolValue(value)

  fun mkIntConst(name: String): GgExpr<GgIntSort> = IntConst(name)

  fun mkArrayConst(name: String, fromSort: GgSort, toSort: GgSort) =
    ArrayConst(name, fromSort, toSort)

  fun mkBoolConst(name: String): GgExpr<GgBoolSort> = BoolConst(name)

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

  fun mkNe(left: GgExpr<GgIntSort>, right: GgExpr<GgIntSort>): GgExpr<GgBoolSort> =
    if (left is IntValue && right is IntValue) mkBoolValue(left.value != right.value)
    else Ne(left, right)

  fun mkSub(left: GgExpr<GgIntSort>, right: GgExpr<GgIntSort>): GgExpr<GgIntSort> =
    if (left is IntValue && right is IntValue) mkIntValue(left.value - right.value)
    else Sub(left, right)

  fun mkDiv(left: GgExpr<GgIntSort>, right: GgExpr<GgIntSort>): GgExpr<GgIntSort> =
    if (left is IntValue && right is IntValue && right.value != 0)
      mkIntValue(euclideanDiv(left.value, right.value))
    else Div(left, right)

  fun mkMod(left: GgExpr<GgIntSort>, right: GgExpr<GgIntSort>): GgExpr<GgIntSort> =
    if (left is IntValue && right is IntValue && right.value != 0)
      mkIntValue(euclideanMod(left.value, right.value))
    else Mod(left, right)

  fun mkLe(left: GgExpr<GgIntSort>, right: GgExpr<GgIntSort>): GgExpr<GgBoolSort> =
    if (left is IntValue && right is IntValue) mkBoolValue(left.value <= right.value)
    else Le(left, right)

  fun mkGe(left: GgExpr<GgIntSort>, right: GgExpr<GgIntSort>): GgExpr<GgBoolSort> =
    if (left is IntValue && right is IntValue) mkBoolValue(left.value >= right.value)
    else Ge(left, right)

  fun mkOr(operands: List<GgExpr<GgBoolSort>>): GgExpr<GgBoolSort> {
    if (operands.any { it is BoolValue && it.value }) return mkBoolValue(true)
    val nonTrivialOperands = operands.filterNot { it is BoolValue }
    return when (nonTrivialOperands.size) {
      0 -> mkBoolValue(false)
      1 -> nonTrivialOperands.single()
      else -> Or(nonTrivialOperands)
    }
  }

  fun mkImplies(left: GgExpr<GgBoolSort>, right: GgExpr<GgBoolSort>): GgExpr<GgBoolSort> =
    when {
      left is BoolValue -> if (left.value) right else mkBoolValue(true)
      right is BoolValue && right.value -> mkBoolValue(true)
      else -> Implies(left, right)
    }

  private fun euclideanMod(left: Int, right: Int): Int = left.mod(right.absoluteValue)

  private fun euclideanDiv(left: Int, right: Int): Int = (left - euclideanMod(left, right)) / right

  private var allocCounter = 0

  fun alloc(): AllocatedPointerExpr = AllocatedPointerExpr(allocCounter++)

  fun allocFieldRead(structType: TrueType, fieldIdx: Int, structValue: GgExpr<*>): FieldPointer {
    return FieldPointer(allocCounter++, structType, fieldIdx, structValue)
  }
}
