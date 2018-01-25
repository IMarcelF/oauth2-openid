pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        git 'https://github.com/NOSiCode-CV/igrp-rest.git'
      }
    }
    stage('Test') {
      steps {
        echo 'Testando...'
      }
    }
    stage('Deploy') {
      steps {
        echo 'Success...!'
      }
    }
  }
}