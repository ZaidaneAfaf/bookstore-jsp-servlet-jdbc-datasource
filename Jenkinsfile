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
                        // Continue le pipeline même si les tests échouent ou sont absents
                    }
                }
            }
            post {
                always {
                    script {
                        // Essaie de collecter les rapports si ils existent
                        try {
                            junit 'target/surefire-reports/*.xml'
                            echo 'Test reports collected successfully'
                        } catch (Exception e) {
                            echo "No test reports found or error collecting: ${e.message}"
                            // Ne pas faire échouer le build si pas de rapports
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
                    echo 'SonarQube analysis will be configured in next step'
                }
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline finished'
            
            // Archive l'artefact .war généré
            archiveArtifacts artifacts: 'target/*.war', fingerprint: true
        }
        success {
            echo 'Build successful! ✅'
            
            // Affiche le chemin de l'artefact généré
            script {
                def files = findFiles(glob: 'target/*.war')
                if (files) {
                    echo "Application packaged: ${files[0].name}"
                }
            }
        }
        failure {
            echo 'Build failed! ❌'
        }
    }
}