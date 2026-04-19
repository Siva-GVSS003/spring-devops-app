pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    environment {
        // Docker Hub credentials stored in Jenkins
        DOCKERHUB_USERNAME = "dockersiva003"
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
                withCredentials([string(
                    credentialsId: 'ansible-vault-pass',
                    // 👆 use the ID you gave when creating secret text
                    variable: 'VAULT_PASS'
                )]) {
                    sh '''
                        echo "$VAULT_PASS" > /tmp/vault-pass.txt
                        ansible-playbook \
                        -i ansible/inventory \
                        ansible/deploy.yml \
                        --vault-password-file /tmp/vault-pass.txt
                        rm -f /tmp/vault-pass.txt
                        # delete immediately after use for security!
                    '''
                }
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