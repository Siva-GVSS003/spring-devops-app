pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    environment {
        DOCKERHUB_USERNAME = "dockersiva003"
        IMAGE_NAME = "dockersiva003/spring-devops-app"
        //IMAGE_TAG = "${env.BRANCH_NAME}-${BUILD_NUMBER}"
        SONAR_URL = "http://172.31.75.66:9000"
        NEXUS_URL = "http://172.31.36.37:8081"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: "${env.BRANCH_NAME}",
                    url: 'https://github.com/Siva-GVSS003/spring-devops-app.git'
                echo "Checked out branch: ${env.BRANCH_NAME}"
            }
        }
        stage('Set Version') {
            steps {
                script {
                    if (env.BRANCH_NAME == 'main') {
                        // PROD → stable release version
                        env.APP_VERSION = "1.0.${BUILD_NUMBER}"
                        env.IMAGE_TAG = "1.0.${BUILD_NUMBER}"
                        env.NEXUS_REPO = "nexus-releases"
                        echo "PROD Release version: ${env.APP_VERSION}"
                    } else {
                        // DEV/STAGING → snapshot version
                        env.APP_VERSION = "1.0-SNAPSHOT"
                        env.IMAGE_TAG = "${env.BRANCH_NAME}-${BUILD_NUMBER}"
                        env.NEXUS_REPO = "nexus-snapshots"
                        echo "SNAPSHOT version: ${env.APP_VERSION}"
                    }
                }
            }
        }

         stage('Test') {
            steps {
                sh "mvn test jacoco:report -Drevision=${env.APP_VERSION}"
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh """
                        mvn sonar:sonar \
                        -Drevision=${env.APP_VERSION} \
                        -Dsonar.projectKey=spring-devops-app \
                        -Dsonar.host.url=${SONAR_URL} \
                        -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                    """
                }
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
                script {
                    if (env.BRANCH_NAME == 'main') {
                        echo "Publishing RELEASE ${env.APP_VERSION} to nexus-releases..."
                    } else {
                        echo "Publishing SNAPSHOT ${env.APP_VERSION} to nexus-snapshots..."
                    }
                }
                sh """
                    mvn deploy \
                    -Drevision=${env.APP_VERSION} \
                    -DskipTests \
                    -s /var/lib/jenkins/settings.xml
                """
                echo "✅ Published to Nexus!"
            }
        }
        stage('Build Docker Image') {
            steps {
                echo 'Building Docker image...'
                sh "docker build -t ${IMAGE_NAME}:${env.IMAGE_TAG} ."
                echo "Built image: ${IMAGE_NAME}:${env.IMAGE_TAG}"
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
                    sh "docker push ${IMAGE_NAME}:${env.IMAGE_TAG}"
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
                        -e image_tag=${env.IMAGE_TAG} \
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
                        -e image_tag=${env.IMAGE_TAG} \
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
                        -e image_tag=${env.IMAGE_TAG} \
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
            echo "✅ SUCCESS - Branch: ${env.BRANCH_NAME} - Version: ${env.APP_VERSION} - Image: ${IMAGE_NAME}:${env.IMAGE_TAG}"
        }
        failure {
            echo "❌ Pipeline FAILED on branch: ${env.BRANCH_NAME}"
        }
    }
}