package com.supercoder.config

import scopt.OParser
import java.io.PrintStream

case class Config(useCursorRules: Boolean = false)

object ArgsParser {
  def parse(args: Array[String], errorStream: PrintStream = System.err): Option[Config] = {
    Console.withErr(errorStream) {
      val builder = OParser.builder[Config]
      val parser = {
        import builder._
        OParser.sequence(
          programName("SuperCoder"),
          opt[String]('c', "use-cursor-rules")
            .action((x, c) => c.copy(useCursorRules = (x == "true")))
            .text("use Cursor rules for the agent"),
          help("help").text("prints this usage text")
        )
      }
      OParser.parse(parser, args, Config())
    }
  }
}
