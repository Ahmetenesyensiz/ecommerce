# 🛒 Özdilek E-commerce Clone

Özdilek'in e-ticaret platformunu klonlayan, **1000 eşzamanlı kullanıcı** altında test edilmiş, yüksek performanslı bir Spring Boot uygulaması.

## 🎯 Proje Hedefi

Bu proje, modern e-ticaret uygulamalarının **ölçeklenebilir mimari**sini ve **yük testi** stratejilerini göstermek için geliştirilmiştir. Özdilek'teki teknik ekibi etkilemek için gerçekçi, yük altında ayakta kalan bir e-ticaret klonu.

## 🏗️ Mimari

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   React SPA     │    │   Load Balancer │    │   Spring Boot   │
│   (Frontend)    │◄──►│   (NGINX)       │◄──►│   (Backend)     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                       │
                       ┌─────────────────┐            │
                       │      Redis      │◄───────────┤
                       │    (Cache)      │            │
                       └─────────────────┘            │
                                                       │
                       ┌─────────────────┐            │
                       │    MongoDB      │◄───────────┘
                       │  (Database)     │
                       └─────────────────┘
```

## 🚀 Özellikler

### ✅ Tamamlanan Özellikler

- **🔐 JWT Authentication** - Register, Login, Refresh Token
- **📦 Product Management** - CRUD operations, search, filtering
- **🏷️ Category Management** - Hierarchical categories
- **🛒 Shopping Cart** - Guest & authenticated users
- **📋 Order Management** - Order creation & tracking
- **👑 Admin Panel** - Product & category management
- **⚡ Redis Caching** - High-performance caching
- **🔍 Full-text Search** - MongoDB text search
- **📊 Monitoring** - Actuator endpoints
- **🌱 Seed Data** - 1000+ realistic products
- **🧪 Load Testing** - k6 scripts for 1000 concurrent users

### 🔄 Gelecek Özellikler

- **💳 Payment Integration** - Stripe/iyzico integration
- **📧 Email Notifications** - Order confirmations
- **📱 Real-time Updates** - WebSocket notifications
- **📈 Analytics Dashboard** - Grafana integration
- **🐳 Docker Deployment** - Container orchestration

## 🛠️ Teknolojiler

### Backend
- **Java 17** - Modern Java features
- **Spring Boot 3.5.5** - Latest Spring Boot
- **Spring Security** - JWT authentication
- **Spring Data MongoDB** - NoSQL database
- **Spring Cache** - Redis caching
- **Lombok** - Boilerplate reduction
- **Validation** - Input validation

### Database & Cache
- **MongoDB 7.0** - Document database
- **Redis 7.2** - In-memory cache
- **MongoDB Atlas Search** - Full-text search

### Testing & Monitoring
- **k6** - Load testing
- **Spring Actuator** - Health checks
- **Prometheus** - Metrics collection
- **Grafana** - Visualization

### DevOps
- **Docker** - Containerization
- **Docker Compose** - Local development
- **Maven** - Build tool

## 📋 Gereksinimler

- **Java 17+**
- **Maven 3.6+**
- **MongoDB 7.0+**
- **Redis 7.2+**
- **Docker & Docker Compose** (opsiyonel)

## 🚀 Hızlı Başlangıç

### 1. Projeyi Klonla
```bash
git clone <repository-url>
cd ecommerce
```

### 2. Veritabanlarını Başlat (Docker ile)
```bash
# MongoDB ve Redis'i Docker ile başlat
docker-compose up -d

# Servislerin çalıştığını kontrol et
docker-compose ps
```

### 3. Uygulamayı Başlat
```bash
# Maven ile
./mvnw spring-boot:run

# Veya Docker profile ile
./mvnw spring-boot:run -Dspring-boot.run.profiles=docker
```

### 4. Test Verilerini Yükle
```bash
# Admin kullanıcısı ile giriş yap
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@ozdilek.com","password":"admin123"}'

# Token'ı kopyala ve seed endpoint'ini çağır
curl -X POST http://localhost:8080/api/admin/seed/database \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 5. Uygulamayı Test Et
```bash
# Ürünleri listele
curl http://localhost:8080/api/products?size=10

# Kategori listesi
curl http://localhost:8080/api/categories

# Health check
curl http://localhost:8080/actuator/health
```

## 🧪 Load Testing

### k6 Kurulumu
```bash
# Windows
choco install k6

# macOS
brew install k6

# Linux
sudo apt-get install k6
```

### Test Çalıştırma
```bash
cd load-testing

# Smoke test (hızlı test)
k6 run k6-smoke-test.js

# Load test (1000 concurrent users)
k6 run k6-load-test.js
```

### Test Sonuçları
- **Target**: 1000 eşzamanlı kullanıcı
- **Duration**: 20 dakika (5m ramp up + 10m steady + 5m ramp down)
- **Success Criteria**: 
  - %95 response time < 500ms
  - Average response time < 200ms
  - Error rate < 1%

## 📊 API Endpoints

### Authentication
```
POST /api/auth/register     # Kullanıcı kaydı
POST /api/auth/login        # Giriş yapma
POST /api/auth/refresh      # Token yenileme
POST /api/auth/logout       # Çıkış yapma
```

### Products
```
GET  /api/products          # Ürün listesi (search, filter, pagination)
GET  /api/products/{id}     # Ürün detayı
GET  /api/products/sku/{sku} # SKU ile ürün arama
GET  /api/products/slug/{slug} # Slug ile ürün arama
POST /api/products          # Ürün oluşturma (Admin)
PUT  /api/products/{id}     # Ürün güncelleme (Admin)
DELETE /api/products/{id}   # Ürün silme (Admin)
```

### Categories
```
GET /api/categories         # Kategori listesi
GET /api/categories/{id}    # Kategori detayı
GET /api/categories/slug/{slug} # Slug ile kategori arama
GET /api/categories/parent/{parentId} # Alt kategoriler
```

### Admin
```
POST /api/admin/seed/database # Veritabanı seed'i
GET  /api/admin/seed/status   # Seed durumu
GET  /api/admin/metrics       # Sistem metrikleri
```

### Monitoring
```
GET /actuator/health        # Health check
GET /actuator/metrics       # Application metrics
GET /actuator/prometheus    # Prometheus metrics
```

## 🗄️ Veritabanı Şeması

### Collections
- **users** - Kullanıcı bilgileri
- **products** - Ürün kataloğu
- **categories** - Ürün kategorileri
- **carts** - Sepet bilgileri
- **orders** - Sipariş bilgileri
- **refresh_tokens** - JWT refresh token'ları

### Indexes
- **users**: email (unique), lastLoginAt
- **products**: sku (unique), slug (unique), text search, categories+price
- **categories**: slug (unique), parentId+sortOrder
- **carts**: userId, sessionId, updatedAt
- **orders**: userId+createdAt, status
- **refresh_tokens**: tokenHash, userId, expiresAt (TTL)

## 🔧 Konfigürasyon

### Application Properties
```properties
# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/ecommerce

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# JWT
app.jwt.secret=your-secret-key
app.jwt.expiration=86400000
app.jwt.refresh-expiration=604800000
```

### Docker Environment
```yaml
# docker-compose.yml
services:
  mongodb:
    image: mongo:7.0
    ports: ["27017:27017"]
  
  redis:
    image: redis:7.2-alpine
    ports: ["6379:6379"]
```

## 📈 Performans Optimizasyonları

### Caching Strategy
- **Redis Cache** - Product list, categories, user sessions
- **Cache TTL** - 30 dakika
- **Cache Invalidation** - Product update'lerde cache temizleme

### Database Optimization
- **Indexes** - Frequently queried fields
- **Connection Pooling** - Optimized MongoDB connections
- **Read Replicas** - Heavy read load için

### Application Tuning
- **JVM Heap** - 2GB heap size
- **Thread Pools** - Optimized thread configuration
- **Garbage Collection** - G1GC tuning

## 🐳 Docker Deployment

### Local Development
```bash
# Servisleri başlat
docker-compose up -d

# Logları izle
docker-compose logs -f

# Servisleri durdur
docker-compose down
```

### Production Deployment
```bash
# Docker image build
docker build -t ecommerce-app .

# Container run
docker run -d \
  --name ecommerce-app \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  ecommerce-app
```

## 📊 Monitoring & Observability

### Health Checks
- **Application Health** - `/actuator/health`
- **Database Health** - MongoDB connection status
- **Cache Health** - Redis connection status

### Metrics
- **Request Metrics** - Response time, throughput, error rate
- **Database Metrics** - Query time, connection count
- **Cache Metrics** - Hit ratio, miss ratio
- **JVM Metrics** - Memory, CPU, GC

### Logging
- **Structured Logging** - JSON format
- **Log Levels** - DEBUG (dev), INFO (prod)
- **Request Tracing** - Unique request IDs

## 🧪 Test Coverage

### Unit Tests
- Service layer tests
- Repository tests
- Controller tests

### Integration Tests
- API endpoint tests
- Database integration tests
- Security tests

### Load Tests
- **k6 Scripts** - 1000 concurrent users
- **Performance Metrics** - Response time, throughput
- **Stress Testing** - System limits testing

## 🔒 Güvenlik

### Authentication
- **JWT Tokens** - Stateless authentication
- **Refresh Tokens** - Secure token renewal
- **Password Hashing** - BCrypt encryption

### Authorization
- **Role-based Access** - USER, ADMIN roles
- **Endpoint Protection** - Secured admin endpoints
- **CORS Configuration** - Cross-origin requests

### Input Validation
- **DTO Validation** - Request validation
- **SQL Injection Prevention** - MongoDB parameterized queries
- **XSS Protection** - Input sanitization

## 📚 API Documentation

### Swagger UI
```
http://localhost:8080/swagger-ui.html
```

### Postman Collection
```
load-testing/postman-collection.json
```

## 🤝 Katkıda Bulunma

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📄 Lisans

Bu proje MIT lisansı altında lisanslanmıştır. Detaylar için [LICENSE](LICENSE) dosyasına bakın.

## 👥 Ekip

- **Backend Developer** - Spring Boot, MongoDB, Redis
- **DevOps Engineer** - Docker, Monitoring, Load Testing
- **QA Engineer** - Test automation, Performance testing

## 📞 İletişim

- **Email** - [ahmetenesyensiz@gmail.com]
- **LinkedIn** - [https://www.linkedin.com/in/ahmetenesyensiz/]
- **GitHub** - [https://github.com/Ahmetenesyensiz]

---

**Not**: Bu proje eğitim amaçlıdır ve Özdilek'in gerçek sistemini klonlamaz. Sadece modern e-ticaret mimarisi ve yük testi stratejilerini göstermek için geliştirilmiştir.
