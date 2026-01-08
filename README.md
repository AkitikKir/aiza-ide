# Aiza IDE

**Aiza IDE** is a cross-platform integrated development environment (IDE) for Vibe Coding, built with Kotlin and Jetpack Compose Multiplatform. It features a powerful built-in AI coding agent powered by the **Aiza API**.

## 🚀 Key Features

- **Built-in AI Agent**: Chat with Aiza to manage your project.
  - Read project structure and file contents.
  - Create and edit files/folders.
  - Run terminal commands and see output.
  - Propose and apply patches.
- **Cross-Platform**: Support for Linux, Windows, and macOS.
- **Integrated Terminal**: Run build commands, tests, and scripts directly.
- **File Explorer**: Browse and manage your project files.
- **Code Editor**: Clean and efficient editing experience with multiple tabs.

## 🛠 Project Structure

```text
.
├── agent/            # AI Agent logic and command execution
├── config/           # Environment and configuration management
├── core/             # API client and data models
├── desktopApp/       # Desktop-specific entry point and packaging
├── ui/               # Shared Compose UI components
├── examples/         # API usage examples and queries
└── .github/          # CI/CD workflows for multi-platform builds
```

## 🚦 Quick Start

### 1. Requirements
- JDK 17 or higher.
- Gradle (provided via wrapper).

### 2. Setup API Key
1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```
2. Open `.env` and set your `AIZA_API_KEY`:
   ```text
   AIZA_API_KEY=your_real_key_here
   ```

### 3. Run Locally
```bash
./gradlew :desktopApp:run
```

## 🤖 AI Agent Commands

The agent can perform actions based on your requests. Use natural language:
- "Create a new module named 'utils' with a String extensions file."
- "Find the error in `Main.kt` and fix it."
- "Run `./gradlew test` and summarize the results."
- "Apply a patch to update the UI color scheme."

## 🔒 Security & Privacy

- **Environment Variables**: API keys are stored in `.env` and are never committed to the repository (excluded via `.gitignore`).
- **GitHub Secrets**: For CI builds, add your `AIZA_API_KEY` to **GitHub Secrets**. The CI workflow will securely inject it into the build environment.
- **Sanitized Examples**: Check `examples/api_queries.md` for safe API usage patterns.

## 📦 Packaging & CI

The project uses GitHub Actions to build and package native distributions:
- **Linux**: `.deb`
- **Windows**: `.msi`
- **macOS**: `.dmg`

To build locally:
```bash
./gradlew :desktopApp:package
```

## 🧪 API Examples

Refer to `examples/api_queries.md` for:
- Sanitized `curl` commands.
- Ktor (Kotlin) implementation details.
- `GET /v1/models` examples.

> **Warning**: Never commit your real API key. If you accidentally expose it, rotate it immediately in the Aiza AI dashboard.
