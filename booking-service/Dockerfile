# Sử dụng Python base image
FROM python:3.11-slim

# Đặt thư mục làm việc
WORKDIR /app

# Copy requirements trước để cache
COPY requirements.txt .

# Cài đặt dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy toàn bộ code vào container
COPY . .

# Expose port 8000 (có thể sửa nếu app.py chạy port khác)
EXPOSE 8002

# Chạy app
CMD ["python", "app.py"]
