package com.supercoder.tools

import com.openai.core.JsonValue
import com.openai.models.{FunctionDefinition, FunctionParameters}
import com.supercoder.base.Tool
import com.supercoder.lib.Console.green
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*

import java.util
import scala.sys.process.*

case class CodeExecutionToolArguments(command: String)

object CodeExecutionTool extends Tool {

  override val functionDefinition = FunctionDefinition
    .builder()
    .name("code-execution")
    .description(
      "Execute a shell command. The query parameter should be a regular expression."
    )
    .parameters(
      FunctionParameters
        .builder()
        .putAdditionalProperty("type", JsonValue.from("object"))
        .putAdditionalProperty(
          "properties",
          JsonValue.from(util.Map.of("command", util.Map.of("type", "string")))
        )
        .build()
    )
    .build()

  override def execute(arguments: String): String = {
    val parsedArguments = decode[CodeExecutionToolArguments](arguments)
    parsedArguments match {
      case Right(args) => {
        val command = args.command
        println(green(s"🔍 Execute shell command: ${command}"))
        try {
          val output: String = command.!!
          return output
        } catch {
          case e: Exception => s"Error: ${e.getMessage}"
        }
      }
      case _ => "Error: Invalid arguments"
    }
  }

}
