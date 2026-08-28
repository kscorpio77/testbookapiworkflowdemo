---
name: implement-jira-story
description: 'Implement a Jira user story or new feature for the Book REST API by reading
the Jira issue using Jira MCP, reading every relevant Confluence page linked
from the Jira story for detailed requirements, architecture guidance, API
behaviour, design decisions, or testing expectations, analysing the existing
repository, planning and implementing the smallest suitable change, adding
automated tests, running verification, creating a Git feature branch,
committing the implementation, pushing the branch, and creating a pull
request. Use this skill whenever the user asks to implement, develop, build,
complete, or work on a Jira Story or new feature'
---

# Implement Jira Story

Use this skill when implementing a new Jira Story or feature for the Book REST
API.

The standard workflow is:

Jira Story
→ Linked Confluence Documentation
→ Understand Functional Requirements
→ Understand Architecture
→ Analyse Existing Codebase
→ Create Implementation Plan
→ Create Feature Branch
→ Implement Feature
→ Add Tests
→ Run Tests
→ Verify Jira and Confluence Requirements
→ Commit
→ Push
→ Create Pull Request

Do not begin implementation until the complete available requirement has been
read.

---

# 1. Required Input

The user should normally provide a Jira issue key.

Example:

`GIG-4`

If a Jira issue key is not supplied and cannot be determined from the
conversation, ask the user for it.

---

# 2. Read the Jira Story

Use Jira MCP to retrieve the complete Jira issue.

Read all relevant information, including:

* Jira issue key
* issue type
* summary
* description
* user story
* acceptance criteria
* priority
* comments
* technical notes
* architecture notes
* attachments when relevant
* related documentation
* Confluence links

Do not begin implementation based only on the user's short prompt if Jira can
be accessed.

If Jira cannot be accessed:

1. Stop.
2. Explain that the Jira story could not be retrieved.
3. Do not guess the requirements.
4. Do not modify the source code.

---

# 3. Find Every Relevant Confluence Reference

Inspect the complete Jira issue for links or references to Confluence.

Check:

* description
* acceptance criteria
* comments
* technical notes
* architecture sections
* related documentation
* implementation guidance
* external links

Any Confluence page that appears relevant to understanding:

* the business requirement
* expected API behaviour
* system architecture
* component responsibilities
* database behaviour
* integration behaviour
* security
* validation
* testing
* design decisions

must be read before implementation begins.

Do not ignore a Confluence page because the Jira story appears understandable
without it.

The linked documentation may contain requirements that are intentionally not
duplicated in Jira.

---

# 4. Read All Relevant Confluence Pages

Use Confluence MCP to retrieve each relevant Confluence page referenced by the
Jira story.

Read enough of each page to understand the complete context.

Look specifically for:

## Functional Requirements

* business rules
* expected user behaviour
* API behaviour
* validation
* error handling
* successful scenarios
* edge cases

## Architecture

* controller responsibilities
* service responsibilities
* repository responsibilities
* database rules
* data flow
* integration boundaries
* dependencies
* system diagrams
* architectural constraints

## API Contract

* HTTP method
* URL
* path parameters
* query parameters
* request body
* response body
* HTTP status codes

## Testing Expectations

* required test scenarios
* acceptance tests
* edge cases
* regression expectations

## Scope

* functionality explicitly included
* functionality explicitly excluded

## Design Decisions

* implementation patterns
* naming rules
* compatibility requirements
* technology constraints

Treat Jira and Confluence together as the complete requirement.

---

# 5. Handling Missing Confluence Access

If the Jira story references a relevant Confluence page but it cannot be read:

1. Stop before modifying source code.
2. Clearly identify the unavailable page.
3. Explain why the missing page may affect the implementation.
4. Do not guess the contents.
5. Ask the user to restore access or provide the missing information.

Implementation must not continue when a referenced document is clearly required
to understand the requirement or architecture.

---

# 6. Build a Complete Requirement Summary

Before modifying code, summarise the requirement.

Use this structure.

## Jira Requirement

Summarise:

* what needs to be built
* the main user story
* Jira acceptance criteria

## Confluence Requirements

Summarise additional behaviour from Confluence.

Examples:

* empty input behaviour
* case sensitivity
* whitespace handling
* HTTP response codes
* error behaviour
* testing requirements

## Architecture Guidance

Summarise relevant architecture details from Confluence.

Examples:

* Controller → Service → Repository
* where business logic belongs
* where database queries belong
* components that should not be changed
* integration boundaries

## Out of Scope

List functionality specifically excluded from the implementation.

## Final Requirement

Create one clear combined understanding of what must be implemented.

Do not modify source code until this summary is complete.

---

# 7. Resolve Requirement Conflicts

If Jira and Confluence appear to contradict each other:

1. Stop the implementation.
2. Describe the conflicting requirements.
3. Identify where each requirement came from.
4. Ask the user which requirement should take precedence.

Do not silently choose one source.

---

# 8. Analyse the Existing Codebase

Inspect the Book REST API repository.

Understand the current implementation before changing files.

Inspect relevant components such as:

* BookController
* BookService
* BookServiceImpl
* BookRepository
* Book entity
* validation
* exception handling
* DTOs if present
* automated tests
* pom.xml
* application configuration

Understand the existing flow.

Typical flow:

HTTP Request
→ Controller
→ Service
→ Repository
→ Database

Compare the existing application architecture with any architecture documented
in Confluence.

Follow the existing architecture unless the Jira or Confluence requirement
explicitly asks for a change.

---

# 9. Identify the Minimum Required Changes

Determine exactly which files need to change.

Do not automatically change all application layers.

For each planned file, explain why it needs to change.

Example:

BookRepository

* add partial case-insensitive title lookup

BookService

* expose title search operation

BookServiceImpl

* validate and normalize search text

BookController

* expose GET search endpoint

Tests

* cover Jira acceptance criteria and Confluence edge cases

Avoid unrelated refactoring.

---

# 10. Create an Implementation Plan

Before modifying files, produce a short plan.

Include:

* Jira key
* feature being implemented
* Confluence pages used
* files likely to change
* new methods or endpoints
* tests required
* architecture rules being followed
* anything explicitly out of scope

Keep the plan focused.

---

# 11. Check Git Status

Before source-code changes:

1. check the current branch
2. check for uncommitted changes
3. identify the default branch
4. identify the remote repository

Do not overwrite unrelated user work.

If unrelated local changes could interfere with the implementation, stop and
inform the user.

Never implement directly on:

* main
* master

---

# 12. Create a Feature Branch

Create a branch based on the Jira key.

Preferred format:

`feature/<jira-key>-<short-description>`

Example:

`feature/GIG-4-search-books-by-title`

Use a concise lowercase description.

Do not:

* force push
* rewrite history
* delete existing branches
* discard unrelated changes

---

# 13. Implement the Feature

Implement the smallest clean solution that satisfies the combined Jira and
Confluence requirement.

Rules:

* follow existing project architecture
* follow Confluence architecture guidance
* follow existing coding conventions
* reuse existing patterns
* avoid unnecessary dependencies
* do not introduce a new framework without a requirement
* do not refactor unrelated code
* preserve existing functionality
* do not implement features listed as out of scope
* prefer a simple and maintainable solution

---

# 14. REST API Rules

For API-related stories, verify all applicable behaviour.

## Request

Check:

* HTTP method
* URL
* path variables
* query parameters
* request body
* required fields
* optional fields

## Response

Check:

* HTTP status
* response body
* empty-result behaviour
* error responses
* validation messages

## Search Behaviour

When relevant, check:

* exact matching
* partial matching
* case sensitivity
* whitespace
* multiple results
* no results

Do not invent behaviour that is not supported by Jira or Confluence.

---

# 15. Follow Architecture Documentation

If Confluence contains architecture documentation, implementation must respect
it.

For example:

If Confluence says:

Controller
→ Service
→ Repository

then:

* controller should handle HTTP concerns
* service should contain business behaviour
* repository should handle database access

Do not move database queries into controllers.

Do not bypass required layers solely because it creates less code.

If the existing codebase differs from the documented architecture, compare the
two and choose the smallest change that respects the requirement.

If a major architecture change appears necessary, explain this before
proceeding.

---

# 16. Add Automated Tests

Tests must cover the complete requirement, not only the short Jira acceptance
criteria.

Use:

1. Jira acceptance criteria
2. Confluence functional rules
3. Confluence testing requirements
4. important affected edge cases

For a title-search story, examples might include:

* exact search
* partial search
* lowercase search
* uppercase search
* mixed-case search
* multiple matches
* no matches
* blank search
* missing search parameter
* leading spaces
* trailing spaces

Use the existing test framework and style.

Do not remove or weaken existing tests.

---

# 17. Run Verification

Run the project tests.

Prefer:

`./mvnw test`

If the wrapper is unavailable:

`mvn test`

When appropriate:

`mvn clean test`

Verify:

* new tests pass
* existing tests pass
* application compiles
* unrelated behaviour remains intact

If a test fails:

1. investigate
2. determine the cause
3. correct the implementation where appropriate
4. rerun tests

Do not claim success without actual successful test execution.

---

# 18. Verify the Complete Requirement

Before committing, compare the implementation against all sources.

## Jira Acceptance Criteria

For each criterion:

* satisfied
* not satisfied

## Confluence Functional Requirements

For each relevant requirement:

* satisfied
* not satisfied

## Architecture

Confirm documented architecture has been followed.

## Testing Requirements

Confirm required scenarios have tests.

## Out of Scope

Confirm excluded functionality was not accidentally implemented.

Do not continue to commit while required items remain unsatisfied.

---

# 19. Review the Git Diff

Inspect the final changes.

Confirm:

* only required files changed
* no secrets were added
* no credentials were added
* no temporary debug code remains
* no commented experimental code remains
* no unrelated formatting changes exist
* architecture guidance was respected
* tests are relevant
* implementation matches Jira and Confluence

---

# 20. Commit the Feature

Create one focused commit unless multiple commits are genuinely useful.

Preferred format:

`feat(<jira-key>): <short feature description>`

Example:

`feat(GIG-4): add book title search`

The commit message must reference the Jira issue.

Avoid messages such as:

* changes
* update
* implemented stuff

---

# 21. Push the Feature Branch

Push the feature branch to the configured GitHub remote.

Do not push directly to the default branch.

Confirm that the branch exists remotely.

---

# 22. Create Pull Request

Create a pull request using available GitHub tooling.

Target the repository's normal default branch.

Preferred title:

`[GIG-4] Add book title search`

Use this general PR structure.

## Jira Story

`GIG-4`

## Requirement

Summarise the feature.

## Confluence Documentation

List every relevant Confluence page used to understand the requirement or
architecture.

## Implementation

Explain the changes made.

## Architecture

Explain how the implementation follows relevant architecture guidance.

## Tests

List tests added or updated.

Include the actual test result.

## Jira Acceptance Criteria

Confirm each criterion.

## Additional Confluence Requirements

Confirm relevant requirements found in Confluence.

## Out of Scope

Confirm excluded functionality was not added.

---

# 23. Do Not Merge Automatically

Creating the pull request completes this workflow.

Do not automatically:

* merge the PR
* delete the branch
* transition Jira to Done
* close Jira
* modify Confluence

Those actions require a separate explicit instruction.

---

# 24. Final Response

Report:

* Jira story
* Confluence pages read
* key additional requirements discovered in Confluence
* architecture guidance followed
* files changed
* tests added or updated
* test result
* branch
* commit ID
* pull request

Keep the summary concise.

---

# Completion Rule

The story is complete only when all applicable conditions are satisfied:

Jira story read
AND
all relevant linked Confluence pages read
AND
functional requirements understood
AND
architecture requirements understood
AND
repository analysed
AND
implementation planned
AND
feature branch created
AND
feature implemented
AND
tests added
AND
tests passing
AND
Jira acceptance criteria verified
AND
Confluence requirements verified
AND
architecture guidance verified
AND
commit created
AND
branch pushed
AND
pull request created

If any required stage cannot be completed, explain exactly where the workflow
stopped and why.
