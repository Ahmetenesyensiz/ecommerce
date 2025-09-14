package com.ozdilek.ecommerce.service;

import com.ozdilek.ecommerce.model.Category;
import com.ozdilek.ecommerce.model.Product;
import com.ozdilek.ecommerce.model.User;
import com.ozdilek.ecommerce.repository.CategoryRepository;
import com.ozdilek.ecommerce.repository.ProductRepository;
import com.ozdilek.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeedService {
    
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    private final Random random = new Random();
    
    @Transactional
    public void seedDatabase() {
        log.info("Starting database seeding...");
        
        // Clear existing data
        clearDatabase();
        
        // Seed categories
        List<Category> categories = seedCategories();
        
        // Seed products
        seedProducts(categories);
        
        // Seed users
        seedUsers();
        
        log.info("Database seeding completed successfully!");
    }
    
    private void clearDatabase() {
        log.info("Clearing existing data...");
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }
    
    private List<Category> seedCategories() {
        log.info("Seeding categories...");
        
        List<Category> categories = new ArrayList<>();
        
        // Ana kategoriler
        String[] mainCategories = {
            "Ev & Yaşam", "Moda", "Elektronik", "Spor & Outdoor", "Kitap & Hobi", 
            "Bebek & Çocuk", "Kozmetik & Kişisel Bakım", "Süpermarket"
        };
        
        for (int i = 0; i < mainCategories.length; i++) {
            Category category = Category.builder()
                    .name(mainCategories[i])
                    .slug(createSlug(mainCategories[i]))
                    .sortOrder(i + 1)
                    .active(true)
                    .build();
            categories.add(categoryRepository.save(category));
        }
        
        // Alt kategoriler
        String[][] subCategories = {
            {"Yatak Odası", "Oturma Odası", "Mutfak", "Banyo", "Bahçe"},
            {"Kadın", "Erkek", "Çocuk", "Ayakkabı", "Çanta"},
            {"Telefon", "Bilgisayar", "TV & Ses", "Küçük Ev Aletleri", "Fotoğraf"},
            {"Fitness", "Outdoor", "Su Sporları", "Kış Sporları", "Takım Sporları"},
            {"Roman", "Bilim Kurgu", "Çocuk Kitapları", "Sanat", "Müzik"},
            {"Bebek Bakımı", "Oyuncak", "Bebek Giyim", "Bebek Beslenme", "Bebek Odası"},
            {"Cilt Bakımı", "Makyaj", "Saç Bakımı", "Parfüm", "Erkek Bakımı"},
            {"Gıda", "İçecek", "Temizlik", "Kişisel Bakım", "Evcil Hayvan"}
        };
        
        for (int i = 0; i < subCategories.length; i++) {
            Category parentCategory = categories.get(i);
            for (String subCatName : subCategories[i]) {
                Category subCategory = Category.builder()
                        .name(subCatName)
                        .slug(createSlug(subCatName))
                        .parentId(parentCategory.getId())
                        .sortOrder(1)
                        .active(true)
                        .build();
                categories.add(categoryRepository.save(subCategory));
            }
        }
        
        log.info("Seeded {} categories", categories.size());
        return categories;
    }
    
    private void seedProducts(List<Category> categories) {
        log.info("Seeding products...");
        
        List<Product> products = new ArrayList<>();
        int productCount = 0;
        
        // Her kategori için 100-150 ürün oluştur
        for (Category category : categories) {
            if (category.getParentId() != null) { // Sadece alt kategoriler için
                int productsInCategory = 100 + random.nextInt(51); // 100-150 arası
                
                for (int i = 0; i < productsInCategory; i++) {
                    Product product = createRandomProduct(category);
                    products.add(product);
                    productCount++;
                    
                    // Batch save için her 100 üründe bir kaydet
                    if (products.size() >= 100) {
                        productRepository.saveAll(products);
                        log.info("Saved {} products so far...", productCount);
                        products.clear();
                    }
                }
            }
        }
        
        // Kalan ürünleri kaydet
        if (!products.isEmpty()) {
            productRepository.saveAll(products);
        }
        
        log.info("Seeded {} products total", productCount);
    }
    
    private Product createRandomProduct(Category category) {
        String[] productNames = generateProductNames(category.getName());
        String productName = productNames[random.nextInt(productNames.length)];
        String sku = "ODZ-" + String.format("%04d", random.nextInt(9999) + 1);
        
        // Fiyat aralığı kategorilere göre
        BigDecimal price = generatePriceForCategory(category.getName());
        
        // Stok durumu
        int stock = random.nextInt(100) + 1;
        boolean available = random.nextDouble() > 0.05; // %95 ihtimalle mevcut
        
        // Rating
        double avgRating = 3.0 + random.nextDouble() * 2.0; // 3.0-5.0 arası
        int ratingCount = random.nextInt(100);
        
        return Product.builder()
                .sku(sku)
                .title(productName)
                .slug(createSlug(productName + "-" + sku))
                .description(generateDescription(productName))
                .price(price)
                .currency("TRY")
                .categories(List.of(category.getId()))
                .images(generateImages(productName))
                .attributes(generateAttributes(category.getName()))
                .stock(stock)
                .available(available)
                .rating(Product.Rating.builder()
                        .avg(avgRating)
                        .count(ratingCount)
                        .build())
                .tags(generateTags(category.getName()))
                .createdAt(LocalDateTime.now().minusDays(random.nextInt(365)))
                .build();
    }
    
    private String[] generateProductNames(String categoryName) {
        // Kategori bazlı ürün isimleri
        switch (categoryName) {
            case "Yatak Odası":
                return new String[]{
                    "Çift Kişilik Yatak Örtüsü", "Tek Kişilik Yatak Örtüsü", "Yastık Kılıfı Seti",
                    "Şilte Koruyucu", "Nevresim Takımı", "Yatak Odası Dekoratif Yastık",
                    "Gardırop Organizatörü", "Yatak Başı Lambası", "Komodin", "Ayna"
                };
            case "Oturma Odası":
                return new String[]{
                    "3+2+1 Koltuk Takımı", "TV Sehpası", "Kahve Masası", "Kitaplık",
                    "Dekoratif Vazo", "Halı", "Perde", "Abajur", "Süs Eşyası", "Masa Lambası"
                };
            case "Mutfak":
                return new String[]{
                    "Tencere Seti", "Tava Seti", "Mutfak Robotu", "Kahve Makinesi",
                    "Mikrodalga Fırın", "Buzdolabı", "Çaydanlık", "Tabak Takımı", "Bardak Seti", "Mutfak Havlusu"
                };
            case "Kadın":
                return new String[]{
                    "Kadın Gömlek", "Kadın Pantolon", "Kadın Elbise", "Kadın Kazak",
                    "Kadın Etek", "Kadın Mont", "Kadın Ayakkabı", "Kadın Çanta", "Kadın Takı", "Kadın Şal"
                };
            case "Erkek":
                return new String[]{
                    "Erkek Gömlek", "Erkek Pantolon", "Erkek Kazak", "Erkek Tişört",
                    "Erkek Mont", "Erkek Ayakkabı", "Erkek Çanta", "Erkek Saat", "Erkek Kemeri", "Erkek Şapka"
                };
            default:
                return new String[]{
                    "Premium Ürün", "Kaliteli Ürün", "Özel Ürün", "Lüks Ürün", "Standart Ürün",
                    "Ekonomik Ürün", "Modern Ürün", "Klasik Ürün", "Trendy Ürün", "Fonksiyonel Ürün"
                };
        }
    }
    
    private BigDecimal generatePriceForCategory(String categoryName) {
        // Kategori bazlı fiyat aralıkları
        switch (categoryName) {
            case "Ev & Yaşam", "Mutfak":
                return BigDecimal.valueOf(50 + random.nextInt(500)); // 50-550 TL
            case "Moda":
                return BigDecimal.valueOf(100 + random.nextInt(800)); // 100-900 TL
            case "Elektronik":
                return BigDecimal.valueOf(200 + random.nextInt(2000)); // 200-2200 TL
            case "Spor & Outdoor":
                return BigDecimal.valueOf(150 + random.nextInt(1000)); // 150-1150 TL
            default:
                return BigDecimal.valueOf(25 + random.nextInt(200)); // 25-225 TL
        }
    }
    
    private String generateDescription(String productName) {
        return productName + " - Yüksek kaliteli malzemelerden üretilmiş, uzun ömürlü ve kullanışlı ürün. " +
               "Modern tasarımı ile evinize şıklık katacak. Kolay temizlenebilir ve bakım gerektirmeyen özellikleri ile tercih edilen bir ürün.";
    }
    
    private List<Product.ProductImage> generateImages(String productName) {
        List<Product.ProductImage> images = new ArrayList<>();
        
        // Ana görsel
        images.add(Product.ProductImage.builder()
                .url("https://cdn.ozdilek.com/products/" + createSlug(productName) + "-1.jpg")
                .alt(productName + " Ana Görsel")
                .sortOrder(1)
                .isPrimary(true)
                .build());
        
        // Ek görseller
        for (int i = 2; i <= 3; i++) {
            images.add(Product.ProductImage.builder()
                    .url("https://cdn.ozdilek.com/products/" + createSlug(productName) + "-" + i + ".jpg")
                    .alt(productName + " Detay Görsel " + (i-1))
                    .sortOrder(i)
                    .isPrimary(false)
                    .build());
        }
        
        return images;
    }
    
    private java.util.Map<String, Object> generateAttributes(String categoryName) {
        java.util.Map<String, Object> attributes = new java.util.HashMap<>();
        
        switch (categoryName) {
            case "Yatak Odası", "Oturma Odası":
                attributes.put("renk", getRandomColor());
                attributes.put("malzeme", getRandomMaterial());
                attributes.put("boyut", getRandomSize());
                break;
            case "Mutfak":
                attributes.put("kapasite", getRandomCapacity());
                attributes.put("renk", getRandomColor());
                attributes.put("malzeme", getRandomMaterial());
                break;
            case "Kadın", "Erkek":
                attributes.put("beden", getRandomSize());
                attributes.put("renk", getRandomColor());
                attributes.put("malzeme", getRandomMaterial());
                break;
            default:
                attributes.put("renk", getRandomColor());
                attributes.put("boyut", getRandomSize());
        }
        
        return attributes;
    }
    
    private List<String> generateTags(String categoryName) {
        List<String> tags = new ArrayList<>();
        tags.add(categoryName.toLowerCase());
        tags.add("popüler");
        
        if (random.nextBoolean()) {
            tags.add("indirimli");
        }
        if (random.nextBoolean()) {
            tags.add("yeni");
        }
        if (random.nextBoolean()) {
            tags.add("önerilen");
        }
        
        return tags;
    }
    
    private void seedUsers() {
        log.info("Seeding users...");
        
        // Admin user
        User admin = User.builder()
                .name("Admin User")
                .email("admin@ozdilek.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .roles(List.of("USER", "ADMIN"))
                .isVerified(true)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(admin);
        
        // Test users
        for (int i = 1; i <= 10; i++) {
            User user = User.builder()
                    .name("Test User " + i)
                    .email("test" + i + "@ozdilek.com")
                    .passwordHash(passwordEncoder.encode("test123"))
                    .roles(List.of("USER"))
                    .isVerified(true)
                    .phone("+90 5" + random.nextInt(10) + " " + random.nextInt(1000) + " " + random.nextInt(100) + " " + random.nextInt(100))
                    .createdAt(LocalDateTime.now().minusDays(random.nextInt(100)))
                    .build();
            userRepository.save(user);
        }
        
        log.info("Seeded users");
    }
    
    // Helper methods
    private String createSlug(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .trim();
    }
    
    private String getRandomColor() {
        String[] colors = {"Beyaz", "Siyah", "Gri", "Kırmızı", "Mavi", "Yeşil", "Sarı", "Pembe", "Mor", "Turuncu"};
        return colors[random.nextInt(colors.length)];
    }
    
    private String getRandomMaterial() {
        String[] materials = {"Pamuk", "Polyester", "Keten", "Yün", "Deri", "Ahşap", "Metal", "Plastik", "Cam", "Seramik"};
        return materials[random.nextInt(materials.length)];
    }
    
    private String getRandomSize() {
        String[] sizes = {"S", "M", "L", "XL", "XXL", "Küçük", "Orta", "Büyük", "120x180", "150x200", "180x200"};
        return sizes[random.nextInt(sizes.length)];
    }
    
    private String getRandomCapacity() {
        String[] capacities = {"1L", "2L", "3L", "5L", "10L", "20L", "30L", "50L", "100L"};
        return capacities[random.nextInt(capacities.length)];
    }
}
