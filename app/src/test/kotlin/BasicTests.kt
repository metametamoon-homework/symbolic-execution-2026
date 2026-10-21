import com.github.metametamoon.app.fullyLoadSsa
import com.github.metametamoon.app.interpreter.GgMachine
import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

class BasicTests {
  private fun baseTest(source: String, funcName: String, expectedNumberOfStates: Int? = null) {
    val tmpProjectDir = Files.createTempDirectory("tmp").toFile()
    tmpProjectDir.resolve("src.go").writeText(source)
    tmpProjectDir
      .resolve("go.mod")
      .writeText(
        """
        module mymodule

        go 1.22.2
        """
          .trimIndent()
      )
    val ssa = fullyLoadSsa(tmpProjectDir)
    val entryFunc = ssa.funcs.single { it.goName == funcName }
    val machine = GgMachine(ssa)
    val resolvedStates = machine.runSymbolicExecutionAndResolve(entryFunc)
    assert(resolvedStates.isNotEmpty())
    for (resolvedState in resolvedStates) {
      println(
        "State: ${resolvedState.entryFunction.goName}: ${resolvedState.initialParameters.joinToString(",")} -> ${resolvedState.resolvedExecutionResult}"
      )
    }
    if (expectedNumberOfStates != null) {
      assertEquals(expected = expectedNumberOfStates, actual = resolvedStates.size)
    }
    for (resolvedState in resolvedStates) {
      testReproduction(source, resolvedState)
    }
  }

  @Test
  fun `simpleIf interpretation`() {
    val testFile = File("../go/example-1-nomain/main.go")
    baseTest(testFile.readText(), "simpleIf", expectedNumberOfStates = 2)
  }

  @Test
  fun `ifElse interpretation`() {
    val testFile = File("../go/example-1-nomain/main.go")
    baseTest(testFile.readText(), "ifElse", expectedNumberOfStates = 2)
  }

  @Test
  fun `nestedIf interpretation`() {
    val testFile = File("../go/example-1-nomain/main.go")
    baseTest(testFile.readText(), "nestedIf", expectedNumberOfStates = 3)
  }

  @Test
  fun `very simple loop`() {
    val testFile = File("../go/example-1-nomain/main.go")
    baseTest(testFile.readText(), "simpleLoop", expectedNumberOfStates = 1)
  }
}
