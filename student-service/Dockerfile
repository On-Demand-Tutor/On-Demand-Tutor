# Sử dụng image cơ sở Java
FROM openjdk:17-jdk

# Thông tin tác giả
LABEL authors="congng"

# Tạo thư mục làm việc trong container
WORKDIR /app

# Sao chép tệp JAR (không cần ghi cứng tên)
COPY target/*.jar app.jar

# Expose port của ứng dụng
EXPOSE 8080

# Câu lệnh để chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
