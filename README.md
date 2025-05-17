# Filmorate

## 📌 Запуск Spring Boot приложения в Docker

### 🔹 **Описание**

Этот проект использует **Docker Compose** для управления зависимостями:

- **PostgreSQL** — основная база данных
- **pgAdmin** — GUI для управления БД

### 📌 **Настройка и запуск проекта**

#### 🔹 **1. Установка зависимостей**

Перед запуском убедитесь, что у вас установлены:

- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)

#### 🔹 **2. Создание `.env` файла**

Создайте файл `.env` в корневой директории проекта и добавьте настройки:

```env
COMPOSE_PROJECT_NAME=filmorate

PG_DB_USERNAME=postgres
PG_DB_PASSWORD=postgres
PG_DB_HOST=postgres
PG_DB_PORT=5432

APP_DB_NAME=filmorate_development

APP_PG_ADMIN_LOGIN=admin@example.com
APP_PG_ADMIN_PASSWORD=1234
APP_PG_ADMIN_PORT=8081

APP_DB_URL="jdbc:postgresql://${PG_DB_HOST}:${PG_DB_PORT}/${APP_DB_NAME}"
```

#### 🔹 **3. Запуск контейнеров**

```bash
docker-compose up -d
```
