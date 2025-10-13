# Utilise une image Tomcat avec Java
FROM tomcat:9.0-jdk17

# Supprime les applications par défaut de Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Copie ton application .war dans Tomcat
COPY target/bookstore-jsp-servlet-jdbc-datasource-1.0.war /usr/local/tomcat/webapps/ROOT.war

# Expose le port 8080
EXPOSE 8080

# Commande pour démarrer Tomcat
CMD ["catalina.sh", "run"]