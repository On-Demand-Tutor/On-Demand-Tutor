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

EXPOSE 8002

# Chạy app
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8000"]
# CMD ["python", "app.py"]
