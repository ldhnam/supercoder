package com.supercoder.tools

import com.openai.models.{FunctionDefinition, FunctionParameters}
import com.supercoder.base.Tool
import com.supercoder.lib.Console.green

import java.util
import scala.sys.process.*
import com.openai.core.JsonValue

object ProjectStructureTool extends Tool {

  override val functionDefinition = FunctionDefinition
    .builder()
    .name("project-structure")
    .description("Get the structure of the current project")
    .parameters(
      FunctionParameters.builder()
        .putAdditionalProperty("type", JsonValue.from("object"))
        .putAdditionalProperty("properties", JsonValue.from(util.Map.of()))
      .build()
    )
    .build()

  override def execute(arguments: String): String = {
    println(green("🔎 Reading project structure..."))
    try {
      val command = "tree --gitignore"
      command.!!
    } catch {
      case e: Exception => s"Error getting project structure: ${e.getMessage}"
    }
  }

}
