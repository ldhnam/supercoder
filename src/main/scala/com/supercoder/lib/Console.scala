package com.supercoder.lib

import scala.io.AnsiColor.*

object Console {
  def bold(text: String): String = s"${BOLD}$text${RESET}"

  def underline(text: String): String =
    s"${UNDERLINED}$text${RESET}"

  def black(text: String): String = s"${BLACK}$text${RESET}"

  def blue(text: String): String = s"${BLUE}$text${RESET}"

  def green(text: String): String = s"${GREEN}$text${RESET}"

  def red(text: String): String = s"${RED}$text${RESET}"

  def yellow(text: String): String =
    s"${YELLOW}$text${RESET}"

  def white(text: String): String =
    s"${WHITE}$text${RESET}"
}
