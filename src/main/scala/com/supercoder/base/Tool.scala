package com.supercoder.base

import com.openai.models.FunctionDefinition

trait Tool {
    val functionDefinition: FunctionDefinition
    def execute(arguments: String): String
}
