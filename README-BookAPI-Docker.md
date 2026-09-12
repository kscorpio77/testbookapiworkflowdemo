# Book API Docker Image --- Build, Push, Download and Run

This guide shows how to package the Spring Boot Book API as a Docker
image, upload it to Docker Hub, and then download and run it on any
laptop with Docker installed.

## Details used in this guide

-   Docker Hub username: `gigtechmentor`
-   Docker image/repository name: `bookapi`
-   Version: `1.0`
-   Full Docker image name: `gigtechmentor/bookapi:1.0`
-   Spring Boot port: `8080`

------------------------------------------------------------------------

## 1. Install and start Docker

Install Docker Desktop if it is not already installed.

Start Docker Desktop and confirm Docker is running:

``` bash
docker --version
```

You can also check:

``` bash
docker info
```

------------------------------------------------------------------------

## 2. Make sure the project contains the required files

Open a terminal in the root folder of the Spring Boot project.

The folder should contain at least:

``` text
project-folder/
├── Dockerfile
├── pom.xml
└── src/
```

Run:

``` bash
ls
```

Make sure you can see `Dockerfile`, `pom.xml`, and `src`.

------------------------------------------------------------------------

## 3. Create the Dockerfile

The Dockerfile should start the Spring Boot application, rather than
only running the test cases.

Use:

``` dockerfile
# Stage 1 - Build the Spring Boot application
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml .

RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests


# Stage 2 - Run the finished Spring Boot application
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
```

In simple terms, this Dockerfile:

1.  Starts with Maven and Java 17.
2.  Copies the project information.
3.  Downloads the libraries needed by the project.
4.  Copies the source code.
5.  Builds the Spring Boot application into a JAR file.
6.  Creates a smaller environment containing Java.
7.  Copies the finished JAR into it.
8.  Starts the Spring Boot application.

------------------------------------------------------------------------

## 4. Log in to Docker Hub

You need to log in before you can push an image to Docker Hub.

Run:

``` bash
docker login
```

Docker may give you a code and ask you to complete the login in your
browser.

Alternatively:

``` bash
docker login -u gigtechmentor
```

Complete the authentication.

------------------------------------------------------------------------

## 5. Build the Docker image

From the project folder containing the Dockerfile, run:

``` bash
docker build -t gigtechmentor/bookapi:1.0 .
```

Important: do not forget the `.` at the end.

The command means:

-   `docker build` --- create a Docker image.
-   `-t` --- give the image a name and version.
-   `gigtechmentor` --- Docker Hub username.
-   `bookapi` --- image/repository name.
-   `1.0` --- version.
-   `.` --- use the Dockerfile and project files from the current
    folder.

------------------------------------------------------------------------

## 6. Check that the image was created

Run:

``` bash
docker images
```

You should see something similar to:

``` text
REPOSITORY                 TAG
gigtechmentor/bookapi      1.0
```

------------------------------------------------------------------------

## 7. Test the image locally before uploading it

Run:

``` bash
docker run -d -p 8080:8080 --name bookapi gigtechmentor/bookapi:1.0
```

Check that the container is running:

``` bash
docker ps
```

Check the application logs if required:

``` bash
docker logs -f bookapi
```

Open the application or its API endpoint using:

``` text
http://localhost:8080
```

Use the appropriate Book API endpoint if the application does not have a
page at `/`.

To stop the application:

``` bash
docker stop bookapi
```

To remove the test container:

``` bash
docker rm bookapi
```

------------------------------------------------------------------------

## 8. Push version 1.0 to Docker Hub

Once the local test is successful, upload the image:

``` bash
docker push gigtechmentor/bookapi:1.0
```

Docker will upload the image layers to Docker Hub.

The image name on Docker Hub will be:

``` text
gigtechmentor/bookapi:1.0
```

For students to download it without signing in, make sure the Docker Hub
repository is public.

------------------------------------------------------------------------

## 9. Verify the image on Docker Hub

Sign in to Docker Hub and open the `bookapi` repository under the
`gigtechmentor` account.

Check that the tag/version is shown as:

``` text
1.0
```

At this point, the image is ready to distribute.

------------------------------------------------------------------------

# Student Instructions

Students do NOT need the source code, Maven, Java, IntelliJ, or the
Dockerfile.

They only need Docker installed and running.

## 10. Download the image manually

To explicitly download the image from Docker Hub:

``` bash
docker pull gigtechmentor/bookapi:1.0
```

Check it:

``` bash
docker images
```

You should see:

``` text
gigtechmentor/bookapi    1.0
```

------------------------------------------------------------------------

## 11. Run the application

Run:

``` bash
docker run -d -p 8080:8080 --name bookapi gigtechmentor/bookapi:1.0
```

Then open:

``` text
http://localhost:8080
```

The Spring Boot application should now be running locally.

------------------------------------------------------------------------

## 12. One-command option for students

Students do not actually have to run `docker pull` first.

They can directly run:

``` bash
docker run -d -p 8080:8080 --name bookapi gigtechmentor/bookapi:1.0
```

If version `1.0` is not already on their laptop, Docker automatically
downloads it from Docker Hub and then starts the application.

So the normal student experience is simply:

``` text
Install Docker Desktop
        ↓
Start Docker Desktop
        ↓
Run one command
        ↓
docker run -d -p 8080:8080 --name bookapi gigtechmentor/bookapi:1.0
        ↓
Docker downloads bookapi:1.0 if required
        ↓
Spring Boot starts
        ↓
http://localhost:8080
```

------------------------------------------------------------------------

## 13. Stop and start the application later

Stop:

``` bash
docker stop bookapi
```

Start the same container again:

``` bash
docker start bookapi
```

There is no need to run `docker run` again if the container still
exists.

Check whether it is running:

``` bash
docker ps
```

Show running and stopped containers:

``` bash
docker ps -a
```

------------------------------------------------------------------------

## 14. Remove the container

If you want to completely remove the container:

``` bash
docker rm -f bookapi
```

You can create it again with:

``` bash
docker run -d -p 8080:8080 --name bookapi gigtechmentor/bookapi:1.0
```

------------------------------------------------------------------------

## 15. Remove the downloaded Docker image

If required:

``` bash
docker rmi gigtechmentor/bookapi:1.0
```

You can download it again:

``` bash
docker pull gigtechmentor/bookapi:1.0
```

------------------------------------------------------------------------

# Complete Command Sequence for the Trainer

``` bash
# Login
docker login

# Build
docker build -t gigtechmentor/bookapi:1.0 .

# Check image
docker images

# Test locally
docker run -d -p 8080:8080 --name bookapi gigtechmentor/bookapi:1.0

# Check container
docker ps

# View logs if needed
docker logs -f bookapi

# Stop and remove test container
docker stop bookapi
docker rm bookapi

# Push to Docker Hub
docker push gigtechmentor/bookapi:1.0
```

------------------------------------------------------------------------

# Complete Command Sequence for a Student

The simplest option:

``` bash
docker run -d -p 8080:8080 --name bookapi gigtechmentor/bookapi:1.0
```

Or, to demonstrate downloading and running as two separate operations:

``` bash
docker pull gigtechmentor/bookapi:1.0

docker run -d -p 8080:8080 --name bookapi gigtechmentor/bookapi:1.0
```

Then use:

``` text
http://localhost:8080
```

------------------------------------------------------------------------

## Final flow

``` text
TRAINER

Spring Boot source code
        ↓
Dockerfile
        ↓
docker build
        ↓
gigtechmentor/bookapi:1.0
        ↓
docker push
        ↓
Docker Hub


STUDENT

Docker Hub
        ↓
gigtechmentor/bookapi:1.0
        ↓
docker run
        ↓
Docker automatically downloads the image
        ↓
Container starts
        ↓
Spring Boot application runs
        ↓
http://localhost:8080
```

## Important note about databases

If the Book API requires a separate MySQL, PostgreSQL, or other database
to start successfully, that database must also be available. In that
situation, Docker Compose is usually a better student setup because one
command can start both the Book API and its database.
