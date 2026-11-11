package com.github.metametamoon.app.expr

interface ExprVisitor {
  val ctx: GgContext

  fun visitIntValue(expr: IntValue): GgExpr<GgIntSort> = expr

  fun visitBoolValue(expr: BoolValue): GgExpr<GgBoolSort> = expr

  fun visitInt32Const(expr: IntConst): GgExpr<GgIntSort> = expr

  fun visitBoolConst(expr: BoolConst): GgExpr<GgBoolSort> = expr

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

  fun visitNe(expr: Ne): GgExpr<GgBoolSort> =
    ctx.mkNe(expr.left.accept(this), expr.right.accept(this))

  fun visitSub(expr: Sub): GgExpr<GgIntSort> =
    ctx.mkSub(expr.left.accept(this), expr.right.accept(this))

  fun visitDiv(expr: Div): GgExpr<GgIntSort> =
    ctx.mkDiv(expr.left.accept(this), expr.right.accept(this))

  fun visitMod(expr: Mod): GgExpr<GgIntSort> =
    ctx.mkMod(expr.left.accept(this), expr.right.accept(this))

  fun visitLe(expr: Le): GgExpr<GgBoolSort> =
    ctx.mkLe(expr.left.accept(this), expr.right.accept(this))

  fun visitGe(expr: Ge): GgExpr<GgBoolSort> =
    ctx.mkGe(expr.left.accept(this), expr.right.accept(this))

  fun visitImplies(expr: Implies): GgExpr<GgBoolSort> =
    ctx.mkImplies(expr.left.accept(this), expr.right.accept(this))

  fun visitOr(expr: Or): GgExpr<GgBoolSort> = ctx.mkOr(expr.operands.map { it.accept(this) })

  fun visitStoreExpr(expr: Store<*, *>): GgExpr<*> = expr

  fun visitSelectExpr(expr: Select<*, *>): GgExpr<*> = expr

  fun apply(expr: GgExpr<*>): GgExpr<*> = expr.accept(this)
}
