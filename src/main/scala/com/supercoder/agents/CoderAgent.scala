package com.supercoder.agents

import com.openai.models.FunctionDefinition
import com.supercoder.base.{BaseChatAgent, ToolCallDescription}
import com.supercoder.tools.{CodeEditTool, CodeSearchTool, CommandExecutionTool, FileReadTool, ProjectStructureTool, UrlFetchTool, WebSearchTool}

val coderAgentPrompt = s"""
You are a senior software engineer AI agent. Your task is to help the user with their coding needs.

You have access to the following tools:

- ${CodeSearchTool.functionDefinition.name}: ${CodeSearchTool.functionDefinition.description}
- ${ProjectStructureTool.functionDefinition.name}: ${ProjectStructureTool.functionDefinition.description}
- ${FileReadTool.functionDefinition.name}: ${FileReadTool.functionDefinition.description}
- ${CodeEditTool.functionDefinition.name}: ${CodeEditTool.functionDefinition.description}
- ${CommandExecutionTool.functionDefinition.name}: ${CommandExecutionTool.functionDefinition.description}
- ${UrlFetchTool.functionDefinition.name}: ${UrlFetchTool.functionDefinition.description}
- ${WebSearchTool.functionDefinition.name}: ${WebSearchTool.functionDefinition.description}

You can use these tools to help you with the user's request.

When using the web-search tool, make sure you also use the url-fetch tool to read the content of the result URLs if needed.


The discussion is about the code of the current project/folder. Always use the relevant tool to learn about the
project if you are unsure before giving answer.
"""

class CoderAgent(additionalPrompt: String = "", model: String = "")
    extends BaseChatAgent(coderAgentPrompt + additionalPrompt, model) {

  final val availableTools = List(
    CodeSearchTool,
    ProjectStructureTool,
    FileReadTool,
    CodeEditTool,
    CommandExecutionTool,
    UrlFetchTool,
    WebSearchTool
  )

  override def toolDefinitionList: List[FunctionDefinition] =
    availableTools.map(_.functionDefinition)

  override def toolExecution(toolCall: ToolCallDescription): String = {
    availableTools.find(_.functionDefinition.name == toolCall.name) match {
      case Some(tool) => tool.execute(toolCall.arguments)
      case None =>
        throw new IllegalArgumentException(s"Tool ${toolCall.name} not found")
    }
  }

}
