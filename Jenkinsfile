pipeline {
    agent any

    environment {
        DOCKERHUB_USERNAME = "dockersiva003"
        IMAGE_NAME = "dockersiva003/spring-devops-app"
        IMAGE_TAG = "latest"
        SONAR_URL = "http://172.31.75.66:9000"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/Siva-GVSS003/spring-devops-app.git'
                echo 'Code checkout done!'
            }
        }

        // NEW: Unit Tests + Coverage Report
        stage('Test') {
            steps {
                echo 'Running unit tests...'
                sh 'mvn test jacoco:report'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    echo 'Test report published!'
                }
            }
        }

        // NEW: SonarQube Code Quality Analysis
        stage('SonarQube Analysis') {
            steps {
                echo 'Running SonarQube analysis...'
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn sonar:sonar \
                        -Dsonar.projectKey=spring-devops-app \
                        -Dsonar.host.url=${SONAR_URL} \
                        -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                    '''
                }
                echo 'SonarQube analysis done!'
            }
        }

        // NEW: Quality Gate Check
        stage('Quality Gate') {
            steps {
                echo 'Checking Quality Gate...'
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                    // if quality gate fails → pipeline stops!
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                echo 'Building Docker image...'
                sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
                echo 'Docker image built!'
            }
        }

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
                echo 'Image pushed!'
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying with Ansible...'
                withCredentials([string(
                    credentialsId: 'ansible-vault-pass',
                    variable: 'VAULT_PASS'
                )]) {
                    sh '''
                        echo "$VAULT_PASS" > /tmp/vault-pass.txt
                        ansible-playbook \
                        -i ansible/inventory \
                        ansible/deploy.yml \
                        --vault-password-file /tmp/vault-pass.txt
                        rm -f /tmp/vault-pass.txt
                    '''
                }
                echo 'Deployment done!'
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline SUCCESS - App deployed!'
        }
        failure {
            echo '❌ Pipeline FAILED - Check logs!'
        }
    }
}