# SuperCoder

Welcome to SuperCoder! This project is designed to serve as a foundation for building a fully functional coding agent capable of interpreting natural language commands and executing code-related tasks. The project integrates various low-level tools (code search, editing, file reading, project structure analysis, and code execution) and provides a basic conversational interface via a terminal-based chat.

## Overview

SuperCoder is structured into several core modules:

- **Agents:** The intelligence layer that decides which tool to use based on user input. Currently, the `CoderAgent` is available in `src/main/scala/com/supercoder/agents/CoderAgent.scala`.

- **Tools:** A set of modular utilities to perform code editing, code search, file reading, project structure analysis, and command execution. These are located in `src/main/scala/com/supercoder/tools/`.

- **UI:** The TerminalChat interface which allows users to interact with the agent in a console environment. See `src/main/scala/com/supercoder/ui/TerminalChat.scala`.

- **Base Classes:** Core abstractions such as `Agent` and `Tool` in `src/main/scala/com/supercoder/base/` define how agents and tools interact.

## Installation

To install SuperCoder as a native binary, execute the following command:

```bash
sbt universal:packageBin
```

This command will compile and package the project into a binary file located at `target/universal/supercoder-0.1.0-SNAPSHOT.zip`. Extract this file to access the binary in the `bin` directory.

Copy the `supercoder` file to somewhere in your `PATH` to run the agent from anywhere in your terminal.

## Usage

### Configure the Agent

#### Option 1: Using OpenAI API
Before running the agent, you need to have the `OPENAI_API_KEY` environment variable configured. You can obtain an API key by signing up at [OpenAI](https://platform.openai.com/).

```shell
export OPENAI_API_KEY=<API_KEY>
export OPENAI_MODEL=<MODEL> # default to "o3-mini", so watch your wallet
```

#### Option 2: Using Local Models or any OpenAI-compatible API
If you have a local model or any other OpenAI-compatible API, you can configure SuperCoder to use it, by setting the following environment variables:

```shell
export SUPERCODER_BASE_URL=<URL>
export SUPERCODER_API_KEY=<URL>
export SUPERCODER_MODEL=<URL>
```

Note that, if you are using Google Gemini, you will need to set `SUPERCODER_GEMINI_MODE=true` as well.

It's important to note that, the model you are using should support tools calling.

### Running the Coding Agent

After building the project, extract and run the generated binary. Once running, you can type natural language commands such as:

- "Search for usage of function XYZ"
- "Edit file path/to/file.scala to add a new method"
- "Show me the project structure"

The agent will interpret your commands and invoke the appropriate tool.

### Interacting with the Tools

SuperCoder supports the following tools:

- **CodeSearchTool**: Helps in searching for specific code patterns across the project.
- **CodeEditTool**: Allows editing of files within the project.
- **FileReadTool**: Reads and displays file content.
- **ProjectStructureTool**: Provides an overview of the project folders and files.
- **CodeExecutionTool**: Executes shell commands based on the agent's assessment.

## Development

For development purposes, follow these instructions to set up your environment:

### Prerequisites

- Java 8 or above
- SBT (Scala Build Tool)

### Setup

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd SuperCoder
   ```

2. Build the project using SBT:
   ```bash
   sbt compile
   ```

3. Run tests to ensure everything is working as expected:
   ```bash
   sbt test
   ```

## Contributing

Contributions, issues, and feature requests are welcome! Please check the [issues page](https://github.com/yourusername/SuperCoder/issues) if you want to contribute.

## License

This project is open source and available under the MIT License.
