package com.github.metametamoon.app

import kotlinx.serialization.json.JsonElement

sealed interface TrueInst

sealed interface TrueValue {
  val type: TrueType
}

sealed interface TrueType

class Cache(
  private val rawInsts: List<Instruction>,
  private val rawValues: List<Value>,
  private val rawTypes: List<Type>,
) {
  private val createdInstsCache = mutableMapOf<Int, TrueInst>()
  private val createdValuesCache = mutableMapOf<Int, TrueValue>()
  private val createdTypesCache = mutableMapOf<Int, TrueType>()

  fun instantiateInstByIndex(i: Int): TrueInst =
    createdInstsCache.getOrPut(i) { convertInst(rawInsts[i]) }

  fun instantiateValueByIndex(i: Int): TrueValue =
    createdValuesCache.getOrPut(i) { convertValue(rawValues[i]) }

  fun instantiateTypeByIndex(i: Int): TrueType =
    createdTypesCache.getOrPut(i) { convertType(rawTypes[i]) }

  private fun convertInst(rawInst: Instruction): TrueInst =
    when (rawInst) {
      is UnOp -> TrueUnOp(rawInst.type, rawInst.op, rawInst.arg, this)
      is BinOp -> TrueBinOp(rawInst.type, rawInst.op, rawInst.lhs, rawInst.rhs, this)
      is Alloc -> TrueAlloc(rawInst.type, rawInst.heap, this)
      is Call -> TrueCall(rawInst.type, rawInst.value, rawInst.args, this)
      is IndexAddr -> TrueIndexAddr(rawInst.type, rawInst.collection, rawInst.index, this)
      is MakeInterface -> TrueMakeInterface(rawInst.type, rawInst.value, this)
      is Phi -> TruePhi(rawInst.type, rawInst.edges, this)
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
      is Parameter -> TrueParameter(rawValue.type, rawValue.name, this)
      is Builtin -> TrueBuiltin(rawValue.type, rawValue.name, this)
      is Const -> TrueConst(rawValue.type, rawValue.value, this)
      is FunctionRef -> TrueFunctionRef(rawValue.type, this)
      is Opaque -> TrueOpaque(rawValue.type, this)
      is Global -> TrueGlobal(rawValue.type, rawValue.name, this)
    }

  private fun convertType(rawType: Type): TrueType =
    when (rawType) {
      is BasicType -> TrueBasicType(rawType.name)
      is PointerType -> TruePointerType(rawType.elem, this)
      is SliceType -> TrueSliceType(rawType.elem, this)
      is InterfaceType -> TrueInterfaceType()
      is SignatureType -> TrueSignatureType(rawType.params, rawType.results, this)
    }
}

/** The instructions that have `register` prefix in go's type struct definitions */
abstract class TypedValue(private val rawType: Int, private val cache: Cache) : TrueValue {
  override val type: TrueType by lazy { cache.instantiateTypeByIndex(rawType) }
}

class TrueUnOp(rawType: Int, val op: String, private val rawArg: Int, private val cache: Cache) :
  TypedValue(rawType, cache), TrueInst {
  val arg: TrueValue by lazy { cache.instantiateValueByIndex(rawArg) }
}

class TrueBinOp(
  rawType: Int,
  val op: String,
  private val rawLhs: Int,
  private val rawRhs: Int,
  private val cache: Cache,
) : TypedValue(rawType, cache), TrueInst {
  val lhs: TrueValue by lazy { cache.instantiateValueByIndex(rawLhs) }
  val rhs: TrueValue by lazy { cache.instantiateValueByIndex(rawRhs) }
}

class TrueAlloc(rawType: Int, val heap: Boolean, cache: Cache) :
  TypedValue(rawType, cache), TrueInst

class TrueCall(
  rawType: Int,
  private val rawCallee: Int,
  private val rawArgs: List<Int>,
  private val cache: Cache,
) : TypedValue(rawType, cache), TrueInst {
  val callee: TrueValue by lazy { cache.instantiateValueByIndex(rawCallee) }
  val args: List<TrueValue> by lazy { rawArgs.map { cache.instantiateValueByIndex(it) } }
}

class TrueIndexAddr(
  rawType: Int,
  private val rawCollection: Int,
  private val rawIndex: Int,
  private val cache: Cache,
) : TypedValue(rawType, cache), TrueInst {
  val collection: TrueValue by lazy { cache.instantiateValueByIndex(rawCollection) }
  val index: TrueValue by lazy { cache.instantiateValueByIndex(rawIndex) }
}

class TrueMakeInterface(rawType: Int, private val rawValue: Int, private val cache: Cache) :
  TypedValue(rawType, cache), TrueInst {
  val value: TrueValue by lazy { cache.instantiateValueByIndex(rawValue) }
}

class TruePhi(rawType: Int, private val rawPrevs: List<Int>, private val cache: Cache) :
  TypedValue(rawType, cache), TrueInst {
  val sources: List<TrueValue> by lazy { rawPrevs.map { cache.instantiateValueByIndex(it) } }
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

class TrueParameter(rawType: Int, val name: String, cache: Cache) : TypedValue(rawType, cache)

class TrueBuiltin(rawType: Int, val name: String, cache: Cache) : TypedValue(rawType, cache)

class TrueConst(rawType: Int, val value: JsonElement, cache: Cache) : TypedValue(rawType, cache)

class TrueFunctionRef(rawType: Int, cache: Cache) : TypedValue(rawType, cache)

class TrueOpaque(rawType: Int, cache: Cache) : TypedValue(rawType, cache)

class TrueGlobal(rawType: Int, val name: String, cache: Cache) : TypedValue(rawType, cache)

class TrueBasicType(val name: String) : TrueType

class TruePointerType(private val rawElem: Int, private val cache: Cache) : TrueType {
  val elem: TrueType by lazy { cache.instantiateTypeByIndex(rawElem) }
}

class TrueSliceType(private val rawElem: Int, private val cache: Cache) : TrueType {
  val elem: TrueType by lazy { cache.instantiateTypeByIndex(rawElem) }
}

class TrueInterfaceType : TrueType

class TrueSignatureType(
  private val rawParams: List<Int>,
  private val rawResults: List<Int>,
  private val cache: Cache,
) : TrueType {
  val params: List<TrueType> by lazy { rawParams.map { cache.instantiateTypeByIndex(it) } }
  val results: List<TrueType> by lazy { rawResults.map { cache.instantiateTypeByIndex(it) } }
}
