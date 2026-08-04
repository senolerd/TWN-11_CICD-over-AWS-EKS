## Java Maven application CI-CD with Jenkins and AWS EKS-ECR 

Required applications expected to installed on Jenkins servers/containers that will be used at pipeline workflow;
- awscli2: For pulling EKS clusters config file
- helm: Installing/Updating the application and its further updates.


Pipeline's workflow stages;
- Pulls java maven app from GitHub
- Compiles app code
- Creates OCI image, tags for ECR and push the image repository
	- Asks AWS ECR login token via aws cli
	- Asks AWS STS what is the Account ID of the given Access Key via aws cli
	- Builds a registry address from Accound ID, and logins to registry
	- Checks repo's existence. Will be created if it's not exist.
	- Builds image with AWS ECR repository, application version and jenkins build no
	- Pushes the repo
- Deploys (creates/upgrades) a new Helm chart to with new image to AWS EKS
- Bumps up the app version in pom.xml for next minor version
- Pushes version updated pom.xml to codebase 

<br>

*Ingress activity (Gateway Class, Gateway and HTTPRoute) is not included to heither to pipeline nor helm chart on purpose.*

*There is another jenkins file "Jenkinsfile-local-kvm" if it would like to be run against to LKE, baremetal, kvm kind of k8s clusters* 


<br>

Quick Policy for the user going to be used their Access Key for deployment pipeline:
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