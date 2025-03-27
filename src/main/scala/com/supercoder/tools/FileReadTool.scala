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

case class FileReadToolArguments(fileName: String)

object FileReadTool extends Tool {

  override val functionDefinition = FunctionDefinition
    .builder()
    .name("file-read")
    .description(
      "Read a file to understand its content. Use this tool to read a file and understand its content."
    )
    .parameters(
      FunctionParameters
        .builder()
        .putAdditionalProperty("type", JsonValue.from("object"))
        .putAdditionalProperty(
          "properties",
          JsonValue.from(util.Map.of("fileName", util.Map.of("type", "string")))
        )
        .build()
    )
    .build()

  override def execute(arguments: String): String = {
    val parsedArguments = decode[FileReadToolArguments](arguments)
    parsedArguments match {
      case Right(args) => {
        val fileName = args.fileName
        println(green(s"📂 Reading file: ${fileName}"))
        val command = s"""cat ${fileName}"""
        val output: String = command.!!
        return output
      }
      case _ => "Error: Invalid arguments"
    }
  }

}
