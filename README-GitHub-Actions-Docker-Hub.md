# GitHub Actions: Build and Push a Docker Image to a Docker Repository

## Purpose

This guide explains, from the beginning, how to create a **GitHub
Actions workflow** that automatically:

1.  Takes your application code from GitHub.
2.  Builds the application as a Docker image.
3.  Logs in to Docker Hub.
4.  Pushes the Docker image to your Docker Hub repository.

The important idea is:

``` text
Developer
    |
    | git push
    v
GitHub Repository
    |
    | GitHub Actions starts automatically
    v
Build Docker Image
    |
    | Login securely
    v
Docker Hub Repository
    |
    v
Docker Image available for students/users
```

> This guide uses **Docker Hub** as the Docker repository. It does not
> use Amazon ECR.

------------------------------------------------------------------------

## 1. What do we need?

Before creating the workflow, you need:

-   A GitHub account.
-   A GitHub repository containing your application.
-   A `Dockerfile` in the repository.
-   A Docker Hub account.
-   A repository created in Docker Hub.
-   A Docker Hub access token.
-   GitHub Secrets containing your Docker Hub username and token.

You **do not need Docker installed on your own computer for GitHub
Actions to build the image**. GitHub provides a temporary computer that
performs the build.

------------------------------------------------------------------------

# Part 1 --- Prepare the application

## 2. Make sure your project is in GitHub

Your project should already be stored in a GitHub repository.

For example:

``` text
https://github.com/kscorpio77/testbookapiworkflowdemo
```

A typical Spring Boot project may look like:

``` text
testbookapiworkflowdemo/
│
├── src/
├── pom.xml
├── Dockerfile
└── README.md
```

The workflow file will be added later under:

``` text
.github/workflows/
```

------------------------------------------------------------------------

## 3. Create the Dockerfile

The `Dockerfile` tells Docker how to package the application.

For a Maven/Spring Boot application using Java 17, a simple example is:

``` dockerfile
# Stage 1: Build the application
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests


# Stage 2: Run the application
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### What does this do?

In simple terms:

``` text
Source Code
     |
     v
Maven builds the application
     |
     v
JAR file is created
     |
     v
Docker puts the JAR into an image
     |
     v
The image can run anywhere Docker is available
```

Commit the `Dockerfile` to GitHub.

------------------------------------------------------------------------

# Part 2 --- Create a Docker Hub repository

## 4. Sign in to Docker Hub

Open Docker Hub and sign in to your account.

Go to:

``` text
https://hub.docker.com/
```

------------------------------------------------------------------------

## 5. Create a repository

In Docker Hub:

1.  Click **Create repository**.
2.  Enter a repository name.

For example:

``` text
testbook-api
```

3.  Choose whether it should be Public or Private.
4.  Click **Create**.

If your Docker Hub username were:

``` text
gigtechmentor
```

and the repository were:

``` text
testbook-api
```

the complete Docker image name would be:

``` text
gigtechmentor/testbook-api
```

With a version:

``` text
gigtechmentor/testbook-api:1.0
```

The part before `/` is the Docker Hub username.

The part after `/` is the Docker Hub repository/image name.

------------------------------------------------------------------------

# Part 3 --- Create a Docker Hub access token

## 6. Why not store the Docker Hub password in the workflow?

Never write a password directly inside a GitHub workflow file.

For example, do **not** do this:

``` yaml
password: my-real-password
```

Anyone who can read the repository could potentially see it.

Instead, use a Docker Hub access token and save it securely as a GitHub
Secret.

------------------------------------------------------------------------

## 7. Create the Docker Hub access token

In Docker Hub:

1.  Sign in.
2.  Open your account settings.
3.  Find **Personal access tokens**.
4.  Create a new access token.
5.  Give it a meaningful description, such as:

``` text
github-actions
```

6.  Give the token permission to push images.
7.  Create the token.
8.  Copy it immediately.

Treat this token like a password.

------------------------------------------------------------------------

# Part 4 --- Store Docker credentials in GitHub Secrets

## 8. Open GitHub repository settings

Open your GitHub repository.

Then go to:

``` text
Settings
   ↓
Secrets and variables
   ↓
Actions
```

Click:

**New repository secret**

------------------------------------------------------------------------

## 9. Create the Docker username secret

Create:

``` text
Name:
DOCKERHUB_USERNAME
```

Value:

``` text
your Docker Hub username
```

For example:

``` text
gigtechmentor
```

Save it.

------------------------------------------------------------------------

## 10. Create the Docker token secret

Create another secret:

``` text
Name:
DOCKERHUB_TOKEN
```

Value:

``` text
the Docker Hub access token created earlier
```

Save it.

You should now have:

``` text
DOCKERHUB_USERNAME
DOCKERHUB_TOKEN
```

GitHub keeps their actual values hidden from the workflow logs.

------------------------------------------------------------------------

# Part 5 --- Create the GitHub Actions workflow

## 11. Create the workflow directory

Inside your GitHub repository, create:

``` text
.github
```

Inside `.github`, create:

``` text
workflows
```

The final structure becomes:

``` text
.github/
└── workflows/
```

------------------------------------------------------------------------

## 12. Create the workflow file

Inside:

``` text
.github/workflows/
```

create:

``` text
docker-build-push.yml
```

The complete path is:

``` text
.github/workflows/docker-build-push.yml
```

GitHub automatically recognises YAML files stored in this directory as
GitHub Actions workflows.

------------------------------------------------------------------------

# Part 6 --- Complete workflow

Put the following into `docker-build-push.yml`:

``` yaml
name: Build and Push Docker Image

# Run this workflow whenever code is pushed to main.
on:
  push:
    branches:
      - main

# Values used throughout the workflow.
env:
  IMAGE_NAME: testbook-api

jobs:

  build-and-push:

    # GitHub provides a temporary Linux computer.
    runs-on: ubuntu-latest

    steps:

      # Step 1
      # Copy the repository code onto the temporary GitHub computer.
      - name: Checkout source code
        uses: actions/checkout@v4

      # Step 2
      # Log in to Docker Hub.
      # The username and token come from GitHub Secrets.
      - name: Login to Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_TOKEN }}

      # Step 3
      # Prepare Docker's build system.
      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      # Step 4
      # Build the Docker image and push it to Docker Hub.
      - name: Build and Push Docker Image
        uses: docker/build-push-action@v6
        with:
          context: .
          push: true
          tags: |
            ${{ secrets.DOCKERHUB_USERNAME }}/${{ env.IMAGE_NAME }}:latest
            ${{ secrets.DOCKERHUB_USERNAME }}/${{ env.IMAGE_NAME }}:${{ github.sha }}
```

------------------------------------------------------------------------

# Part 7 --- Understand every part of the workflow

## 13. Workflow name

``` yaml
name: Build and Push Docker Image
```

This is simply the name displayed on the GitHub **Actions** page.

------------------------------------------------------------------------

## 14. When should the workflow run?

``` yaml
on:
  push:
    branches:
      - main
```

This means:

> Whenever somebody pushes code to the `main` branch, start this
> workflow automatically.

For example:

``` bash
git add .
git commit -m "Update application"
git push origin main
```

The `git push` causes GitHub Actions to start.

------------------------------------------------------------------------

## 15. Image name

``` yaml
env:
  IMAGE_NAME: testbook-api
```

Instead of repeatedly writing the image name, we store it once.

If you want another image name, change:

``` text
testbook-api
```

------------------------------------------------------------------------

## 16. Job

``` yaml
jobs:
  build-and-push:
```

A **job** is simply a collection of work GitHub needs to perform.

Here the job is responsible for:

``` text
Get code
   ↓
Login to Docker Hub
   ↓
Build Docker image
   ↓
Push Docker image
```

------------------------------------------------------------------------

## 17. GitHub's temporary computer

``` yaml
runs-on: ubuntu-latest
```

GitHub creates a temporary Ubuntu computer for the workflow.

You do not have to create this machine yourself.

When the workflow finishes, GitHub removes the temporary machine.

------------------------------------------------------------------------

## 18. Checkout the source code

``` yaml
- name: Checkout source code
  uses: actions/checkout@v4
```

The temporary computer initially does not have your application code.

This step copies the repository code onto that computer.

Think of it as:

``` text
GitHub Repository
       |
       | copy
       v
Temporary GitHub Computer
```

------------------------------------------------------------------------

## 19. Login to Docker Hub

``` yaml
- name: Login to Docker Hub
  uses: docker/login-action@v3
  with:
    username: ${{ secrets.DOCKERHUB_USERNAME }}
    password: ${{ secrets.DOCKERHUB_TOKEN }}
```

Docker Hub needs to know who is trying to push the image.

Instead of putting the username/token directly into the file, GitHub
retrieves them from Secrets.

------------------------------------------------------------------------

## 20. Set up Docker Buildx

``` yaml
- name: Set up Docker Buildx
  uses: docker/setup-buildx-action@v3
```

This prepares Docker's image-building system on the temporary GitHub
computer.

You normally do not need to change this step.

------------------------------------------------------------------------

## 21. Build and push

``` yaml
- name: Build and Push Docker Image
  uses: docker/build-push-action@v6
```

This performs the main work.

------------------------------------------------------------------------

## 22. Build context

``` yaml
context: .
```

The `.` means:

> Use the current repository directory when building the Docker image.

Docker therefore looks for the `Dockerfile` in the root directory.

------------------------------------------------------------------------

## 23. Push the image

``` yaml
push: true
```

This is extremely important.

It tells the action:

> Do not just build the image. Push the finished image to Docker Hub.

------------------------------------------------------------------------

# Part 8 --- Docker image tags

The workflow creates two tags.

## 24. `latest`

``` yaml
${{ secrets.DOCKERHUB_USERNAME }}/${{ env.IMAGE_NAME }}:latest
```

For example:

``` text
gigtechmentor/testbook-api:latest
```

This provides a convenient name for the latest build.

------------------------------------------------------------------------

## 25. Git commit tag

The second tag is:

``` yaml
${{ secrets.DOCKERHUB_USERNAME }}/${{ env.IMAGE_NAME }}:${{ github.sha }}
```

`${{ github.sha }}` represents the unique Git commit that started the
workflow.

The resulting image could look like:

``` text
gigtechmentor/testbook-api:81d13a38...
```

This is useful because you can identify exactly which version of the
source code created an image.

------------------------------------------------------------------------

# Part 9 --- Commit the workflow

## 26. Add the workflow to Git

Run:

``` bash
git add .
```

Then:

``` bash
git commit -m "Add Docker build and push workflow"
```

Then:

``` bash
git push origin main
```

The final command pushes the code to GitHub.

Because the workflow contains:

``` yaml
on:
  push:
    branches:
      - main
```

GitHub Actions should automatically start.

------------------------------------------------------------------------

# Part 10 --- Watch the workflow

## 27. Open GitHub Actions

Open the GitHub repository.

Click:

**Actions**

You should see:

``` text
Build and Push Docker Image
```

Open the workflow run.

You should see steps similar to:

``` text
✓ Checkout source code
✓ Login to Docker Hub
✓ Set up Docker Buildx
✓ Build and Push Docker Image
```

If all steps show green ticks, the image has been pushed successfully.

------------------------------------------------------------------------

# Part 11 --- Verify the image in Docker Hub

## 28. Open Docker Hub

Sign in to Docker Hub and open:

``` text
Repositories
```

Then open:

``` text
testbook-api
```

You should see tags such as:

``` text
latest
81d13a38...
```

The exact commit tag will be different for every Git commit.

------------------------------------------------------------------------

# Part 12 --- Run the image

Once the image is in Docker Hub, another person can download and run it.

For example:

``` bash
docker run -d -p 8080:8080 --name testbook-api gigtechmentor/testbook-api:latest
```

Replace `gigtechmentor` with your actual Docker Hub username if
different.

------------------------------------------------------------------------

## 29. What does this command mean?

``` bash
docker run
```

means:

> Create and start a Docker container.

``` bash
-d
```

means:

> Run it in the background.

``` bash
-p 8080:8080
```

means:

> Connect port 8080 on the computer to port 8080 inside the container.

``` bash
--name testbook-api
```

gives the running container a friendly name.

``` bash
gigtechmentor/testbook-api:latest
```

tells Docker which image to use.

If the image is not already on the computer, Docker automatically
downloads it from Docker Hub.

------------------------------------------------------------------------

# Part 13 --- Complete CI/CD flow

The complete process is now:

``` text
Developer changes Java code
          |
          v
git add .
          |
          v
git commit
          |
          v
git push origin main
          |
          v
GitHub receives the code
          |
          v
GitHub Actions starts
          |
          v
Checkout source code
          |
          v
Login to Docker Hub
          |
          v
Read Dockerfile
          |
          v
Build Docker image
          |
          v
Push Docker image
          |
          v
Docker Hub
          |
          v
gigtechmentor/testbook-api:latest
          |
          v
Students/users can run the image
```

------------------------------------------------------------------------

# Part 14 --- Optional: Allow manual execution

Sometimes, particularly during a classroom demonstration, you may want
to run the workflow manually as well.

Change:

``` yaml
on:
  push:
    branches:
      - main
```

to:

``` yaml
on:
  push:
    branches:
      - main

  workflow_dispatch:
```

Now the workflow supports both:

``` text
Push to main
     OR
Run manually from GitHub
```

On the GitHub **Actions** page, you can select the workflow and use
**Run workflow**.

------------------------------------------------------------------------

# Part 15 --- Recommended final workflow for a demo

``` yaml
name: Build and Push Docker Image

on:
  push:
    branches:
      - main

  workflow_dispatch:

env:
  IMAGE_NAME: testbook-api

jobs:
  build-and-push:

    runs-on: ubuntu-latest

    steps:

      - name: Checkout source code
        uses: actions/checkout@v4

      - name: Login to Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_TOKEN }}

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Build and Push Docker Image
        uses: docker/build-push-action@v6
        with:
          context: .
          push: true
          tags: |
            ${{ secrets.DOCKERHUB_USERNAME }}/${{ env.IMAGE_NAME }}:latest
            ${{ secrets.DOCKERHUB_USERNAME }}/${{ env.IMAGE_NAME }}:${{ github.sha }}
```

------------------------------------------------------------------------

# Part 16 --- Common problems

## Error: Dockerfile not found

Check that your repository contains:

``` text
Dockerfile
```

and that it is in the root directory if you are using:

``` yaml
context: .
```

------------------------------------------------------------------------

## Error: Username or password incorrect

Check:

``` text
GitHub
→ Repository
→ Settings
→ Secrets and variables
→ Actions
```

Confirm these secrets exist:

``` text
DOCKERHUB_USERNAME
DOCKERHUB_TOKEN
```

Do not put the token directly in the workflow.

------------------------------------------------------------------------

## Error: Requested access to the resource is denied

Common reasons include:

-   Incorrect Docker Hub username.
-   Incorrect access token.
-   Token does not have permission to push.
-   Image/repository name is incorrect.

Check the Docker Hub account and repository details.

------------------------------------------------------------------------

## Workflow does not start after `git push`

Check that you pushed to:

``` text
main
```

because the workflow currently listens for:

``` yaml
branches:
  - main
```

Also confirm that the workflow file is located under:

``` text
.github/workflows/
```

------------------------------------------------------------------------

# Part 17 --- Important concept for students

The application **source code itself is not being pushed into Docker Hub
as normal files**.

There are two different repositories involved:

### GitHub repository

Stores things such as:

``` text
Java source code
pom.xml
Dockerfile
tests
GitHub Actions workflow
README
```

### Docker Hub repository

Stores the **Docker image** created from that source code.

Therefore the correct flow is:

``` text
Source Code
    |
    v
GitHub
    |
    v
GitHub Actions
    |
    v
Docker Image
    |
    v
Docker Hub
```

This distinction is important when explaining CI/CD.

------------------------------------------------------------------------

# Part 18 --- Quick checklist

Before running the demonstration, verify:

-   [ ] Application code is in GitHub.
-   [ ] `Dockerfile` exists.
-   [ ] Application builds successfully.
-   [ ] Docker Hub account exists.
-   [ ] Docker Hub repository exists.
-   [ ] Docker Hub access token exists.
-   [ ] `DOCKERHUB_USERNAME` exists in GitHub Secrets.
-   [ ] `DOCKERHUB_TOKEN` exists in GitHub Secrets.
-   [ ] `.github/workflows/docker-build-push.yml` exists.
-   [ ] Workflow listens to the `main` branch.
-   [ ] Code has been committed.
-   [ ] Code has been pushed to GitHub.
-   [ ] GitHub Actions workflow completes successfully.
-   [ ] Docker image appears in Docker Hub.

------------------------------------------------------------------------

# Summary

After this setup, the developer only needs to make a code change and
run:

``` bash
git add .
git commit -m "My changes"
git push origin main
```

Everything after that can happen automatically:

``` text
git push
   ↓
GitHub
   ↓
GitHub Actions
   ↓
Build Docker Image
   ↓
Push Docker Image
   ↓
Docker Hub
   ↓
Image ready to use
```

That is the basic CI/CD workflow for automatically building an
application and publishing its Docker image to Docker Hub.
