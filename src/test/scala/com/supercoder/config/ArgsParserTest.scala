package com.supercoder.config

import munit.FunSuite
import java.io.{ByteArrayOutputStream, PrintStream}

class ArgsParserTest extends FunSuite {
  private val errorStream = new ByteArrayOutputStream()
  private val errorPrinter = new PrintStream(errorStream)

  override def beforeEach(context: BeforeEach): Unit = {
    errorStream.reset()
  }

  test("should parse use-cursor-rules flag correctly") {
    val args = Array("--use-cursor-rules", "true")
    val result = ArgsParser.parse(args, errorPrinter)
    assert(result.isDefined)
    assert(result.get.useCursorRules)
    assert(errorStream.toString.isEmpty)
  }

  test("should handle use-cursor-rules set to false") {
    val args = Array("--use-cursor-rules", "false")
    val result = ArgsParser.parse(args, errorPrinter)
    assert(result.isDefined)
    assert(!result.get.useCursorRules)
    assert(errorStream.toString.isEmpty)
  }

  test("should handle short form of use-cursor-rules flag") {
    val args = Array("-c", "true")
    val result = ArgsParser.parse(args, errorPrinter)
    assert(result.isDefined)
    assert(result.get.useCursorRules)
    assert(errorStream.toString.isEmpty)
  }

  test("should handle help flag") {
    val args = Array("--help")
    val result = ArgsParser.parse(args, errorPrinter)
    assert(result.isDefined)
    assert(!result.get.useCursorRules)
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