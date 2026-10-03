package com.github.metametamoon.app

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable sealed interface Instruction

@Serializable sealed interface Value

@Serializable @SerialName("unop") data class UnOp(val op: String, val arg: Int) : Instruction

@Serializable
@SerialName("binop")
data class BinOp(val op: String, val lhs: Int, val rhs: Int) : Instruction

@Serializable @SerialName("alloc") data class Alloc(val heap: Boolean) : Instruction

@Serializable @SerialName("call") data class Call(val value: Int, val args: List<Int>) : Instruction

@Serializable
@SerialName("indexaddr")
data class IndexAddr(val collection: Int, val index: Int) : Instruction

@Serializable @SerialName("makeinterface") data class MakeInterface(val value: Int) : Instruction

@Serializable @SerialName("phi") data class Phi(val edges: List<Int>) : Instruction

@Serializable @SerialName("if") data class If(val cond: Int) : Instruction

@Serializable @SerialName("jump") data object Jump : Instruction

@Serializable @SerialName("panic") data class Panic(val value: Int) : Instruction

@Serializable
@SerialName("store")
data class Store(val addr: Int, @SerialName("val") val value: Int) : Instruction

@Serializable @SerialName("defer") data object Defer : Instruction

@Serializable @SerialName("rundefers") data object RunDefers : Instruction

@Serializable @SerialName("return") data class Return(val results: List<Int>) : Instruction

@Serializable @SerialName("instr") data class InstructionValue(val index: Int) : Value

@Serializable @SerialName("parameter") data class Parameter(val name: String) : Value

@Serializable @SerialName("builtin") data class Builtin(val name: String) : Value

@Serializable
@SerialName("const")
data class Const(@SerialName("constvalue") val value: JsonElement) : Value

@Serializable @SerialName("function") data object FunctionRef : Value

@Serializable @SerialName("opaque") data object Opaque : Value

@Serializable
@SerialName("global")
data class Global(
  @SerialName("name") val name: String
  //  @SerialName("type"),
  //  val type:
) : Value
