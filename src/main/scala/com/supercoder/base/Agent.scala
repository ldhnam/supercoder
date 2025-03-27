package com.supercoder.base

import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.models.*

import java.util
import java.util.Optional
import scala.collection.mutable.ListBuffer
import com.supercoder.lib.Console.blue

object AgentConfig {
  val OpenAIAPIBaseURL: String = sys.env.get("SUPERCODER_BASE_URL")
    .orElse(sys.env.get("OPENAI_BASE_URL"))
    .getOrElse("https://api.openai.com/v1")

  val OpenAIModel: String = sys.env.get("SUPERCODER_MODEL")
    .orElse(sys.env.get("OPENAI_MODEL"))
    .getOrElse(ChatModel.O3_MINI.toString)

  val OpenAIAPIKey: String = sys.env.get("SUPERCODER_API_KEY")
    .orElse(sys.env.get("OPENAI_API_KEY"))
    .getOrElse(throw new RuntimeException("You need to config SUPERCODER_API_KEY or OPENAI_API_KEY variable"))

  val IsGeminiMode: String = sys.env.get("SUPERCODER_GEMINI_MODE").getOrElse("false").toLowerCase
}

case class ToolCallDescription(
    name: String = "",
    arguments: String = "",
    id: String = ""
) {

  def addName(name: Optional[String]): ToolCallDescription =
    copy(name = this.name + name.orElse(""))

  def addArguments(arguments: Optional[String]): ToolCallDescription =
    copy(arguments = this.arguments + arguments.orElse(""))

  def addId(id: Optional[String]): ToolCallDescription =
    copy(id = this.id + id.orElse(""))

}

abstract class BaseChatAgent(prompt: String) {
  private val client = OpenAIOkHttpClient.builder()
    .baseUrl(AgentConfig.OpenAIAPIBaseURL)
    .apiKey(AgentConfig.OpenAIAPIKey)
    .build()

  private var chatHistory: ListBuffer[ChatCompletionMessageParam] =
    ListBuffer.empty

  def toolExecution(toolCall: ToolCallDescription): String
  def toolDefinitionList: List[FunctionDefinition]

  private def addMessageToHistory(message: ChatCompletionMessageParam): Unit =
    chatHistory = chatHistory :+ message

  private def createAssistantMessageBuilder(
      content: String
  ): ChatCompletionAssistantMessageParam.Builder = {
    ChatCompletionAssistantMessageParam
      .builder()
      .content(content)
      .refusal("")
  }

  private def createUserMessageBuilder(
      content: String
  ): ChatCompletionUserMessageParam.Builder =
    ChatCompletionUserMessageParam
      .builder()
      .content(content)

  private def createAssistantToolCallMessage(
      toolCall: ToolCallDescription
  ): Unit = {
    var messageBuilder = createAssistantMessageBuilder("")
    messageBuilder.addToolCall(
      ChatCompletionMessageToolCall
        .builder()
        .id(toolCall.id)
        .function(
          ChatCompletionMessageToolCall.Function
            .builder()
            .name(toolCall.name)
            .arguments(toolCall.arguments)
            .build()
        )
        .build()
    )

    addMessageToHistory(
      ChatCompletionMessageParam.ofAssistant(messageBuilder.build())
    )
  }

  private def createToolResponseMessage(
      result: String,
      toolCallId: String
  ): ChatCompletionMessageParam = {
    val toolResponse = ChatCompletionMessageParam.ofTool(
      ChatCompletionToolMessageParam
        .builder()
        .content(result)
        .toolCallId(toolCallId)
        .build()
    )

    toolResponse
  }

  // Helper method to build base parameters with system prompt and chat history
  private def buildBaseParams(): ChatCompletionCreateParams.Builder = {
    val params = ChatCompletionCreateParams
      .builder()
      .addSystemMessage(prompt)
      .model(AgentConfig.OpenAIModel)

    // Add all messages from chat history
    chatHistory.foreach(params.addMessage)
    params
  }

  def chat(message: String): Unit = {
    // Add user message to chat history
    if (message.nonEmpty) {
      addMessageToHistory(
        ChatCompletionMessageParam.ofUser(
          createUserMessageBuilder(message).build()
        )
      )
    }

    // Build parameters with tool definition
    var params = buildBaseParams()
    toolDefinitionList.foreach(tool =>
      params.addTool(
        ChatCompletionTool
          .builder()
          .function(tool)
          .build()
      )
    )

    // Stream the response with support for cancelling using Ctrl+C
    val streamResponse = client.chat().completions().createStreaming(params.build())
    var currentMessageBuilder = new StringBuilder()
    var currentToolCall = ToolCallDescription()

    // Set up a SIGINT handler to cancel the streaming response only after streaming starts
    import sun.misc.{Signal, SignalHandler}
    var cancelStreaming = false
    var streamingStarted = false
    val intSignal = new Signal("INT")
    val oldHandler = Signal.handle(intSignal, new SignalHandler {
      override def handle(sig: Signal): Unit = {
        if (streamingStarted) {
          cancelStreaming = true
        } // else ignore Ctrl+C if streaming hasn't started
      }
    })

    try {
      val it = streamResponse.stream().iterator()
      streamingStarted = true
      while(it.hasNext && !cancelStreaming) {
        val chunk = it.next()
        val delta = chunk.choices.getFirst.delta
        if (delta.toolCalls().isPresent && !delta.toolCalls().get().isEmpty) {
          val toolCall = delta.toolCalls().get().getFirst
          if (toolCall.function().isPresent) {
            val toolFunction = toolCall.function().get()
            currentToolCall = currentToolCall
              .addName(toolFunction.name())
              .addArguments(toolFunction.arguments())
              .addId(toolCall.id())
          }
        }
        if (delta.content().isPresent) {
          val chunkContent = delta.content().get()
          currentMessageBuilder.append(chunkContent)
          print(blue(chunkContent))
        }
      }
      if (cancelStreaming) {
        println(blue("\nStreaming cancelled by user"))
      }
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      // Restore original SIGINT handler and close stream
      Signal.handle(intSignal, oldHandler)
      streamResponse.close()
      if (currentMessageBuilder.nonEmpty) {
        println()
        addMessageToHistory(
          ChatCompletionMessageParam.ofAssistant(
            createAssistantMessageBuilder(currentMessageBuilder.toString())
              .build()
          )
        )
      }
      if (currentToolCall.id.nonEmpty || currentToolCall.name.nonEmpty) {
        handleToolCall(currentToolCall)
      }
    }
  }

  private def handleToolCall(toolCall: ToolCallDescription): Unit = {
    val toolResult = toolExecution(toolCall)

    if (AgentConfig.IsGeminiMode != "true") {
      // Add the assistant's tool call message to chat history
      createAssistantToolCallMessage(toolCall)
      // Add result to chat history
      addMessageToHistory(createToolResponseMessage(toolResult, toolCall.id))
    } else {
      // Add the result as assistant's message
      addMessageToHistory(
        ChatCompletionMessageParam.ofAssistant(
          createAssistantMessageBuilder(s"I will need to use the ${toolCall.name} tool...").build()
        )
      )
      addMessageToHistory(
        ChatCompletionMessageParam.ofUser(
          createUserMessageBuilder(s"Here's the tool call result: ${toolResult}").build()
        )
      )
    }

    // Trigger follow up response from assistant
    chat("")
  }

}
