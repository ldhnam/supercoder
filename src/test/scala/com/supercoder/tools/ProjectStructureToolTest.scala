package com.supercoder.tools

import munit.FunSuite
import io.circe.syntax._
import io.circe.generic.auto._
import java.io.{ByteArrayOutputStream, PrintStream}
import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters._
import scala.sys.process._

class ProjectStructureToolTest extends FunSuite {
  private val testProjectRoot = Files.createTempDirectory("project_structure_test")
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
    assert(ProjectStructureTool.functionDefinition.name == "project-structure")
    assert(ProjectStructureTool.functionDefinition.description.get.contains("Get the structure"))
  }

  test("should handle empty arguments") {
    val result = ProjectStructureTool.execute("{}")
    assert(result.contains("Error getting project structure"))
  }

  test("should handle invalid JSON arguments") {
    val result = ProjectStructureTool.execute("invalid json")
    assert(result.contains("Error getting project structure"))
  }

  test("should handle non-existent directory") {
    val originalDir = System.getProperty("user.dir")
    System.setProperty("user.dir", "/non/existent/dir")

    try {
      val result = ProjectStructureTool.execute("{}")
      assert(result.contains("Error getting project structure"))
    } finally {
      System.setProperty("user.dir", originalDir)
    }
  }

  test("should print correct message") {
    ProjectStructureTool.execute("{}")
    assert(outputStream.toString.contains("🔎 Reading project structure"))
  }
} 