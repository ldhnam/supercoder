# SuperCoder

Welcome to SuperCoder! A coding agent that runs in your terminal.

<img width="1323" alt="image" src="https://github.com/user-attachments/assets/bf8944b7-4c64-405e-92c3-5217caa351e6" />

## Features

SuperCoder equips you with an array of powerful tools to simplify your development workflow. It offers the following features:

- Code Search: Performs complex code searches across your project to quickly locate specific patterns.
- Project Structure Exploration: Provides an organized view of your project's folders and files, making navigation a breeze.
- Code Editing: Enables you to modify your codebase seamlessly with natural language commands.
- Bug Fixing: Automatically fixes bugs and implements improvements based on your detailed requests.
- Cursor Rules Support: Leverages Cursor Rules to intelligently understand and modify your code at precise locations.

## Installation

We have a pre-built binary that works on Linux, MacOS and Windows.

- **Step 1:** Download the ZIP bundle from the [Release](https://github.com/huytd/supercoder/releases) page.

  <img width="258" alt="image" src="https://github.com/user-attachments/assets/7d2d7196-1a35-4752-a6d0-5816955b81dc" />
  
- **Step 2:** Extract to a folder on your computer, and make sure the `bin/supercoder` or `bin/supercoder.bat` binary is accessible in your system's `PATH`.
- **Step 3:** In your terminal, run the `supercoder` command from any folder you want to work on.

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

It's important to note that the model you are using should support tools calling.

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

Contributions, issues, and feature requests are welcome! Please check the [issues page](issues) if you want to contribute.

## License

This project is open source and available under the MIT License.
