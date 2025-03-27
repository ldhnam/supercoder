package com.supercoder.tools

import munit.FunSuite
import io.circe.syntax._
import io.circe.generic.auto._
import java.io.{ByteArrayOutputStream, PrintStream}
import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters._
import scala.sys.process._

class FileReadToolTest extends FunSuite {
  private val testProjectRoot = Files.createTempDirectory("file_read_test")
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
    assert(FileReadTool.functionDefinition.name == "file-read")
    assert(FileReadTool.functionDefinition.description.get.contains("Read a file"))
  }

  test("should read existing file") {
    val testFile = testProjectRoot.resolve("test.txt")
    val content = "Hello, World!"
    Files.write(testFile, content.getBytes)

    val args = FileReadToolArguments(testFile.toString).asJson.noSpaces
    val result = FileReadTool.execute(args)

    assert(result.trim == content)
    assert(outputStream.toString.contains("📂 Reading file"))
  }

  test("should handle non-existent file") {
    val args = FileReadToolArguments("/non/existent/file.txt").asJson.noSpaces
    val result = FileReadTool.execute(args)
    assert(result.contains("Error"))
  }

  test("should handle invalid JSON arguments") {
    val result = FileReadTool.execute("invalid json")
    assert(result == "Error: Invalid arguments")
  }

  test("should handle empty filename") {
    val args = FileReadToolArguments("").asJson.noSpaces
    val result = FileReadTool.execute(args)
    assert(result.contains("Error"))
  }

  test("should handle file with special characters") {
    val testFile = testProjectRoot.resolve("test file with spaces.txt")
    val content = "Special characters: !@#$%^&*()"
    Files.write(testFile, content.getBytes)

    val args = FileReadToolArguments(testFile.toString).asJson.noSpaces
    val result = FileReadTool.execute(args)

    assert(result.trim == content)
  }

  test("should handle file with newlines") {
    val testFile = testProjectRoot.resolve("multiline.txt")
    val content = "Line 1\nLine 2\nLine 3"
    Files.write(testFile, content.getBytes)

    val args = FileReadToolArguments(testFile.toString).asJson.noSpaces
    val result = FileReadTool.execute(args)

    assert(result.trim == content)
  }
} 