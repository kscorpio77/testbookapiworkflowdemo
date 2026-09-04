# GitHub Copilot Automation Demo: Jira → Code → Tests → Pull Request

> **Purpose:** A complete classroom/demo setup for connecting GitHub
> Copilot Cloud Agent/Automations to Jira and Confluence through the
> Atlassian Rovo MCP Server, then using an automation to read a Jira
> story, move it through the workflow, implement the story in a Spring
> Boot repository, run tests, push a feature branch, create a pull
> request, and move the Jira story to **In Review**.
>
> **Demo repository used in this guide:**
> `kscorpio77/spring-boot-book-api-demo`\
> **Demo Jira site:** `https://gigtech.atlassian.net`\
> **Demo Jira story:** `AI-3`\
> **Demo Cloud ID:** `0f24971e-0448-471e-88ae-b8a2cdb545fd`
>
> **Target workflow:**
> `To Do → In Progress → Code → Tests → Push → Pull Request → In Review`
>
> **Important:** Never put an Atlassian API token, Base64 credential,
> password, or other secret directly in this README, the automation
> prompt, or the repository.

------------------------------------------------------------------------

## 1. What the Demo Shows

The final demo demonstrates this flow:

``` text
Jira AI-3 = To Do
        |
        v
GitHub Copilot Automation starts
        |
        v
Atlassian Rovo MCP starts
        |
        v
Get accessible Atlassian resources
        |
        v
Read Jira AI-3
        |
        v
Check existing GitHub branch / PR
        |
        v
Jira: To Do → In Progress
        |
        v
Copilot analyses Spring Boot repository
        |
        v
Implement acceptance criteria
        |
        v
Add / update automated tests
        |
        v
Run Maven tests
        |
        v
Create feature/AI-3
        |
        v
Commit + Push
        |
        v
Create OPEN Pull Request
        |
        v
Jira: In Progress → In Review
        |
        v
Add PR URL to Jira, if available
        |
        v
STOP — Human reviews and merges
```

The automation must **never automatically merge the PR** and must
**never automatically move AI-3 to Done**.

------------------------------------------------------------------------

# PART A --- PREREQUISITES

## 2. GitHub Prerequisites

Before configuring MCP, confirm:

1.  You have a paid GitHub Copilot plan that supports Copilot Cloud
    Agent/Automations.
2.  The repository is private or internal if required by the current
    GitHub Automations availability rules.
3.  GitHub Copilot Cloud Agent is enabled for the repository.
4.  Automations are enabled for the repository/organization.
5.  You have repository administrator access to configure MCP servers
    and Agent secrets.
6.  GitHub Actions is enabled.
7.  The repository builds successfully before the demo.

For this demo:

``` text
Repository:
kscorpio77/spring-boot-book-api-demo
```

GitHub documentation:

-   https://docs.github.com/en/copilot/how-tos/use-copilot-agents/cloud-agent/create-automations
-   https://docs.github.com/en/copilot/concepts/agents/cloud-agent/about-automations

------------------------------------------------------------------------

## 3. Atlassian Prerequisites

You need:

1.  A Jira Cloud site.
2.  An Atlassian account that can access the Jira project/story.
3.  Permission to read the Jira story.
4.  Permission to transition the Jira story.
5.  Permission to add Jira comments if the demo will add the PR URL.
6.  Confluence access if the Jira story links to Confluence
    requirements.
7.  Atlassian Rovo MCP API-token authentication enabled by your
    organization.

For this demo:

``` text
Jira site:
https://gigtech.atlassian.net

Story:
AI-3
```

------------------------------------------------------------------------

# PART B --- ENABLE ATLASSIAN ROVO MCP

## 4. Enable API-Token Authentication for Rovo MCP

If you are an Atlassian organization administrator:

1.  Open:

    `https://admin.atlassian.com`

2.  Select the correct Atlassian organization.

3.  Open **Rovo**.

4.  Open **Rovo MCP server**.

5.  Open **Authentication**.

6.  Enable **API token authentication**.

For this demo, API-token authentication must be enabled because the
GitHub Copilot cloud setup will authenticate to Atlassian
non-interactively.

Atlassian documentation:

https://support.atlassian.com/security-and-access-policies/docs/control-atlassian-rovo-mcp-server-settings/

------------------------------------------------------------------------

## 5. Configure Rovo MCP Permission Groups

In Atlassian Administration:

1.  Go to **Rovo → Rovo MCP server**.
2.  Review the MCP permission groups.
3.  Enable the minimum permissions required by the demo.

The useful groups are:

``` text
Jira:
Read
Write
Search

Confluence:
Read
Search
```

For the demo, keep destructive/admin capabilities disabled unless there
is a specific reason to use them.

Recommended:

``` text
Read       = Enabled
Write      = Enabled where required
Search     = Enabled

Delete     = Disabled
Manage     = Disabled
```

The Jira write capability is required because the automation needs to
transition:

``` text
To Do → In Progress
In Progress → In Review
```

It may also add a Jira comment containing the GitHub PR URL.

Atlassian Rovo MCP v2 exposes Jira tools such as:

``` text
getJiraIssue
listJiraIssueTransitions
transitionJiraIssue
addOrEditJiraIssueComment
searchJiraIssuesUsingJql
```

Official supported-tools documentation:

https://support.atlassian.com/atlassian-ai-gateway/docs/supported-tools/

------------------------------------------------------------------------

# PART C --- FIND THE ATLASSIAN CLOUD ID

## 6. Find the Cloud ID

The **Cloud ID is not the Organization ID**.

The easiest method is the Atlassian `tenant_info` endpoint.

For a site:

``` text
https://<your-site>.atlassian.net
```

open:

``` text
https://<your-site>.atlassian.net/_edge/tenant_info
```

For this demo:

``` text
https://gigtech.atlassian.net/_edge/tenant_info
```

The browser returns JSON similar to:

``` json
{
  "cloudId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
}
```

Copy the value of `cloudId`.

For this demo the value obtained was:

``` text
0f24971e-0448-471e-88ae-b8a2cdb545fd
```

Keep this value. It is used by Jira MCP operations.

You can also find the Cloud ID from Atlassian Administration. In many
Atlassian admin URLs, the identifier after `/s/` is the site's Cloud ID.

Official Atlassian reference:

https://support.atlassian.com/jira/kb/retrieve-my-atlassian-sites-cloud-id/

------------------------------------------------------------------------

# PART D --- CREATE THE ATLASSIAN API TOKEN

## 7. Create an Atlassian API Token

Open:

``` text
https://id.atlassian.com/manage-profile/security/api-tokens
```

Then:

1.  Sign in using the Atlassian account that has access to the Jira
    project.
2.  Select **Create API token with scopes**.
3.  Give the token a descriptive name.

Example:

``` text
GitHub-Copilot-Rovo-MCP
```

4.  Select an expiry date appropriate for the demo/environment.
5.  Select the required application/scopes.
6.  Create the token.
7.  **Copy the token immediately.**

Atlassian does not let you retrieve the token value later.

Store it temporarily in a password manager or another secure location
while completing the setup.

Official reference:

https://support.atlassian.com/atlassian-account/docs/manage-api-tokens-for-your-atlassian-account

------------------------------------------------------------------------

## 8. Token Scopes for This Demo

For the Rovo MCP v2 workflow, use least privilege.

The important Jira scopes are:

``` text
read:jira:agent-interface
search:jira:agent-interface
write:jira:agent-interface
```

If the automation needs to read/search Confluence pages linked from
Jira, also use:

``` text
read:confluence:agent-interface
search:confluence:agent-interface
```

The setup used during this demo also included:

``` text
read:account
```

Therefore a practical scope set for this classroom example is:

``` text
read:account
read:jira:agent-interface
search:jira:agent-interface
write:jira:agent-interface
read:confluence:agent-interface
search:confluence:agent-interface
```

Do **not** add Jira Delete or Manage scopes merely for this demo.

According to the Rovo MCP v2 tool catalogue:

``` text
getJiraIssue
    requires read:jira:agent-interface

transitionJiraIssue
    requires write:jira:agent-interface

addOrEditJiraIssueComment
    requires write:jira:agent-interface

searchJiraIssuesUsingJql
    requires search:jira:agent-interface

getConfluenceContent
    requires read:confluence:agent-interface

searchConfluence
    requires search:confluence:agent-interface
```

Always verify the current Atlassian documentation because MCP
scopes/tool availability can evolve:

https://support.atlassian.com/atlassian-ai-gateway/docs/supported-tools/

------------------------------------------------------------------------

# PART E --- BUILD THE BASIC AUTH VALUE

## 9. Understand the Authentication Format

Atlassian API-token authentication for Rovo MCP uses HTTP Basic
authentication.

The credential that must be Base64 encoded is:

``` text
ATLASSIAN_EMAIL:ATLASSIAN_API_TOKEN
```

For example:

``` text
your.email@example.com:YOUR_API_TOKEN
```

**Do not Base64-encode only the token.**

The email and token together, separated by a single colon, are encoded.

------------------------------------------------------------------------

## 10. Base64 Encode the Credential

### macOS / Linux

Run:

``` bash
printf '%s' 'your.email@example.com:YOUR_API_TOKEN' | base64
```

The command returns a Base64 string.

Copy the output.

### Security warning

Do not:

-   commit this value to Git
-   put it in `README.md`
-   put it directly in the automation prompt
-   put it directly into a public screenshot
-   paste it into chat
-   store it as an ordinary repository variable

Treat the Base64 value as a secret because it can be decoded back to the
email/token combination.

Atlassian reference:

https://support.atlassian.com/atlassian-ai-gateway/docs/configure-authentication-via-api-token/

------------------------------------------------------------------------

# PART F --- CREATE THE GITHUB COPILOT AGENT SECRET

## 11. Create an Agents Secret

GitHub Copilot Cloud Agent uses a dedicated **Agents** secret store.

Do not create this only as a normal GitHub Actions secret.

In the GitHub repository:

1.  Open the repository.
2.  Click **Settings**.
3.  In the left sidebar, find **Secrets and variables**.
4.  Click **Agents**.
5.  Select the **Secrets** tab.
6.  Click **New repository secret**.

Create:

``` text
Name:
COPILOT_MCP_ATLASSIAN_API_KEY
```

For the value, paste the Base64 value generated in the previous step.

Example conceptually:

``` text
COPILOT_MCP_ATLASSIAN_API_KEY
        =
Base64(ATLASSIAN_EMAIL:ATLASSIAN_API_TOKEN)
```

Never put the literal secret value in this README.

### Why the name starts with `COPILOT_MCP_`

GitHub requires secrets/variables referenced by repository MCP
configuration to use the `COPILOT_MCP_` prefix.

Official GitHub documentation:

https://docs.github.com/en/copilot/how-tos/copilot-on-github/customize-copilot/customize-cloud-agent/configure-secrets-and-variables

------------------------------------------------------------------------

# PART G --- CONFIGURE ATLASSIAN MCP IN GITHUB COPILOT

## 12. Open Repository MCP Settings

On GitHub:

1.  Open:

    `kscorpio77/spring-boot-book-api-demo`

2.  Click **Settings**.

3.  In the sidebar, under **Code, planning, and automation**, click
    **Copilot**.

4.  Click **MCP servers**.

5.  Locate **MCP configuration**.

This is the repository-level MCP configuration used by GitHub Copilot
Cloud Agent.

> Note: In GitHub's web interface this is a JSON configuration entered
> in **Settings → Copilot → MCP servers**. It does not have to be a
> committed `.vscode/mcp.json` file. VS Code can also use an `mcp.json`,
> but that is a separate client-side configuration.

Official GitHub reference:

https://docs.github.com/en/copilot/how-tos/copilot-on-github/customize-copilot/configure-mcp-servers

------------------------------------------------------------------------

## 13. Paste the MCP Configuration

The configuration that was successfully used for this demo is:

``` json
{
  "mcpServers": {
    "atlassian-rovo-mcp": {
      "command": "npx",
      "type": "local",
      "tools": ["*"],
      "args": [
        "mcp-remote@latest",
        "https://mcp.atlassian.com/v2/mcp",
        "--header",
        "Authorization: Basic $ATLASSIAN_API_KEY"
      ],
      "env": {
        "ATLASSIAN_API_KEY": "$COPILOT_MCP_ATLASSIAN_API_KEY"
      }
    }
  }
}
```

Then click:

``` text
Save MCP configuration
```

### Important details

Use:

``` text
https://mcp.atlassian.com/v2/mcp
```

For this demonstrated setup, do **not** use the old v1 endpoint.

Also, during troubleshooting we tested:

``` text
https://mcp.atlassian.com/v2/mcp?tools=all
```

but that configuration did not behave reliably for this particular
GitHub Copilot demonstration. The working configuration returned to:

``` text
https://mcp.atlassian.com/v2/mcp
```

The `tools` setting:

``` json
"tools": ["*"]
```

is convenient for a classroom demo. For a production implementation,
replace broad access with a least-privilege allowlist when practical.

------------------------------------------------------------------------

## 14. How Secret Substitution Works

This part is important to explain during the demo.

The MCP configuration contains:

``` json
"env": {
  "ATLASSIAN_API_KEY": "$COPILOT_MCP_ATLASSIAN_API_KEY"
}
```

GitHub resolves:

``` text
$COPILOT_MCP_ATLASSIAN_API_KEY
```

from the **Agents secret**.

It then exposes that secret to the MCP process as:

``` text
ATLASSIAN_API_KEY
```

The MCP command uses it here:

``` text
Authorization: Basic $ATLASSIAN_API_KEY
```

Therefore:

``` text
Atlassian token
      |
      v
email:token
      |
      v
Base64 encode
      |
      v
GitHub Agents Secret
COPILOT_MCP_ATLASSIAN_API_KEY
      |
      v
MCP env mapping
ATLASSIAN_API_KEY
      |
      v
Authorization: Basic ...
      |
      v
Atlassian Rovo MCP v2
```

The actual credential never needs to appear in the automation prompt.

------------------------------------------------------------------------

# PART H --- VERIFY MCP BEFORE BUILDING THE FULL AUTOMATION

## 15. Create a Jira Diagnostic Automation First

This was an important lesson from the demo setup.

Before creating the large development workflow, create a tiny diagnostic
automation.

Go to:

``` text
Repository
→ Agents
→ Automations
→ Create new
```

Name:

``` text
JIRA Diagnostic
```

Use a manual trigger for initial testing.

Use the default/normal Copilot agent.

For this diagnostic, no GitHub write tools are required.

Paste:

``` text
This is a diagnostic test only.

Use the configured Atlassian Rovo MCP server.

First retrieve the accessible Atlassian resources.

Confirm access to:
https://gigtech.atlassian.net

Use Atlassian Cloud ID:
0f24971e-0448-471e-88ae-b8a2cdb545fd

Then retrieve Jira issue AI-3.

Do NOT modify Jira.
Do NOT transition AI-3.
Do NOT modify repository files.
Do NOT create a branch.
Do NOT commit or push.
Do NOT create a pull request.

Return:

Atlassian MCP Available:
Story ID:
Title:
Current Status:
Acceptance Criteria:
Result:
```

Save and select **Run now**.

------------------------------------------------------------------------

## 16. Expected Successful Diagnostic Output

A healthy session should show a sequence similar to:

``` text
Setting up environment
Clone repository
Start 'runtime-tools' MCP server
Start 'github-mcp-server' MCP server
Start 'atlassian-rovo-mcp' MCP server
Start 'playwright' MCP server

Get accessible Atlassian resources
Get Jira issue
```

Then output similar to:

``` text
Atlassian MCP Available: Yes
Story ID: AI-3
Title: Add API to search books by author
Current Status: To Do
```

For this demo, AI-3's acceptance criteria were:

``` text
GET /api/books/author/{author}
Case-insensitive search
Return all matching books
Return an empty list when no matches exist
Add unit tests
```

If this diagnostic fails, **do not proceed to the full development
automation**.

Fix MCP connectivity first.

------------------------------------------------------------------------

# PART I --- PREPARE JIRA FOR THE DEMO

## 17. Jira Story State

For the clean classroom demonstration, set:

``` text
AI-3 = To Do
```

The Jira workflow used in the demo is:

``` text
To Do
  |
  v
In Progress
  |
  v
In Review
  |
  v
Done
```

The automation handles:

``` text
To Do → In Progress → In Review
```

A human handles:

``` text
In Review → Done
```

------------------------------------------------------------------------

## 18. Check for Old GitHub Demo Artifacts

Before the live demo, check that there is no stale:

``` text
feature/AI-3
```

branch and no existing open AI-3 pull request.

This prevents the automation from correctly detecting duplicate work and
stopping before the audience sees the full workflow.

Do not delete real work merely for the demo. Only clean up artifacts
that you know came from previous disposable demo runs.

------------------------------------------------------------------------

# PART J --- CREATE THE DEVELOPMENT AUTOMATION

## 19. Create the Automation

In GitHub:

``` text
Repository
→ Agents
→ Automations
→ Create new
```

Suggested name:

``` text
AI-3 Development Workflow
```

or:

``` text
SleepWhileWorking
```

For the classroom demonstration, start with a **Manual** trigger.

After the demo works reliably, you can change/add a schedule such as
Daily.

GitHub supports manual/on-demand execution and scheduled automations.

------------------------------------------------------------------------

## 20. Select the Repository

Select:

``` text
kscorpio77/spring-boot-book-api-demo
```

Use the same/default agent configuration that worked in
`JIRA Diagnostic`.

If a custom agent or skill changes tool availability, test without it
first.

------------------------------------------------------------------------

## 21. Configure GitHub Automation Tools

The Atlassian Jira operations come from the repository Rovo MCP
configuration.

The GitHub repository actions are controlled by the automation's GitHub
tools.

For the development demo, enable the minimum GitHub capabilities needed
for:

``` text
Repository:
- read/search as required
- Commit/Push Changes

Pull Requests:
- Read
- Search
- Create PR
```

Do **not** enable automatic PR merge capability for this demonstration.

The important write capabilities are:

``` text
Commit/Push Changes
Create PR
```

GitHub documentation states that the tools selected for an automation
determine what actions Copilot can perform. Use least privilege.

------------------------------------------------------------------------

# PART K --- COPY/PASTE DEVELOPMENT AUTOMATION PROMPT

## 22. Final Automation Prompt

Paste the following into the automation prompt.

``` text
Act as a senior Java Spring Boot developer.

This automation implements Jira story AI-3 in the configured
GitHub repository.

Use the configured Atlassian Rovo MCP server for Jira operations
and the available GitHub tools for repository and pull-request
operations.

==================================================
ATLASSIAN CONFIGURATION
==================================================

Jira site:

https://gigtech.atlassian.net

Atlassian Cloud ID:

0f24971e-0448-471e-88ae-b8a2cdb545fd

Whenever an Atlassian MCP operation requires a cloudId, use exactly:

0f24971e-0448-471e-88ae-b8a2cdb545fd

Process ONLY Jira story:

AI-3

Never process or modify another Jira issue.

==================================================
STEP 1 — INITIALIZE ATLASSIAN AND READ AI-3
==================================================

Use the configured Atlassian Rovo MCP server.

First retrieve the accessible Atlassian resources.

Confirm access to:

https://gigtech.atlassian.net

Then retrieve Jira issue AI-3.

Read:

- Story ID
- Title
- Current status
- Description
- Acceptance criteria
- Comments
- Priority
- Linked documentation

If relevant Confluence documentation is explicitly linked from AI-3,
read it using Atlassian Rovo MCP before implementation.

If no Confluence documentation is linked, continue using the
requirements in AI-3.

Do not guess missing requirements.

Do not report Atlassian MCP as unavailable without first attempting:

1. Atlassian resource discovery
2. Jira issue retrieval

If AI-3 cannot be retrieved after those attempts, STOP and report
the problem.

==================================================
STEP 2 — PREVENT DUPLICATE WORK
==================================================

Before starting implementation, check GitHub for:

- an existing branch named feature/AI-3
- an existing OPEN pull request from feature/AI-3
- an existing OPEN pull request whose title starts with "AI-3:"

If an OPEN pull request already exists for AI-3:

STOP.

Do not create another implementation, branch, commit, or pull request.

Report the existing pull request.

==================================================
STEP 3 — CHECK JIRA STATUS
==================================================

If AI-3 is:

Done
or
In Review

STOP.

Do not implement the story again.

If AI-3 is:

In Progress

check whether existing GitHub work exists for AI-3.

Do not create duplicate work.

Report the existing state and STOP.

Only start a NEW implementation when the current Jira status is:

To Do

==================================================
STEP 4 — MOVE AI-3 TO IN PROGRESS
==================================================

Before changing source code, transition AI-3:

To Do → In Progress

After performing the transition, retrieve AI-3 again.

Verify:

Current Status = In Progress

If the transition fails or AI-3 cannot be verified as In Progress:

STOP.

Do not modify source code.
Do not create a branch.
Do not commit.
Do not push.
Do not create a pull request.

==================================================
STEP 5 — CREATE FEATURE BRANCH
==================================================

Create and work only on:

feature/AI-3

Never commit directly to the default branch.

==================================================
STEP 6 — IMPLEMENT AI-3
==================================================

Analyse the existing Spring Boot Book API repository.

Understand the existing:

- Controller
- Service
- Repository
- Model
- Tests

Implement ALL acceptance criteria retrieved from AI-3.

Do not make unrelated changes.

Follow the repository's existing:

- architecture
- coding conventions
- naming conventions
- project structure

Prefer the smallest clean implementation necessary to satisfy
the Jira story.

==================================================
STEP 7 — TEST
==================================================

Add or update the automated tests required by AI-3.

Run all relevant tests.

If tests fail because of the implementation:

1. Investigate the failure.
2. Fix the implementation or tests as appropriate.
3. Run the tests again.

Continue only when the relevant tests pass.

If the tests cannot be made to pass:

STOP.

Leave AI-3 as In Progress.

Do not create a pull request.

==================================================
STEP 8 — COMMIT AND PUSH
==================================================

Commit the completed implementation on:

feature/AI-3

Use this commit-message format:

AI-3: <Jira story title>

Use the available GitHub Commit/Push Changes capability to push
feature/AI-3 to GitHub.

Verify that the changes were successfully pushed.

If the push fails:

STOP.

Leave AI-3 as In Progress.

Do not move AI-3 to In Review.

==================================================
STEP 9 — CREATE PULL REQUEST
==================================================

After the branch has been pushed successfully, use the available
GitHub Create PR capability.

Create ONE pull request from:

feature/AI-3

to the repository's default branch.

Use this PR title:

AI-3: <Jira story title>

The pull request description must contain:

- Jira story ID
- Requirement summary
- Acceptance criteria implemented
- Code changes made
- Files changed
- Tests added or updated
- Test results
- Any assumptions

Do NOT merge the pull request.

The pull request MUST remain OPEN for human review.

Verify that the pull request was created successfully.

If PR creation fails:

STOP.

Leave AI-3 as In Progress.

Do not move AI-3 to In Review.

==================================================
STEP 10 — MOVE AI-3 TO IN REVIEW
==================================================

ONLY perform this step when ALL of the following are true:

- AI-3 was successfully implemented
- relevant tests passed
- feature/AI-3 was successfully pushed
- an OPEN pull request was successfully created

Transition AI-3:

In Progress → In Review

After performing the transition, retrieve AI-3 again.

Verify:

Current Status = In Review

If the transition fails, report the failure.

NEVER transition AI-3 to Done.

==================================================
STEP 11 — ADD PR LINK TO JIRA
==================================================

If Jira commenting is available through Atlassian Rovo MCP,
add a comment to AI-3 containing:

Implementation completed and pull request created for human review.

Pull Request:
<PR URL>

Do not comment on another Jira issue.

==================================================
STEP 12 — FINAL REPORT
==================================================

Return:

Jira Story:
Jira Title:
Original Status:
Final Status:
Branch:
Implementation:
Files Changed:
Tests Added/Updated:
Test Result:
Commit:
Push Result:
Pull Request:
Pull Request Status:
Jira Comment:
Result:

==================================================
MANDATORY SAFETY AND WORKFLOW RULES
==================================================

- Process ONLY AI-3.
- Never modify another Jira issue.
- Never transition another Jira issue.
- Never commit directly to the default branch.
- Use feature/AI-3.
- Never create duplicate AI-3 work.
- Never create more than one open AI-3 pull request.
- Never merge the pull request automatically.
- Never transition AI-3 to Done.
- A human must review and merge the pull request.
- Verify Jira after every status transition.
- Do not start implementation until AI-3 is verified In Progress.
- Do not move AI-3 to In Review unless tests pass.
- Do not move AI-3 to In Review unless push succeeds.
- Do not move AI-3 to In Review unless the PR is successfully created.
- If implementation or tests fail, leave AI-3 In Progress.
- If push fails, leave AI-3 In Progress.
- If PR creation fails, leave AI-3 In Progress.
- If Jira access cannot be established after attempting Atlassian
  resource discovery and Jira retrieval, stop and report the problem.
```

------------------------------------------------------------------------

# PART L --- RUN THE CLASSROOM DEMO

## 23. Final Pre-Demo Checklist

Immediately before presenting:

-   [ ] AI-3 is **To Do**
-   [ ] Jira site opens successfully
-   [ ] AI-3 is visible to the Atlassian token owner
-   [ ] Cloud ID is correct
-   [ ] API token is active and not expired
-   [ ] `COPILOT_MCP_ATLASSIAN_API_KEY` exists under **Secrets and
    variables → Agents**
-   [ ] MCP configuration uses `https://mcp.atlassian.com/v2/mcp`
-   [ ] MCP configuration is saved
-   [ ] `JIRA Diagnostic` can retrieve AI-3
-   [ ] No unwanted old `feature/AI-3` branch exists
-   [ ] No old open AI-3 PR exists
-   [ ] Development automation has **Commit/Push Changes**
-   [ ] Development automation has **Create PR**
-   [ ] PR merge is not granted/automated
-   [ ] Repository builds successfully
-   [ ] The automation is saved
-   [ ] The automation is initially configured for manual execution

------------------------------------------------------------------------

## 24. Run It

Open:

``` text
Repository
→ Agents
→ Automations
→ AI-3 Development Workflow
```

Click:

``` text
Run now
```

Open the generated Copilot session.

During the classroom demo, point out the important events:

``` text
Start 'atlassian-rovo-mcp' MCP server
Get accessible Atlassian resources
Get Jira issue
Transition Jira issue
Repository analysis
Feature implementation
Maven tests
Commit/Push
Create Pull Request
Transition Jira issue
```

------------------------------------------------------------------------

## 25. What Success Should Look Like

At the end:

### Jira

Before:

``` text
AI-3 = To Do
```

During development:

``` text
AI-3 = In Progress
```

After successful PR creation:

``` text
AI-3 = In Review
```

### GitHub

You should have:

``` text
Branch:
feature/AI-3

Commit:
AI-3: Add API to search books by author

Pull Request:
AI-3: Add API to search books by author

PR status:
OPEN
```

The PR remains open for human review.

------------------------------------------------------------------------

# PART M --- TROUBLESHOOTING

## 26. MCP Server Starts but Jira Tools Are "Unavailable"

A particularly important scenario encountered during preparation was:

``` text
Start 'atlassian-rovo-mcp' MCP server
```

appearing successfully, followed by:

``` text
Atlassian Rovo MCP tools are unavailable in this session
```

Do not immediately recreate the API token.

First run the small `JIRA Diagnostic` automation.

### If JIRA Diagnostic works

This proves:

``` text
MCP server configuration   = working
Authentication             = working
Cloud ID                   = working
Jira read access           = working
Automation environment     = capable of using Rovo
```

Investigate the particular development automation/session, its
agent/skill/tool selection, or recreate that automation cleanly.

### If JIRA Diagnostic also fails

Then inspect:

1.  Agents secret
2.  MCP JSON
3.  token expiry
4.  token scopes
5.  Atlassian Rovo API-token authentication setting
6.  Jira user permissions
7.  Cloud ID
8.  MCP startup/session logs

------------------------------------------------------------------------

## 27. Inspect MCP Startup Logs

Open the Copilot session logs and inspect the MCP startup section.

A healthy startup should show:

``` text
Start 'atlassian-rovo-mcp' MCP server
```

When available, inspect the detailed MCP logs/tool registration.

Do not treat the wrapper merely reaching:

``` text
MCP servers are ready
```

as proof that every individual tool is usable. Confirm by actually
calling:

``` text
Get accessible Atlassian resources
Get Jira issue
```

------------------------------------------------------------------------

## 28. Wrong Endpoint

For this demo, use:

``` text
https://mcp.atlassian.com/v2/mcp
```

Do not accidentally leave the old:

``` text
/v1/mcp
```

configuration from an earlier setup.

Atlassian Rovo MCP v2 is the version used by this guide.

Official getting-started page:

https://support.atlassian.com/atlassian-ai-gateway/docs/get-started-with-the-atlassian-remote-mcp-server/

------------------------------------------------------------------------

## 29. Wrong Cloud ID

Do not confuse:

``` text
Organization ID
```

with:

``` text
Cloud ID
```

Verify again using:

``` text
https://gigtech.atlassian.net/_edge/tenant_info
```

For this demo:

``` text
0f24971e-0448-471e-88ae-b8a2cdb545fd
```

------------------------------------------------------------------------

## 30. Jira Read Works but Transition Fails

If:

``` text
Get Jira issue = works
```

but:

``` text
Transition Jira issue = fails/unavailable
```

check:

1.  `write:jira:agent-interface` is present on the API token.
2.  The Atlassian Rovo MCP Jira **Write** permission group is enabled.
3.  The Jira user represented by the token has permission to transition
    AI-3.
4.  The Jira workflow actually allows the requested transition.
5.  Use `listJiraIssueTransitions` when necessary to inspect available
    transitions.

------------------------------------------------------------------------

## 31. Jira Works but Push Fails

This is a GitHub automation capability issue, not an Atlassian issue.

Check the automation's **Tools** configuration.

Ensure:

``` text
Commit/Push Changes = enabled
```

The development prompt cannot grant itself a GitHub capability that the
automation configuration has not granted.

------------------------------------------------------------------------

## 32. Push Works but PR Creation Fails

Check:

``` text
Create PR = enabled
```

under the automation's GitHub Pull Request tools.

Do not compensate by enabling Merge PR.

The demo intentionally leaves the PR open.

------------------------------------------------------------------------

## 33. Automation Stops Because AI-3 Is In Progress

This is intentional duplicate protection.

The prompt only starts a brand-new implementation when:

``` text
AI-3 = To Do
```

If an earlier test moved AI-3 to In Progress but stopped before
completing the GitHub workflow, reset the disposable demo story to **To
Do** only when you have verified that doing so is appropriate and no
real work is being overwritten.

------------------------------------------------------------------------

## 34. Automation Stops Because an AI-3 PR Already Exists

Also intentional.

Before the classroom demo, check for an existing open PR from:

``` text
feature/AI-3
```

The automation should not create duplicate pull requests.

------------------------------------------------------------------------

# PART N --- OPTIONAL SKILLS

## 35. Agent Skills

GitHub Copilot Automations can inherit repository Agent skills.

During preparation, a skill named:

``` text
implement-jira-story
```

was activated for implementation tasks.

For the first clean demo, it is safer to prove the workflow using the
automation prompt and known-good MCP setup before adding a large skill.

Once the base workflow is stable, a skill can hold reusable
implementation guidance such as:

``` text
Read Jira
Read linked Confluence
Analyse architecture
Plan
Implement
Test
Commit
Push
Create PR
```

Avoid having the skill and automation prompt specify conflicting:

-   branch naming conventions
-   commit formats
-   PR formats
-   Jira status rules

Choose one authoritative convention.

------------------------------------------------------------------------

# PART O --- SECURITY / PRODUCTION HARDENING

## 36. Security Rules

For a classroom demo, `tools: ["*"]` makes troubleshooting easier.

For production:

1.  Follow least privilege.
2.  Restrict MCP tools where practical.
3.  Keep Jira Delete disabled unless explicitly required.
4.  Keep Jira Manage disabled unless explicitly required.
5.  Keep automatic PR merging disabled for this workflow.
6.  Never put credentials in prompts.
7.  Never commit API tokens or Base64 credentials.
8.  Rotate/revoke demo tokens after use when appropriate.
9.  Use a dedicated service account for production automation when
    appropriate.
10. Review Copilot session logs because automation sessions may be
    visible to repository users.

------------------------------------------------------------------------

# PART P --- 10-MINUTE CLASSROOM DEMO SCRIPT

## 37. Suggested Presentation Order

### Minute 1 --- Show Jira

Open AI-3.

Explain:

``` text
Status = To Do

Requirement:
Search books by author

Acceptance Criteria:
GET /api/books/author/{author}
Case-insensitive
Return all matches
Empty list if none
Unit tests
```

### Minute 2 --- Show Cloud ID

Open:

``` text
https://gigtech.atlassian.net/_edge/tenant_info
```

Explain that `cloudId` identifies the Atlassian Cloud site and is
different from the Organization ID.

### Minute 3 --- Explain API Token

Show the Atlassian API-token page, but **never reveal the token**.

Explain:

``` text
Token scopes
→ Jira Read
→ Jira Search
→ Jira Write
→ Confluence Read/Search
```

### Minute 4 --- Show GitHub Agents Secret

Open:

``` text
Settings
→ Secrets and variables
→ Agents
```

Show the **name only**:

``` text
COPILOT_MCP_ATLASSIAN_API_KEY
```

Do not reveal its value.

Explain that its value is:

``` text
Base64(email:api-token)
```

### Minute 5 --- Show MCP Configuration

Open:

``` text
Settings
→ Copilot
→ MCP servers
```

Show:

``` text
https://mcp.atlassian.com/v2/mcp
```

Explain the secret substitution.

### Minute 6 --- Run JIRA Diagnostic

Run the diagnostic.

Point out:

``` text
Start atlassian-rovo-mcp
Get accessible Atlassian resources
Get Jira issue
```

### Minute 7 --- Run Development Automation

Open the full automation and click:

``` text
Run now
```

### Minutes 8--9 --- Watch Agent

Highlight:

``` text
Jira To Do → In Progress
Code analysis
Implementation
Tests
Commit
Push
PR creation
Jira In Progress → In Review
```

### Minute 10 --- Show Final State

Show:

``` text
Jira = In Review
GitHub feature branch exists
PR = OPEN
Tests = PASS
```

Explain:

``` text
Human review → Merge → Done
```

The AI does not perform the final merge or Done transition.

------------------------------------------------------------------------

# PART Q --- OFFICIAL REFERENCES

## 38. GitHub

Configure repository MCP servers:

https://docs.github.com/en/copilot/how-tos/copilot-on-github/customize-copilot/configure-mcp-servers

Configure Copilot Cloud Agent secrets and variables:

https://docs.github.com/en/copilot/how-tos/copilot-on-github/customize-copilot/customize-cloud-agent/configure-secrets-and-variables

Create Copilot automations:

https://docs.github.com/en/copilot/how-tos/use-copilot-agents/cloud-agent/create-automations

About Copilot automations:

https://docs.github.com/en/copilot/concepts/agents/cloud-agent/about-automations

------------------------------------------------------------------------

## 39. Atlassian

Rovo MCP getting started:

https://support.atlassian.com/atlassian-ai-gateway/docs/get-started-with-the-atlassian-remote-mcp-server/

API-token authentication for Rovo MCP:

https://support.atlassian.com/atlassian-ai-gateway/docs/configure-authentication-via-api-token/

Rovo MCP supported tools and scopes:

https://support.atlassian.com/atlassian-ai-gateway/docs/supported-tools/

Create/manage Atlassian API tokens:

https://support.atlassian.com/atlassian-account/docs/manage-api-tokens-for-your-atlassian-account

Find Atlassian Cloud ID:

https://support.atlassian.com/jira/kb/retrieve-my-atlassian-sites-cloud-id/

------------------------------------------------------------------------

# Final Architecture

``` text
+------------------+
|      JIRA        |
|      AI-3        |
|      To Do       |
+--------+---------+
         |
         | Atlassian Rovo MCP v2
         v
+---------------------------+
| GitHub Copilot Automation |
|                           |
| Read Jira                 |
| Transition Jira           |
| Analyse repository        |
| Implement                 |
| Test                      |
+-------------+-------------+
              |
              | GitHub tools
              v
+---------------------------+
|       GitHub Repo         |
|                           |
| feature/AI-3              |
| commit                    |
| push                      |
| OPEN pull request         |
+-------------+-------------+
              |
              | Rovo MCP
              v
+---------------------------+
|          JIRA             |
|                           |
|       In Review           |
+-------------+-------------+
              |
              v
        HUMAN REVIEW
              |
              v
         MERGE / DONE
```

------------------------------------------------------------------------

# End of Demo Guide

**Recommended approach:** Keep `JIRA Diagnostic` as a permanent
read-only health check. Build the development automation separately. If
the development workflow ever reports that Rovo tools are unavailable,
run the diagnostic first before changing tokens, Cloud IDs, scopes, or
MCP configuration.
