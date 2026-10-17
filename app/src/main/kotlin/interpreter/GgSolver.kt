package com.github.metametamoon.app.interpreter

import com.github.metametamoon.app.expr.ExprVisitor
import com.github.metametamoon.app.expr.GgContext
import com.github.metametamoon.app.expr.GgExpr
import com.github.metametamoon.app.expr.GgIntSort
import com.github.metametamoon.app.expr.GgSort
import com.github.metametamoon.app.expr.IntConst
import com.github.metametamoon.app.expr.IntValue
import com.github.metametamoon.app.expr.Z3Translator
import com.github.metametamoon.app.expr.uncheckedCast
import com.github.metametamoon.app.expr.uncheckedExprCast
import com.microsoft.z3.BoolSort
import com.microsoft.z3.Context
import com.microsoft.z3.Expr
import com.microsoft.z3.IntNum
import com.microsoft.z3.Solver
import com.microsoft.z3.Status

sealed interface SolverResult {
  data class Sat(val model: GgModel) : SolverResult

  data object Unsat : SolverResult

  data class Unknown(val reasonOfUnknown: String) : SolverResult
}

class GgModel(override val ctx: GgContext, val substitutions: Map<GgExpr<*>, GgExpr<*>>) :
  ExprVisitor {

  private inline fun <reified Sort : GgSort> getLeafValue(before: GgExpr<Sort>): GgExpr<Sort> {
    val after = substitutions[before] ?: error("No substitution for $before")
    return after.uncheckedExprCast()
  }

  override fun visitInt32Const(expr: IntConst): GgExpr<GgIntSort> = getLeafValue(expr)
}

class GgSolver(val ctx: GgContext, val z3ctx: Context) {
  val z3Solver: Solver by lazy {
    z3ctx.mkSolver()
  }

  fun solve(pathConstraints: PathConstraints): SolverResult {
    val translator = Z3Translator(z3ctx)
    val constraints =
      pathConstraints.logicalConstraints.map {
        translator.translate(it).uncheckedCast<Expr<BoolSort>, _>()
      }
    z3Solver.add(*constraints.toTypedArray())
    val result = z3Solver.check()
    return when (result) {
      Status.UNKNOWN -> SolverResult.Unknown(z3Solver.reasonUnknown)
      Status.UNSATISFIABLE -> SolverResult.Unsat
      Status.SATISFIABLE -> {
        val z3model = z3Solver.model
        val translated =
          translator.leafValues.map { (z3Expr, expr) ->
            val evalledGgExpr =
              when (val evalledZ3 = z3model.eval(z3Expr, true)) {
                is IntNum -> IntValue(evalledZ3.int)
                is Any -> TODO()
              }
            expr to evalledGgExpr
          }
        val model = GgModel(ctx, translated.toMap())
        SolverResult.Sat(model)
      }
    }
  }
}
