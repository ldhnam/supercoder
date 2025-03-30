package com.supercoder.tools

import com.openai.models.FunctionDefinition
import com.supercoder.base.Tool
import com.supercoder.lib.Console.green
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*

import java.net.URLEncoder

case class WebSearchToolArguments(query: String, limit: Int)

object WebSearchTool extends Tool {
  val searxngInstance: String = sys.env.getOrElse("SEARXNG_URL", "")

  override val functionDefinition: FunctionDefinition = FunctionDefinition.builder()
    .name("web-search")
    .description("Perform web search using SearxNG. Use this when you need to find information that is not in the codebase or when you need to find a specific library or tool. Arguments: {\"query\": \"<search-query>\", \"limit\": <max-results>}")
    .build()

  override def execute(arguments: String): String = {
    if (searxngInstance.isEmpty) {
      return "Error: SearxNG instance URL is not set. Please set the SEARXNG_URL environment variable."
    }
    val parsedArguments = decode[WebSearchToolArguments](arguments)
    parsedArguments match {
      case Right(args) =>
        val query = args.query
        val encodedQuery = URLEncoder.encode(args.query, "UTF-8")
        val limit = args.limit
        println(green(s"🔍 Searching the web for: $query"))
        try {
          val searchUrl = s"$searxngInstance/search?q=$encodedQuery&format=json&limit=$limit"
          val response = requests.get(searchUrl)
          response.text()
        } catch {
          case e: Exception => s"Error: ${e.getMessage}"
        }
      case Left(error) =>
        s"Error: Invalid arguments - ${error.getMessage}"
    }
  }
}
