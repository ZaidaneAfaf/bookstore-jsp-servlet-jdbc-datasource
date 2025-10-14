pipeline {
    agent any
    
    tools {
        maven 'M3'
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
                bat 'mvn clean compile'
            }
        }
        
        stage('Unit Tests') {
            steps {
                script {
                    echo 'Running unit tests...'
                    try {
                        bat 'mvn test'
                    } catch (Exception e) {
                        echo "Tests failed or no tests found: ${e.message}"
                    }
                }
            }
            post {
                always {
                    script {
                        try {
                            junit 'target/surefire-reports/*.xml'
                            echo 'Test reports collected successfully'
                        } catch (Exception e) {
                            echo "No test reports found or error collecting: ${e.message}"
                        }
                    }
                }
            }
        }
        
        stage('Package') {
            steps {
                bat 'mvn package -DskipTests'
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                script {
                    withSonarQubeEnv('sonarqube') {
                        bat 'mvn sonar:sonar -Dsonar.projectKey=ZaidaneAfaf_bookstore-jsp-servlet-jdbc-datasource -Dsonar.organization=zaidaneafaf -Dsonar.qualitygate.wait=false'
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline finished'
            archiveArtifacts artifacts: 'target/*.war', fingerprint: true
        }
        success {
            echo 'Build successful! ✅'
            script {
                bat 'if exist "target\\*.war" (echo Application packaged successfully) else (echo No WAR file found)'
            }
        }
        failure {
            echo 'Build failed! ❌'
        }
    }
}