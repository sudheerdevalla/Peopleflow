pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat 'mvnw.cmd clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                bat 'mvnw.cmd test'
            }
        }

        stage('Archive Artifact') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Deploy to AWS') {
            steps {
                sshagent(['peopleflow-aws']) {
                    bat '''
                        scp -o StrictHostKeyChecking=no target\\hrapp-0.0.1-SNAPSHOT.jar ubuntu@peopleflow.renwion.in:/home/ubuntu/Peopleflow/target/hrapp-0.0.1-SNAPSHOT.jar

                        ssh -o StrictHostKeyChecking=no ubuntu@peopleflow.renwion.in "sudo systemctl restart peopleflow"

                        ssh -o StrictHostKeyChecking=no ubuntu@peopleflow.renwion.in "sudo systemctl is-active peopleflow"
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'PeopleFlow CI/CD build and deployment completed successfully!'
        }

        failure {
            echo 'PeopleFlow CI/CD build or deployment failed.'
        }
    }
}
