package com.github.metametamoon.app.interpreter

import com.github.metametamoon.app.TrueFunc
import com.github.metametamoon.app.TrueProgram
import com.github.metametamoon.app.expr.GgContext
import com.microsoft.z3.Context
import kotlinx.collections.immutable.toPersistentMap

class GgMachine(val source: TrueProgram) {
  private val z3ctx = Context()
  val ctx = GgContext()
  val solver = GgSolver(ctx, z3ctx)
  private val interpreter = GgInterpreter(solver)

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
        is ExecutionStatus.Returned -> {
          val apply = result.model.apply(execStatus.value.single())
          ResolvedExecutionResult.Returns(apply.intValue())
        }
      },
    )
  }

  fun runSymbolicExecution(entryFunction: TrueFunc): List<GgState> {
    val initialState =
      GgState(
        ctx,
        source,
        InstructionPointer(entryFunction, entryFunction.blocks.first().instrs.first()),
      )
    initialState.paramMap =
      entryFunction.params.associateWith { freshConstant(initialState.ctx) }.toPersistentMap()
    val pathSelector = BfsPathSelector()
    pathSelector.pushAliveStates(listOf(initialState))
    val finishedStates = mutableListOf<GgState>()
    while (true) {
      val nextState = pathSelector.queryNextState() ?: break
      val nextStates = interpreter.step(nextState)
      pathSelector.pushAliveStates(
        nextStates.filter { it.executionStatus is ExecutionStatus.Alive }
      )
      nextStates.filterTo(finishedStates) { it.executionStatus is ExecutionStatus.Returned }
    }
    return finishedStates
  }

  fun runSymbolicExecutionAndResolve(entryFunction: TrueFunc): List<GgResolvedState> =
    runSymbolicExecution(entryFunction).map { resolveState(it, entryFunction) }
}
