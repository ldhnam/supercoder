package com.supercoder.tools

import com.openai.models.FunctionDefinition
import com.supercoder.base.Tool
import com.supercoder.lib.Console.green
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*

case class UrlFetchToolArguments(url: String)

object UrlFetchTool extends Tool {
  override val functionDefinition: FunctionDefinition = FunctionDefinition.builder()
    .name("url-fetch")
    .description("Fetch content from a specified URL. Arguments: {\"url\": \"<target-url>\"}")
    .build()

  override def execute(arguments: String): String = {
    val parsedArguments = decode[UrlFetchToolArguments](arguments)
    parsedArguments match {
      case Right(args) =>
        val url = args.url
        println(green(s"\uD83D\uDD0D Fetching URL: $url"))
        try {
          val response = requests.get(
            url,
            connectTimeout = 5000,
            readTimeout = 10000
          )
          response.text()
        } catch {
          case e: Exception => s"Error: ${e.getMessage}"
        }
      case Left(error) =>
        s"Error: Invalid arguments - ${error.getMessage}"
    }
  }
}
