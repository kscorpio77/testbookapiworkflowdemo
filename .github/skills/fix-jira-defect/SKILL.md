---
name: fix-jira-defect
description: 'Diagnose and fix a software defect for the Book REST API by reading the Jira
defect using Jira MCP, reading any linked Confluence pages for additional
requirements, architecture guidance, API behaviour, or technical context,
analysing the existing codebase, identifying the root cause, implementing
the smallest safe fix, adding or updating tests, running verification,
creating a Git branch, committing the fix, pushing the branch, and creating
a pull request. Use this skill whenever the user asks to fix, investigate,
debug, resolve, or work on a Jira Bug or defect.'
---


# Fix Jira Defect

Use this skill when investigating and fixing an existing defect in the Book REST API.

The defect workflow is:

Jira Defect
→ Linked Confluence Documentation
→ Understand Requirement and Architecture
→ Analyse Existing Code
→ Reproduce Defect
→ Identify Root Cause
→ Create Branch
→ Implement Fix
→ Add Tests
→ Run Tests
→ Verify Requirements
→ Commit
→ Push
→ Create Pull Request

Do not make unrelated changes.

---

# 1. Required Input

The user should normally provide a Jira issue key.

Example:

`GIG-8`

If the user has not provided a Jira issue key and it cannot be determined from
the conversation, ask for it.

---

# 2. Read the Jira Defect

Use the available Jira MCP tools to retrieve the complete Jira issue.

Read all relevant information, including:

* Jira issue key
* issue type
* summary
* description
* acceptance criteria
* steps to reproduce
* actual result
* expected result
* priority
* comments when relevant
* linked documentation
* attachments when relevant
* Confluence links

Do not rely only on the user's description if the Jira issue can be retrieved.

If Jira cannot be accessed:

1. Stop the workflow.
2. Explain that the Jira defect could not be retrieved.
3. Do not guess the requirements.
4. Do not modify the source code.

---

# 3. Detect Confluence References

After reading the Jira issue, inspect the entire Jira content for Confluence
references.

Look for Confluence URLs or references in:

* description
* acceptance criteria
* comments
* related documentation
* technical notes
* architecture sections
* implementation notes
* linked resources

If one or more Confluence pages are referenced, they MUST be read before
changing any code.

Do not treat Confluence links as optional documentation.

---

# 4. Read Linked Confluence Pages

Use the available Confluence MCP tools to open every relevant Confluence page
referenced by the Jira defect.

Read the pages carefully to identify additional information such as:

* functional requirements
* technical requirements
* API contracts
* expected HTTP responses
* error behaviour
* validation rules
* architecture guidance
* controller/service/repository responsibilities
* database behaviour
* integration details
* security requirements
* testing expectations
* non-functional requirements
* out-of-scope behaviour
* coding constraints
* design decisions
* known limitations

Treat the Jira issue and relevant Confluence documentation together as the
complete source of requirements.

If a linked Confluence page cannot be accessed:

1. Stop before changing code.
2. Tell the user which Confluence page could not be read.
3. Explain that the complete requirement or architecture context is therefore
   unavailable.
4. Do not guess what the missing document contains.

---

# 5. Summarise the Complete Defect

Before modifying any source code, provide a brief summary containing:

## Jira Defect

Explain:

* what is broken
* actual behaviour
* expected behaviour

## Confluence Context

If Confluence documentation exists, summarise the additional information found
there.

Include anything that changes or clarifies:

* expected behaviour
* architecture
* API design
* validation
* testing
* implementation constraints

## Final Understanding

State the complete behaviour that the implementation must satisfy.

Do not modify code until this understanding is clear.

---

# 6. Analyse the Existing Repository

Inspect the existing Book REST API repository.

Understand the current architecture, coding style, and implementation before
changing anything.

Inspect relevant files such as:

* BookController
* BookService
* BookServiceImpl
* BookRepository
* Book entity
* exception handling
* validation
* tests
* pom.xml
* application configuration

Do not automatically modify every layer.

Only change files that are required to fix the defect.

Compare the current implementation with both:

1. Jira requirements
2. Confluence documentation

---

# 7. Identify the Defective Execution Path

Trace how the defective behaviour happens.

For example:

Request
→ Controller
→ Service
→ Repository
→ Database
→ Response

Identify where actual behaviour starts differing from expected behaviour.

Do not fix symptoms without understanding the cause.

---

# 8. Reproduce or Confirm the Defect

Before applying the fix, reproduce or confirm the defect whenever practical.

Use one or more of the following:

* existing automated test
* new failing test
* application execution
* API request
* code-path inspection

Record the behaviour that demonstrates the defect.

Do not claim a defect was reproduced unless it was actually reproduced or
confirmed through code analysis.

---

# 9. Identify the Root Cause

Clearly identify why the defect occurs.

Explain the root cause in simple terms.

Example:

The controller calculates a 404 response when the book is missing, but the
method always returns 204 at the end, so the 404 response is ignored.

The root cause must be understood before implementation begins.

---

# 10. Check Git Status

Before modifying files:

1. Check the current Git branch.
2. Check for uncommitted changes.
3. Identify the repository default branch.
4. Check the configured remote.

Do not overwrite unrelated work.

If unrelated uncommitted changes could interfere with the defect fix, stop and
tell the user.

Never implement the defect directly on:

* main
* master

---

# 11. Create a Defect Branch

Create a branch using the Jira issue key.

Preferred format:

`bugfix/<jira-key>-<short-description>`

Example:

`bugfix/GIG-8-delete-nonexistent-book`

Use a short lowercase description.

Do not:

* force push
* rewrite Git history
* delete branches
* discard unrelated work

---

# 12. Implement the Fix

Make the smallest appropriate change that fixes the root cause and satisfies
the complete requirement from Jira and Confluence.

Rules:

* follow the existing architecture
* follow existing coding conventions
* do not refactor unrelated code
* avoid introducing unnecessary dependencies
* do not introduce new frameworks unless required
* preserve existing API behaviour unless the defect requires a change
* do not implement functionality that is explicitly out of scope
* prefer simple solutions
* keep backward compatibility where possible

If Confluence defines an architecture or implementation rule, follow it unless
it conflicts with the Jira requirement.

If Jira and Confluence appear to contradict one another:

1. Stop.
2. Explain the conflict.
3. Ask the user which requirement should take precedence.

Do not silently choose one.

---

# 13. Add Automated Tests

Every defect fix should include a regression test whenever practical.

The regression test should:

* demonstrate the defective behaviour before the fix
* pass after the fix

Also preserve tests for successful behaviour.

Tests should cover applicable requirements from both Jira and Confluence.

Possible scenarios include:

* expected successful response
* defective scenario
* missing data
* invalid input
* error response
* boundary cases

Do not remove, disable, or weaken existing tests simply to make the build pass.

---

# 14. Run Verification

Run the project's tests.

Prefer:

`./mvnw test`

If the Maven wrapper does not exist:

`mvn test`

If appropriate:

`mvn clean test`

Verify:

* new regression test passes
* related tests pass
* existing tests pass
* application compiles

If tests fail:

1. investigate the failure
2. determine whether it is related to the fix
3. correct the implementation where appropriate
4. run the tests again

Do not claim tests passed unless they were actually executed successfully.

Do not commit a known broken implementation.

---

# 15. Verify Against Jira and Confluence

Before committing, verify the implementation against the full requirement.

Create a short verification checklist.

## Jira

For each acceptance criterion:

* satisfied
* not satisfied

## Confluence

For each relevant additional rule:

* satisfied
* not satisfied

## Architecture

Confirm that any architectural guidance documented in Confluence was followed.

## Out of Scope

Confirm that unrelated functionality was not introduced.

If something is not satisfied, continue working before committing.

---

# 16. Review the Git Diff

Review all changed files.

Confirm:

* only necessary files changed
* no secrets were added
* no credentials were added
* no debug statements remain
* no temporary code remains
* no unrelated formatting changes were introduced
* tests cover the defect
* Confluence architecture guidance was respected

---

# 17. Commit the Fix

Create one focused commit unless there is a clear reason for multiple commits.

Preferred format:

`fix(<jira-key>): <short defect description>`

Example:

`fix(GIG-8): return 404 when deleting missing book`

The commit message must contain the Jira issue key.

Avoid vague messages such as:

* fix bug
* changes
* updates

---

# 18. Push the Branch

Push the defect branch to the configured remote.

Never push directly to the default branch.

Confirm the remote branch exists.

---

# 19. Create the Pull Request

Use available GitHub tooling to create a pull request.

Target the repository's normal default branch.

Preferred title format:

`[GIG-8] Return 404 when deleting a missing book`

The pull request description should contain:

## Jira Defect

Include the Jira issue key.

## Confluence Documentation

Include relevant Confluence page links if the Jira defect referenced them.

## Problem

Explain the observed defect.

## Root Cause

Explain why the defect happened.

## Fix

Explain what was changed.

## Architecture

Mention any important Confluence architecture guidance followed.

## Testing

List tests added or updated.

State the actual test result.

## Jira Acceptance Criteria

Show how the Jira criteria were satisfied.

## Confluence Requirements

Show how relevant Confluence requirements were satisfied.

Do not claim tests passed unless they actually passed.

---

# 20. Do Not Merge Automatically

Creating the pull request completes the source-control workflow.

Do not automatically:

* merge the pull request
* delete the branch
* close the Jira issue
* transition Jira to Done
* modify Confluence documentation

These actions require a separate explicit user request.

---

# 21. Final Response

Provide a concise final summary containing:

* Jira issue
* Confluence pages used
* root cause
* files changed
* tests added or updated
* test result
* branch
* commit ID
* pull request

Also report any step that could not be completed.

---

# Completion Rule

The defect is complete only when all applicable steps succeed:

Jira defect read
AND
Confluence documentation read when referenced
AND
complete requirements understood
AND
architecture understood when documented
AND
repository analysed
AND
defect reproduced or confirmed
AND
root cause identified
AND
branch created
AND
fix implemented
AND
tests added or updated
AND
tests passing
AND
Jira requirements verified
AND
Confluence requirements verified
AND
commit created
AND
branch pushed
AND
pull request created

If any required stage cannot be completed, explain exactly where the workflow
stopped and why.
