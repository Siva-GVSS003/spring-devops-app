pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    environment {
        // Docker Hub credentials stored in Jenkins
        DOCKERHUB_USERNAME = "YOUR_DOCKERHUB_USERNAME"
        IMAGE_NAME = "${DOCKERHUB_USERNAME}/spring-devops-app"
        IMAGE_TAG = "latest"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/YOUR_GITHUB_USERNAME/spring-devops-app.git'
                echo 'Code checkout done!'
            }
        }

        stage('Test') {
            steps {
                echo 'Running tests...'
                sh 'mvn test'
            }
        }

        stage('Build JAR') {
            steps {
                echo 'Building JAR...'
                sh 'mvn clean package -DskipTests'
                echo 'JAR built successfully!'
            }
        }

        // NEW STAGE: Build Docker Image
        stage('Build Docker Image') {
            steps {
                echo 'Building Docker image...'
                sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
                echo 'Docker image built!'
            }
        }

        // NEW STAGE: Push to Docker Hub
        stage('Push to Docker Hub') {
            steps {
                echo 'Pushing to Docker Hub...'
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh "echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin"
                    sh "docker push ${IMAGE_NAME}:${IMAGE_TAG}"
                }
                echo 'Image pushed to Docker Hub!'
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying with Ansible...'
                sh 'ansible-playbook -i ansible/inventory ansible/deploy.yml'
                echo 'Deployment done!'
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline SUCCESS - App deployed as Docker container!'
        }
        failure {
            echo '❌ Pipeline FAILED - Check the logs!'
        }
    }
}