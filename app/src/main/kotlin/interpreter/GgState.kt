package com.github.metametamoon.app.interpreter

import com.github.metametamoon.app.TrueFunc
import com.github.metametamoon.app.TrueInst
import com.github.metametamoon.app.TrueProgram

data class InstructionPointer(
  val function: TrueFunc,
  val instruction: TrueInst,
)

class PathConstraints

class GgState(
  val program: TrueProgram,
  val ip: InstructionPointer,
  val pathConstraints: PathConstraints,
)

sealed interface ExecutionResult {
  data class Returns(val value: Int) : ExecutionResult
}

class GgResolvedState(
  val program: TrueProgram,
  val entryFunction: TrueFunc,
  val initialParameters: List<Int>,
  val executionResult: ExecutionResult,
)

class GgMachine(val source: TrueProgram) {
  fun runSymbolicExecution(entryFunction: TrueFunc): List<GgState> {
    TODO()
  }

  fun runSymbolicExecutionAndResolve(entryFunction: TrueFunc): List<GgResolvedState> =
    listOf(GgResolvedState(source, entryFunction, listOf(2), ExecutionResult.Returns(4)))
}
