package com.github.metametamoon.app.interpreter

import com.github.metametamoon.app.TrueFunc
import com.github.metametamoon.app.TrueInst
import com.github.metametamoon.app.TrueParameter
import com.github.metametamoon.app.TrueProgram
import com.github.metametamoon.app.expr.GgBoolSort
import com.github.metametamoon.app.expr.GgContext
import com.github.metametamoon.app.expr.GgExpr
import com.github.metametamoon.app.expr.GgIntSort
import com.github.metametamoon.app.expr.IntValue
import com.microsoft.z3.Context
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap

data class InstructionPointer(
  val function: TrueFunc,
  val instruction: TrueInst,
)

class PathConstraints(
  var logicalConstraints: PersistentList<GgExpr<GgBoolSort>> = persistentListOf()
) {
  fun addCs(expr: GgExpr<GgBoolSort>) {
    logicalConstraints = logicalConstraints.adding(expr)
  }

  fun clone(): PathConstraints = PathConstraints(logicalConstraints.toPersistentList())
}

var freshConstantCounter = 0

fun freshConstant(ctx: GgContext): GgExpr<GgIntSort> {
  freshConstantCounter++
  return ctx.mkIntConst("x$freshConstantCounter") // TODO add interning
}

var stateIdCounter: Int = 0

sealed interface ExecutionStatus {
  data class Returned(val value: List<GgExpr<*>>) : ExecutionStatus

  data object Alive : ExecutionStatus
}

class GgState(
  val ctx: GgContext,
  val program: TrueProgram,
  var ip: InstructionPointer,
  val pathConstraints: PathConstraints = PathConstraints(),
  var executionStatus: ExecutionStatus = ExecutionStatus.Alive,
) {
  val id = stateIdCounter++
  var paramMap = persistentMapOf<TrueParameter, GgExpr<GgIntSort>>()

  fun clone(): GgState {
    return GgState(
        ctx,
        program = program,
        ip = ip,
        pathConstraints = pathConstraints.clone(),
        executionStatus = executionStatus,
      )
      .also { it.paramMap = paramMap }
  }
}

sealed interface ResolvedExecutionResult {
  data class Returns(val value: Int) : ResolvedExecutionResult
}

class GgResolvedState(
  val program: TrueProgram,
  val entryFunction: TrueFunc,
  val initialParameters: List<Int>,
  val resolvedExecutionResult: ResolvedExecutionResult,
)

class GgMachine(val source: TrueProgram) {
  private val z3ctx = Context()
  private val interpreter = GgInterpreter()

  fun resolveState(state: GgState, entryFunction: TrueFunc): GgResolvedState {
    val solver = GgSolver(state.ctx, z3ctx)
    val result = solver.solve(state.pathConstraints)
    require(result is SolverResult.Sat)
    return GgResolvedState(
      state.program,
      entryFunction,
      state.paramMap.map {
        val paramSymbol = it.value
        val evalledParam = result.model.apply(paramSymbol)
        evalledParam.intValue()
      },
      when (val execStatus = state.executionStatus) {
        ExecutionStatus.Alive -> error("Alive")
        is ExecutionStatus.Returned ->
          ResolvedExecutionResult.Returns(result.model.apply(execStatus.value.single()).intValue())
      },
    )
  }

  fun runSymbolicExecution(entryFunction: TrueFunc): List<GgState> {
    val initialState =
      GgState(
        GgContext(),
        source,
        InstructionPointer(entryFunction, entryFunction.blocks.first().instrs.first()),
      )
    initialState.paramMap =
      entryFunction.params.associateWith { freshConstant(initialState.ctx) }.toPersistentMap()
    val aliveStatesQueue = mutableListOf(initialState)
    val finishedStates = mutableListOf<GgState>()
    while (aliveStatesQueue.isNotEmpty()) {
      val nextState = aliveStatesQueue.removeFirst()
      val nextStates = interpreter.step(nextState)
      nextStates.filterTo(aliveStatesQueue) { it.executionStatus is ExecutionStatus.Alive }
      nextStates.filterTo(finishedStates) { it.executionStatus is ExecutionStatus.Returned }
    }
    return finishedStates
  }

  fun runSymbolicExecutionAndResolve(entryFunction: TrueFunc): List<GgResolvedState> =
    runSymbolicExecution(entryFunction).map { resolveState(it, entryFunction) }
}

private fun GgExpr<*>.intValue(): Int = (this as IntValue).value
