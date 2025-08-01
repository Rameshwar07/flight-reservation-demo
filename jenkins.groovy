pipeline {
    agent any

    environment {
        IMAGE_NAME = 'rameshwar07/flight-reservation-demo:latest'
    }

    stages {
        stage('PULL') {
            steps {
                git branch: 'main', url: 'https://github.com/Rameshwar07/flight-reservation-demo.git'
            }
        }

        stage('BUILD') {
            steps {
                dir("${WORKSPACE}/flight-reservation-demo") {
                    sh 'mvn clean package'
                }
            }
        }

        stage('QA-TEST') {
            steps {
                dir("${WORKSPACE}/flight-reservation-demo") {
                    withSonarQubeEnv(installationName: 'sonar', credentialsId: 'sonar-token') {
                        sh 'mvn sonar:sonar -Dsonar.projectKey=flight-reservation-demo'
                    }
                }
            }
        }

        stage('Quality-Gate') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Docker-Build') {
            steps {
                dir("${WORKSPACE}/flight-reservation-demo") {
                    sh """
                        docker build -t ${IMAGE_NAME} .
                        docker push ${IMAGE_NAME}
                        docker rmi ${IMAGE_NAME}
                    """
                }
            }
        }

        stage('DEPLOY') {
            steps {
                sh '''
                    sudo mkdir -p /opt/flight-reservation-demo
                    sudo cp ${WORKSPACE}/flight-reservation-demo/target/*.jar /opt/flight-reservation-demo/
                '''
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline executed successfully!'
        }
        failure {
            echo '❌ Pipeline failed!'
        }
    }
}
