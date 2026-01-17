# Java CLI Simulator

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Status](https://img.shields.io/badge/Status-Completed-success?style=for-the-badge)

A robust, functional Command Line Interpreter (CLI) built from scratch in Java. This project simulates core operating system shell commands, file system manipulation, and I/O redirection, demonstrating a deep understanding of Java I/O streams, NIO (Non-blocking I/O), and string parsing algorithms.

## 🚀 Overview

This application mimics the behavior of a Linux/Unix terminal (or Windows Command Prompt). It interacts directly with the host operating system's file system, allowing users to navigate directories, manage files, and perform bulk operations like recursive copying and archive compression.

The architecture strictly separates concerns between input parsing and command execution, ensuring a modular and extensible codebase.

## ✨ Key Features

### 📂 File System Management
* **Navigation:** Seamless directory traversal (`cd`, `pwd`) including parent directory (`..`) support.
* **Manipulation:** Create/Delete directories (`mkdir`, `rmdir`) and files (`touch`, `rm`).
* **Recursive Operations:** Deep copy capabilities (`cp -r`) to duplicate entire directory trees.

### 📝 Data & IO Processing
* **Stream Redirection:** Full support for standard output redirection using `>` (overwrite) and `>>` (append) operators.
* **File Analysis:** Word count tool (`wc`) providing line, word, and character metrics.
* **Concatenation:** Read and display file contents (`cat`).

### 📦 Archiving
* **Compression:** Custom implementation of `zip` to compress files and directories recursively.
* **Extraction:** `unzip` functionality to extract archives to the current directory.

## 🛠 Technical Architecture

The project is designed using a modular approach consisting of two primary components:

### 1. The Parser (`Parser` Class)
Responsible for syntactical analysis of user input.
* **Tokenization:** Splits raw input strings while respecting command arguments.
* **Operator Detection:** Identifies IO redirection operators (`>`, `>>`) and separates the destination file path from the command arguments.

### 2. The Terminal (`Terminal` Class)
Acts as the execution engine.
* **Java NIO Integration:** Utilizes `java.nio.file.Files` and `Paths` for modern, efficient file handling.
* **Recursion:** Implements recursive algorithms for directory walking (used in `cp -r` and `zip`).
* **State Management:** Maintains the current working directory context throughout the session.

## 💻 Installation & Usage

### Prerequisites
* Java Development Kit (JDK) 8 or higher.

## 📚 Command Reference

| Command | Arguments | Description |
| :--- | :--- | :--- |
| `pwd` | None | Prints the absolute path of the current working directory. |
| `cd` | `[path]` or `..` | Changes the directory. Supports relative paths, absolute paths, and `..` for parent directory. |
| `ls` | None | Lists all files and directories in the current folder. |
| `mkdir` | `[dir1] [dir2]...` | Creates one or multiple new directories. |
| `rmdir` | `[dir]` or `*` | Removes an empty directory. Use `*` to attempt removing **all** empty subdirectories in the current folder. |
| `touch` | `[file]` | Creates a new, empty file. |
| `cp` | `[-r] [source] [dest]` | Copies a file. Add the `-r` flag as the first argument to recursively copy a directory. |
| `rm` | `[file]` | Deletes a specific file. |
| `cat` | `[file]` | Reads and prints the content of a file to the terminal. |
| `wc` | `[file]` | "Word Count": Displays the number of lines, words, and characters in a file. |
| `zip` | `[name.zip] [file/dir]` | Compresses a file or directory recursively into a new zip archive. |
| `unzip` | `[name.zip]` | Extracts the contents of a zip archive into the current directory. |
| `exit` | None | Terminates the CLI session and closes the program. |

## 👥 Contributors

This project was collaboratively designed, developed, and tested by our engineering team:

* **Mousa Mohamed Mousa**
* **Omar Mohamed Farag**
* **Mohab Amr**
* **Mariel Robert John**
* **Malak Amr**
*

### Running the Application

1.  **Clone the repository**
    ```bash
    git clone [https://github.com/yourusername/java-cli-simulator.git](https://github.com/yourusername/java-cli-simulator.git)
    cd java-cli-simulator
    ```

2.  **Compile the source**
    ```bash
    javac Main.java
    ```

3.  **Run the CLI**
    ```bash
    java Main
    ```

### Sample Session

```bash
> pwd
C:\Users\Dev\Projects\JavaCLI

> mkdir docs assets
> cd assets
> touch style.css logo.png
> ls
style.css
logo.png

> cd ..
> cp -r assets backup_assets
Directory copied successfully

> ls > file_list.txt
> cat file_list.txt
docs
assets
backup_assets
file_list.txt


