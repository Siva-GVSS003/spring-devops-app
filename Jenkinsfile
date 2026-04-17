pipeline {
    agent any
    // 'agent any' means: run this pipeline on any available Jenkins agent

    tools {
        maven 'Maven'
        // tells Jenkins to use Maven tool we configured in Global Tools
    }

    stages {

        // STAGE 1: Get code from GitHub
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/YOUR_USERNAME/spring-devops-app.git'
                echo 'Code checkout done!'
            }
        }

        // STAGE 2: Run unit tests
        stage('Test') {
            steps {
                echo 'Running tests...'
                sh 'mvn test'
            }
        }

        // STAGE 3: Build the JAR
        stage('Build') {
            steps {
                echo 'Building JAR...'
                sh 'mvn clean package -DskipTests'
                echo 'Build done!'
            }
        }

        // STAGE 4: Deploy using Ansible
        stage('Deploy') {
            steps {
                echo 'Deploying with Ansible...'
                sh 'ansible-playbook -i /etc/ansible/devops/inventory /etc/ansible/devops/deploy.yml'
                echo 'Deployment done!'
            }
        }
    }

    // AFTER PIPELINE: notify result
    post {
        success {
            echo '✅ Pipeline SUCCESS - App deployed!'
        }
        failure {
            echo '❌ Pipeline FAILED - Check the logs!'
        }
    }
}