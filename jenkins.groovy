pipeline {
    agent any 
    stages{
        stage('PULL'){
            steps{
                git branch: 'main', url: 'https://github.com/mayurmwagh/flight-reservation-demo.git'
            }
        }
        stage('BUILD'){
            steps{
                sh '''
                    cd FlightReservationApplication
                    mvn clean package
                '''
            }
        }
        stage('QA-TEST'){
            steps{
               withSonarQubeEnv(installationName: 'sonar', credentialsId: 'sonar-demo') {
                sh '''
                    cd FlightReservationApplication
                    mvn sonar:sonar -Dsonar.projectKey=flight-reservation-backend-demo 
                '''
                } 
            }
        }
        stage('Quality-Gate'){
            steps{
                timeout(time: 10, unit:'MINUTES'){
                    waitForQualityGate abortPipeline: true 
                }
            }
        }
        stage('Docker-Build'){
            steps{
                sh '''
                    cd FlightReservationApplication
                    docker build -t mayurwagh/flight-reservation-demo:latest . 
                    docker push mayurwagh/flight-reservation-demo:latest
                    docker rmi mayurwagh/flight-reservation-demo:latest
                '''
            }
        }
        stage('Deploy'){
            steps{
                sh '''
                    cd FlightReservationApplication
                    kubectl apply -f k8s/
                '''
            }
        }
    }
}