# E-commerce Load Testing

Bu klasör e-ticaret uygulamasının yük testleri için k6 script'lerini içerir.

## Kurulum

### 1. k6 Kurulumu

**Windows:**
```bash
# Chocolatey ile
choco install k6

# Veya manuel olarak
# https://github.com/grafana/k6/releases adresinden indirin
```

**macOS:**
```bash
brew install k6
```

**Linux:**
```bash
# Ubuntu/Debian
sudo gpg -k
sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update
sudo apt-get install k6

# CentOS/RHEL
sudo dnf install https://dl.k6.io/rpm/repo.rpm
sudo dnf install k6
```

## Test Senaryoları

### 1. Smoke Test
Temel fonksiyonaliteyi test eder. Hızlı ve hafif bir test.

```bash
k6 run k6-smoke-test.js
```

**Hedefler:**
- 10 virtual user
- 2 dakika süre
- %95 request < 1 saniye
- %10'dan az hata oranı

### 2. Load Test
1000 eşzamanlı kullanıcı ile gerçek yük testi.

```bash
k6 run k6-load-test.js
```

**Hedefler:**
- 1000 eşzamanlı kullanıcı
- 20 dakika toplam süre (5m ramp up + 10m steady + 5m ramp down)
- %95 request < 500ms
- Ortalama response time < 200ms
- %1'den az hata oranı

## Test Senaryoları Detayları

### Senaryo Dağılımı:
- **%60** - Ürün arama ve listeleme
- **%25** - Ürün detay görüntüleme  
- **%10** - Kullanıcı kimlik doğrulama
- **%5** - Admin işlemleri

### Test Edilen Endpoint'ler:

#### Public Endpoints:
- `GET /api/categories` - Kategori listesi
- `GET /api/products` - Ürün listesi (filtreleme, arama, sayfalama)
- `GET /api/products/{id}` - Ürün detayı
- `GET /api/products/sku/{sku}` - SKU ile ürün arama
- `GET /api/products/slug/{slug}` - Slug ile ürün arama

#### Authentication:
- `POST /api/auth/register` - Kullanıcı kaydı
- `POST /api/auth/login` - Giriş yapma
- `POST /api/auth/refresh` - Token yenileme
- `POST /api/auth/logout` - Çıkış yapma

#### Admin (Authenticated):
- `GET /api/admin/metrics` - Sistem metrikleri
- `GET /api/admin/seed/status` - Seed durumu
- `POST /api/admin/seed/database` - Veritabanı seed'i

## Test Verileri

### Test Kullanıcıları:
- `test1@ozdilek.com` - `test123`
- `test2@ozdilek.com` - `test123`
- ...
- `test10@ozdilek.com` - `test123`
- `admin@ozdilek.com` - `admin123`

### Arama Terimleri:
- Ev & Yaşam: "yatak", "çift kişilik", "nevresim", "yastık", "koltuk"
- Mutfak: "tencere", "tava", "mikrodalga"
- Moda: "gömlek", "pantolon", "elbise", "kazak", "mont"
- Elektronik: "telefon", "bilgisayar", "kulaklık", "kamera"

## Test Sonuçları

### Başarı Kriterleri:
- **Response Time**: %95 < 500ms, Ortalama < 200ms
- **Error Rate**: < %1
- **Throughput**: Minimum 1000 RPS
- **Memory Usage**: < 2GB
- **CPU Usage**: < %80

### Ölçülen Metrikler:
- `http_req_duration` - Request süresi
- `http_req_failed` - Başarısız request oranı
- `http_reqs` - Toplam request sayısı
- `vus` - Virtual user sayısı
- `vus_max` - Maksimum virtual user
- `error_rate` - Custom hata oranı
- `product_search_duration` - Ürün arama süresi
- `auth_duration` - Kimlik doğrulama süresi
- `product_detail_duration` - Ürün detay süresi

## Test Çalıştırma

### Ön Gereksinimler:
1. MongoDB çalışıyor olmalı (localhost:27017)
2. Redis çalışıyor olmalı (localhost:6379)
3. Spring Boot uygulaması çalışıyor olmalı (localhost:8080)

### Adımlar:

1. **Uygulamayı başlat:**
```bash
cd ..
./mvnw spring-boot:run
```

2. **Smoke test çalıştır:**
```bash
k6 run k6-smoke-test.js
```

3. **Load test çalıştır:**
```bash
k6 run k6-load-test.js
```

4. **Sonuçları analiz et:**
- Console output'u kontrol et
- Grafana dashboard'u kur (opsiyonel)
- MongoDB ve Redis metriklerini izle

## Performans İyileştirme

### Cache Hit Ratio:
- Redis cache hit ratio > %80 olmalı
- MongoDB query time < 50ms olmalı

### Database Optimization:
- Proper indexing on frequently queried fields
- Connection pooling optimization
- Read replica usage

### Application Optimization:
- JVM heap size optimization
- Garbage collection tuning
- Thread pool configuration

## Troubleshooting

### Yaygın Sorunlar:

1. **Connection Refused:**
   - Uygulama çalışıyor mu kontrol et
   - Port 8080 açık mı kontrol et

2. **MongoDB Connection Error:**
   - MongoDB çalışıyor mu kontrol et
   - Connection string doğru mu kontrol et

3. **High Error Rate:**
   - Memory/CPU usage kontrol et
   - Database connection pool kontrol et
   - Application logs kontrol et

4. **Slow Response Times:**
   - Database query performance kontrol et
   - Cache hit ratio kontrol et
   - Network latency kontrol et

## Sonuç Raporu

Test tamamlandıktan sonra şu bilgileri topla:

1. **Performance Metrics:**
   - Average response time
   - 95th percentile response time
   - Error rate
   - Throughput (RPS)

2. **System Metrics:**
   - CPU usage
   - Memory usage
   - Database connection count
   - Cache hit ratio

3. **Recommendations:**
   - Bottleneck'ler
   - İyileştirme önerileri
   - Scaling stratejileri
