package com.supercoder.lib

import munit.FunSuite
import java.nio.file.{Files, Path, Paths}
import java.nio.charset.StandardCharsets
import scala.jdk.CollectionConverters._

class CursorRulesLoaderTest extends FunSuite {
  private val testProjectRoot = Files.createTempDirectory("cursor_test")
  private val rulesDir = testProjectRoot.resolve(Paths.get(".cursor", "rules"))
  private val cursorRulesFile = testProjectRoot.resolve(".cursorrules")

  override def beforeEach(context: BeforeEach): Unit = {
    // Create test directories
    Files.createDirectories(rulesDir)
    
    // Clean up any existing files
    if (Files.exists(cursorRulesFile)) {
      Files.delete(cursorRulesFile)
    }
    if (Files.exists(rulesDir)) {
      Files.list(rulesDir).iterator().asScala.foreach(Files.delete(_))
    }
  }

  override def afterAll(): Unit = {
    // Clean up test directory
    def deleteRecursively(path: Path): Unit = {
      if (Files.isDirectory(path)) {
        Files.list(path).iterator().asScala.foreach(deleteRecursively)
      }
      Files.delete(path)
    }

    deleteRecursively(testProjectRoot)
  }

  test("should load rules when both .mdc files and .cursorrules exist") {
    // Create test files
    val mdcFile1 = rulesDir.resolve("test1.mdc")
    val mdcFile2 = rulesDir.resolve("test2.mdc")
    Files.write(mdcFile1, "rule1".getBytes(StandardCharsets.UTF_8))
    Files.write(mdcFile2, "rule2".getBytes(StandardCharsets.UTF_8))
    Files.write(cursorRulesFile, "main rules".getBytes(StandardCharsets.UTF_8))

    // Override system property for testing
    val originalDir = System.getProperty("user.dir")
    System.setProperty("user.dir", testProjectRoot.toString)

    try {
      val rules = CursorRulesLoader.loadRules()
      assert(rules.contains("rule1"))
      assert(rules.contains("rule2"))
      assert(rules.contains("main rules"))
      assert(rules.split("\n").length == 3)
    } finally {
      System.setProperty("user.dir", originalDir)
    }
  }

  test("should load rules when only .mdc files exist") {
    val mdcFile = rulesDir.resolve("test.mdc")
    Files.write(mdcFile, "mdc rule".getBytes(StandardCharsets.UTF_8))

    val originalDir = System.getProperty("user.dir")
    System.setProperty("user.dir", testProjectRoot.toString)

    try {
      val rules = CursorRulesLoader.loadRules()
      assert(rules == "mdc rule")
    } finally {
      System.setProperty("user.dir", originalDir)
    }
  }

  test("should load rules when only .cursorrules exists") {
    Files.write(cursorRulesFile, "main rules".getBytes(StandardCharsets.UTF_8))

    val originalDir = System.getProperty("user.dir")
    System.setProperty("user.dir", testProjectRoot.toString)

    try {
      val rules = CursorRulesLoader.loadRules()
      assert(rules == "main rules")
    } finally {
      System.setProperty("user.dir", originalDir)
    }
  }

  test("should return empty string when no rules files exist") {
    val originalDir = System.getProperty("user.dir")
    System.setProperty("user.dir", testProjectRoot.toString)

    try {
      val rules = CursorRulesLoader.loadRules()
      assert(rules.isEmpty)
    } finally {
      System.setProperty("user.dir", originalDir)
    }
  }

  test("should properly concatenate multiple .mdc files") {
    val mdcFile1 = rulesDir.resolve("test1.mdc")
    val mdcFile2 = rulesDir.resolve("test2.mdc")
    val mdcFile3 = rulesDir.resolve("test3.mdc")
    Files.write(mdcFile1, "rule1".getBytes(StandardCharsets.UTF_8))
    Files.write(mdcFile2, "rule2".getBytes(StandardCharsets.UTF_8))
    Files.write(mdcFile3, "rule3".getBytes(StandardCharsets.UTF_8))

    val originalDir = System.getProperty("user.dir")
    System.setProperty("user.dir", testProjectRoot.toString)

    try {
      val rules = CursorRulesLoader.loadRules()
      assert(rules.contains("rule1"))
      assert(rules.contains("rule2"))
      assert(rules.contains("rule3"))
      assert(rules.split("\n").length == 3)
    } finally {
      System.setProperty("user.dir", originalDir)
    }
  }

  test("should handle non-existent rules directory") {
    Files.delete(rulesDir)

    val originalDir = System.getProperty("user.dir")
    System.setProperty("user.dir", testProjectRoot.toString)

    try {
      val rules = CursorRulesLoader.loadRules()
      assert(rules.isEmpty)
    } finally {
      System.setProperty("user.dir", originalDir)
    }
  }
} 