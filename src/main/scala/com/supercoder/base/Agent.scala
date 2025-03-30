package com.supercoder.base

import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.core.http.Headers
import com.openai.models.*
import com.supercoder.Main
import com.supercoder.Main.AppConfig
import com.supercoder.lib.Console.{blue, red}
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*

import java.util
import java.util.Optional
import scala.collection.mutable.ListBuffer

object AgentConfig {
  val BasePrompt = s"""
# Tool calling
For each function call, return a json object with function name and arguments within <@TOOL></@TOOL> XML tags:

<@TOOL>
{"name": <function-name>, "arguments": "<json-encoded-string-of-the-arguments>"}
</@TOOL>

The arguments value is ALWAYS a JSON-encoded string, when there is no arguments, use empty string "".

For example:
<@TOOL>
{"name": "file-read", "arguments": "{\"fileName\": \"example.txt\"}"}
</@TOOL>

<@TOOL>
{"name": "project-structure", "arguments": ""}
</@TOOL>

The client will response with <@TOOL-RESULT>[content]</@TOOL-RESULT> XML tags to provide the result of the function call.
Use it to continue the conversation with the user.

# Safety
Please refuse to answer any unsafe or unethical requests.
Do not execute any command that could harm the system or access sensitive information.
When you want to execute some potentially unsafe command, please ask for user confirmation first before generating the tool call instruction.

# Agent Instructions
"""
  val OpenAIAPIBaseURL: String = sys.env.get("SUPERCODER_BASE_URL")
    .orElse(sys.env.get("OPENAI_BASE_URL"))
    .getOrElse("https://api.openai.com/v1")

  val OpenAIModel: String = sys.env.get("SUPERCODER_MODEL")
    .orElse(sys.env.get("OPENAI_MODEL"))
    .getOrElse(ChatModel.O3_MINI.toString)

  val OpenAIAPIKey: String = sys.env.get("SUPERCODER_API_KEY")
    .orElse(sys.env.get("OPENAI_API_KEY"))
    .getOrElse(throw new RuntimeException("You need to config SUPERCODER_API_KEY or OPENAI_API_KEY variable"))
}

case class ToolCallDescription(
    name: String = "",
    arguments: String = "",
) {

  def addName(name: Optional[String]): ToolCallDescription =
    copy(name = this.name + name.orElse(""))

  def addArguments(arguments: Optional[String]): ToolCallDescription =
    copy(arguments = this.arguments + arguments.orElse(""))

}

abstract class BaseChatAgent(prompt: String, model: String = AgentConfig.OpenAIModel) {
  private val client = OpenAIOkHttpClient.builder()
    .baseUrl(AgentConfig.OpenAIAPIBaseURL)
    .apiKey(AgentConfig.OpenAIAPIKey)
    .headers(Headers.builder()
      .put("HTTP-Referer", "https://github.com/huytd/supercoder/")
      .put("X-Title", "SuperCoder")
      .build())
    .build()

  private var chatHistory: ListBuffer[ChatCompletionMessageParam] =
    ListBuffer.empty

  def selectedModel: String = if (model.nonEmpty) model else AgentConfig.OpenAIModel

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

  // Helper method to build base parameters with system prompt and chat history
  private def buildBaseParams(): ChatCompletionCreateParams.Builder = {
    val params = ChatCompletionCreateParams
      .builder()
      .addSystemMessage(AgentConfig.BasePrompt + prompt)
      .model(selectedModel)

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

    val params = buildBaseParams().build()
    val streamResponse = client.chat().completions().createStreaming(params)
    val currentMessageBuilder = new StringBuilder()
    var currentToolCall = ToolCallDescription()

    import sun.misc.{Signal, SignalHandler}
    var cancelStreaming = false
    var streamingStarted = false

    // Define markdown markers
    val boldMarker = "**"
    val codeMarker = "`"

    // Add state flags for markdown
    var inBold = false
    var inInlineCode = false

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
      val wordBuffer = new StringBuilder()
      var isInToolTag = false
      var currentToolTagEndMarker: Option[String] = None // Keep track of the expected closing tag

      while(it.hasNext && !cancelStreaming) {
        val chunk = it.next()
        val delta = chunk.choices.getFirst.delta

        if (delta.content().isPresent) {
          val content = delta.content().get()
          wordBuffer.append(content)
          // We defer appending to currentMessageBuilder until content is finalized/printed

          val toolStart = "<@TOOL>"
          val toolEnd = "</@TOOL>"
          val toolResultStart = "<@TOOL-RESULT>"
          val toolResultEnd = "</@TOOL-RESULT>"

          var continueProcessingBuffer = true
          while (continueProcessingBuffer && wordBuffer.nonEmpty) {
            continueProcessingBuffer = false // Assume loop stops unless something is processed

            if (isInToolTag) {
              val endMarker = currentToolTagEndMarker.getOrElse(toolEnd) // Default, should be set
              val endTagIndex = wordBuffer.indexOf(endMarker)
              if (endTagIndex != -1) {
                // Found the end tag for the current tool block
                val tagContentWithMarker = wordBuffer.substring(0, endTagIndex + endMarker.length)
                if (AppConfig.isDebugMode) print(red(tagContentWithMarker)) else print(tagContentWithMarker) // Print the whole tag block
                currentMessageBuilder.append(tagContentWithMarker) // Add to history
                wordBuffer.delete(0, tagContentWithMarker.length)
                isInToolTag = false
                currentToolTagEndMarker = None
                continueProcessingBuffer = true // Indicate something was processed
              } else {
                // End tag not yet in buffer, wait for more data
                // No partial printing for tool tags
              }
            } else if (inBold) {
              val endMarkerIndex = wordBuffer.indexOf(boldMarker)
              if (endMarkerIndex != -1) {
                // Found the end bold marker
                val boldContent = wordBuffer.substring(0, endMarkerIndex)
                val fullBoldBlock = boldMarker + boldContent + boldMarker
                print(blue(fullBoldBlock)) // Print the complete bold block
                currentMessageBuilder.append(fullBoldBlock) // Add to history
                wordBuffer.delete(0, endMarkerIndex + boldMarker.length)
                inBold = false
                continueProcessingBuffer = true
              } else {
                // End marker not yet in buffer, wait for more data
              }
            } else if (inInlineCode) {
              val endMarkerIndex = wordBuffer.indexOf(codeMarker)
              if (endMarkerIndex != -1) {
                // Found the end code marker
                val codeContent = wordBuffer.substring(0, endMarkerIndex)
                val fullCodeBlock = codeMarker + codeContent + codeMarker
                print(blue(fullCodeBlock)) // Print the complete code block
                currentMessageBuilder.append(fullCodeBlock) // Add to history
                wordBuffer.delete(0, endMarkerIndex + codeMarker.length)
                inInlineCode = false
                continueProcessingBuffer = true
              } else {
                // End marker not yet in buffer, wait for more data
              }
            } else {
              // Not in tool tag or markdown block: Look for start markers or process plain text
              val toolStartIndex = wordBuffer.indexOf(toolStart)
              val toolResultStartIndex = wordBuffer.indexOf(toolResultStart)
              val boldStartIndex = wordBuffer.indexOf(boldMarker)
              val codeStartIndex = wordBuffer.indexOf(codeMarker)

              // Find the earliest marker index
              val markers = List(
                (toolStartIndex, toolStart, toolEnd, false), // (index, startMarker, endMarker, isMarkdown)
                (toolResultStartIndex, toolResultStart, toolResultEnd, false),
                (boldStartIndex, boldMarker, boldMarker, true),
                (codeStartIndex, codeMarker, codeMarker, true)
              ).filter(_._1 != -1).sortBy(_._1) // Keep only found markers, sort by index

              if (markers.nonEmpty) {
                val (startIndex, startMarker, endMarker, isMarkdownMarker) = markers.head

                // Process plain text before the marker
                if (startIndex > 0) {
                  val beforeMarker = wordBuffer.substring(0, startIndex)
                  val (words, remaining) = processWords(beforeMarker)
                  if (words.nonEmpty) {
                    val processedText = words.map { case (word, ws) =>
                      print(blue(word)); print(ws); word + ws // Print and collect text
                    }.mkString
                    currentMessageBuilder.append(processedText) // Add printed text to history
                  }
                  // Handle any remaining partial word - print it as is for now
                  if (remaining.nonEmpty) {
                      print(blue(remaining))
                      currentMessageBuilder.append(remaining)
                  }
                  wordBuffer.delete(0, startIndex) // Consume the processed plain text
                  continueProcessingBuffer = true
                }

                // Handle the marker itself
                if (wordBuffer.startsWith(startMarker)) {
                  if (isMarkdownMarker) {
                    // Start of a markdown block, just consume marker and set flag
                    wordBuffer.delete(0, startMarker.length)
                    if (startMarker == boldMarker) inBold = true
                    else if (startMarker == codeMarker) inInlineCode = true
                  } else {
                    // Start of a tool tag block
                    if (AppConfig.isDebugMode) print(red(startMarker)) else print(startMarker) // Print start tag
                    currentMessageBuilder.append(startMarker) // Add start tag to history
                    wordBuffer.delete(0, startMarker.length)
                    isInToolTag = true
                    currentToolTagEndMarker = Some(endMarker)
                  }
                  continueProcessingBuffer = true
                }
                // If wordBuffer doesn't start with marker after deleting prefix, loop again

              } else {
                // No markers found, process buffer as plain text
                val (words, remaining) = processWords(wordBuffer.toString())
                if (words.nonEmpty) {
                  val processedText = words.map{ case (word, ws) =>
                      print(blue(word)); print(ws); word + ws // Print and collect text
                  }.mkString
                  currentMessageBuilder.append(processedText) // Add printed text to history
                  val processedLength = wordBuffer.length() - remaining.length()
                  wordBuffer.delete(0, processedLength)
                  continueProcessingBuffer = true
                }
                // Keep 'remaining' in buffer for next iteration or chunk
              }
            }
          } // End of inner buffer processing loop
        } // End if delta.content().isPresent
      } // End of main while(it.hasNext) loop

      // After the loop, process any remaining content in the buffer
      if (wordBuffer.nonEmpty) {
          // If streaming was cancelled or ended mid-tag/markdown, print remaining plainly
          if (isInToolTag || inBold || inInlineCode) {
              if(AppConfig.isDebugMode) print(red(wordBuffer.toString())) else print(blue(wordBuffer.toString()))
          } else {
              // Print remaining plain text
              print(blue(wordBuffer.toString()))
          }
          currentMessageBuilder.append(wordBuffer.toString()) // Append whatever is left to history
          wordBuffer.clear()
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
        val messageContent = currentMessageBuilder.toString()
        addMessageToHistory(
          ChatCompletionMessageParam.ofAssistant(
            createAssistantMessageBuilder(messageContent)
              .build()
          )
        )

        // Check if the message contains a tool call
        val toolCallRegex = """(?s)<@TOOL>(.*?)</@TOOL>""".r
        val toolCallMatch = toolCallRegex.findFirstMatchIn(messageContent).map(_.group(1))
        if (toolCallMatch.isDefined) {
          val toolCallJson = toolCallMatch.get
          try {
            val parseResult: Either[Error, ToolCallDescription] = decode[ToolCallDescription](toolCallJson)
            currentToolCall = parseResult.getOrElse(ToolCallDescription())
          } catch {
            case e: Exception =>
              println(red(s"Error parsing tool call: ${e.getMessage}"))
          }
        }
      }
      if (currentToolCall.name.nonEmpty) {
        handleToolCall(currentToolCall)
      }
    }
  }

  // Helper function to process words and whitespace
  private def processWords(text: String): (ListBuffer[(String, String)], String) = {
    val words = ListBuffer[(String, String)]()
    var remainingText = text
    var continueProcessing = true

    while (continueProcessing) {
      val whitespaceIndex = remainingText.indexWhere(_.isWhitespace)
      if (whitespaceIndex != -1) {
        val word = remainingText.substring(0, whitespaceIndex)
        val whitespace = remainingText.substring(whitespaceIndex).takeWhile(_.isWhitespace)
        if (word.nonEmpty) {
          words += ((word, whitespace))
        } else {
          // Handle leading whitespace? For now, just consume it with the next word or as trailing.
          // If printing just whitespace: print(whitespace)
        }
        remainingText = remainingText.substring(whitespaceIndex + whitespace.length)
        if (remainingText.isEmpty) continueProcessing = false
      } else {
        // No more whitespace, the rest is a partial word or empty
        continueProcessing = false
      }
    }
    (words, remainingText) // Return processed words and any remaining partial word
  }

  private def handleToolCall(toolCall: ToolCallDescription): Unit = {
    val toolResult = toolExecution(toolCall)

    // Add the result as assistant's message
    addMessageToHistory(
      ChatCompletionMessageParam.ofAssistant(
        createAssistantMessageBuilder(s"Calling ${toolCall.name} tool...").build()
      )
    )
    addMessageToHistory(
      ChatCompletionMessageParam.ofUser(
        createUserMessageBuilder(s"<@TOOL-RESULT>${toolResult}</@TOOL-RESULT>").build()
      )
    )

    // Trigger follow-up response from assistant
    chat("")
  }

}
