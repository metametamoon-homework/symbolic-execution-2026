package com.github.metametamoon.app

import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Block(
  val index: Int,
  val succs: List<Int> = emptyList(),
  val instrs: List<Int> = emptyList(),
  val prevs: List<Int> = emptyList(),
)

@Serializable data class Func(val name: String, val blocks: List<Block> = emptyList())

@Serializable
data class Program(
  val types: List<Type> = emptyList(),
  val values: List<Value> = emptyList(),
  val instrs: List<Instruction> = emptyList(),
  val funcs: List<Func> = emptyList(),
)

@Serializable
data class TrueBlock(
  val index: Int,
  val succs: List<Int> = emptyList(),
  val instrs: List<TrueInst> = emptyList(),
  val prefs: List<Int> = emptyList(),
)

@Serializable data class TrueFunc(val name: String, val blocks: List<TrueBlock> = emptyList())

@Serializable data class TrueProgram(val funcs: List<TrueFunc> = emptyList())

private val json = Json {
  ignoreUnknownKeys = true
  coerceInputValues = true
  classDiscriminator = "kind"
}

fun loadSsa(helper: File, goProjectDir: File): Program {
  val p =
    ProcessBuilder(helper.path, goProjectDir.path)
      .redirectError(ProcessBuilder.Redirect.INHERIT)
      .start()
  val text = p.inputStream.bufferedReader().readText()
  val errors = p.errorStream.bufferedReader().readText()
  println("errs:$errors")
  check(p.waitFor() == 0) { "go-ssa-dump failed" }
  return json.decodeFromString(text)
}

fun Func.translate(cache: Cache): TrueFunc =
  TrueFunc(name = name, blocks = blocks.map { it.translate(cache) })

fun Block.translate(cache: Cache): TrueBlock =
  TrueBlock(index, succs, instrs.map { cache.instantiateInstByIndex(it) })

fun Program.translate(cache: Cache): TrueProgram =
  TrueProgram(funcs = funcs.map { it.translate(cache) })

fun main() {
  val result =
    loadSsa(
      File(
        "/home/metametamoon/workspace/learning/symbolic-execution/se-repo/go/helper/go-ssa-dump"
      ),
      File("/home/metametamoon/workspace/learning/symbolic-execution/se-repo/go/example-1"),
    )
  val cacheA = Cache(rawInsts = result.instrs, rawValues = result.values, rawTypes = result.types)

  val convertedResult = result.translate(cacheA)
  for (func in convertedResult.funcs) {
    println("${func.name}:")
    println("${func.blocks.size} blocks")
    val lists =
      func.blocks.mapIndexed { index, block -> "$index -> ${block.succs}" }.joinToString("\n\t")
    println("Block edges:")
    println("\t" + lists)
    println()
  }
}
