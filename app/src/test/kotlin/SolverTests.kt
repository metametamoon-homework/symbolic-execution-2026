import com.github.metametamoon.app.expr.BoolValue
import com.github.metametamoon.app.expr.GgBoolSort
import com.github.metametamoon.app.expr.GgContext
import com.github.metametamoon.app.expr.GgExpr
import com.github.metametamoon.app.expr.GgIntSort
import com.github.metametamoon.app.expr.IntValue
import com.github.metametamoon.app.interpreter.GgModel
import com.github.metametamoon.app.interpreter.GgSolver
import com.github.metametamoon.app.interpreter.PathConstraints
import com.github.metametamoon.app.interpreter.SolverResult
import com.microsoft.z3.Context
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

class SolverTests {
  private val ctx = GgContext()

  private fun solve(vararg constraints: GgExpr<GgBoolSort>): SolverResult =
    Context().use { z3ctx ->
      val pathConstraints = PathConstraints()
      constraints.forEach { pathConstraints.addCs(it) }
      GgSolver(ctx, z3ctx).solve(pathConstraints)
    }

  private fun solveSat(vararg constraints: GgExpr<GgBoolSort>): GgModel {
    val result = solve(*constraints)
    assertIs<SolverResult.Sat>(result)
    return result.model
  }

  private fun GgModel.intValueOf(expr: GgExpr<GgIntSort>): Int =
    assertIs<IntValue>(apply(expr)).value

  private fun GgModel.boolValueOf(expr: GgExpr<GgBoolSort>): Boolean =
    assertIs<BoolValue>(apply(expr)).value

  @Test
  fun `arithmetic constraints are satisfiable`() {
    val x = ctx.mkIntConst("x")
    val y = ctx.mkIntConst("y")
    val model =
      solveSat(
        ctx.mkEq(ctx.mkSub(x, y), ctx.mkIntValue(3)),
        ctx.mkEq(ctx.mkMul(x, y), ctx.mkIntValue(10)),
        ctx.mkGe(x, ctx.mkIntValue(0)),
        ctx.mkLe(y, ctx.mkIntValue(100)),
      )

    val xValue = model.intValueOf(x)
    val yValue = model.intValueOf(y)
    assertEquals(3, xValue - yValue)
    assertEquals(10, xValue * yValue)
    assertTrue(xValue >= 0)
    assertTrue(yValue <= 100)
  }

  @Test
  fun `boolean and division constraints are satisfiable`() {
    val x = ctx.mkIntConst("x")
    val p = ctx.mkBoolConst("p")
    val q = ctx.mkBoolConst("q")
    val model =
      solveSat(
        ctx.mkOr(listOf(p, q)),
        ctx.mkNot(q),
        ctx.mkImplies(p, ctx.mkEq(ctx.mkDiv(x, ctx.mkIntValue(3)), ctx.mkIntValue(4))),
        ctx.mkNe(ctx.mkMod(x, ctx.mkIntValue(3)), ctx.mkIntValue(0)),
        ctx.mkNe(x, ctx.mkIntValue(13)),
      )

    val xValue = model.intValueOf(x)
    val pValue = model.boolValueOf(p)
    val qValue = model.boolValueOf(q)
    assertTrue(pValue || qValue)
    assertTrue(!qValue)
    assertTrue(!pValue || Math.floorDiv(xValue, 3) == 4) // aka implies
    assertTrue(Math.floorMod(xValue, 3) != 0)
    assertTrue(xValue != 13)
    assertEquals(14, xValue)
  }

  @Test
  fun `contradicting bounds are unsatisfiable`() {
    val x = ctx.mkIntConst("x")
    val y = ctx.mkIntConst("y")
    val result =
      solve(
        ctx.mkGe(ctx.mkSub(x, y), ctx.mkIntValue(5)),
        ctx.mkLe(x, ctx.mkIntValue(10)),
        ctx.mkGe(y, ctx.mkIntValue(6)),
      )

    assertEquals(SolverResult.Unsat, result)
  }

  @Test
  fun `contradicting implications are unsatisfiable`() {
    val x = ctx.mkIntConst("x")
    val p = ctx.mkBoolConst("p")
    val q = ctx.mkBoolConst("q")
    val result =
      solve(
        ctx.mkOr(listOf(p, q)),
        ctx.mkImplies(p, ctx.mkLt(x, ctx.mkIntValue(0))),
        ctx.mkImplies(q, ctx.mkGt(x, ctx.mkIntValue(10))),
        ctx.mkGe(x, ctx.mkIntValue(0)),
        ctx.mkLe(x, ctx.mkIntValue(10)),
      )

    assertEquals(SolverResult.Unsat, result)
  }
}
