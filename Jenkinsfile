pipeline {
  agent {
    kubernetes {
      yaml '''
        apiVersion: v1
        kind: Pod
        spec:
          containers:
          - name: buildkit
            image: alpine:3.23.5
            command: ["sleep"]
            args: ["infinity"]
          - name: maven
            image: maven:3.10.0-eclipse-temurin-25-alpine
            command: ["sleep"]
            args: ["infinity"]
      '''
    }
  }
  stages {
    stage('Test') {
      steps {
        container('maven') {
          script {
            sh """
              mvn test
            """
          }
        }
      }
    }
    stage('Push') {
      steps {
        container('buildkit') {
          script {
            sh """
              echo Pushing to ghcr.io
            """
          }
        }
      }
    }
  }
}
