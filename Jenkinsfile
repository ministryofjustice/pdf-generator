def prepare_env() {
    sh '''
    #!/usr/env/bin bash
    docker pull mojdigitalstudio/hmpps-oraclejdk-builder:latest
    '''
}

pipeline {

    agent { label "jenkins_slave" }

    stages {

        stage('Setup') {
            steps {
                prepare_env()
            }
        }

        stage('Build') {
            steps {
                sh '''
                    docker run --rm -v `pwd`:/home/tools/data mojdigitalstudio/hmpps-oraclejdk-builder bash -c "./gradlew build"
                    mkdir -p ./build/artifacts
                    cp build/libs/*.jar build/artifacts/
                    ls -1 build/artifacts/pdfGenerator-*.jar | sed \'s/^.*pdfGenerator-\\(.*\\)\\.jar.*$/\\1/\' > build/artifacts/version.txt
                    cp build/artifacts/pdfGenerator*.jar build/artifacts/pdfGenerator.jar
                '''
            }
        }

        stage ('Package') {
            steps {
                sh 'docker build -t 895523100917.dkr.ecr.eu-west-2.amazonaws.com/hmpps/pdf-generator:latest --file ./Dockerfile .'
                sh 'aws ecr get-login --no-include-email --region eu-west-2 | source /dev/stdin'
                sh 'docker push 895523100917.dkr.ecr.eu-west-2.amazonaws.com/hmpps/pdf-generator:latest'
            }
        }

        stage('trigger deployment') {
            steps {
                build job: 'New_Tech/Deploy_PDF_Generator', parameters: [[$class: 'StringParameterValue', name: 'environment_type', value: 'dev']]
            }
        }

    }

    post {
        always {
            deleteDir()
        }
    }

}
