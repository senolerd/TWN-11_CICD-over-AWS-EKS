



Plan:
- Write K8s manifest files for Deployment and Service configuration
- Integrate deploy step in the CI/CD pipeline to deploy newly built application image from DockerHub private registry to the EKS cluster
- So the complete CI/CD project we build has the following configuration:
    a. CI step: Increment version
    b. CI step: Build artifact for Java Maven Application
    c. CI step: Build and push Docker image to Docker Hub
    d. CD step: Deploy new application to EKS cluster
    e. Commit the version update


.
