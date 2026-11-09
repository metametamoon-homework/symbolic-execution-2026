import com.github.metametamoon.app.fullyLoadSsa
import com.github.metametamoon.app.interpreter.GgMachine
import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

class BasicTests {
  private val structsSource = File("../go/structs-and-arrays/main.go").readText()

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

  @Test
  fun `testStructBasic interpretation`() {
    baseTest(structsSource, "testStructBasic")
  }

  @Test
  fun `testStructModification interpretation`() {
    baseTest(structsSource, "testStructModification")
  }

  @Test
  fun `testStructPointer interpretation`() {
    baseTest(structsSource, "testStructPointer")
  }

  @Test
  fun `testStructPointerModification interpretation`() {
    baseTest(structsSource, "testStructPointerModification")
  }

  @Test
  fun `testNestedStructPointer interpretation`() {
    baseTest(structsSource, "testNestedStructPointer")
  }

  @Test
  fun `testArrayFixed interpretation`() {
    baseTest(structsSource, "testArrayFixed")
  }

  @Test
  fun `testArrayModification interpretation`() {
    baseTest(structsSource, "testArrayModification")
  }

  @Test
  fun `testSliceCreation interpretation`() {
    baseTest(structsSource, "testSliceCreation")
  }

  @Test
  fun `testSliceAppend interpretation`() {
    baseTest(structsSource, "testSliceAppend")
  }

  @Test
  fun `testSliceModification interpretation`() {
    baseTest(structsSource, "testSliceModification")
  }

  @Test
  fun `testStructWithArray interpretation`() {
    baseTest(structsSource, "testStructWithArray")
  }

  @Test
  fun `testNestedStructs interpretation`() {
    baseTest(structsSource, "testNestedStructs")
  }

  @Test
  fun `testNestedStructModification interpretation`() {
    baseTest(structsSource, "testNestedStructModification")
  }

  @Test
  fun `testArrayOfStructs interpretation`() {
    baseTest(structsSource, "testArrayOfStructs")
  }

  @Test
  fun `testArrayOfStructsModification interpretation`() {
    baseTest(structsSource, "testArrayOfStructsModification")
  }

  @Test
  fun `Aliasing interpretation`() {
    baseTest(structsSource, "Aliasing")
  }

  @Test
  fun `testPathConstraintMutability interpretation`() {
    baseTest(structsSource, "testPathConstraintMutability")
  }
}
