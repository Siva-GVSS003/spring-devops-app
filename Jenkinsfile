pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    environment {
        DOCKERHUB_USERNAME = "dockersiva003"
        IMAGE_NAME = "dockersiva003/spring-devops-app"
        IMAGE_TAG = "${BUILD_NUMBER}-${BUILD_NUMBER}"
        SONAR_URL = "http://172.31.75.66:9000"
        NEXUS_URL = "http://172.31.36.37:8081"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/Siva-GVSS003/spring-devops-app.git'
                echo "Checked out branch: ${env.BRANCH_NAME}"
            }
        }

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

        stage('Quality Gate') {
            steps {
                echo 'Checking Quality Gate...'
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        // NEW: Publish JAR to Nexus
        stage('Publish to Nexus') {
            steps {
                echo 'Publishing JAR to Nexus...'
                sh '''
                    mvn deploy \
                    -DskipTests \
                    -s /var/lib/jenkins/settings.xml
                '''
                echo "JAR published to Nexus! ✅"
            }
        }

        stage('Build Docker Image') {
            steps {
                echo 'Building Docker image...'
                sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
                echo "Built image: ${IMAGE_NAME}:${IMAGE_TAG}"
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
                echo 'Image pushed to Docker Hub!'
            }
        }

         stage('Deploy to DEV') {
            when {
                branch 'dev'
            }
            steps {
                echo '🚀 Deploying to DEV environment...'
                withCredentials([string(
                    credentialsId: 'ansible-vault-pass',
                    variable: 'VAULT_PASS'
                )]) {
                    sh """
                        echo "\$VAULT_PASS" > /tmp/vault-pass.txt
                        ansible-playbook \
                        -i ansible/inventory \
                        ansible/deploy.yml \
                        -e target_env=dev \
                        -e image_tag=${IMAGE_TAG} \
                        --vault-password-file /tmp/vault-pass.txt
                        rm -f /tmp/vault-pass.txt
                    """
                }
                echo '✅ Deployed to DEV!'
            }
        }

        // Deploy to STAGING — only on staging branch
        stage('Deploy to STAGING') {
            when {
                branch 'staging'
            }
            steps {
                echo '🚀 Deploying to STAGING environment...'
                withCredentials([string(
                    credentialsId: 'ansible-vault-pass',
                    variable: 'VAULT_PASS'
                )]) {
                    sh """
                        echo "\$VAULT_PASS" > /tmp/vault-pass.txt
                        ansible-playbook \
                        -i ansible/inventory \
                        ansible/deploy.yml \
                        -e target_env=staging \
                        -e image_tag=${IMAGE_TAG} \
                        --vault-password-file /tmp/vault-pass.txt
                        rm -f /tmp/vault-pass.txt
                    """
                }
                echo '✅ Deployed to STAGING!'
            }
        }

        // Deploy to PROD — only on main branch
        stage('Deploy to PROD') {
            when {
                branch 'main'
            }
            steps {
                echo '🚀 Deploying to PROD environment...'
                withCredentials([string(
                    credentialsId: 'ansible-vault-pass',
                    variable: 'VAULT_PASS'
                )]) {
                    sh """
                        echo "\$VAULT_PASS" > /tmp/vault-pass.txt
                        ansible-playbook \
                        -i ansible/inventory \
                        ansible/deploy.yml \
                        -e target_env=prod \
                        -e image_tag=${IMAGE_TAG} \
                        --vault-password-file /tmp/vault-pass.txt
                        rm -f /tmp/vault-pass.txt
                    """
                }
                echo '✅ Deployed to PROD!'
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline SUCCESS on branch: ${env.BRANCH_NAME}"
        }
        failure {
            echo "❌ Pipeline FAILED on branch: ${env.BRANCH_NAME}"
        }
    }
}