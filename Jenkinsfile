pipeline {
    agent any
    
    tools {
        maven 'M3'  // Maven doit être configuré dans Jenkins
    }
    
    stages {
        stage('Clone') {
            steps {
                git branch: 'develop',
                    url: 'https://github.com/ZaidaneAfaf/bookstore-jsp-servlet-jdbc-datasource.git',
                    credentialsId: 'github-bookstore-tp'
            }
        }
        
        stage('Compile') {
            steps {
                sh 'mvn clean compile'
            }
        }
        
        stage('Unit Tests') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                script {
                    echo 'SonarQube analysis will be configured in next step'
                }
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline finished'
        }
        success {
            echo 'Build successful!'
        }
        failure {
            echo 'Build failed!'
        }
    }
}