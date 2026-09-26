# AI Policy

This document describes how I use AI in the development of GuiShop. It also serves as a guideline for contributors who want to use AI in their work on this project.

## Why I use AI

I work as a full-time software developer. In my free time I work on various projects, most of which are only used by me, my family and a few friends. GuiShop is one of the few I publicly release, so I'm stricter about its code quality. With limited time available, AI helps me actually get things done and keep the project moving.

## My AI workflow

These are the steps I follow when I use AI on GuiShop. They exist so that GuiShop stays
a project I could maintain without AI. I make the decisions, I understand what ships, and
AI is an assistant rather than an author. Contributors are expected to follow the same approach.

- **Design the feature.** I write a design for the feature before writing any code. AI can help by giving advice, discussing different approaches and giving a second opinion, but the decisions are mine.
- **Write tests.** I let AI generate unit tests and game tests based on the design. I verify and refine them until they cover the essential behavior, so they define how the feature should work.
- **Code.** AI can draft the scaffolding, but I write the feature. The code has to meet the design, the project's code conventions and reads like code I'd write myself. I make sure I understand the code, and simplify it where I can.
- **Verify.** I test the feature manually in-game and expand the automated tests to catch any bugs. When I find one, a test is written for it so it can't come back unnoticed. Coverage reports help me check that the essential logic is covered.

## Contributing

AI-assisted pull requests are welcome, as long as you follow these guidelines:

1. **Disclose it.** Mention in the pull request description that AI was used and what for.
2. **Understand your code.** You should be able to explain any part of your change if asked. Pull requests that look like unreviewed AI output will be closed.
3. **Own your code.** You're responsible for your change, whether it came from AI, the internet or your own head. Make sure you have the right to contribute it, and don't submit code copied from projects with an incompatible license.
4. **Verify generated tests.** Check that each test actually tests the intended behavior, not just that it passes.

AI-assisted contributions are reviewed the same way as any other contribution.