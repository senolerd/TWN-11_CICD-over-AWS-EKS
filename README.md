## Java Maven application CI-CD with Jenkins and AWS EKS-ECR 






Required applications expected to installed on Jenkins servers/containers that will be used at pipeline workflow;
- awscli2: For pulling EKS clusters config file
- helm: Installing/Updating the application and its further updates.


Plan:
- Write K8s manifest files for Deployment and Service configuration
- Integrate deploy step in the CI/CD pipeline to deploy newly built application image from DockerHub private registry to the EKS cluster
- So the complete CI/CD project we build has the following configuration:
    a. CI step: Increment version
    b. CI step: Build artifact for Java Maven Application
    c. CI step: Build and push Docker image to Docker Hub
    d. CD step: Deploy new application to EKS cluster
    e. Commit the version update








Quick Policy for the user going to be used for deployment pipeline:
```
{
	"Version": "2012-10-17",
	"Statement": [
		{
			"Sid": "DescribeCluster_for_getting_cluster_config_and_full_ecr",
			"Effect": "Allow",
			"Action": [
				"ecr:*",
				"eks:DescribeCluster"
			],
			"Resource": "*"
		}
	]
}
```