# Sử dụng Python 3.11 slim
FROM python:3.11-slim

# Cài đặt các package cần thiết
RUN apt-get update && apt-get install -y \
    build-essential \
    default-libmysqlclient-dev \
    pkg-config \
    netcat-openbsd \
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

# Copy entrypoint script
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Mở port 8000
EXPOSE 8000

# Dùng entrypoint.sh chờ DB + migrate + start server
ENTRYPOINT ["/entrypoint.sh"]
