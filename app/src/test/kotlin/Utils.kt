import com.github.metametamoon.app.interpreter.ExecutionResult
import com.github.metametamoon.app.interpreter.GgResolvedState
import com.github.metametamoon.app.runProcess
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.test.assertEquals

fun testReproduction(goSource: String, resolvedState: GgResolvedState) {
  val freshDir = Files.createTempDirectory("reproduction")
  try {
    val sourceFile = freshDir.resolve("source.go")
    sourceFile.toFile().writeText(goSource)
    freshDir
      .resolve("go.mod")
      .toFile()
      .writeText(
        """
        module mymodule

        go 1.22.2
        """
          .trimIndent()
      )
    val mainFile = freshDir.resolve("main.go")
    val paramString = resolvedState.initialParameters.joinToString(separator = ", ")
    val mainContent =
      """
          package main
          import "fmt"

          func main() {
            result := ${resolvedState.entryFunction.goName}($paramString)
            fmt.Printf("%d", result)
          }
        """
        .trimIndent()
    mainFile.writeText(mainContent)

    val expectedOutput =
      when (val executionResult = resolvedState.executionResult) {
        is ExecutionResult.Returns -> executionResult.value.toString()
      }
    assertEquals(expectedOutput, runGoProgram(freshDir))
  } finally {
    freshDir.toFile().deleteRecursively()
  }
}

private fun runGoProgram(programDir: Path): String {
  val result = runProcess("go", "run", ".", workingDir = programDir.toFile())
  println("stderr: ${result.stderr}")
  assertEquals(
    0,
    result.exitCode,
    "go run exited with code ${result.exitCode}, stderr:\n${result.stderr}",
  )
  return result.stdout
}
