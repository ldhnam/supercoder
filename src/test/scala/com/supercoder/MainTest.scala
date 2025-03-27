package com.supercoder

import munit.FunSuite
import com.supercoder.config.ArgsParser
import com.supercoder.lib.CursorRulesLoader
import java.io.{ByteArrayOutputStream, PrintStream}

class MainTest extends FunSuite {
  private val errorStream = new ByteArrayOutputStream()
  private val errorPrinter = new PrintStream(errorStream)

  override def beforeEach(context: BeforeEach): Unit = {
    errorStream.reset()
  }

  test("should parse valid arguments") {
    val args = Array("--use-cursor-rules", "true")
    val result = ArgsParser.parse(args, errorPrinter)
    assert(result.isDefined)
    assert(result.get.useCursorRules)
    assert(errorStream.toString.isEmpty)
  }

  test("should handle invalid arguments") {
    val args = Array("--invalid-arg")
    val result = ArgsParser.parse(args, errorPrinter)
    assert(result.isEmpty)
    assert(errorStream.toString.contains("Unknown option --invalid-arg"))
  }

  test("should handle missing value for use-cursor-rules") {
    val args = Array("--use-cursor-rules")
    val result = ArgsParser.parse(args, errorPrinter)
    assert(result.isEmpty)
    assert(errorStream.toString.contains("Missing value after --use-cursor-rules"))
  }
} 