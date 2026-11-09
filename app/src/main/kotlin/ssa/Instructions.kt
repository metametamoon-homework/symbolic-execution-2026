package com.github.metametamoon.app

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable sealed interface Instruction

@Serializable sealed interface Value

@Serializable sealed interface Type

@Serializable @SerialName("basic") data class BasicType(val name: String) : Type

@Serializable @SerialName("pointer") data class PointerType(val elem: Int) : Type

@Serializable @SerialName("slice") data class SliceType(val elem: Int) : Type

@Serializable @SerialName("array") data class ArrayType(val len: Int, val elem: Int) : Type

@Serializable @SerialName("struct") data class StructType(val fields: List<Int>) : Type

@Serializable @SerialName("interface") data object InterfaceType : Type

@Serializable
@SerialName("signature")
data class SignatureType(val params: List<Int>, val results: List<Int>) : Type

@Serializable @SerialName("tuple") data class TupleType(val values: List<Int>) : Type

@Serializable
@SerialName("unop")
data class UnOp(val type: Int, val op: String, val arg: Int) : Instruction

@Serializable
@SerialName("binop")
data class BinOp(val type: Int, val op: String, val lhs: Int, val rhs: Int) : Instruction

@Serializable @SerialName("convert") data class Convert(val type: Int, val x: Int) : Instruction

@Serializable @SerialName("alloc") data class Alloc(val type: Int, val heap: Boolean) : Instruction

@Serializable
@SerialName("call")
data class Call(val type: Int, val value: Int, val args: List<Int>) : Instruction

@Serializable
@SerialName("index")
data class Index(val type: Int, val collection: Int, val index: Int) : Instruction

@Serializable
@SerialName("indexaddr")
data class IndexAddr(val type: Int, val collection: Int, val index: Int) : Instruction

@Serializable
@SerialName("fieldaddr")
data class FieldAddr(val type: Int, @SerialName("struct") val struct: Int, val field: Int) :
  Instruction

@Serializable
@SerialName("makeinterface")
data class MakeInterface(val type: Int, val value: Int) : Instruction

@Serializable @SerialName("phi") data class Phi(val type: Int, val edges: List<Int>) : Instruction

@Serializable @SerialName("if") data class If(val cond: Int) : Instruction

@Serializable @SerialName("jump") data object Jump : Instruction

@Serializable
@SerialName("slice")
data class Slice(
  val type: Int,
  val x: Int,
  val low: Int? = null,
  val high: Int? = null,
  val max: Int? = null,
) : Instruction

@Serializable @SerialName("panic") data class Panic(val value: Int) : Instruction

@Serializable
@SerialName("store")
data class Store(val addr: Int, @SerialName("val") val value: Int) : Instruction

@Serializable @SerialName("defer") data object Defer : Instruction

@Serializable @SerialName("rundefers") data object RunDefers : Instruction

@Serializable @SerialName("return") data class Return(val results: List<Int>) : Instruction

@Serializable @SerialName("instr") data class InstructionValue(val index: Int) : Value

@Serializable @SerialName("parameter") data class Parameter(val type: Int, val name: String) : Value

@Serializable @SerialName("builtin") data class Builtin(val type: Int, val name: String) : Value

@Serializable
@SerialName("const")
data class Const(val type: Int, @SerialName("constvalue") val value: JsonElement) : Value

@Serializable @SerialName("function") data class FunctionRef(val type: Int) : Value

@Serializable @SerialName("opaque") data class Opaque(val type: Int) : Value

@Serializable @SerialName("global") data class Global(val type: Int, val name: String) : Value
