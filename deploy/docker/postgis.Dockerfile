FROM postgis/postgis:latest
ENV POSTGRES_USER=postgres \
    POSTGRES_PASSWORD=postgres \
    POSTGRES_DB=appdb

WORKDIR /usr/local/app

COPY backend/db/reset.sql /docker-entrypoint-initdb.d/10-reset.sql

CMD ["postgres"]