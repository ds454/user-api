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
      '''
    }
  }
  stages {
    stage('Release') {
      steps {
        container('buildkit') {
          script {
            sh """
                  echo Hello, World!
                  ls -lah
          """
          }
        }
      }
    }
  }
}
