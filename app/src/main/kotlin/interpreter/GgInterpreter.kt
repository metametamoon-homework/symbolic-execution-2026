package com.github.metametamoon.app.interpreter

import com.github.metametamoon.app.TrueAlloc
import com.github.metametamoon.app.TrueBinOp
import com.github.metametamoon.app.TrueBlock
import com.github.metametamoon.app.TrueBuiltin
import com.github.metametamoon.app.TrueCall
import com.github.metametamoon.app.TrueConst
import com.github.metametamoon.app.TrueConvert
import com.github.metametamoon.app.TrueDefer
import com.github.metametamoon.app.TrueFieldAddr
import com.github.metametamoon.app.TrueFunctionRef
import com.github.metametamoon.app.TrueGlobal
import com.github.metametamoon.app.TrueIf
import com.github.metametamoon.app.TrueIndex
import com.github.metametamoon.app.TrueIndexAddr
import com.github.metametamoon.app.TrueJump
import com.github.metametamoon.app.TrueMakeInterface
import com.github.metametamoon.app.TrueOpaque
import com.github.metametamoon.app.TruePanic
import com.github.metametamoon.app.TrueParameter
import com.github.metametamoon.app.TruePhi
import com.github.metametamoon.app.TrueReturn
import com.github.metametamoon.app.TrueRunDefers
import com.github.metametamoon.app.TrueSlice
import com.github.metametamoon.app.TrueStore
import com.github.metametamoon.app.TrueUnOp
import com.github.metametamoon.app.TrueValue
import com.github.metametamoon.app.TypedValue
import com.github.metametamoon.app.expr.GgBoolSort
import com.github.metametamoon.app.expr.GgExpr
import com.github.metametamoon.app.expr.GgIntSort
import com.github.metametamoon.app.expr.runtimeExprCast

fun InstructionPointer.block(): TrueBlock = function.blocks.single { instruction in it.instrs }

class GgInterpreter(private val solver: GgSolver) {

  private fun lookupValue(state: GgState, value: TrueValue): GgExpr<*> {
    val storedInRegister = state.valuesEvaluated[value]
    return when {
      storedInRegister != null -> storedInRegister
      value is TrueParameter -> {
        state.paramMap[value] ?: error("no param ???")
      }

      value is TrueConst -> {
        state.ctx.mkIntValue(value.value.toString().toInt())
      }

      else -> TODO()
    }
  }

  private fun evalValue(state: GgState, value: TrueValue): GgExpr<*> {
    return when (value) {
      is TypedValue ->
        when (value) {
          is TrueAlloc -> TODO()
          is TrueBinOp -> {
            val leftValue = value.lhs
            val rightValue = value.rhs

            val leftEvaluated = lookupValue(state, leftValue).runtimeExprCast<GgIntSort, _>()
            val rightEvaluated = lookupValue(state, rightValue).runtimeExprCast<GgIntSort, _>()
            when (value.op) {
              ">" -> state.ctx.mkGt(leftEvaluated, rightEvaluated)
              "<" -> state.ctx.mkLt(leftEvaluated, rightEvaluated)
              "*" -> state.ctx.mkMul(leftEvaluated, rightEvaluated)
              "+" -> state.ctx.mkAdd(leftEvaluated, rightEvaluated)
              else -> TODO("Unsupported")
            }
          }

          is TrueBuiltin -> TODO()
          is TrueCall -> TODO()
          is TrueConvert -> TODO()
          is TrueConst -> {
            state.ctx.mkIntValue(value.value.toString().toInt())
          }

          is TrueFunctionRef -> TODO()
          is TrueGlobal -> TODO()
          is TrueIndex -> TODO()
          is TrueIndexAddr -> TODO()
          is TrueFieldAddr -> TODO()
          is TrueSlice -> TODO()
          is TrueMakeInterface -> TODO()
          is TrueOpaque -> TODO()
          is TrueParameter -> {
            state.paramMap[value] ?: error("no param ???")
          }

          is TruePhi -> {

            val joins = value.sources
            val currentBlock = state.ip.block()
            val idx = currentBlock.preds.indexOf(state.prevBlock.index)
            val value = joins[idx]
            lookupValue(state, value)
          }

          is TrueUnOp -> TODO()
        }
    }
  }

  private fun nextStmt(state: GgState) {
    val currentInst = state.ip.instruction
    val function = state.ip.function
    val block = function.blocks.single { currentInst in it.instrs }
    val idx = block.instrs.indexOf(currentInst)
    val nextInst = block.instrs[idx + 1]
    val newIp = InstructionPointer(function, nextInst)
    state.ip = newIp
  }

  data class ForkResult(
    val trueState: GgState?,
    val falseState: GgState?,
  )

  private fun fork(state: GgState, condition: GgExpr<GgBoolSort>): ForkResult {
    val forkedState: GgState = state.clone()
    state.pathConstraints.addCs(condition)
    forkedState.pathConstraints.addCs(state.ctx.mkNot(condition))
    val trueBranchSolverResult = solver.solve(state.pathConstraints)
    val trueState =
      if (trueBranchSolverResult is SolverResult.Sat) {
        state
      } else null
    val falseBranchSolverResult = solver.solve(forkedState.pathConstraints)
    val falseState =
      if (falseBranchSolverResult is SolverResult.Sat) {
        forkedState
      } else null

    return ForkResult(trueState, falseState)
  }

  fun step(state: GgState): List<GgState> {
    val stmt = state.ip.instruction
    val blockIdx = state.ip.function.blocks.indexOf(state.ip.block())
    println(
      "state=${state.id} block=$blockIdx stmt=${stmt::class.simpleName} (${stmt::class.qualifiedName})"
    )
    when (stmt) {
      is TrueAlloc -> TODO()
      is TrueBinOp -> {
        val value = evalValue(state, stmt)
        state.valuesEvaluated = state.valuesEvaluated.putting(stmt, value)
        nextStmt(state)
        return listOf(state)
      }

      is TrueCall -> TODO()
      is TrueConvert -> TODO()
      is TrueDefer -> TODO()
      is TrueIf -> {
        val binOpCond = evalValue(state, stmt.cond).runtimeExprCast<GgBoolSort, _>()
        val func = state.ip.function
        val block = state.ip.block()
        val trueBlock = func.blocks[block.succs[0]]
        val falseBlock = func.blocks[block.succs[1]]
        val forking = fork(state, binOpCond)
        forking.trueState?.let {
          it.ip = InstructionPointer(it.ip.function, trueBlock.instrs.first())
          it.prevBlock = block
        }

        forking.falseState?.let {
          it.ip = InstructionPointer(it.ip.function, falseBlock.instrs.first())
          it.prevBlock = block
        }
        return listOfNotNull(forking.trueState, forking.falseState)
      }

      is TrueIndex -> TODO()
      is TrueIndexAddr -> TODO()
      is TrueFieldAddr -> TODO()
      is TrueSlice -> TODO()
      is TrueJump -> {
        val func = state.ip.function
        val block = state.ip.block()
        val trueBlock = func.blocks[block.succs[0]]
        state.ip = InstructionPointer(func, trueBlock.instrs.first())
        state.prevBlock = block
        return listOf(state)
      }

      is TrueMakeInterface -> TODO()
      is TruePanic -> TODO()
      is TruePhi -> {

        val value = evalValue(state, stmt)
        state.valuesEvaluated = state.valuesEvaluated.putting(stmt, value)
        nextStmt(state)
        return listOf(state)
      }

      is TrueReturn -> {
        val returnedValues = stmt.results.map { lookupValue(state, it) }
        state.executionStatus = ExecutionStatus.Returned(returnedValues)
        return listOf(state)
      }

      is TrueRunDefers -> TODO()
      is TrueStore -> TODO()
      is TrueUnOp -> TODO()
    }
  }
}
