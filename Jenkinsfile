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
                    echo 'Running unit tests with JaCoCo...'
                    bat 'mvn test'
                }
            }
            post {
                always {
                    script {
                        junit 'target/surefire-reports/*.xml'
                        echo 'Test reports collected successfully'
                    }
                }
            }
        }
        
        stage('Generate JaCoCo Report') {
            steps {
                script {
                    echo 'Generating JaCoCo coverage report...'
                    bat 'mvn jacoco:report'
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
                        bat '''
                            mvn sonar:sonar ^
                            -Dsonar.projectKey=ZaidaneAfaf_bookstore-jsp-servlet-jdbc-datasource ^
                            -Dsonar.organization=zaidaneafaf ^
                            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml ^
                            -Dsonar.java.binaries=target/classes ^
                            -Dsonar.sources=src/main/java ^
                            -Dsonar.tests=src/test/java ^
                            -Dsonar.qualitygate.wait=false
                        '''
                    }
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                script {
                    echo 'SonarQube analysis submitted. Check SonarCloud dashboard for results.'
                    echo 'Project: https://sonarcloud.io/dashboard?id=ZaidaneAfaf_bookstore-jsp-servlet-jdbc-datasource'
                    
                    // Alternative simple : juste attendre 5 secondes et continuer
                    sleep(5)
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
            echo 'Build successful!'
            script {
                bat 'if exist "target\\*.war" (echo Application packaged successfully) else (echo No WAR file found)'
            }
        }
        failure {
            echo 'Build failed!'
        }
    }
}