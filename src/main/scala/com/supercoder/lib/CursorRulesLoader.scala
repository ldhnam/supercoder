package com.supercoder.lib

import java.nio.file.{Files, Paths, Path}
import java.nio.charset.StandardCharsets
import scala.jdk.CollectionConverters._

object CursorRulesLoader {
  /**
   * Loads all *.mdc files in the project's .cursor/rules directory, then reads the .cursorrules file in the project's root,
   * combines them and returns them as a single string.
   * 
   * The returned string consists of the content of all .mdc files (separated by newlines) followed by a newline and the 
   * content of the .cursorrules file.
   */
  def loadRules(): String = {
    // Determine the project root directory
    val projectRoot = Paths.get(System.getProperty("user.dir"))

    // Define the path for .cursorrules file in the project's root
    val cursorRulesFile: Path = projectRoot.resolve(".cursorrules")

    // Define the directory path for .cursor/rules
    val rulesDir: Path = projectRoot.resolve(Paths.get(".cursor", "rules"))

    // Initialize content for .mdc files
    val mdcContent: String = if (Files.exists(rulesDir) && Files.isDirectory(rulesDir)) {
      // List all files in the rulesDir ending with .mdc
      val mdcFiles = Files.list(rulesDir).iterator().asScala
        .filter(path => Files.isRegularFile(path) && path.getFileName.toString.endsWith(".mdc"))
        .toList
      // Read each file and concatenate contents separated by newline
      mdcFiles.map { path =>
        new String(Files.readAllBytes(path), StandardCharsets.UTF_8)
      }.mkString("\n")
    } else {
      ""
    }

    // Read the .cursorrules file if it exists
    val mainRulesContent: String = if (Files.exists(cursorRulesFile) && Files.isRegularFile(cursorRulesFile)) {
      new String(Files.readAllBytes(cursorRulesFile), StandardCharsets.UTF_8)
    } else {
      ""
    }

    // Combine the content from .mdc files and .cursorrules file
    // Separating the two parts with a newline if both are non-empty
    if (mdcContent.nonEmpty && mainRulesContent.nonEmpty) {
      mdcContent + "\n" + mainRulesContent
    } else {
      mdcContent + mainRulesContent
    }
  }
}
