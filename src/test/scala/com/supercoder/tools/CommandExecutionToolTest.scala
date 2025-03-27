package com.supercoder.tools

import munit.FunSuite
import io.circe.syntax._
import io.circe.generic.auto._
import java.io.{ByteArrayOutputStream, PrintStream}
import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters._
import scala.sys.process._

class CommandExecutionToolTest extends FunSuite {
  private val testProjectRoot = Files.createTempDirectory("command_execution_test")
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
    assert(CommandExecutionTool.functionDefinition.name == "command-execution")
    assert(CommandExecutionTool.functionDefinition.description.get.contains("Execute a shell command"))
  }

  test("should execute valid command") {
    val args = CommandExecutionToolArguments("echo 'Hello, World!'").asJson.noSpaces
    val result = CommandExecutionTool.execute(args)
    assert(result.trim == "Hello, World!")
    assert(outputStream.toString.contains("🔍 Execute shell command"))
  }

  test("should handle invalid command") {
    val args = CommandExecutionToolArguments("nonexistent_command").asJson.noSpaces
    val result = CommandExecutionTool.execute(args)
    assert(result.contains("Error"))
  }

  test("should handle invalid JSON arguments") {
    val result = CommandExecutionTool.execute("invalid json")
    assert(result == "Error: Invalid arguments")
  }

  test("should handle empty command") {
    val args = CommandExecutionToolArguments("").asJson.noSpaces
    val result = CommandExecutionTool.execute(args)
    assert(result.contains("Error"))
  }

  test("should handle command with special characters") {
    val args = CommandExecutionToolArguments("echo 'Hello, World! @#$%^&*'").asJson.noSpaces
    val result = CommandExecutionTool.execute(args)
    assert(result.trim == "Hello, World! @#$%^&*")
  }

  test("should handle command with multiple lines") {
    val args = CommandExecutionToolArguments("echo -e 'Line 1\\nLine 2\\nLine 3'").asJson.noSpaces
    val result = CommandExecutionTool.execute(args)
    assert(result.trim == "Line 1\nLine 2\nLine 3")
  }

  test("should handle command with spaces") {
    val args = CommandExecutionToolArguments("echo 'Hello World'").asJson.noSpaces
    val result = CommandExecutionTool.execute(args)
    assert(result.trim == "Hello World")
  }
} 