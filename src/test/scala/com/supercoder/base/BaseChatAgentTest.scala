package com.supercoder.base

import munit.FunSuite
import java.io.{ByteArrayOutputStream, PrintStream}
import scala.collection.mutable.ListBuffer
import com.openai.models.FunctionDefinition

class TestChatAgent(prompt: String) extends BaseChatAgent(prompt) {
  override def toolExecution(toolCall: ToolCallDescription): String = "test result"
  override def toolDefinitionList: List[FunctionDefinition] = List.empty
}

class BaseChatAgentTest extends FunSuite {
  private val originalOut = System.out
  private val outputStream = new ByteArrayOutputStream()
  private val printStream = new PrintStream(outputStream)

  override def beforeEach(context: BeforeEach): Unit = {
    outputStream.reset()
    System.setOut(printStream)
  }

  override def afterEach(context: AfterEach): Unit = {
    System.setOut(originalOut)
  }

  test("should initialize with prompt") {
    val prompt = "Test prompt"
    val agent = new TestChatAgent(prompt)
    // Note: We can't directly test the private prompt field
    // Instead, we can verify the behavior that depends on it
    agent.chat("test message")
    assert(outputStream.toString.contains("test message"))
  }

  test("should handle empty message") {
    val agent = new TestChatAgent("Test prompt")
    agent.chat("")
    assert(outputStream.toString.isEmpty)
  }

  test("should handle tool call description") {
    val toolCall = ToolCallDescription(
      name = "test-tool",
      arguments = "{}",
      id = "test-id"
    )

    val withName = toolCall.addName(java.util.Optional.of("test-tool"))
    val withArgs = withName.addArguments(java.util.Optional.of("{}"))
    val withId = withArgs.addId(java.util.Optional.of("test-id"))

    assert(withId.name == "test-tool")
    assert(withId.arguments == "{}")
    assert(withId.id == "test-id")
  }

  test("should handle empty optional in tool call description") {
    val toolCall = ToolCallDescription()
    val emptyOptional = java.util.Optional.empty[String]()

    val result = toolCall
      .addName(emptyOptional)
      .addArguments(emptyOptional)
      .addId(emptyOptional)

    assert(result.name.isEmpty)
    assert(result.arguments.isEmpty)
    assert(result.id.isEmpty)
  }

  test("should handle agent config environment variables") {
    // Test default values
    assert(AgentConfig.OpenAIAPIBaseURL.contains("api.openai.com"))
    assert(AgentConfig.OpenAIModel.contains("O3_MINI"))
    assert(AgentConfig.IsGeminiMode == "false")

    // Test API key requirement
    intercept[RuntimeException] {
      AgentConfig.OpenAIAPIKey
    }
  }

  test("should handle chat history") {
    val agent = new TestChatAgent("Test prompt")
    
    // Send multiple messages
    agent.chat("First message")
    agent.chat("Second message")
    
    // The output should contain both messages
    val output = outputStream.toString
    assert(output.contains("First message"))
    assert(output.contains("Second message"))
  }

  test("should handle tool execution result") {
    val agent = new TestChatAgent("Test prompt")
    
    // Send a message that would trigger tool execution
    agent.chat("Use a tool")
    
    // The output should contain the tool execution result
    val output = outputStream.toString
    assert(output.contains("test result"))
  }

  test("should handle streaming response") {
    val agent = new TestChatAgent("Test prompt")
    
    // Send a message that would trigger streaming
    agent.chat("Stream response")
    
    // The output should contain the streamed content
    val output = outputStream.toString
    assert(output.nonEmpty)
  }

  test("should handle error in tool execution") {
    val errorAgent = new BaseChatAgent("Test prompt") {
      override def toolExecution(toolCall: ToolCallDescription): String = 
        throw new RuntimeException("Tool execution error")
      override def toolDefinitionList: List[FunctionDefinition] = List.empty
    }
    
    // Send a message that would trigger tool execution
    errorAgent.chat("Use a tool")
    
    // The output should contain the error message
    val output = outputStream.toString
    assert(output.contains("Tool execution error"))
  }
} 