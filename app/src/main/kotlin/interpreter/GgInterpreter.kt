package com.github.metametamoon.app.interpreter

import com.github.metametamoon.app.TrueAlloc
import com.github.metametamoon.app.TrueBinOp
import com.github.metametamoon.app.TrueBlock
import com.github.metametamoon.app.TrueBuiltin
import com.github.metametamoon.app.TrueCall
import com.github.metametamoon.app.TrueConst
import com.github.metametamoon.app.TrueDefer
import com.github.metametamoon.app.TrueFunctionRef
import com.github.metametamoon.app.TrueGlobal
import com.github.metametamoon.app.TrueIf
import com.github.metametamoon.app.TrueIndexAddr
import com.github.metametamoon.app.TrueJump
import com.github.metametamoon.app.TrueMakeInterface
import com.github.metametamoon.app.TrueOpaque
import com.github.metametamoon.app.TruePanic
import com.github.metametamoon.app.TrueParameter
import com.github.metametamoon.app.TruePhi
import com.github.metametamoon.app.TrueReturn
import com.github.metametamoon.app.TrueRunDefers
import com.github.metametamoon.app.TrueStore
import com.github.metametamoon.app.TrueUnOp
import com.github.metametamoon.app.TrueValue
import com.github.metametamoon.app.TypedValue
import com.github.metametamoon.app.expr.GgBoolSort
import com.github.metametamoon.app.expr.GgExpr
import com.github.metametamoon.app.expr.GgIntSort
import com.github.metametamoon.app.expr.uncheckedExprCast

fun InstructionPointer.block(): TrueBlock = function.blocks.single { instruction in it.instrs }

class GgInterpreter {

  fun evalValue(state: GgState, value: TrueValue): GgExpr<*> {
    return when (value) {
      is TypedValue ->
        when (value) {
          is TrueAlloc -> TODO()
          is TrueBinOp -> {
            val leftValue = value.lhs
            val rightValue = value.rhs

            val leftEvaluated = evalValue(state, leftValue).uncheckedExprCast<GgIntSort, _>()
            val rightEvaluated = evalValue(state, rightValue).uncheckedExprCast<GgIntSort, _>()
            when (value.op) {
              ">" -> state.ctx.mkGt(leftEvaluated, rightEvaluated)
              "*" -> state.ctx.mkMul(leftEvaluated, rightEvaluated)
              else -> TODO("Unsupported")
            }
          }

          is TrueBuiltin -> TODO()
          is TrueCall -> TODO()
          is TrueConst -> {
            state.ctx.mkIntValue(value.value.toString().toInt())
          }

          is TrueFunctionRef -> TODO()
          is TrueGlobal -> TODO()
          is TrueIndexAddr -> TODO()
          is TrueMakeInterface -> TODO()
          is TrueOpaque -> TODO()
          is TrueParameter -> {
            state.paramMap[value] ?: error("no param ???")
          }

          is TruePhi -> TODO()
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

  fun fork(state: GgState, condition: GgExpr<GgBoolSort>): ForkResult {
    val falseState = state.clone()
    state.pathConstraints.addCs(condition)
    falseState.pathConstraints.addCs(state.ctx.mkNot(condition))
    return ForkResult(state, falseState)
  }

  fun step(state: GgState): List<GgState> {
    val stmt = state.ip.instruction
    when (stmt) {
      is TrueAlloc -> TODO()
      is TrueBinOp -> {
        nextStmt(state)
        return listOf(state)
      }

      is TrueCall -> TODO()
      is TrueDefer -> TODO()
      is TrueIf -> {
        val binOpCond = evalValue(state, stmt.cond).uncheckedExprCast<GgBoolSort, _>()
        val func = state.ip.function
        val block = state.ip.block()
        val trueBlock = func.blocks[block.succs[0]]
        val falseBlock = func.blocks[block.succs[1]]
        val forking = fork(state, binOpCond)
        forking.trueState?.let {
          it.ip = InstructionPointer(it.ip.function, trueBlock.instrs.first())
        }

        forking.falseState?.let {
          it.ip = InstructionPointer(it.ip.function, falseBlock.instrs.first())
        }
        return listOfNotNull(forking.trueState, forking.falseState)
      }

      is TrueIndexAddr -> TODO()
      is TrueJump -> TODO()
      is TrueMakeInterface -> TODO()
      is TruePanic -> TODO()
      is TruePhi -> TODO()
      is TrueReturn -> {
        val returnedValues = stmt.results.map { evalValue(state, it) }
        state.executionStatus = ExecutionStatus.Returned(returnedValues)
        return listOf(state)
      }
      is TrueRunDefers -> TODO()
      is TrueStore -> TODO()
      is TrueUnOp -> TODO()
    }
  }
}
