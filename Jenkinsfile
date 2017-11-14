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
        build 'TesteMaven'
      }
    }
    stage('Deploy') {
      steps {
        echo 'Success...!'
      }
    }
  }
}