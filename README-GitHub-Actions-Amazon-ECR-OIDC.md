# GitHub Actions → Amazon ECR using OIDC

## Complete step-by-step guide

This guide explains how to take application source code stored in
**GitHub**, automatically build a **Docker image** using **GitHub
Actions**, and push that image into **Amazon Elastic Container Registry
(Amazon ECR)**.

The example values used in this guide are:

``` text
GitHub owner:       kscorpio77
GitHub repository:  testbookapiworkflowdemo
AWS region:         us-east-1
ECR repository:     testbook-api
IAM role:           GitHubActionsECRRole
```

> **Security approach:** This guide uses **GitHub OIDC**. You do not
> need to save a permanent AWS Access Key and Secret Access Key in
> GitHub.

------------------------------------------------------------------------

# 1. Understand what we are building

The complete flow is:

``` text
Developer changes code
        |
        v
git push
        |
        v
GitHub Repository
        |
        v
GitHub Actions starts
        |
        v
Build Docker image
        |
        v
GitHub proves its identity to AWS using OIDC
        |
        v
AWS temporarily allows the IAM role
        |
        v
Login to Amazon ECR
        |
        v
Push Docker image
        |
        v
Amazon ECR
```

There are three important parts:

``` text
GitHub       = stores the source code
GitHub Actions = performs the automated work
Amazon ECR   = stores the finished Docker image
```

The Java source code itself is **not** pushed into ECR as normal source
files. GitHub Actions uses the source code to create a Docker image, and
the **image** is pushed to ECR.

------------------------------------------------------------------------

# 2. What is Amazon ECR?

**Amazon Elastic Container Registry (ECR)** is AWS's Docker image
registry.

You can think of it as a private storage area for Docker images.

For example:

``` text
Source Code
    |
    v
Docker Image
    |
    v
Amazon ECR
```

An ECR image address looks similar to:

``` text
AWS_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/testbook-api:latest
```

------------------------------------------------------------------------

# 3. What is OIDC?

OIDC allows GitHub Actions to prove to AWS:

> "I am a workflow running from an approved GitHub repository."

AWS checks that identity and, if it matches the rules you created, gives
the workflow **temporary AWS credentials**.

This is preferable to storing permanent AWS credentials in GitHub.

The flow is:

``` text
GitHub Actions
      |
      | OIDC identity
      v
AWS IAM
      |
      | Is this GitHub repository trusted?
      v
IAM Role
      |
      | Yes
      v
Temporary AWS access
```

------------------------------------------------------------------------

# PART A --- Prepare the application

# 4. Make sure the application is in GitHub

For this example:

``` text
https://github.com/kscorpio77/testbookapiworkflowdemo
```

A typical project may contain:

``` text
testbookapiworkflowdemo/
│
├── src/
├── pom.xml
├── Dockerfile
├── README.md
└── .github/
    └── workflows/
```

------------------------------------------------------------------------

# 5. Make sure you have a Dockerfile

For a Spring Boot application using Java 17, a simple example is:

``` dockerfile
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests


FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

The Dockerfile tells Docker how to turn the application into a Docker
image.

------------------------------------------------------------------------

# PART B --- Create the Amazon ECR repository

# 6. Sign in to AWS

Open the AWS Management Console and sign in.

Search for:

``` text
ECR
```

Open:

**Elastic Container Registry**

------------------------------------------------------------------------

# 7. Create the ECR repository

Go to:

``` text
Amazon ECR
→ Repositories
→ Create repository
```

Choose a **Private repository**.

Repository name:

``` text
testbook-api
```

For this example, select:

``` text
us-east-1
```

as the AWS region.

Create the repository.

You should now have:

``` text
testbook-api
```

in Amazon ECR.

------------------------------------------------------------------------

# 8. Understand the ECR repository address

Your repository URI will look similar to:

``` text
AWS_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/testbook-api
```

For example:

``` text
123456789012.dkr.ecr.us-east-1.amazonaws.com/testbook-api
```

Do not copy the example account number. Use the repository URI displayed
in your own AWS account.

------------------------------------------------------------------------

# PART C --- Connect GitHub securely to AWS

# 9. Open IAM

In AWS, search for:

``` text
IAM
```

IAM controls who is allowed to access AWS resources.

We need AWS to trust GitHub Actions.

------------------------------------------------------------------------

# 10. Create the GitHub OIDC Identity Provider

Go to:

``` text
IAM
→ Identity providers
→ Add provider
```

Choose:

``` text
OpenID Connect
```

For **Provider URL**, enter:

``` text
https://token.actions.githubusercontent.com
```

For **Audience**, enter:

``` text
sts.amazonaws.com
```

Then create/add the provider.

### What did we just do?

We told AWS:

> GitHub Actions is an identity provider that AWS is allowed to
> recognise.

This does **not** yet give GitHub permission to push anything to ECR.

------------------------------------------------------------------------

# PART D --- Create the IAM role

# 11. Create an IAM role

Go to:

``` text
IAM
→ Roles
→ Create role
```

Select the option for **Web identity**.

Select the GitHub OIDC provider:

``` text
token.actions.githubusercontent.com
```

Audience:

``` text
sts.amazonaws.com
```

Depending on the current AWS screen, you may be asked for GitHub
organisation/repository information.

For this example:

``` text
GitHub owner/organisation:
kscorpio77

Repository:
testbookapiworkflowdemo
```

Do **not** enter the full URL:

``` text
https://github.com/kscorpio77/testbookapiworkflowdemo.git
```

where AWS expects only the owner or repository name.

Give the role a clear name:

``` text
GitHubActionsECRRole
```

------------------------------------------------------------------------

# 12. Give the role permission to push to ECR

The role needs permission to perform the operations required to upload
an image.

For a quick classroom/demo setup, AWS's managed ECR push-oriented
permissions may be used if appropriate for your account.

For a more controlled setup, create an IAM policy containing only the
permissions needed.

An example policy is:

``` json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ecr:GetAuthorizationToken"
      ],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage",
        "ecr:InitiateLayerUpload",
        "ecr:UploadLayerPart",
        "ecr:CompleteLayerUpload",
        "ecr:PutImage"
      ],
      "Resource": "arn:aws:ecr:us-east-1:AWS_ACCOUNT_ID:repository/testbook-api"
    }
  ]
}
```

Replace:

``` text
AWS_ACCOUNT_ID
```

with your own AWS account ID.

### Why are there two sections?

This:

``` text
ecr:GetAuthorizationToken
```

allows the workflow to log in to ECR.

The other permissions allow it to upload the Docker image to:

``` text
testbook-api
```

The second section is restricted to the intended ECR repository.

------------------------------------------------------------------------

# PART E --- Configure the role's Trust Policy

# 13. Why is the Trust Policy important?

There are two different questions AWS needs to answer.

First:

``` text
Is this GitHub workflow allowed to become this IAM role?
```

That is controlled by the **Trust Policy**.

Second:

``` text
After becoming the role, what can it do?
```

That is controlled by the role's **Permissions Policy**.

So:

``` text
Trust Policy
    =
WHO can use the role?

Permissions Policy
    =
WHAT can the role do?
```

------------------------------------------------------------------------

# 14. Open the Trust Policy

Go to:

``` text
IAM
→ Roles
→ GitHubActionsECRRole
→ Trust relationships
→ Edit trust policy
```

A typical trust policy contains:

``` json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "arn:aws:iam::AWS_ACCOUNT_ID:oidc-provider/token.actions.githubusercontent.com"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
        },
        "StringLike": {
          "token.actions.githubusercontent.com:sub": "YOUR_GITHUB_OIDC_SUBJECT"
        }
      }
    }
  ]
}
```

Replace:

``` text
AWS_ACCOUNT_ID
```

with your AWS account ID.

------------------------------------------------------------------------

# 15. Important: GitHub's OIDC subject format

The `sub` value tells AWS exactly which GitHub workflow identity is
trusted.

GitHub changed the default subject format for repositories created on or
after **15 July 2026**.

For older repositories, a subject may look like:

``` text
repo:kscorpio77/testbookapiworkflowdemo:ref:refs/heads/main
```

For newer repositories, it can include immutable numeric owner and
repository IDs, conceptually like:

``` text
repo:OWNER@OWNER_ID/REPOSITORY@REPOSITORY_ID:ref:refs/heads/main
```

Therefore, do not blindly copy an old trust policy from an old tutorial.

Use the OIDC subject format appropriate for your repository.

For a broad diagnostic rule for a new repository, you may encounter a
pattern similar to:

``` text
repo:kscorpio77@*/testbookapiworkflowdemo@*:*
```

Once authentication works, restrict the rule as tightly as practical,
preferably using the actual owner ID, repository ID, and branch.

------------------------------------------------------------------------

# 16. Why the branch matters

A trust rule can be restricted to:

``` text
main
```

This means a workflow from another branch cannot use the AWS role.

Conceptually:

``` text
GitHub repository
      |
      +--- main --------> AWS access allowed
      |
      +--- random branch -> AWS access denied
```

This is safer than trusting every branch.

------------------------------------------------------------------------

# PART F --- Copy the IAM Role ARN

# 17. Find the Role ARN

Open:

``` text
IAM
→ Roles
→ GitHubActionsECRRole
```

Copy the **ARN**.

It looks like:

``` text
arn:aws:iam::AWS_ACCOUNT_ID:role/GitHubActionsECRRole
```

You will use this in the GitHub Actions workflow.

------------------------------------------------------------------------

# PART G --- Create the GitHub Actions workflow

# 18. Create the workflow directory

Inside the GitHub repository create:

``` text
.github/workflows/
```

------------------------------------------------------------------------

# 19. Create the workflow file

Create:

``` text
.github/workflows/04-push-image-to-ecr.yml
```

------------------------------------------------------------------------

# 20. Complete workflow example

Use:

``` yaml
name: 02 - Build and Push Docker Image to ECR

on:
  push:
    branches:
      - main

  workflow_dispatch:

permissions:
  id-token: write
  contents: read

env:
  AWS_REGION: us-east-1
  ECR_REPOSITORY: testbook-api

jobs:

  build-and-push:

    name: Build Docker Image and Push to Amazon ECR

    runs-on: ubuntu-latest

    steps:

      - name: Checkout source code
        uses: actions/checkout@v4

      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v6.3.0
        with:
          role-to-assume: arn:aws:iam::AWS_ACCOUNT_ID:role/GitHubActionsECRRole
          aws-region: ${{ env.AWS_REGION }}

      - name: Confirm AWS identity
        run: aws sts get-caller-identity

      - name: Login to Amazon ECR
        id: login-ecr
        uses: aws-actions/amazon-ecr-login@v2

      - name: Build, tag and push Docker image
        env:
          ECR_REGISTRY: ${{ steps.login-ecr.outputs.registry }}
          IMAGE_TAG: ${{ github.sha }}
        run: |
          docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG .
          docker tag $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG \
            $ECR_REGISTRY/$ECR_REPOSITORY:latest
          docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG
          docker push $ECR_REGISTRY/$ECR_REPOSITORY:latest
```

Replace:

``` text
AWS_ACCOUNT_ID
```

with your actual AWS account ID.

------------------------------------------------------------------------

# PART H --- Understand every part of the workflow

# 21. Workflow name

``` yaml
name: 02 - Build and Push Docker Image to ECR
```

This is simply the name shown on GitHub's **Actions** page.

------------------------------------------------------------------------

# 22. Automatic trigger

``` yaml
on:
  push:
    branches:
      - main
```

This means:

> Run the workflow automatically whenever code is pushed to `main`.

For example:

``` bash
git add .
git commit -m "Update application"
git push origin main
```

The last command starts the workflow.

------------------------------------------------------------------------

# 23. Manual trigger

``` yaml
workflow_dispatch:
```

This allows you to start the workflow manually from the GitHub Actions
screen.

This is particularly useful during a classroom demonstration.

------------------------------------------------------------------------

# 24. OIDC permission

This is essential:

``` yaml
permissions:
  id-token: write
  contents: read
```

### `id-token: write`

This allows GitHub Actions to request an OIDC identity token.

It does **not** mean GitHub can freely write to your AWS account.

### `contents: read`

This allows the workflow to read the GitHub repository so it can obtain
the application code.

------------------------------------------------------------------------

# 25. AWS region

``` yaml
AWS_REGION: us-east-1
```

This tells the workflow which AWS region contains the ECR repository.

------------------------------------------------------------------------

# 26. ECR repository

``` yaml
ECR_REPOSITORY: testbook-api
```

This is the ECR repository that will receive the image.

------------------------------------------------------------------------

# 27. Temporary GitHub computer

``` yaml
runs-on: ubuntu-latest
```

GitHub creates a temporary Linux computer.

The workflow performs the build on this computer.

You do **not** need your own laptop to remain switched on.

------------------------------------------------------------------------

# 28. Checkout source code

``` yaml
- name: Checkout source code
  uses: actions/checkout@v4
```

This copies the GitHub repository onto the temporary GitHub computer.

------------------------------------------------------------------------

# 29. Configure AWS credentials

``` yaml
- name: Configure AWS credentials
  uses: aws-actions/configure-aws-credentials@v6.3.0
  with:
    role-to-assume: arn:aws:iam::AWS_ACCOUNT_ID:role/GitHubActionsECRRole
    aws-region: ${{ env.AWS_REGION }}
```

This is where OIDC authentication happens.

Conceptually:

``` text
GitHub
   |
   | "Here is my OIDC identity"
   v
AWS
   |
   | Check Trust Policy
   v
GitHubActionsECRRole
   |
   v
Temporary AWS credentials
```

There is no need to put permanent AWS Access Keys into this workflow
when OIDC is configured correctly.

------------------------------------------------------------------------

# 30. Confirm AWS identity

``` yaml
- name: Confirm AWS identity
  run: aws sts get-caller-identity
```

This is a useful demonstration and troubleshooting step.

It shows which AWS identity the workflow is currently using.

If this succeeds, GitHub has successfully authenticated to AWS.

------------------------------------------------------------------------

# 31. Login to Amazon ECR

``` yaml
- name: Login to Amazon ECR
  id: login-ecr
  uses: aws-actions/amazon-ecr-login@v2
```

This logs Docker into your private Amazon ECR registry.

The action produces the registry address, which we use in the next step.

------------------------------------------------------------------------

# 32. Git commit as an image version

``` yaml
IMAGE_TAG: ${{ github.sha }}
```

Every Git commit has a unique identifier.

Using it as the Docker image tag connects the image back to the exact
source-code version that created it.

For example:

``` text
testbook-api:a91b25...
```

------------------------------------------------------------------------

# 33. Build the Docker image

``` bash
docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG .
```

In simple terms:

> Read the Dockerfile and create the Docker image.

------------------------------------------------------------------------

# 34. Add the `latest` tag

``` bash
docker tag $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG \
  $ECR_REGISTRY/$ECR_REPOSITORY:latest
```

The same image now has two useful labels:

``` text
testbook-api:COMMIT_ID
testbook-api:latest
```

The commit tag gives traceability.

`latest` gives a convenient name for the newest successful image.

------------------------------------------------------------------------

# 35. Push the image

``` bash
docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG
docker push $ECR_REGISTRY/$ECR_REPOSITORY:latest
```

These commands upload the image into Amazon ECR.

------------------------------------------------------------------------

# PART I --- Commit and push the workflow

# 36. Commit the files

Run:

``` bash
git add .
```

Then:

``` bash
git commit -m "Add ECR Docker workflow"
```

Then:

``` bash
git push origin main
```

Because the workflow listens for pushes to `main`, GitHub Actions should
start automatically.

------------------------------------------------------------------------

# PART J --- Monitor the workflow

# 37. Open GitHub Actions

Go to:

``` text
GitHub Repository
→ Actions
```

Open:

``` text
02 - Build and Push Docker Image to ECR
```

A successful run should show green ticks for steps such as:

``` text
✓ Checkout source code
✓ Configure AWS credentials
✓ Confirm AWS identity
✓ Login to Amazon ECR
✓ Build, tag and push Docker image
```

------------------------------------------------------------------------

# PART K --- Verify the image in Amazon ECR

# 38. Open ECR

Go to:

``` text
AWS
→ Elastic Container Registry
→ Repositories
→ testbook-api
```

You should see image tags such as:

``` text
latest
```

and:

``` text
a Git commit ID
```

This confirms the image has reached ECR.

------------------------------------------------------------------------

# PART L --- Complete process in one picture

``` text
Java/Spring Boot Source Code
           |
           v
      Git Repository
           |
           | git push
           v
        GitHub
           |
           v
     GitHub Actions
           |
           +----------------------+
           |                      |
           v                      |
     Checkout Code                |
           |                      |
           v                      |
     Request OIDC Token           |
           |                      |
           v                      |
        AWS IAM                   |
           |                      |
           v                      |
 GitHubActionsECRRole             |
           |                      |
           v                      |
 Temporary AWS Access             |
           |                      |
           v                      |
     Login to ECR                 |
           |                      |
           v                      |
    Build Docker Image <----------+
           |
           v
       Tag Image
           |
           v
       Push Image
           |
           v
       Amazon ECR
           |
           v
 testbook-api:latest
```

------------------------------------------------------------------------

# PART M --- Common errors

# 39. Error: `Not authorized to perform sts:AssumeRoleWithWebIdentity`

Example:

``` text
Could not assume role with OIDC:
Not authorized to perform sts:AssumeRoleWithWebIdentity
```

This happens **before ECR**.

It normally means AWS rejected the GitHub identity.

Check:

``` text
IAM
→ Roles
→ GitHubActionsECRRole
→ Trust relationships
```

Verify:

-   The GitHub OIDC provider is correct.
-   The audience is `sts.amazonaws.com`.
-   The role allows `sts:AssumeRoleWithWebIdentity`.
-   The `sub` condition matches the repository's actual GitHub OIDC
    subject.
-   For newer repositories, check whether immutable owner/repository IDs
    are required.
-   The workflow contains `id-token: write`.

Do **not** start changing Docker or ECR permissions for this particular
error. Authentication has failed before those steps.

------------------------------------------------------------------------

# 40. Error: ECR login fails

If AWS authentication succeeds but ECR login fails, check that the IAM
role has:

``` text
ecr:GetAuthorizationToken
```

------------------------------------------------------------------------

# 41. Error: image push is denied

If login works but `docker push` fails, check that the role has
permissions including:

``` text
ecr:BatchCheckLayerAvailability
ecr:InitiateLayerUpload
ecr:UploadLayerPart
ecr:CompleteLayerUpload
ecr:PutImage
```

Also verify that the policy points to the correct:

``` text
region
AWS account
ECR repository
```

------------------------------------------------------------------------

# 42. Error: repository does not exist

Make sure this exists in ECR:

``` text
testbook-api
```

and make sure the workflow contains exactly:

``` yaml
ECR_REPOSITORY: testbook-api
```

------------------------------------------------------------------------

# 43. Error: Dockerfile not found

Make sure the repository contains:

``` text
Dockerfile
```

If the Dockerfile is in the root of the repository, this works:

``` bash
docker build ... .
```

The final `.` means the current directory.

------------------------------------------------------------------------

# 44. Error: workflow never starts

Check:

``` text
.github/workflows/04-push-image-to-ecr.yml
```

Also verify that you pushed to:

``` text
main
```

because the trigger is:

``` yaml
branches:
  - main
```

------------------------------------------------------------------------

# PART N --- Do we need AWS Access Key and Secret Key?

With the OIDC approach described in this guide:

``` text
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
```

do **not** need to be stored as permanent GitHub Secrets.

Instead:

``` text
GitHub OIDC identity
        |
        v
AWS IAM Role
        |
        v
Temporary credentials
```

This avoids keeping long-lived AWS keys in GitHub.

------------------------------------------------------------------------

# PART O --- Do we need Docker installed on the developer's laptop?

For this GitHub Actions workflow:

**No.**

GitHub's temporary runner performs the Docker build.

The developer's computer mainly needs Git to push the source code:

``` text
Developer laptop
       |
       | git push
       v
GitHub
       |
       | GitHub's runner builds Docker image
       v
Amazon ECR
```

Installing Docker locally is still useful if you want to test the Docker
image before pushing your changes.

------------------------------------------------------------------------

# PART P --- Recommended demonstration sequence

For a classroom demo, show the process in this order:

1.  Show the application in GitHub.
2.  Show the `Dockerfile`.
3.  Show the `testbook-api` repository in ECR.
4.  Show the GitHub OIDC Identity Provider in IAM.
5.  Show `GitHubActionsECRRole`.
6.  Show its **Trust relationship**.
7.  Explain the difference between the Trust Policy and Permissions
    Policy.
8.  Show the workflow YAML.
9.  Make a small code change.
10. Commit it.
11. Push it to `main`.
12. Open GitHub Actions.
13. Watch AWS authentication succeed.
14. Watch ECR login succeed.
15. Watch Docker build.
16. Watch Docker push.
17. Open Amazon ECR.
18. Show the newly created image/tag.

------------------------------------------------------------------------

# PART Q --- Final checklist

Before the demonstration, verify:

-   [ ] Application source code is in GitHub.
-   [ ] `Dockerfile` exists.
-   [ ] ECR repository `testbook-api` exists.
-   [ ] Correct AWS region is being used.
-   [ ] GitHub OIDC Identity Provider exists in IAM.
-   [ ] Provider URL is `https://token.actions.githubusercontent.com`.
-   [ ] Audience is `sts.amazonaws.com`.
-   [ ] IAM role `GitHubActionsECRRole` exists.
-   [ ] Trust Policy allows the correct GitHub repository identity.
-   [ ] Trust Policy allows `sts:AssumeRoleWithWebIdentity`.
-   [ ] IAM role has the required ECR permissions.
-   [ ] Workflow has `id-token: write`.
-   [ ] Workflow has `contents: read`.
-   [ ] Correct role ARN is in the workflow.
-   [ ] `AWS_REGION` is correct.
-   [ ] `ECR_REPOSITORY` is `testbook-api`.
-   [ ] Workflow file is under `.github/workflows/`.
-   [ ] Code is pushed to `main`.
-   [ ] AWS authentication succeeds.
-   [ ] ECR login succeeds.
-   [ ] Docker build succeeds.
-   [ ] Docker push succeeds.
-   [ ] Image appears in ECR.

------------------------------------------------------------------------

# Final workflow

Here is the workflow again without the detailed explanations:

``` yaml
name: 02 - Build and Push Docker Image to ECR

on:
  push:
    branches:
      - main

  workflow_dispatch:

permissions:
  id-token: write
  contents: read

env:
  AWS_REGION: us-east-1
  ECR_REPOSITORY: testbook-api

jobs:

  build-and-push:

    name: Build Docker Image and Push to Amazon ECR

    runs-on: ubuntu-latest

    steps:

      - name: Checkout source code
        uses: actions/checkout@v4

      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v6.3.0
        with:
          role-to-assume: arn:aws:iam::AWS_ACCOUNT_ID:role/GitHubActionsECRRole
          aws-region: ${{ env.AWS_REGION }}

      - name: Confirm AWS identity
        run: aws sts get-caller-identity

      - name: Login to Amazon ECR
        id: login-ecr
        uses: aws-actions/amazon-ecr-login@v2

      - name: Build, tag and push Docker image
        env:
          ECR_REGISTRY: ${{ steps.login-ecr.outputs.registry }}
          IMAGE_TAG: ${{ github.sha }}
        run: |
          docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG .
          docker tag $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG \
            $ECR_REGISTRY/$ECR_REPOSITORY:latest
          docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG
          docker push $ECR_REGISTRY/$ECR_REPOSITORY:latest
```

Replace `AWS_ACCOUNT_ID` with the AWS account ID that owns the IAM role
and ECR repository.

------------------------------------------------------------------------

# Summary

After the one-time AWS and GitHub configuration, the everyday process
becomes very simple:

``` bash
git add .
git commit -m "Application changes"
git push origin main
```

Then automatically:

``` text
GitHub receives code
       ↓
GitHub Actions starts
       ↓
GitHub authenticates to AWS using OIDC
       ↓
AWS temporarily grants the IAM role
       ↓
Docker image is built
       ↓
GitHub Actions logs in to ECR
       ↓
Docker image is pushed
       ↓
Amazon ECR stores the image
```

This provides a secure CI/CD approach without storing permanent AWS
Access Keys in the GitHub repository.
