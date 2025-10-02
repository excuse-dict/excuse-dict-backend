FROM openjdk:17

# 작업 디렉토리 설정
WORKDIR /app

# JAR 파일 복사
COPY build/libs/excuse_dict-0.0.1-SNAPSHOT.jar app.jar

# 포트 노출
EXPOSE 8081

# JVM 메모리 제한
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:MaxMetaspaceSize=128m -XX:+UseG1GC"

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=prod"]