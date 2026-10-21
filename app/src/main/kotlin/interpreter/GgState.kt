package com.github.metametamoon.app.interpreter

import com.github.metametamoon.app.TrueBlock
import com.github.metametamoon.app.TrueFunc
import com.github.metametamoon.app.TrueInst
import com.github.metametamoon.app.TrueParameter
import com.github.metametamoon.app.TrueProgram
import com.github.metametamoon.app.TrueValue
import com.github.metametamoon.app.expr.GgBoolSort
import com.github.metametamoon.app.expr.GgContext
import com.github.metametamoon.app.expr.GgExpr
import com.github.metametamoon.app.expr.GgIntSort
import com.github.metametamoon.app.expr.IntValue
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList

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
  var valuesEvaluated: PersistentMap<TrueValue, GgExpr<*>> = persistentMapOf(),
) {
  val id: Int = stateIdCounter++
  var paramMap: PersistentMap<TrueParameter, GgExpr<GgIntSort>> =
    persistentMapOf<TrueParameter, GgExpr<GgIntSort>>()

  var prevBlock: TrueBlock = ip.block()

  fun clone(): GgState {
    return GgState(
        ctx,
        program = program,
        ip = ip,
        pathConstraints = pathConstraints.clone(),
        executionStatus = executionStatus,
        valuesEvaluated = valuesEvaluated,
      )
      .also {
        it.paramMap = paramMap
        it.prevBlock = prevBlock
      }
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

fun GgExpr<*>.intValue(): Int = (this as IntValue).value
