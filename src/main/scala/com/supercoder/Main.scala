package com.supercoder

import com.supercoder.ui.TerminalChat
import com.supercoder.agents.CoderAgent

object Main {
  def main(args: Array[String]): Unit = {
    val agent = new CoderAgent()
    TerminalChat.run(agent)
  }
}
