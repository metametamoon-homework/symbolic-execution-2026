package com.github.metametamoon.app

import java.io.File
import java.util.concurrent.CompletableFuture

data class ProcessResult(val exitCode: Int, val stdout: String, val stderr: String)

fun runProcess(vararg command: String, workingDir: File? = null): ProcessResult {
  val process = ProcessBuilder(*command).directory(workingDir).start()
  val stderrReading = CompletableFuture.supplyAsync {
    process.errorStream.bufferedReader().readText()
  }
  val stdout = process.inputStream.bufferedReader().readText()
  val stderr = stderrReading.join()
  return ProcessResult(exitCode = process.waitFor(), stdout = stdout, stderr = stderr)
}
