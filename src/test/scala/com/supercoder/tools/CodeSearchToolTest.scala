package com.supercoder.tools

import munit.FunSuite
import io.circe.syntax._
import io.circe.generic.auto._
import java.io.{ByteArrayOutputStream, PrintStream}
import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters._
import scala.sys.process._

class CodeSearchToolTest extends FunSuite {
  private val testProjectRoot = Files.createTempDirectory("code_search_test")
  private val originalOut = System.out
  private val outputStream = new ByteArrayOutputStream()
  private val printStream = new PrintStream(outputStream)

  override def beforeEach(context: BeforeEach): Unit = {
    outputStream.reset()
    System.setOut(printStream)
  }

  override def afterEach(context: AfterEach): Unit = {
    System.setOut(originalOut)
  }

  override def afterAll(): Unit = {
    def deleteRecursively(path: Path): Unit = {
      if (Files.isDirectory(path)) {
        Files.list(path).iterator().asScala.foreach(deleteRecursively)
      }
      Files.delete(path)
    }
    deleteRecursively(testProjectRoot)
  }

  test("should have correct function definition") {
    assert(CodeSearchTool.functionDefinition.name == "code-search")
    assert(CodeSearchTool.functionDefinition.description.get.contains("Search for code"))
  }

  test("should handle valid search query") {
    // Create a test file with some content
    val testFile = testProjectRoot.resolve("test.scala")
    Files.write(testFile, "def testFunction() = println(\"test\")".getBytes)

    val args = CodeSearchToolArguments("testFunction").asJson.noSpaces
    val result = CodeSearchTool.execute(args)

    assert(result.contains("testFunction"))
    assert(outputStream.toString.contains("🔍 Search code for query"))
  }

  test("should handle invalid JSON arguments") {
    val result = CodeSearchTool.execute("invalid json")
    assert(result == "Error: Invalid arguments")
  }

  test("should handle empty query") {
    val args = CodeSearchToolArguments("").asJson.noSpaces
    val result = CodeSearchTool.execute(args)
    assert(result.contains("Error"))
  }

  test("should handle special characters in query") {
    val args = CodeSearchToolArguments("def\\s+\\w+").asJson.noSpaces
    val result = CodeSearchTool.execute(args)
    assert(!result.contains("Error: Invalid arguments"))
  }

  test("should handle non-existent directory") {
    val originalDir = System.getProperty("user.dir")
    System.setProperty("user.dir", "/non/existent/dir")

    try {
      val args = CodeSearchToolArguments("test").asJson.noSpaces
      val result = CodeSearchTool.execute(args)
      assert(result.contains("Error"))
    } finally {
      System.setProperty("user.dir", originalDir)
    }
  }
} 