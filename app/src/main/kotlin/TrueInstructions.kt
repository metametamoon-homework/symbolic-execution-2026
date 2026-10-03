package com.github.metametamoon.app

import kotlinx.serialization.json.JsonElement

sealed interface TrueInst

sealed interface TrueValue

class Cache(
  private val rawInsts: List<Instruction>,
  private val rawValues: List<Value>,
) {
  private val createdInstsCache = mutableMapOf<Int, TrueInst>()
  private val createdValuesCache = mutableMapOf<Int, TrueValue>()

  fun instantiateInstByIndex(i: Int): TrueInst =
    createdInstsCache.getOrPut(i) { convertInst(rawInsts[i]) }

  fun instantiateValueByIndex(i: Int): TrueValue =
    createdValuesCache.getOrPut(i) { convertValue(rawValues[i]) }

  private fun convertInst(rawInst: Instruction): TrueInst =
    when (rawInst) {
      is UnOp -> TrueUnOp(rawInst.op, rawInst.arg, this)
      is BinOp -> TrueBinOp(rawInst.op, rawInst.lhs, rawInst.rhs, this)
      is Alloc -> TrueAlloc(rawInst.heap)
      is Call -> TrueCall(rawInst.value, rawInst.args, this)
      is IndexAddr -> TrueIndexAddr(rawInst.collection, rawInst.index, this)
      is MakeInterface -> TrueMakeInterface(rawInst.value, this)
      is Phi -> TruePhi(rawInst.edges, this)
      is If -> TrueIf(rawInst.cond, this)
      is Jump -> TrueJump()
      is Panic -> TruePanic(rawInst.value, this)
      is Store -> TrueStore(rawInst.addr, rawInst.value, this)
      is Defer -> TrueDefer()
      is RunDefers -> TrueRunDefers()
      is Return -> TrueReturn(rawInst.results, this)
    }

  private fun convertValue(rawValue: Value): TrueValue =
    when (rawValue) {
      is InstructionValue ->
        instantiateInstByIndex(rawValue.index) as? TrueValue
          ?: error(
            "instruction ${rawValue.index} (${rawInsts[rawValue.index]}) does not produce a value"
          )
      is Parameter -> TrueParameter(rawValue.name)
      is Builtin -> TrueBuiltin(rawValue.name)
      is Const -> TrueConst(rawValue.value)
      is FunctionRef -> TrueFunctionRef()
      is Opaque -> TrueOpaque()
      is Global -> TrueGlobal(rawValue.name)
    }
}

class TrueUnOp(val op: String, private val rawArg: Int, private val cache: Cache) :
  TrueInst, TrueValue {
  val arg: TrueValue by lazy { cache.instantiateValueByIndex(rawArg) }
}

class TrueBinOp(
  val op: String,
  private val rawLhs: Int,
  private val rawRhs: Int,
  private val cache: Cache,
) : TrueInst, TrueValue {
  val lhs: TrueValue by lazy { cache.instantiateValueByIndex(rawLhs) }
  val rhs: TrueValue by lazy { cache.instantiateValueByIndex(rawRhs) }
}

class TrueAlloc(val heap: Boolean) : TrueInst, TrueValue

class TrueCall(
  private val rawCallee: Int,
  private val rawArgs: List<Int>,
  private val cache: Cache,
) : TrueInst, TrueValue {
  val callee: TrueValue by lazy { cache.instantiateValueByIndex(rawCallee) }
  val args: List<TrueValue> by lazy { rawArgs.map { cache.instantiateValueByIndex(it) } }
}

class TrueIndexAddr(
  private val rawCollection: Int,
  private val rawIndex: Int,
  private val cache: Cache,
) : TrueInst, TrueValue {
  val collection: TrueValue by lazy { cache.instantiateValueByIndex(rawCollection) }
  val index: TrueValue by lazy { cache.instantiateValueByIndex(rawIndex) }
}

class TrueMakeInterface(private val rawValue: Int, private val cache: Cache) : TrueInst, TrueValue {
  val value: TrueValue by lazy { cache.instantiateValueByIndex(rawValue) }
}

class TruePhi(private val rawPrevs: List<Int>, private val cache: Cache) : TrueInst, TrueValue {
  val sources: List<TrueValue> by lazy {
    rawPrevs.map { cache.instantiateValueByIndex(it) }
  }
}

class TrueIf(private val rawCond: Int, private val cache: Cache) : TrueInst {
  val cond: TrueValue by lazy { cache.instantiateValueByIndex(rawCond) }
}

class TrueJump : TrueInst

class TruePanic(private val rawValue: Int, private val cache: Cache) : TrueInst {
  val value: TrueValue by lazy { cache.instantiateValueByIndex(rawValue) }
}

class TrueStore(private val rawAddr: Int, private val rawValue: Int, private val cache: Cache) :
  TrueInst {
  val addr: TrueValue by lazy { cache.instantiateValueByIndex(rawAddr) }
  val value: TrueValue by lazy { cache.instantiateValueByIndex(rawValue) }
}

class TrueDefer : TrueInst

class TrueRunDefers : TrueInst

class TrueReturn(private val rawResults: List<Int>, private val cache: Cache) : TrueInst {
  val results: List<TrueValue> by lazy { rawResults.map { cache.instantiateValueByIndex(it) } }
}

class TrueParameter(val name: String) : TrueValue

class TrueBuiltin(val name: String) : TrueValue

class TrueConst(val value: JsonElement) : TrueValue

class TrueFunctionRef : TrueValue

class TrueOpaque : TrueValue

class TrueGlobal(val name: String) : TrueValue
