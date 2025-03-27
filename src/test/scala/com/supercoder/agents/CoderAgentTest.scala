package com.supercoder.agents

import munit.FunSuite
import com.supercoder.base.ToolCallDescription
import com.supercoder.tools.{CodeSearchTool, ProjectStructureTool, FileReadTool, CodeEditTool, CommandExecutionTool}

class CoderAgentTest extends FunSuite {
  test("should initialize with default prompt") {
    val agent = new CoderAgent()
    assert(agent.availableTools.nonEmpty)
    assert(agent.availableTools.contains(CodeSearchTool))
    assert(agent.availableTools.contains(ProjectStructureTool))
    assert(agent.availableTools.contains(FileReadTool))
    assert(agent.availableTools.contains(CodeEditTool))
    assert(agent.availableTools.contains(CommandExecutionTool))
  }

  test("should initialize with additional prompt") {
    val additionalPrompt = "Additional instructions"
    val agent = new CoderAgent(additionalPrompt)
    assert(agent.availableTools.nonEmpty)
  }

  test("should throw exception for unknown tool") {
    val agent = new CoderAgent()
    val unknownToolCall = ToolCallDescription("unknown_tool", "{}")
    
    intercept[IllegalArgumentException] {
      agent.toolExecution(unknownToolCall)
    }
  }
} 