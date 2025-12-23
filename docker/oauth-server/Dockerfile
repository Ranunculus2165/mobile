FROM python:3.11-slim

WORKDIR /app

# 시스템 의존성 설치
RUN apt-get update && apt-get install -y \
    gcc \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Python 의존성 복사 및 설치
COPY docs/legendwon/oauth2.0_code-main/requirements.txt /tmp/requirements.txt
RUN pip install --no-cache-dir -r /tmp/requirements.txt

# OAuth 서버 코드 복사
COPY docs/legendwon/oauth2.0_code-main/app.py \
     docs/legendwon/oauth2.0_code-main/oauth2.py \
     docs/legendwon/oauth2.0_code-main/models.py \
     ./

# 환경변수 설정
ENV FLASK_APP=app.py
ENV PYTHONUNBUFFERED=1
ENV PORT=3000

# 포트 노출
EXPOSE 3000

# 시작 스크립트
RUN echo '#!/bin/bash\n\
set -e\n\
echo "=== OAuth Server Starting ==="\n\
if [ ! -f /app/oauth2.db ]; then\n\
    echo "Database not found. Initializing..."\n\
    flask initdb\n\
    echo "Creating test data..."\n\
    flask create-test-data\n\
    echo "Database initialized successfully!"\n\
else\n\
    echo "Database already exists. Skipping initialization."\n\
fi\n\
echo "Starting OAuth server on port ${PORT:-3000}..."\n\
exec python app.py' > /app/start.sh && chmod +x /app/start.sh

# 서버 실행
CMD ["/app/start.sh"]
