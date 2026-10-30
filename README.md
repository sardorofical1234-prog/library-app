# Library App — kutubxona boshqaruvi (REST API)

![CI](https://github.com/sardorofical1234-prog/library-app/actions/workflows/ci.yml/badge.svg)

Java 21 · Spring Boot 4.1 · Spring Data JPA · Spring Security (HTTP Basic) · PostgreSQL · Lombok

## Imkoniyatlar
- Ro'yxatdan o'tish (`MEMBER` roli) va login (HTTP Basic)
- Kitoblar CRUD (qo'shish / o'zgartirish / o'chirish — faqat `ADMIN`), qidiruv
- Kitobni ijaraga olish / qaytarish
- Qoidalar: ijara muddati 14 kun, bir vaqtda ko'pi bilan 3 ta kitob, bir kitobning ikki nusxasini olib bo'lmaydi
- Kechikkan ijaralar ro'yxati (ADMIN)
- Xatolar uchun yagona JSON format, validatsiya
- Mockito bilan unit testlar

## Ishga tushirish
1. Postgres: `docker compose up -d`
2. Dastur: `./mvnw spring-boot:run`  (Windows: `mvnw.cmd spring-boot:run`)
3. Testlar: `./mvnw test`

Baza sozlamalarini o'zgartirish: `DB_URL`, `DB_USER`, `DB_PASSWORD` muhit o'zgaruvchilari
(yoki `src/main/resources/application.yml`).

Birinchi ishga tushganda avtomatik yaratiladi:
- admin: `admin` / `admin123` (faqat o'rganish uchun!)
- 4 ta namunaviy kitob

## Struktura
```
com.library.demo
├── config/        SecurityConfig, DataInitializer
├── controller/    Auth, Book, Loan (HTTP qatlam)
├── dto/           request/response recordlar (Java 21 records)
├── entity/        AppUser, Book, Loan, Role
├── exception/     ApiException, GlobalExceptionHandler
├── repository/    Spring Data JPA
└── service/       biznes mantiq
```

## API
Swagger UI: http://localhost:8080/swagger-ui.html (Authorize tugmasi: `admin` / `admin123`)

| Metod | URL | Kim | Tavsif |
|---|---|---|---|
| POST | /api/auth/register | hamma | Ro'yxatdan o'tish |
| GET | /api/auth/me | login | Men kimman |
| GET | /api/books?search=java | login | Kitoblar (qidiruv ixtiyoriy) |
| GET | /api/books/{id} | login | Bitta kitob |
| POST | /api/books | ADMIN | Kitob qo'shish |
| PUT | /api/books/{id} | ADMIN | Kitobni o'zgartirish |
| DELETE | /api/books/{id} | ADMIN | Kitobni o'chirish |
| POST | /api/loans/borrow/{bookId} | login | Kitob olish |
| POST | /api/loans/{loanId}/return | login | Kitobni qaytarish |
| GET | /api/loans/my | login | Mening ijaralarim |
| GET | /api/loans/all | ADMIN | Barcha ijaralar |
| GET | /api/loans/overdue | ADMIN | Kechikkanlar |

## curl namunalari
```bash
# Ro'yxatdan o'tish
curl -X POST localhost:8080/api/auth/register -H "Content-Type: application/json" \
  -d '{"fullName":"Ali Valiyev","username":"ali","password":"secret123"}'

# Kitoblar ro'yxati
curl -u ali:secret123 localhost:8080/api/books

# Kitob olish (id=1)
curl -u ali:secret123 -X POST localhost:8080/api/loans/borrow/1

# Mening ijaralarim
curl -u ali:secret123 localhost:8080/api/loans/my

# Kitobni qaytarish (loan id=1)
curl -u ali:secret123 -X POST localhost:8080/api/loans/1/return

# Admin: yangi kitob qo'shish
curl -u admin:admin123 -X POST localhost:8080/api/books -H "Content-Type: application/json" \
  -d '{"title":"Head First Java","author":"Kathy Sierra","isbn":"978-0596009205","publishedYear":2005,"totalCopies":2}'
```

## Keyingi qadamlar (o'rganish uchun)
- JWT autentifikatsiya
- Pagination (`Pageable`)
- Swagger/OpenAPI (springdoc)
- Testcontainers bilan integratsion testlar
- Flyway migratsiyalari (`ddl-auto: update` o'rniga)
- Frontend (React yoki Thymeleaf)
