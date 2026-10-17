package com.github.metametamoon.app.expr

interface ExprVisitor {
  val ctx: GgContext

  fun visitIntValue(expr: IntValue): GgExpr<GgIntSort> = expr

  fun visitBoolValue(expr: BoolValue): GgExpr<GgBoolSort> = expr

  fun visitInt32Const(expr: IntConst): GgExpr<GgIntSort> = expr

  fun visitEq(expr: Eq): GgExpr<GgBoolSort> =
    ctx.mkEq(expr.left.accept(this), expr.right.accept(this))

  fun visitAdd(expr: Add): GgExpr<GgIntSort> =
    ctx.mkAdd(expr.left.accept(this), expr.right.accept(this))

  fun visitMul(expr: Mul): GgExpr<GgIntSort> =
    ctx.mkMul(expr.left.accept(this), expr.right.accept(this))

  fun visitGt(expr: Gt): GgExpr<GgBoolSort> =
    ctx.mkGt(expr.left.accept(this), expr.right.accept(this))

  fun visitLt(expr: Lt): GgExpr<GgBoolSort> =
    ctx.mkLt(expr.left.accept(this), expr.right.accept(this))

  fun visitAnd(expr: And): GgExpr<GgBoolSort> = ctx.mkAnd(expr.operands.map { it.accept(this) })

  fun visitNot(expr: Not): GgExpr<GgBoolSort> = ctx.mkNot(expr.operand.accept(this))

  fun apply(expr: GgExpr<*>): GgExpr<*> = expr.accept(this)
}
