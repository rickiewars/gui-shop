# Contributing to GuiShop

Thanks for your interest in contributing! Bug reports, ideas and pull requests are all welcome. This document explains how to get started and what I look for in a contribution.

## Reporting issues

Found a bug or have an idea? Open an issue on [GitHub](https://github.com/rickiewars/gui-shop/issues). For bugs, please first update to the latest GuiShop version for your Minecraft version and check that the problem still happens there. If it does, include:

- The Minecraft version and GuiShop version you're using.
- Include the economy provider you're using, either being GuiShop's built-in economy provider or one from an external economy mod.
- The steps to reproduce the problem.
- The expected behavior.
- The actual behavior.
- Relevant logs or error messages.

## Before you start

For small fixes, feel free to open a pull request right away. If you're planning a larger change and aren't sure it fits the project, please open an issue first so we can discuss it.

## Project setup

GuiShop supports multiple Minecraft versions from a single codebase using [Stonecutter](https://stonecutter.kikugie.dev/). Each supported version is built from the same source. The folders under `versions/` only contain each version's `gradle.properties`. Everything else in that directory is generated during the build. The currently active version is set in `stonecutter.gradle`.

Version-specific code is written with Stonecutter comments, for example:

```java
public static void example() {
    //? if >=26.3 {
    System.out.println("code for 26.3 and newer");
    //?} elif >=26.1 {
    /*System.out.println("code between 26.1 and 26.3");
    *///?} else {
    /*System.out.println("code for older versions");
     *///?}
}
```

If you omit the curly braces, the condition only affects the next non-empty line following it. Never use that here as it makes the code more difficult to follow in some cases.

**Don't do this:**

```java
public static void example() {
    //? if >=26.3
    System.out.println("Don't do this!");
}
```

**Do this:**

```java
public static void example() {
    //? if >=26.3 {
    System.out.println("Do this!");
    //?}
}
```

See the [comment syntax overview](https://stonecutter.kikugie.dev/wiki/v2/guides/start/comment-overview) for the full syntax.

## Building and testing

Replace `26.3` with the version you're working on:

```sh
# Build the mod
./gradlew :26.3:build

# Run only the unit tests (JUnit)
./gradlew :26.3:test

# Run the game tests
./gradlew :26.3:runGameTest

# Generate a code coverage report for all tests
./gradlew :26.3:jacocoFullReport
```

Every pull request is built and tested on all supported Minecraft versions by GitHub Actions. If your change affects version-specific code, please test it on the versions involved.

## Pull request guidelines

- **Keep it focused.** One feature or fix per pull request, so it stays small enough to review properly.
- **Add tests.** New logic should come with tests that show it works as intended. The coverage report helps you find important branches that aren't tested yet.
- **Keep mixins thin.** Mixins can't be tested, so they should only call into regular, testable code.
- **Access Minecraft and external mods through `api`.** Core logic should not call Minecraft or other mods directly.
- **Keep version-specific code in `api` and `mixin`.** Core logic should not contain any Stonecutter conditionals.
- **Let names do the explaining.** A method name should describe what it does clearly. Comments explaining the code is rarely necessary.
- **Follow the existing code style.** Readability and maintainability come first.
- **Describe your change.** Explain what it does and why, and link the related issue if there is one.

## Using AI

AI-assisted contributions are welcome and are reviewed like any other contribution. Please read the [AI policy](AI_POLICY.md) before opening one. It describes how I use AI on GuiShop and what I expect from contributors who use it: disclose that you used AI, be able to explain the code you submit, and verify that generated tests actually test something.

## License

By contributing, you agree that your contributions are licensed under the [MIT License](LICENSE).