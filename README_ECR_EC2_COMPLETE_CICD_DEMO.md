# Complete CI/CD Demo: GitHub Actions → Amazon ECR → EC2 → Docker → Spring Boot

This guide is written for an **absolute beginner**. It covers the
complete deployment path:

``` text
Developer
   |
   | git push
   v
GitHub Repository
   |
   v
GitHub Actions
   |
   | OIDC authentication
   v
AWS IAM Role
   |
   v
Build Docker Image
   |
   v
Amazon ECR (N. Virginia / us-east-1)
   |
   | docker pull
   v
EC2 (Stockholm / eu-north-1)
   |
   v
Docker Container
   |
   v
Spring Boot Book API :8080
```

> **Important:** In this demo, ECR is in **N. Virginia (`us-east-1`)**
> and EC2 is in **Stockholm (`eu-north-1`)**. They do not have to be in
> the same region. The EC2 instance must explicitly authenticate to ECR
> in `us-east-1`.

------------------------------------------------------------------------

## 1. Demo-specific values

These values are already known for this demo:

  -------------------------------------------------------------------------------------------------
  Item                                Value
  ----------------------------------- -------------------------------------------------------------
  AWS Account ID                      `316255801172`

  ECR Region                          `us-east-1`

  ECR Repository                      `testbook-api`

  ECR Registry                        `316255801172.dkr.ecr.us-east-1.amazonaws.com`

  Full ECR Repository URI             `316255801172.dkr.ecr.us-east-1.amazonaws.com/testbook-api`

  GitHub AWS Role                     `arn:aws:iam::316255801172:role/GitHubActionsECRRole`

  EC2 Region                          `eu-north-1` (Stockholm)

  Application Port                    `8080`

  EC2 Ubuntu username                 `ubuntu`

  Container name                      `testbook-api`
  -------------------------------------------------------------------------------------------------

You will still need to substitute:

-   `<EC2_PUBLIC_IP>` with the public IPv4 address of your EC2 instance.
-   `<KEY_FILE>.pem` with the PEM file downloaded when the EC2 key pair
    is created.

------------------------------------------------------------------------

# PART A --- CREATE THE EC2 SECURITY GROUP

## 2. Open the EC2 console

1.  Sign in to the AWS Management Console.
2.  In the top-right region selector choose **Europe (Stockholm)
    `eu-north-1`**.
3.  Search for **EC2**.
4.  Open **EC2**.
5.  In the left menu choose **Security Groups**.
6.  Click **Create security group**.

## 3. Configure the security group

Example:

``` text
Security group name: AICICDDemo-SG
Description: Security group for Docker CI/CD classroom demo
VPC: Select the same/default VPC that will be used by the EC2 instance
```

### Inbound rules

Add these rules.

### Rule 1 --- SSH

``` text
Type: SSH
Protocol: TCP
Port: 22
Source: My IP
```

For a real system, do **not** normally expose SSH to the whole Internet.

If your public IP changes, you may need to edit this rule later.

### Rule 2 --- Spring Boot application

``` text
Type: Custom TCP
Protocol: TCP
Port: 8080
Source: Anywhere-IPv4
```

This permits the classroom browser/API demo:

``` text
http://<EC2_PUBLIC_IP>:8080
```

For a production system, the application would normally be behind a load
balancer/reverse proxy and port 8080 would not usually be opened to the
whole Internet.

### Outbound rules

For this demo, leave the normal default outbound rule:

``` text
All traffic
Destination: 0.0.0.0/0
```

This allows EC2 to download packages and communicate with Amazon ECR.

Click **Create security group**.

------------------------------------------------------------------------

# PART B --- CREATE THE EC2 KEY PAIR

## 4. Create a PEM key

1.  In EC2, choose **Key Pairs**.
2.  Click **Create key pair**.
3.  Example name:

``` text
aidemo
```

4.  Key pair type: **RSA**
5.  Private key file format: **`.pem`**
6.  Click **Create key pair**.

Your browser downloads something similar to:

``` text
aidemo.pem
```

Keep this file private.

> Never commit a `.pem` file to GitHub.

------------------------------------------------------------------------

# PART C --- LAUNCH THE UBUNTU EC2 INSTANCE

## 5. Launch an instance

1.  Go to **EC2 → Instances**.
2.  Click **Launch instances**.
3.  Name:

``` text
AICICDDemo
```

4.  Select an **Ubuntu Server** AMI suitable for the demo.
5.  Select an instance type eligible/appropriate for your account and
    demo workload.
6.  Under **Key pair**, select:

``` text
aidemo
```

7.  Under **Network settings**, choose **Select existing security
    group**.
8.  Select:

``` text
AICICDDemo-SG
```

9.  Review the storage configuration.
10. Click **Launch instance**.

Wait until:

``` text
Instance state: Running
Status checks: 2/2 checks passed
```

## 6. Find the public IP

Select the EC2 instance and copy:

``` text
Public IPv4 address
```

For example:

``` text
13.x.x.x
```

This guide calls it:

``` text
<EC2_PUBLIC_IP>
```

> A normal EC2 public IPv4 address can change after stop/start unless
> you use an Elastic IP. If your IP changes, update GitHub's `EC2_HOST`
> secret and any SSH security-group rule that depends on a specific IP.

------------------------------------------------------------------------

# PART D --- FIX THE PEM FILE PERMISSIONS

## 7. Open Terminal on the Mac

Go to the directory containing the PEM file.

For example:

``` bash
cd ~/Downloads
```

Check it:

``` bash
ls -l aidemo.pem
```

Restrict the private-key permissions:

``` bash
chmod 400 aidemo.pem
```

Check again:

``` bash
ls -l aidemo.pem
```

The purpose is to make the private key readable only by your user. SSH
rejects private keys that are too widely accessible.

------------------------------------------------------------------------

# PART E --- CONNECT TO EC2

## 8. SSH into Ubuntu

Run:

``` bash
ssh -i aidemo.pem ubuntu@<EC2_PUBLIC_IP>
```

Example:

``` bash
ssh -i aidemo.pem ubuntu@13.x.x.x
```

The first connection may ask:

``` text
Are you sure you want to continue connecting (yes/no/[fingerprint])?
```

Type:

``` text
yes
```

After connecting, the prompt will look similar to:

``` text
ubuntu@ip-xxx-xxx-xxx-xxx:~$
```

You are now executing commands **inside EC2**.

------------------------------------------------------------------------

# PART F --- INSTALL DOCKER ON EC2

## 9. Update Ubuntu

``` bash
sudo apt update
```

## 10. Install Docker

``` bash
sudo apt install -y docker.io
```

Start Docker:

``` bash
sudo systemctl start docker
```

Enable Docker after reboot:

``` bash
sudo systemctl enable docker
```

Check:

``` bash
docker --version
```

## 11. Allow the `ubuntu` user to use Docker

``` bash
sudo usermod -aG docker ubuntu
```

The new group membership normally requires a new login session.

Exit:

``` bash
exit
```

Reconnect:

``` bash
ssh -i aidemo.pem ubuntu@<EC2_PUBLIC_IP>
```

Test:

``` bash
docker ps
```

If that works without `sudo`, Docker is ready.

Optional test:

``` bash
docker run hello-world
```

------------------------------------------------------------------------

# PART G --- INSTALL AWS CLI ON EC2

## 12. Install AWS CLI

On Ubuntu:

``` bash
sudo apt update
sudo apt install -y awscli
```

Check:

``` bash
aws --version
```

If your Ubuntu package repositories do not provide the desired AWS CLI
package/version, install AWS CLI v2 using the current AWS installation
instructions.

------------------------------------------------------------------------

# PART H --- GIVE EC2 PERMISSION TO PULL FROM ECR

The EC2 server needs permission to **pull** the private Docker image.

Do not put an AWS access key and secret key on the EC2 server for this
demo. Use an **EC2 IAM role**.

## 13. Create an IAM role for EC2

1.  AWS Console → **IAM**.
2.  Choose **Roles**.
3.  Click **Create role**.
4.  Trusted entity type: **AWS service**.
5.  Use case: **EC2**.
6.  Continue to permissions.
7.  Search for:

``` text
AmazonEC2ContainerRegistryReadOnly
```

8.  Select it.
9.  Continue.
10. Role name:

``` text
EC2ECRPullRole
```

11. Click **Create role**.

This role allows EC2 to obtain ECR authorization information and pull
images.

## 14. Attach the IAM role to EC2

1.  Go to **EC2 → Instances**.
2.  Select `AICICDDemo`.
3.  Choose **Actions → Security → Modify IAM role**.
4.  Select:

``` text
EC2ECRPullRole
```

5.  Click **Update IAM role**.

No reboot should normally be required.

## 15. Verify the EC2 role

SSH into EC2 and run:

``` bash
aws sts get-caller-identity
```

You should see account `316255801172` and an assumed-role ARN referring
to `EC2ECRPullRole`.

If this fails, fix the EC2 IAM-role attachment before continuing.

------------------------------------------------------------------------

# PART I --- MANUALLY PULL AND RUN THE IMAGE FROM ECR

This section proves that **ECR → EC2 → Docker** works before automating
deployment.

## 16. Make sure an image already exists in ECR

Your GitHub push workflow should have pushed an image such as:

``` text
316255801172.dkr.ecr.us-east-1.amazonaws.com/testbook-api:latest
```

You can verify it in:

``` text
AWS Console
→ ECR
→ N. Virginia (us-east-1)
→ Private repositories
→ testbook-api
```

## 17. Login Docker on EC2 to ECR

Remember:

-   EC2 is in `eu-north-1`.
-   ECR is in `us-east-1`.
-   The command must therefore request the ECR password from
    **`us-east-1`**.

Run on EC2:

``` bash
aws ecr get-login-password --region us-east-1 \
| docker login \
  --username AWS \
  --password-stdin 316255801172.dkr.ecr.us-east-1.amazonaws.com
```

Expected:

``` text
Login Succeeded
```

`AWS` above is the fixed username used by ECR's Docker registry
authentication mechanism. It is not your personal IAM username.

## 18. Pull the image

``` bash
docker pull 316255801172.dkr.ecr.us-east-1.amazonaws.com/testbook-api:latest
```

Check:

``` bash
docker images
```

## 19. Run the container

First remove any old demo container if one exists:

``` bash
docker stop testbook-api || true
docker rm testbook-api || true
```

Now run:

``` bash
docker run -d \
  --name testbook-api \
  --restart unless-stopped \
  -p 8080:8080 \
  316255801172.dkr.ecr.us-east-1.amazonaws.com/testbook-api:latest
```

Check:

``` bash
docker ps
```

View logs:

``` bash
docker logs testbook-api
```

Follow logs live if required:

``` bash
docker logs -f testbook-api
```

Press `Ctrl+C` to stop following the logs; this does not stop the
container.

## 20. Test from inside EC2

If your API endpoint is `/api/books`:

``` bash
curl http://localhost:8080/api/books
```

Or test the root endpoint if your application exposes one:

``` bash
curl http://localhost:8080
```

## 21. Test from your Mac/browser

Use:

``` text
http://<EC2_PUBLIC_IP>:8080/api/books
```

If it works locally on EC2 but not from your Mac:

1.  Confirm the container is running with `docker ps`.
2.  Confirm `-p 8080:8080` was used.
3.  Confirm the EC2 security group allows inbound TCP `8080`.
4.  Confirm the application actually listens on port `8080`.

------------------------------------------------------------------------

# PART J --- UNDERSTAND THE AUTOMATED DEPLOYMENT

The manual test proved this section:

``` text
ECR
 |
 | docker pull
 v
EC2
 |
 | docker run
 v
Application
```

Now GitHub Actions will automate the complete sequence:

``` text
git push / manual workflow
        |
        v
GitHub Actions
        |
        +--> Assume GitHubActionsECRRole using OIDC
        |
        +--> Login to ECR
        |
        +--> docker build
        |
        +--> docker push
        |
        v
Amazon ECR
        |
        | GitHub connects to EC2 using SSH
        v
EC2
        |
        +--> Login to ECR using EC2ECRPullRole
        |
        +--> docker pull
        |
        +--> stop/remove old container
        |
        +--> docker run new image
        v
Application :8080
```

There are deliberately **two different AWS roles**:

``` text
GitHubActionsECRRole
Purpose: GitHub Actions can push to ECR.

EC2ECRPullRole
Purpose: EC2 can pull from ECR.
```

------------------------------------------------------------------------

# PART K --- CONFIGURE GITHUB SECRETS FOR SSH DEPLOYMENT

GitHub does **not** need AWS access keys because the ECR push uses OIDC.

However, in this simple classroom deployment GitHub needs SSH
information so it can connect to EC2.

## 22. Create `EC2_HOST`

GitHub repository:

``` text
Settings
→ Secrets and variables
→ Actions
→ New repository secret
```

Name:

``` text
EC2_HOST
```

Value:

``` text
<EC2_PUBLIC_IP>
```

## 23. Create `EC2_USERNAME`

Name:

``` text
EC2_USERNAME
```

Value:

``` text
ubuntu
```

## 24. Create `EC2_SSH_KEY`

Open the PEM file **locally** on your Mac:

``` bash
cat ~/Downloads/aidemo.pem
```

Copy the complete private-key contents, including:

``` text
-----BEGIN ... PRIVATE KEY-----
...
-----END ... PRIVATE KEY-----
```

Create GitHub repository secret:

``` text
EC2_SSH_KEY
```

Paste the complete PEM contents as its value.

> Never place the private key directly in the YAML file or commit it to
> Git.

------------------------------------------------------------------------

# PART L --- COMPLETE GITHUB ACTIONS WORKFLOW

Create:

``` text
.github/workflows/03-cicd-ecr-ec2-deploy.yml
```

Use:

``` yaml
name: "03 - Build, Push to ECR and Deploy to EC2"

on:
  workflow_dispatch:

permissions:
  id-token: write
  contents: read

env:
  AWS_REGION: us-east-1
  ECR_REPOSITORY: testbook-api
  ECR_REGISTRY: 316255801172.dkr.ecr.us-east-1.amazonaws.com
  CONTAINER_NAME: testbook-api

jobs:

  build-push-deploy:
    runs-on: ubuntu-latest

    steps:

      # ----------------------------------------------------
      # 1. CHECKOUT SOURCE CODE
      # ----------------------------------------------------
      - name: Checkout source code
        uses: actions/checkout@v6

      # ----------------------------------------------------
      # 2. ASSUME AWS IAM ROLE USING GITHUB OIDC
      # ----------------------------------------------------
      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v6.3.0
        with:
          role-to-assume: arn:aws:iam::316255801172:role/GitHubActionsECRRole
          aws-region: ${{ env.AWS_REGION }}

      # ----------------------------------------------------
      # 3. LOGIN GITHUB RUNNER'S DOCKER CLIENT TO ECR
      # ----------------------------------------------------
      - name: Login to Amazon ECR
        id: login-ecr
        uses: aws-actions/amazon-ecr-login@v2

      # ----------------------------------------------------
      # 4. BUILD DOCKER IMAGE
      # ----------------------------------------------------
      - name: Build Docker image
        run: |
          docker build \
            -t $ECR_REGISTRY/$ECR_REPOSITORY:latest \
            -t $ECR_REGISTRY/$ECR_REPOSITORY:${{ github.sha }} \
            .

      # ----------------------------------------------------
      # 5. PUSH IMAGE TO ECR
      # ----------------------------------------------------
      - name: Push Docker image to ECR
        run: |
          docker push $ECR_REGISTRY/$ECR_REPOSITORY:latest
          docker push $ECR_REGISTRY/$ECR_REPOSITORY:${{ github.sha }}

      # ----------------------------------------------------
      # 6. PREPARE SSH PRIVATE KEY ON THE TEMPORARY RUNNER
      # ----------------------------------------------------
      - name: Configure SSH key
        shell: bash
        run: |
          mkdir -p ~/.ssh
          printf '%s\n' "${{ secrets.EC2_SSH_KEY }}" > ~/.ssh/ec2_key.pem
          chmod 600 ~/.ssh/ec2_key.pem

          # Record the EC2 host key so SSH can verify the server.
          ssh-keyscan -H "${{ secrets.EC2_HOST }}" >> ~/.ssh/known_hosts

      # ----------------------------------------------------
      # 7. CONNECT TO EC2 AND DEPLOY
      # ----------------------------------------------------
      - name: Deploy latest image to EC2
        shell: bash
        run: |
          ssh \
            -i ~/.ssh/ec2_key.pem \
            "${{ secrets.EC2_USERNAME }}@${{ secrets.EC2_HOST }}" \
            'bash -s' <<'REMOTE_SCRIPT'

          set -e

          AWS_REGION="us-east-1"
          ECR_REGISTRY="316255801172.dkr.ecr.us-east-1.amazonaws.com"
          ECR_REPOSITORY="testbook-api"
          CONTAINER_NAME="testbook-api"

          echo "Authenticating EC2 Docker client with Amazon ECR..."

          aws ecr get-login-password --region "$AWS_REGION" \
          | docker login \
              --username AWS \
              --password-stdin "$ECR_REGISTRY"

          echo "Pulling latest Docker image..."

          docker pull \
            "$ECR_REGISTRY/$ECR_REPOSITORY:latest"

          echo "Stopping existing container if present..."

          docker stop "$CONTAINER_NAME" || true
          docker rm "$CONTAINER_NAME" || true

          echo "Starting new container..."

          docker run -d \
            --name "$CONTAINER_NAME" \
            --restart unless-stopped \
            -p 8080:8080 \
            "$ECR_REGISTRY/$ECR_REPOSITORY:latest"

          echo "Waiting for application startup..."
          sleep 15

          echo "Running container:"
          docker ps --filter "name=$CONTAINER_NAME"

          echo "Deployment completed."

          REMOTE_SCRIPT
```

------------------------------------------------------------------------

# PART M --- IMPORTANT SSH SECURITY-GROUP POINT FOR GITHUB-HOSTED RUNNERS

Your manual SSH rule may be:

``` text
SSH / TCP 22 / My IP
```

That allows **your Mac** to connect, but a GitHub-hosted Actions runner
does not normally originate from your home public IP.

Therefore the automated SSH step may fail even though manual SSH works.

For a short-lived classroom demo, you may temporarily choose to allow
SSH from a broader source, but opening port 22 to `0.0.0.0/0` increases
exposure and is not recommended as a normal deployment design.

A more production-oriented design avoids exposing SSH to changing
GitHub-hosted runner IPs, for example by using AWS Systems Manager or a
controlled/self-hosted runner/network path.

If you temporarily broaden SSH access for a classroom demonstration,
restore the restrictive rule immediately after the demo.

------------------------------------------------------------------------

# PART N --- RUN THE COMPLETE WORKFLOW

## 25. Commit the workflow

From your repository:

``` bash
git add .github/workflows/03-cicd-ecr-ec2-deploy.yml
git commit -m "Add ECR to EC2 deployment workflow"
git push origin main
```

## 26. Run it

In GitHub:

``` text
Repository
→ Actions
→ 03 - Build, Push to ECR and Deploy to EC2
→ Run workflow
```

Watch each step.

Expected sequence:

``` text
Checkout source code
        ↓
Configure AWS credentials
        ↓
Login to Amazon ECR
        ↓
Build Docker image
        ↓
Push Docker image to ECR
        ↓
Configure SSH key
        ↓
SSH to EC2
        ↓
EC2 login to ECR
        ↓
EC2 pull image
        ↓
Stop old container
        ↓
Start new container
        ↓
Deployment completed
```

------------------------------------------------------------------------

# PART O --- VERIFY THE AUTOMATED DEPLOYMENT

SSH to EC2:

``` bash
ssh -i aidemo.pem ubuntu@<EC2_PUBLIC_IP>
```

Check:

``` bash
docker ps
```

Check the image:

``` bash
docker images
```

Check logs:

``` bash
docker logs testbook-api
```

Test:

``` bash
curl http://localhost:8080/api/books
```

From your browser/Mac:

``` text
http://<EC2_PUBLIC_IP>:8080/api/books
```

------------------------------------------------------------------------

# PART P --- TROUBLESHOOTING

## Problem: `Permission denied (publickey)`

Check the PEM file:

``` bash
chmod 400 aidemo.pem
```

Confirm that the EC2 instance was launched using the corresponding key
pair and that the username is:

``` text
ubuntu
```

## Problem: SSH timeout

Check:

-   EC2 is running.
-   Public IP is correct.
-   Security group allows TCP 22 from the connecting source.
-   EC2 has a reachable public network path.

## Problem: `docker: permission denied`

Reconnect after:

``` bash
sudo usermod -aG docker ubuntu
```

Or temporarily test:

``` bash
sudo docker ps
```

## Problem: port 8080 already allocated

Check:

``` bash
docker ps
```

Stop/remove the old container:

``` bash
docker stop testbook-api || true
docker rm testbook-api || true
```

If another process owns the port:

``` bash
sudo ss -ltnp | grep :8080
```

## Problem: `Unable to locate credentials` on EC2

Run:

``` bash
aws sts get-caller-identity
```

If it fails, verify `EC2ECRPullRole` is attached to the EC2 instance.

## Problem: ECR `AccessDeniedException`

The EC2 IAM role needs ECR pull permissions. Confirm:

``` text
AmazonEC2ContainerRegistryReadOnly
```

is attached to `EC2ECRPullRole`.

For GitHub's push stage, confirm `GitHubActionsECRRole` has the required
ECR push permissions.

## Problem: ECR login points to wrong region

Your ECR is in:

``` text
us-east-1
```

Use:

``` bash
aws ecr get-login-password --region us-east-1
```

not `eu-north-1`.

The EC2 instance being in Stockholm does not change the ECR repository's
region.

## Problem: `Cannot perform an interactive login from a non TTY device`

Usually inspect the error immediately before it. If:

``` bash
aws ecr get-login-password ...
```

fails, Docker receives no password.

Test first:

``` bash
aws sts get-caller-identity
```

and then:

``` bash
aws ecr get-login-password --region us-east-1
```

## Problem: container starts and exits

Run:

``` bash
docker ps -a
```

Then:

``` bash
docker logs testbook-api
```

The application logs usually reveal the actual Spring Boot startup
problem.

------------------------------------------------------------------------

# PART Q --- STOPPING THE DEMO WITHOUT LOSING THE EC2 CONFIGURATION

When the class/demo is finished, you can normally **stop** the EC2
instance instead of terminating it.

Stopping preserves the EBS-backed instance storage and installed
software, subject to your instance/storage configuration.

Do not choose **Terminate** unless you intend to remove the instance.

Remember that a normal public IPv4 address may change after a
stop/start. If it changes:

1.  Update `EC2_HOST` in GitHub Secrets.
2.  Use the new IP for manual SSH.
3.  Review security-group source rules.

------------------------------------------------------------------------

# PART R --- WHAT EACH SECURITY MECHANISM IS DOING

## GitHub → AWS

``` text
GitHub OIDC token
        ↓
AWS validates GitHub identity
        ↓
GitHubActionsECRRole
        ↓
Temporary AWS credentials
        ↓
ECR push permission
```

No permanent AWS access key or secret key needs to be stored in GitHub
for the ECR push.

## GitHub → EC2

For this beginner SSH deployment:

``` text
GitHub Secret: EC2_SSH_KEY
        ↓
SSH
        ↓
EC2
```

This is separate from AWS OIDC.

## EC2 → ECR

``` text
EC2 instance
        ↓
EC2ECRPullRole
        ↓
Temporary AWS credentials from the instance role
        ↓
ECR authorization
        ↓
docker pull
```

No permanent IAM-user access key needs to be stored on EC2.

------------------------------------------------------------------------

# PART S --- FINAL BEGINNER CHECKLIST

Before the demo, verify every item:

-   [ ] ECR `testbook-api` exists in `us-east-1`.
-   [ ] GitHub OIDC provider exists in IAM.
-   [ ] `GitHubActionsECRRole` exists.
-   [ ] GitHub role trust policy permits the intended
    repository/workflow identity.
-   [ ] GitHub role has ECR push permissions.
-   [ ] EC2 instance exists in Stockholm.
-   [ ] EC2 security group allows your manual SSH connection.
-   [ ] TCP 8080 is allowed for the demo client.
-   [ ] PEM file is available.
-   [ ] PEM permissions are restricted.
-   [ ] Manual SSH works.
-   [ ] Docker is installed on EC2.
-   [ ] AWS CLI is installed on EC2.
-   [ ] `EC2ECRPullRole` is attached to EC2.
-   [ ] `aws sts get-caller-identity` works on EC2.
-   [ ] Manual ECR login from EC2 works.
-   [ ] Manual `docker pull` works.
-   [ ] Manual `docker run -p 8080:8080` works.
-   [ ] Application is reachable on port 8080.
-   [ ] GitHub `EC2_HOST` secret exists.
-   [ ] GitHub `EC2_USERNAME` secret exists.
-   [ ] GitHub `EC2_SSH_KEY` secret exists.
-   [ ] Automated SSH source is permitted by the EC2 security group for
    the demo.
-   [ ] Full GitHub Actions workflow succeeds.
-   [ ] New container is running on EC2.
-   [ ] Application endpoint responds after automated deployment.

------------------------------------------------------------------------

# Classroom summary

The complete demonstration can be taught in three stages:

``` text
DEMO 1 — CI
GitHub → Build/Test

DEMO 2 — IMAGE PUBLISHING
GitHub → GitHub Actions → Docker Build → ECR

DEMO 3 — COMPLETE CI/CD
GitHub
   ↓
GitHub Actions
   ↓
Build/Test
   ↓
Docker Build
   ↓
ECR
   ↓
EC2
   ↓
Docker
   ↓
Spring Boot Application
```

The most important distinction is:

``` text
GitHubActionsECRRole
= permission for GitHub Actions to push to ECR

EC2ECRPullRole
= permission for EC2 to pull from ECR

EC2_SSH_KEY
= permission for the deployment workflow to SSH into EC2
```

This keeps the responsibilities separate and makes the complete CI/CD
security model easier to understand.
