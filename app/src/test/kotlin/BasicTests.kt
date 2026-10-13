import com.github.metametamoon.app.fullyLoadSsa
import com.github.metametamoon.app.interpreter.GgMachine
import java.io.File
import java.nio.file.Files
import org.junit.jupiter.api.Test

class BasicTests {
  private fun baseTest(source: String, funcName: String) {
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
    val helperPath = File("go/helper/go-ssa-dump")
    val ssa = fullyLoadSsa(tmpProjectDir)
    //    printSsaInfo(ssa)
    val entryFunc = ssa.funcs.single { it.goName == funcName }
    val machine = GgMachine(ssa)
    val resolvedStates = machine.runSymbolicExecutionAndResolve(entryFunc)
    assert(resolvedStates.isNotEmpty())
    for (resolvedState in resolvedStates) {
      testReproduction(source, resolvedState)
    }
  }

  @Test
  fun `first test`() {
    val testFile = File("../go/example-1-nomain/main.go")
    println(testFile.absolutePath)
    baseTest(testFile.readText(), "simpleIf")
  }
}
