# GitHub Copilot Agentic Workflow Demo – Spring Boot + Jira + GitHub Actions

## 1. Purpose

This guide explains how to build a GitHub Copilot Agentic Workflow demo from a clean starting point.

The demo uses:

- Spring Boot Book API
- GitHub
- GitHub Actions
- GitHub Copilot
- GitHub Agentic Workflows (`gh aw`)
- Jira
- Atlassian Rovo MCP
- GitHub Secrets

The goal is to demonstrate the following flow:

```text
Jira Story
    ↓
GitHub Actions
    ↓
GitHub Copilot Agentic Workflow
    ↓
Atlassian Rovo MCP
    ↓
Read Jira Story
    ↓
Understand Acceptance Criteria
    ↓
Inspect Spring Boot Application
    ↓
Implement Code
    ↓
Add / Update Tests
    ↓
Run Tests
    ↓
Create Pull Request
    ↓
Human Review
```

---

# 2. Starting Point

Assume that initially we only have a Spring Boot Book API project.

Example:

```text
spring-boot-book-api-demo/
│
├── src/
│   ├── main/
│   │   └── java/
│   └── test/
│       └── java/
│
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

The application may already provide APIs such as:

```text
GET  /books
GET  /books/{id}
POST /books
```

For the demo, Jira will contain a new development requirement.

Example:

```text
AI-7 – Add DELETE Book API
```

---

# 3. Demo Architecture

```text
                    ┌──────────────────────┐
                    │        JIRA          │
                    │                      │
                    │ AI-7                 │
                    │ Status: To Do        │
                    │ copilot-ready        │
                    └──────────▲───────────┘
                               │
                         Atlassian Rovo
                              MCP
                               │
                               ▼
                    ┌──────────────────────┐
                    │   GitHub Actions     │
                    │                      │
                    │ Agentic Workflow     │
                    │                      │
                    │ GitHub Copilot       │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │ Spring Boot Book API │
                    └──────────┬───────────┘
                               │
                               ▼
                         Implement Story
                               │
                               ▼
                           Run Tests
                               │
                               ▼
                         Pull Request
                               │
                               ▼
                         Human Review
```

---

# 4. Prerequisites

Before creating the workflow, make sure you have:

- A GitHub account
- GitHub Copilot access
- Access to GitHub Actions
- A Jira Cloud account/project
- Permission to use Atlassian Rovo MCP
- Git installed
- Java installed
- Maven available, or Maven Wrapper included in the project
- A Spring Boot application
- Permission to create GitHub repository secrets

Check Java:

```bash
java -version
```

Check Git:

```bash
git --version
```

If Maven is installed locally:

```bash
mvn -version
```

If the project contains Maven Wrapper:

```bash
./mvnw --version
```

---

# 5. Verify the Spring Boot Application

Before introducing Copilot or Jira, make sure the application works normally.

Run:

```bash
./mvnw clean test
```

or:

```bash
mvn clean test
```

Then start the application if required:

```bash
./mvnw spring-boot:run
```

Make sure the existing Book API works.

This establishes a clean baseline before AI makes any changes.

---

# 6. Create the GitHub Repository

Create a new GitHub repository.

Example:

```text
spring-boot-book-api-demo
```

From the local project:

```bash
cd spring-boot-book-api-demo
```

Initialize Git if required:

```bash
git init
```

Set the main branch:

```bash
git branch -M main
```

Add the GitHub repository:

```bash
git remote add origin https://github.com/<YOUR-USERNAME>/spring-boot-book-api-demo.git
```

Verify:

```bash
git remote -v
```

Add the application:

```bash
git add .
```

Commit:

```bash
git commit -m "Initial Spring Boot Book API"
```

Push:

```bash
git push -u origin main
```

---

# 7. Install GitHub CLI

GitHub CLI provides the `gh` command.

## macOS

Using Homebrew:

```bash
brew install gh
```

Verify:

```bash
gh --version
```

If GitHub CLI was previously installed and needs reinstalling:

```bash
brew uninstall gh
brew install gh
```

---

# 8. Authenticate GitHub CLI

Run:

```bash
gh auth login
```

Select:

```text
GitHub.com
```

Then:

```text
HTTPS
```

Use browser authentication when prompted.

Verify authentication:

```bash
gh auth status
```

The command should confirm that the correct GitHub account is authenticated.

---

# 9. Install GitHub Agentic Workflows CLI

Agentic Workflows are provided through the `gh aw` GitHub CLI extension.

Install it:

```bash
gh extension install github/gh-aw
```

Verify:

```bash
gh aw version
```

If already installed, update it before the demo:

```bash
gh extension upgrade github/gh-aw
```

---

# 10. Check the Environment

Run:

```bash
gh aw doctor
```

This helps detect problems with the local GitHub/Agentic Workflow environment.

Resolve important errors before continuing.

---

# 11. Initialize Agentic Workflows

From the root of the repository:

```bash
gh aw init
```

This prepares the repository for Agentic Workflow development.

Depending on the version and options used, supporting files may be created under `.github`.

For example:

```text
.github/
│
├── agents/
│
├── skills/
│
├── mcp.json
│
└── workflows/
```

Not every generated file is required for every workflow.

---

# 12. Understand the Important Folders

## `.github/workflows`

This is the most important location for the workflow demo.

Our workflow source will be similar to:

```text
.github/workflows/jira-developer.md
```

After compilation there will also be a generated workflow file similar to:

```text
.github/workflows/jira-developer.lock.yml
```

---

## `.github/skills`

Keep this folder if the demo also demonstrates GitHub Copilot Skills.

Example:

```text
.github/
└── skills/
    └── spring-boot-development/
        └── SKILL.md
```

A Skill provides reusable instructions to Copilot.

For example, a Spring Boot Skill could tell Copilot:

```text
Follow the existing controller/service/repository architecture.

Use constructor injection.

Follow existing exception-handling conventions.

Add unit tests for new functionality.

Do not change unrelated code.

Run Maven tests before completing the task.
```

---

## `.github/agents`

Custom agents can be useful, but a custom agent is not automatically required simply to run the Jira development workflow.

Only keep custom agents if they are useful for the demo.

---

## `mcp.json`

Do not confuse local MCP configuration with the MCP configuration used by the GitHub Actions Agentic Workflow.

The workflow itself must contain/configure the tools it requires when running remotely.

A local `mcp.json` file used by an IDE or local Copilot session is not automatically what makes the GitHub Actions workflow connect to Jira.

---

# 13. Understand the Two MCP Uses

There can be two separate MCP-related concepts in the environment.

## Agentic Workflow tooling

This helps with development and management of Agentic Workflows.

## Atlassian Rovo MCP

This provides access to Atlassian services such as Jira.

The important runtime connection for this demo is:

```text
GitHub Copilot
      ↓
Atlassian Rovo MCP
      ↓
Jira
```

---

# 14. Prepare Jira

Create or use a Jira project.

For the demo, create a simple story.

Example:

```text
AI-7
```

Title:

```text
Add DELETE endpoint for Books
```

Description:

```text
As an API consumer,
I want to delete a book using its ID,
so that books that are no longer required can be removed.
```

Acceptance Criteria:

```text
1. Add DELETE /books/{id}.
2. Return HTTP 204 when the book is successfully deleted.
3. Return HTTP 404 when the book does not exist.
4. Follow the existing Spring Boot architecture.
5. Add appropriate automated tests.
6. Existing tests must continue to pass.
```

Set:

```text
Status: To Do
```

Add label:

```text
copilot-ready
```

---

# 15. Why Use `copilot-ready`?

The workflow should not necessarily be hard-coded to AI-7.

Instead, we can establish a rule:

```text
Status = To Do
AND
Label = copilot-ready
```

Example:

```text
AI-4    Done
AI-5    In Review
AI-6    To Do
AI-7    To Do       copilot-ready
AI-8    Backlog
```

In this example, AI-7 is the eligible story.

This demonstrates that the automation can work with future Jira stories rather than being written specifically for AI-7.

---

# 16. Configure Atlassian Rovo MCP

The Agentic Workflow needs access to Jira.

The logical connection is:

```text
GitHub Actions
      ↓
Agentic Workflow
      ↓
Atlassian Rovo MCP
      ↓
Jira Cloud
```

The Atlassian Rovo MCP endpoint and authentication method should be configured according to the current Atlassian/GitHub Agentic Workflow documentation.

Do not place usernames, passwords, tokens or other credentials directly inside committed workflow files.

Use GitHub Secrets for sensitive values.

---

# 17. Create Atlassian Credentials

Create the appropriate Atlassian credential/token required for the chosen Rovo MCP authentication approach.

Keep the credential secure.

Do **not** write something like this directly into the repository:

```text
ATLAS_TOKEN=my-secret-token
```

Do not commit credentials into Git.

---

# 18. Configure GitHub Repository Secrets

Open:

```text
GitHub Repository
    ↓
Settings
    ↓
Secrets and variables
    ↓
Actions
```

Select:

```text
New repository secret
```

Create the Atlassian/Rovo secret or secrets required by the workflow.

The exact names depend on the authentication configuration used by the workflow.

Secrets can also be configured through `gh aw`.

Example:

```bash
gh aw secrets set <SECRET_NAME>
```

Check the workflow's secret requirements using:

```bash
gh aw secrets bootstrap
```

The important concept is:

```text
Workflow
    ↓
references
    ↓
GitHub Secret
    ↓
credential supplied securely
    ↓
Rovo MCP
    ↓
Jira
```

---

# 19. Configure Copilot Authentication

The Agentic Workflow uses GitHub Copilot as its AI engine.

Where supported, the workflow can use:

```yaml
permissions:
  copilot-requests: write
```

Organisation settings and Copilot billing/access must allow the workflow to use Copilot.

An alternative configuration may require a Copilot GitHub token stored as a repository secret.

For example:

```text
COPILOT_GITHUB_TOKEN
```

Never hard-code such a token into the workflow.

---

# 20. Create the Agentic Workflow

Create a workflow:

```bash
gh aw new jira-developer
```

This should create a Markdown workflow source similar to:

```text
.github/workflows/jira-developer.md
```

---

# 21. Why Is the Workflow Markdown?

A normal GitHub Action is usually written directly in YAML.

An Agentic Workflow is different.

We write:

```text
Configuration
+
Natural-language instructions
```

inside a Markdown workflow.

For example:

```text
jira-developer.md
```

The Agentic Workflow compiler then produces the GitHub Actions workflow.

Conceptually:

```text
jira-developer.md
        ↓
   gh aw compile
        ↓
jira-developer.lock.yml
        ↓
   GitHub Actions
```

---

# 22. Configure Manual Execution

For the first demo, use manual execution.

The workflow configuration should include:

```yaml
on:
  workflow_dispatch:
```

This allows the presenter to click:

```text
Run workflow
```

from the GitHub Actions screen.

This is more predictable for a live demonstration than waiting for a scheduled execution.

---

# 23. Configure Copilot as the Engine

The workflow uses:

```yaml
engine: copilot
```

Conceptually:

```text
GitHub Actions
      ↓
Agentic Workflow
      ↓
Copilot
```

Copilot is the AI reasoning engine performing the development task.

---

# 24. Configure Permissions

The workflow requires appropriate permissions.

The exact permissions depend on what the workflow is allowed to do.

Conceptually:

```yaml
permissions:
  contents: read
  pull-requests: read
  issues: read
  copilot-requests: write
```

Follow the principle of giving the workflow only the permissions it actually needs.

---

# 25. Configure GitHub Tools

The workflow may expose GitHub tools to the Copilot engine.

For example:

```yaml
tools:
  github:
    toolsets: [default]
```

This allows the Agentic Workflow to interact with approved GitHub functionality.

---

# 26. Configure Jira/Rovo MCP Tools

Configure the Atlassian Rovo MCP integration in the workflow.

The goal is to allow Copilot to perform operations such as:

```text
Search Jira
        ↓
Find eligible story
        ↓
Read Jira story
        ↓
Read acceptance criteria
```

Keep Jira permissions limited to what is required by the demo.

For the first version of the demo, Jira can be treated primarily as the source of the development requirement.

---

# 27. Write the AI Instructions

The natural-language part of `jira-developer.md` should tell Copilot exactly what outcome is expected.

Example instructions:

```markdown
# Jira Developer

Find one Jira story that:

- is currently in To Do
- has the label `copilot-ready`

Read the complete Jira story and its acceptance criteria.

Before making any changes:

1. Inspect the existing Spring Boot application.
2. Understand the existing architecture.
3. Identify the controller, service, repository and tests related to the requirement.
4. Follow the existing coding conventions.

Implement only the functionality requested by the Jira story.

Do not make unrelated changes.

Add or update automated tests for the new functionality.

Run the project's Maven test suite.

If tests fail because of the implementation, investigate and correct the implementation.

Do not remove existing tests simply to make the build pass.

Create a pull request for the completed implementation.

The pull request should include the Jira story key in its title.

The pull request description should explain:

- the Jira story implemented
- the code changes
- the tests added or updated
- the test results
```

---

# 28. Compile the Workflow

Run:

```bash
gh aw compile jira-developer
```

This converts the Agentic Workflow definition into the generated GitHub Actions workflow.

Conceptually:

```text
.github/workflows/jira-developer.md

              ↓

       gh aw compile

              ↓

.github/workflows/jira-developer.lock.yml
```

---

# 29. Do Not Manually Maintain `lock.yml`

Treat:

```text
jira-developer.md
```

as the workflow source.

Treat:

```text
jira-developer.lock.yml
```

as generated output.

If workflow configuration changes, compile again:

```bash
gh aw compile jira-developer
```

Avoid manually changing generated workflow code unless there is a very specific reason.

---

# 30. Inspect the MCP Configuration

Before running the demo:

```bash
gh aw mcp inspect jira-developer
```

Use this to check the MCP tools available to the workflow.

Confirm that the required Jira/Rovo tools are available.

---

# 31. Check Required Secrets

Run:

```bash
gh aw secrets bootstrap
```

Confirm that the workflow has access to all required secrets.

Do not wait until the live demo to discover a missing secret.

---

# 32. Run Agentic Workflow Diagnostics

Run:

```bash
gh aw doctor
```

Resolve important errors before continuing.

---

# 33. Compile All Workflows

As a final check:

```bash
gh aw compile
```

Then:

```bash
git status
```

Review the files that have been generated or changed.

---

# 34. Commit the Workflow

Add the workflow source:

```bash
git add .github/workflows/jira-developer.md
```

Add the generated workflow:

```bash
git add .github/workflows/jira-developer.lock.yml
```

If Skills are part of the demo:

```bash
git add .github/skills/
```

Commit:

```bash
git commit -m "Add Jira Copilot agentic workflow"
```

Push:

```bash
git push origin main
```

---

# 35. Check GitHub Actions

Open:

```text
GitHub Repository
      ↓
Actions
```

The Jira Developer workflow should appear.

Example:

```text
Actions

Jira Developer
```

Do not trigger it yet if preparing for the live demonstration.

---

# 36. Prepare Jira Immediately Before the Demo

Before presenting, check the Jira board.

Recommended state:

```text
AI-4    Done
AI-5    In Review
AI-6    To Do
AI-7    To Do        copilot-ready
AI-8    Backlog
```

Make sure AI-7 contains clear acceptance criteria.

Also make sure another story does not accidentally satisfy the same workflow selection rule.

---

# 37. Verify the Repository Is Clean

Before the demonstration:

```bash
git status
```

Prefer:

```text
nothing to commit, working tree clean
```

Check branches:

```bash
git branch
```

If this is a repeated demo, remove old demo branches and old pull requests as appropriate so the demonstration starts from a predictable state.

---

# 38. Run the Workflow

For the live demo, open:

```text
GitHub
   ↓
Repository
   ↓
Actions
   ↓
Jira Developer
   ↓
Run workflow
```

Alternatively:

```bash
gh aw run jira-developer
```

For a presentation, the GitHub Actions UI is usually easier for the audience to follow.

---

# 39. What Happens at Runtime?

```text
START
  │
  ▼
GitHub Action starts
  │
  ▼
Agentic Workflow starts
  │
  ▼
GitHub Copilot starts
  │
  ▼
Connect to Atlassian Rovo MCP
  │
  ▼
Search Jira
  │
  ▼
Find To Do + copilot-ready
  │
  ▼
Read AI-7
  │
  ▼
Read Acceptance Criteria
  │
  ▼
Inspect Spring Boot Repository
  │
  ▼
Understand Existing Architecture
  │
  ▼
Implement Requirement
  │
  ▼
Add / Update Tests
  │
  ▼
Run Maven Tests
  │
  ├──────── FAIL ────────┐
  │                      │
  │                  Investigate
  │                      │
  │                  Fix Problem
  │                      │
  │                      ▼
  │                  Run Tests
  │
 PASS
  │
  ▼
Create Pull Request
  │
  ▼
Human Review
```

---

# 40. Example Implementation

Suppose AI-7 asks for:

```text
DELETE /books/{id}
```

Copilot may inspect:

```text
BookController
BookService
BookRepository
BookNotFoundException
BookControllerTest
BookServiceTest
```

It may determine that the implementation requires:

```text
BookController
     ↓
Add DELETE endpoint

BookService
     ↓
Add deleteBook()

Repository
     ↓
Use existing repository functionality

Exception Handling
     ↓
Return 404 when appropriate

Tests
     ↓
Successful deletion test
Book-not-found test
```

The important point is that these implementation steps are not necessarily hard-coded into the workflow.

Copilot discovers the appropriate changes by inspecting the application and reading the Jira requirement.

---

# 41. Tests

The workflow should run:

```bash
./mvnw test
```

or:

```bash
mvn test
```

Expected process:

```text
Implement Code
      ↓
Run Tests
      ↓
 ┌────┴────┐
 │         │
FAIL      PASS
 │         │
 ▼         ▼
Fix      Continue
 │
 ▼
Run Tests Again
```

This demonstrates that Copilot is not simply generating code; it is also validating the implementation.

---

# 42. Pull Request

The workflow should finish by creating a pull request.

Example:

```text
AI-7: Add DELETE Book endpoint
```

The PR description should explain:

```text
Jira Story:
AI-7

Changes:
- Added DELETE /books/{id}
- Added service deletion logic
- Added 404 handling
- Added automated tests

Validation:
- Maven tests executed
- Existing tests pass
- New tests pass
```

---

# 43. Human Review

The workflow should not be presented as replacing the developer review process.

The intended model is:

```text
Jira Requirement
      ↓
Copilot Implementation
      ↓
Automated Tests
      ↓
Pull Request
      ↓
Developer Review
      ↓
Approval
      ↓
Merge
```

The human remains responsible for reviewing the generated change.

---

# 44. Jira Status Updates

For the first demo, keep Jira status changes optional.

A simple and reliable first demonstration is:

```text
Jira
 ↓
Read Requirement
 ↓
Implement Code
 ↓
Run Tests
 ↓
Create PR
```

A later version can extend the process to:

```text
Jira: To Do
      ↓
Jira: In Progress
      ↓
Implement
      ↓
Test
      ↓
Create PR
      ↓
Jira: In Review
```

This should only be enabled when the required write permissions and controlled write mechanism have been deliberately configured.

---

# 45. Optional Scheduled Execution

Once manual execution works reliably, scheduling can be added.

Example:

```yaml
on:
  workflow_dispatch:
  schedule:
    - cron: "0 7 * * *"
```

This means the workflow can:

```text
Run manually
+
Run automatically on schedule
```

Important:

GitHub Actions cron schedules use **UTC**.

Therefore UK daylight-saving time must be considered when choosing a fixed cron expression.

For a live demo, prefer:

```yaml
workflow_dispatch:
```

because it allows the presenter to decide exactly when the workflow starts.

---

# 46. `workflow_dispatch` vs `schedule` vs `push`

### Manual

```yaml
on:
  workflow_dispatch:
```

Means:

```text
Run when someone manually triggers it.
```

### Schedule

```yaml
on:
  schedule:
    - cron: "0 9 * * *"
```

Means:

```text
Run automatically according to the cron schedule.
```

### Push

```yaml
on:
  push:
```

Means:

```text
Run when code is pushed.
```

These are simply different events that can start a GitHub Action.

---

# 47. Recommended Repository Structure

A clean demo repository might look like:

```text
spring-boot-book-api-demo/
│
├── .github/
│   │
│   ├── skills/
│   │   └── spring-boot-development/
│   │       └── SKILL.md
│   │
│   └── workflows/
│       ├── jira-developer.md
│       └── jira-developer.lock.yml
│
├── src/
│   ├── main/
│   │   └── java/
│   │
│   └── test/
│       └── java/
│
├── pom.xml
├── mvnw
├── mvnw.cmd
├── README.md
└── GITHUB-COPILOT-WORKFLOW-DEMO.md
```

Keep the repository simple.

Only retain extra Agent, Skill or MCP files when they serve a clear purpose in the demonstration.

---

# 48. Pre-Demo Checklist

Run through this checklist before presenting.

## Local Environment

- [ ] Git installed
- [ ] Java installed
- [ ] Maven/Maven Wrapper works
- [ ] GitHub CLI installed
- [ ] `gh auth status` succeeds
- [ ] `gh aw version` succeeds
- [ ] `gh aw doctor` shows no blocking problem

## GitHub

- [ ] Repository accessible
- [ ] Main branch clean
- [ ] GitHub Actions enabled
- [ ] GitHub Copilot available
- [ ] Required Copilot permissions configured
- [ ] Required repository secrets configured
- [ ] Workflow committed
- [ ] Compiled `.lock.yml` committed

## Jira

- [ ] Jira accessible
- [ ] Rovo MCP authentication works
- [ ] AI-7 exists
- [ ] AI-7 is in `To Do`
- [ ] AI-7 has `copilot-ready`
- [ ] Acceptance criteria are clear
- [ ] No unintended story also matches the selection criteria

## Workflow

- [ ] `gh aw compile` succeeds
- [ ] `gh aw mcp inspect jira-developer` succeeds
- [ ] `gh aw secrets bootstrap` shows required secrets are available
- [ ] Manual workflow trigger is visible in GitHub Actions

## Application

- [ ] Application builds before demo
- [ ] Existing tests pass
- [ ] No existing AI-7 implementation
- [ ] No old AI-7 demo branch causing confusion
- [ ] No old AI-7 pull request causing confusion

---

# 49. Recommended Live Demo Sequence

Do not spend the live demonstration installing tools and creating tokens.

Complete those prerequisites beforehand.

During the presentation, show the following sequence.

## Step 1 – Show the Existing Application

Show the Spring Boot Book API.

Explain:

> This is our existing application. The requested functionality has not yet been implemented.

---

## Step 2 – Show Jira

Open AI-7.

Show:

```text
Status: To Do
Label: copilot-ready
```

Show the acceptance criteria.

Explain:

> Jira is our source of the development requirement.

---

## Step 3 – Show the Agentic Workflow

Open:

```text
.github/workflows/jira-developer.md
```

Explain:

> Instead of hard-coding every development step, we give Copilot an objective and rules.

Show the natural-language instructions.

---

## Step 4 – Explain MCP

Explain:

```text
Copilot
   ↓
Rovo MCP
   ↓
Jira
```

Say:

> MCP provides the controlled connection that allows the workflow to access Jira.

---

## Step 5 – Show GitHub Secrets

Open:

```text
Settings
→ Secrets and variables
→ Actions
```

Show only the **secret names**.

Do not expose secret values.

Explain:

> Sensitive credentials are stored securely and are not committed to the repository.

---

## Step 6 – Trigger the Workflow

Open:

```text
Actions
→ Jira Developer
→ Run workflow
```

Start it.

Explain:

> I am not asking Copilot manually to implement AI-7. I am starting our automated Agentic Workflow.

---

## Step 7 – Watch GitHub Actions

Show the running workflow.

Explain the sequence:

```text
Start workflow
      ↓
Connect to Jira
      ↓
Find eligible story
      ↓
Read requirement
      ↓
Inspect code
      ↓
Implement
      ↓
Test
      ↓
Create PR
```

---

## Step 8 – Show the Pull Request

When the workflow finishes, open the PR.

Show:

- Jira story reference
- Changed files
- Implementation
- New tests
- Test results

---

## Step 9 – Finish With Human Review

Explain:

> Copilot has implemented and tested the requirement, but it has not bypassed our engineering controls. The result is presented as a pull request for human review.

---

# 50. Key Demo Message

The key message is:

```text
Traditional automation:

Developer tells the system HOW to perform every step.

Agentic workflow:

Developer tells the AI WHAT outcome is required
and provides rules, tools and boundaries.
```

For this demo:

```text
WHAT?

Implement a Copilot-ready Jira story.
```

Copilot determines:

```text
Which story?
What code is relevant?
Which classes need changing?
What tests are required?
Did the implementation work?
What should go into the PR?
```

within the permissions and instructions provided by the workflow.

---

# 51. Final End-to-End Flow

```text
                         ┌──────────────┐
                         │     Jira     │
                         │              │
                         │    AI-7      │
                         │    To Do     │
                         │copilot-ready │
                         └──────┬───────┘
                                │
                                ▼
                         Atlassian Rovo
                              MCP
                                │
                                ▼
┌──────────────────────────────────────────────────┐
│                   GitHub                         │
│                                                  │
│ GitHub Actions                                   │
│       │                                          │
│       ▼                                          │
│ Agentic Workflow                                 │
│       │                                          │
│       ▼                                          │
│ GitHub Copilot                                   │
│       │                                          │
│       ▼                                          │
│ Inspect Spring Boot Repository                   │
│       │                                          │
│       ▼                                          │
│ Implement Jira Story                             │
│       │                                          │
│       ▼                                          │
│ Add / Update Tests                               │
│       │                                          │
│       ▼                                          │
│ Run Maven Tests                                  │
│       │                                          │
│       ▼                                          │
│ Create Pull Request                              │
└────────────────────────┬─────────────────────────┘
                         │
                         ▼
                    Human Review
                         │
                         ▼
                       Merge
```

---

# 52. Useful Commands Cheat Sheet

```bash
# Check GitHub CLI
gh --version

# Login
gh auth login

# Check authentication
gh auth status

# Install Agentic Workflows extension
gh extension install github/gh-aw

# Upgrade Agentic Workflows extension
gh extension upgrade github/gh-aw

# Check Agentic Workflow CLI
gh aw version

# Check environment
gh aw doctor

# Initialize repository
gh aw init

# Create workflow
gh aw new jira-developer

# Compile workflow
gh aw compile jira-developer

# Compile all workflows
gh aw compile

# Inspect MCP
gh aw mcp inspect jira-developer

# Check/setup secrets
gh aw secrets bootstrap

# Run workflow
gh aw run jira-developer

# Check Git
git status

# Commit workflow
git add .
git commit -m "Add Jira Copilot agentic workflow"
git push
```

---

# 53. Demo Troubleshooting

## `gh` command not found

Install GitHub CLI:

```bash
brew install gh
```

---

## `gh aw` command not found

Install the Agentic Workflow extension:

```bash
gh extension install github/gh-aw
```

---

## GitHub authentication problem

Run:

```bash
gh auth status
```

If necessary:

```bash
gh auth login
```

---

## Workflow does not appear under Actions

Check that the generated workflow exists:

```text
.github/workflows/jira-developer.lock.yml
```

Compile again:

```bash
gh aw compile jira-developer
```

Commit and push:

```bash
git add .
git commit -m "Compile Jira developer workflow"
git push
```

---

## Jira story cannot be found

Check:

```text
Jira connection
Rovo MCP
Jira project
Story status
copilot-ready label
Workflow search instructions
```

---

## Workflow cannot authenticate to Jira

Check repository secrets.

Run:

```bash
gh aw secrets bootstrap
```

Also inspect the MCP configuration:

```bash
gh aw mcp inspect jira-developer
```

---

## Workflow picks the wrong Jira story

Check whether multiple stories have:

```text
Status = To Do
Label = copilot-ready
```

For a controlled live demo, keep only one story eligible.

---

## Tests fail

First confirm that tests passed before the demo:

```bash
./mvnw clean test
```

This helps distinguish an existing application problem from a change introduced by the workflow.

---

# 54. Demo Reset Procedure

If the same Jira story is used repeatedly for demonstrations:

1. Close the previous pull request if required.
2. Delete the previous demo branch.
3. Restore the `main` branch to the original Book API state.
4. Move AI-7 back to `To Do`.
5. Restore the `copilot-ready` label.
6. Confirm that AI-7 has not already been implemented.
7. Run the existing Maven tests.
8. Run `gh aw doctor`.
9. Run `gh aw compile`.
10. Confirm GitHub Actions is ready.
11. Trigger the workflow again.

This provides a predictable clean starting point for every demonstration.

---

# 55. One-Minute Explanation for the Audience

> We have a normal Spring Boot Book API hosted in GitHub. Development requirements are managed in Jira.
>
> We have created a GitHub Agentic Workflow that runs through GitHub Actions and uses GitHub Copilot as its AI engine.
>
> Through Atlassian Rovo MCP, the workflow can access Jira and identify a story that has been marked as ready for Copilot.
>
> Copilot reads the requirement and acceptance criteria, examines the existing Spring Boot application, determines which code needs to change, implements the requirement and adds appropriate tests.
>
> The workflow then runs the tests and produces a pull request.
>
> The developer still reviews and approves the pull request before the code is merged.
>
> This allows us to automate development work while retaining normal engineering controls and human review.

---

# 56. Demo Success Criteria

The demonstration is successful when:

```text
✓ Workflow starts from GitHub Actions

✓ Copilot accesses Jira through Rovo MCP

✓ Correct copilot-ready Jira story is identified

✓ Story acceptance criteria are understood

✓ Existing Spring Boot code is inspected

✓ Required functionality is implemented

✓ Automated tests are added or updated

✓ Maven tests pass

✓ Pull request is created

✓ Jira story is referenced in the PR

✓ Human can review the generated change
```

That completes the end-to-end GitHub Copilot Agentic Workflow demonstration.