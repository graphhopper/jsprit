#!groovy
@Library('ci-scripts') _

pipeline {
    agent any

    stages {
        stage('Test') {
            environment {
                CODECOV_TOKEN = '5b1293bb-5536-4f60-bfa3-93b4d15eefeb'
            }
            steps {
                script {
                    docker.image('maven:3.5.3-jdk-10-slim').inside() {
                        sh 'mvn test'
                    }
                }
            }
            post {
                always {
                    junit(testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true)
                    sh 'curl -s https://codecov.io/bash | bash'
                }
            }
        }

        stage('Push to Maven') {
            when {
                expression { env.BRANCH_NAME in ['master', 'staging'] }
            }

            steps {
                deployToMaven()
            }
        }
    }

    post {
        always {
            pipelineUtils('cleanWorkSpace')
        }
    }
}
