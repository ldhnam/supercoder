package com.supercoder.tools

import com.openai.models.FunctionDefinition
import com.supercoder.base.Tool
import com.supercoder.lib.Console.{green, yellow}

import java.io.File
import scala.io.Source
import scala.collection.mutable.ArrayBuffer

object ProjectStructureTool extends Tool {
  private val gitignorePatterns = ArrayBuffer.empty[String]
  
  override val functionDefinition = FunctionDefinition
    .builder()
    .name("project-structure")
    .description("Get the structure of the current project. Arguments: 'null'")
    .build()

  override def execute(arguments: String): String = {
    println(green("🔎 Reading project structure..."))
    loadGitignore()
    buildProjectTree(new File("."), 0)
  }

  private def loadGitignore(): Unit = {
    val gitignoreFile = new File(".gitignore")
    if (gitignoreFile.exists()) {
      val source = Source.fromFile(gitignoreFile)
      try {
        gitignorePatterns ++= source.getLines()
          .map(_.trim)
          .filter(_.nonEmpty)
          .filter(!_.startsWith("#"))

        // Include some default patterns for common directories
        gitignorePatterns ++= List(
          "node_modules",
          "build",
          "dist",
          "out",
          "target",
          ".idea",
          ".vscode",
          ".git"
        )
      } finally {
        source.close()
      }
    }
  }

  private def isIgnored(file: File): Boolean = {
    val path = file.getPath.replace(File.separator, "/")
    val relativePath = path.stripPrefix("./")

    gitignorePatterns.exists { pattern =>
      val isDirPattern = pattern.endsWith("/")
      val cleanPattern = pattern.stripSuffix("/")

      if (isDirPattern && !file.isDirectory) {
        false
      } else if (cleanPattern.contains("/")) {
        // Handle patterns with path components
        relativePath.matches(cleanPattern
          .replace(".", "\\.")
          .replace("*", "[^/]*")
          .replace("**", ".*"))
      } else {
        // Handle simple patterns
        file.getName.matches(cleanPattern
          .replace(".", "\\.")
          .replace("*", ".*"))
      }
    }
  }

  private def buildProjectTree(dir: File, depth: Int): String = {
    val builder = new StringBuilder
    val prefix = "│   " * depth
    
    if (depth == 0) {
      builder.append(".\n")
    }

    val (dirs, files) = dir.listFiles().partition(_.isDirectory)
    val filteredDirs = dirs.filterNot(isIgnored).sortBy(_.getName)
    val filteredFiles = files.filterNot(isIgnored).sortBy(_.getName)

    filteredDirs.foreach { subDir =>
      builder.append(s"$prefix├── ${subDir.getName}/\n")
      builder.append(buildProjectTree(subDir, depth + 1))
    }

    filteredFiles.foreach { file =>
      builder.append(s"$prefix├── ${file.getName}\n")
    }

    builder.toString()
  }
}