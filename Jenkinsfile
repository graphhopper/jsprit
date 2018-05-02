#!groovy

pipeline {
    agent {
        docker { image 'maven:3.5.3-jdk-10-slim' }
    }

    stages {
        stage('Test') {
            environment {
                CODECOV_TOKEN = '5b1293bb-5536-4f60-bfa3-93b4d15eefeb'
            }
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit(testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true)
                    sh 'curl -s https://codecov.io/bash | bash'
                }
            }
        }
    }
}
