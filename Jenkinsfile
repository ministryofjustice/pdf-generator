def get_pdfgenerator_version() {
    sh """
    branch=\$(echo ${GIT_BRANCH} | sed 's/\\//_/g')
    build_prefix="\$(grep "version = " build.gradle | awk -F = '{print \$2}' | sed 's/\\'//g')${BUILD_NUMBER}"
    if [ \\"\$branch\\" = \\"master\\" ]; then
        echo "Master Branch build detected"
        echo \$build_prefix${BUILD_NUMBER} > pdfgenerator.version;       
    else
        echo "Non Master Branch build detected"
        echo \$build_prefix${BUILD_NUMBER}-\$branch > pdfgenerator.version;
    fi
    """
    return readFile("./pdfgenerator.version")
}

pipeline {
    agent { label "jenkins_slave" }

    environment {
        docker_image = "hmpps/new-tech-pdfgenerator"
        aws_region = 'eu-west-2'
        ecr_repo = ''
        pdfgenerator_VERSION = get_pdfgenerator_version()
    }

    options { 
        disableConcurrentBuilds() 
    }

    stages {
        stage ('Notify build started') {
            steps {
                slackSend(message: "Build Started - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL.replace('http://', 'https://').replace(':8080', '')}|Open>)")
            }
        }

        stage ('Initialize') {
            steps {
                sh '''
                    #!/bin/bash +x
                    echo "PATH = ${PATH}"
                    echo "pdfgenerator_VERSION = ${pdfgenerator_VERSION}"
                '''
            }
        }

       stage('Verify Prerequisites') {
           steps {
               sh '''
                    #!/bin/bash +x
                    echo "Testing AWS Connectivity and Credentials"
                    aws sts get-caller-identity
               '''
           }
       }

       stage('Gradle Build') {
           steps {
                sh '''
                    #!/bin/bash +x
                    make gradle-build pdfgenerator_version=${pdfgenerator_VERSION};
                '''
           }
       }

        stage('Get ECR Login') {
            steps {
                sh '''
                    #!/bin/bash +x
                    make ecr-login
                    ls -ail
                '''
                // Stash the ecr repo to save a repeat aws api call
                stash includes: 'ecr.repo', name: 'ecr.repo'
            }
        }
        stage('Build Docker image') {
           steps {
                unstash 'ecr.repo'
                sh '''
                    #!/bin/bash +x
                    make build pdfgenerator_version=${pdfgenerator_VERSION}
                '''
            }
        }
        stage('Image Tests') {
            steps {
                // Run dgoss tests
                sh '''
                    #!/bin/bash +x
                    make test
                '''
            }
        }
        stage('Push image') {
            steps{
                unstash 'ecr.repo'
                sh '''
                    #!/bin/bash +x
                    make push pdfgenerator_version=${pdfgenerator_VERSION}
                '''
                
            }            
        }
        stage ('Remove untagged ECR images') {
            steps{
                unstash 'ecr.repo'
                sh '''
                    #!/bin/bash +x
                    make clean-remote
                '''
            }
        }
        stage('Remove Unused docker image') {
            steps{
                unstash 'ecr.repo'
                sh '''
                    #!/bin/bash +x
                    make clean-local pdfgenerator_version=${pdfgenerator_VERSION}
                '''
            }
        }
    }
    post {
        always {
            // Add a sleep to allow docker step to fully release file locks on failed run
            sleep(time: 3, unit: "SECONDS")
            deleteDir()
        }
        success {
            slackSend(message: "Build successful -${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL.replace('http://', 'https://').replace(':8080', '')}|Open>)", color: 'good')
        }
        failure {
            slackSend(message: "Build failed - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL.replace('http://', 'https://').replace(':8080', '')}|Open>)", color: 'danger')
        }
    }
}
