# Sử dụng Python 3.11 slim
FROM python:3.11-slim

# Cài đặt các package cần thiết
RUN apt-get update && apt-get install -y \
    build-essential \
    default-libmysqlclient-dev \
    pkg-config \
    && rm -rf /var/lib/apt/lists/*

# Tạo thư mục làm việc
WORKDIR /app
ENV PYTHONDONTWRITEBYTECODE=1 PYTHONUNBUFFERED=1

# Copy file requirements trước để cache pip install
COPY requirements.txt .

# Cài đặt dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy toàn bộ code vào container
COPY . .

# Mở port 8000
EXPOSE 8000

# Lệnh chạy server Django
CMD ["python", "manage.py", "runserver", "0.0.0.0:8000"]
