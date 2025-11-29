FROM mysql:8.0
ENV LANG=C.UTF-8

# 🔽 컨텍스트 기준 경로라서 앞에 db/ 붙여줘야 함
COPY db/schema.sql /docker-entrypoint-initdb.d/01_schema.sql
COPY db/init.sql   /docker-entrypoint-initdb.d/02_init.sql
