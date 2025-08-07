# Sử dụng image cơ sở Java
FROM openjdk:17-jdk

# Thông tin tác giả
LABEL authors="congng"

# Tạo thư mục làm việc trong container
WORKDIR /app

# Sao chép tệp JAR vào container
COPY target/feedback-service-0.0.1-SNAPSHOT.jar /app/feedback-service-0.0.1-SNAPSHOT.jar

# Expose port của ứng dụng
EXPOSE 8083

# Câu lệnh để chạy ứng dụng
ENTRYPOINT ["java", "-jar", "/app/feedback-service-0.0.1-SNAPSHOT.jar"]