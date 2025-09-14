import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('error_rate');
const productSearchDuration = new Trend('product_search_duration');
const authDuration = new Trend('auth_duration');
const productDetailDuration = new Trend('product_detail_duration');

// Configuration
const BASE_URL = 'http://localhost:8080';
const API_BASE = `${BASE_URL}/api`;

// Test configuration
export const options = {
  stages: [
    // Ramp up to 1000 users over 5 minutes
    { duration: '5m', target: 1000 },
    // Stay at 1000 users for 10 minutes
    { duration: '10m', target: 1000 },
    // Ramp down to 0 users over 5 minutes
    { duration: '5m', target: 0 },
  ],
  thresholds: {
    // Error rate should be less than 1%
    error_rate: ['rate<0.01'],
    // 95% of requests should be faster than 500ms
    http_req_duration: ['p(95)<500'],
    // Average response time should be less than 200ms
    http_req_duration: ['avg<200'],
  },
};

// Test data
const testUsers = [
  { email: 'test1@ozdilek.com', password: 'test123' },
  { email: 'test2@ozdilek.com', password: 'test123' },
  { email: 'test3@ozdilek.com', password: 'test123' },
  { email: 'test4@ozdilek.com', password: 'test123' },
  { email: 'test5@ozdilek.com', password: 'test123' },
  { email: 'test6@ozdilek.com', password: 'test123' },
  { email: 'test7@ozdilek.com', password: 'test123' },
  { email: 'test8@ozdilek.com', password: 'test123' },
  { email: 'test9@ozdilek.com', password: 'test123' },
  { email: 'test10@ozdilek.com', password: 'test123' },
];

const searchQueries = [
  'yatak', 'çift kişilik', 'nevresim', 'yastık', 'koltuk', 
  'tv sehpası', 'kahve masası', 'tencere', 'tava', 'mikrodalga',
  'gömlek', 'pantolon', 'elbise', 'kazak', 'mont', 'ayakkabı',
  'telefon', 'bilgisayar', 'kulaklık', 'kamera', 'tablet'
];

const categories = [
  'yatak-odasi', 'oturma-odasi', 'mutfak', 'banyo', 'bahce',
  'kadin', 'erkek', 'cocuk', 'ayakkabi', 'canta',
  'telefon', 'bilgisayar', 'tv-ses', 'kucuk-ev-aletleri'
];

// Global variables
let authTokens = {};
let productIds = [];

export function setup() {
  console.log('🚀 Starting load test setup...');
  
  // First, seed the database if needed
  const seedResponse = http.post(`${API_BASE}/admin/seed/database`, {}, {
    headers: { 'Content-Type': 'application/json' },
  });
  
  if (seedResponse.status === 200) {
    console.log('✅ Database seeded successfully');
  } else {
    console.log('⚠️ Database seeding failed or already done');
  }
  
  // Get some product IDs for testing
  const productsResponse = http.get(`${API_BASE}/products?size=50`);
  if (productsResponse.status === 200) {
    const products = JSON.parse(productsResponse.body);
    productIds = products.content.map(p => p.id);
    console.log(`📦 Found ${productIds.length} products for testing`);
  }
  
  return { productIds };
}

export default function(data) {
  const scenario = Math.random();
  
  if (scenario < 0.60) {
    // 60% - Product browsing and search
    browseProducts();
  } else if (scenario < 0.85) {
    // 25% - Product detail view
    viewProductDetail(data.productIds);
  } else if (scenario < 0.95) {
    // 10% - Authentication flow
    authenticateUser();
  } else {
    // 5% - Admin operations (if authenticated)
    adminOperations();
  }
  
  // Random sleep between 1-3 seconds to simulate real user behavior
  sleep(Math.random() * 2 + 1);
}

function browseProducts() {
  const searchQuery = searchQueries[Math.floor(Math.random() * searchQueries.length)];
  const category = categories[Math.floor(Math.random() * categories.length)];
  const page = Math.floor(Math.random() * 10);
  
  const params = {
    q: searchQuery,
    category: category,
    page: page,
    size: 20,
    sort: ['price-asc', 'price-desc', 'name-asc', 'newest'][Math.floor(Math.random() * 4)]
  };
  
  const url = `${API_BASE}/products?${new URLSearchParams(params)}`;
  const startTime = Date.now();
  
  const response = http.get(url, {
    headers: { 'Content-Type': 'application/json' },
  });
  
  const duration = Date.now() - startTime;
  productSearchDuration.add(duration);
  
  const success = check(response, {
    'product search status is 200': (r) => r.status === 200,
    'product search has products': (r) => {
      try {
        const data = JSON.parse(r.body);
        return data.content && data.content.length > 0;
      } catch (e) {
        return false;
      }
    },
  });
  
  errorRate.add(!success);
}

function viewProductDetail(productIds) {
  if (productIds.length === 0) return;
  
  const productId = productIds[Math.floor(Math.random() * productIds.length)];
  const startTime = Date.now();
  
  const response = http.get(`${API_BASE}/products/${productId}`, {
    headers: { 'Content-Type': 'application/json' },
  });
  
  const duration = Date.now() - startTime;
  productDetailDuration.add(duration);
  
  const success = check(response, {
    'product detail status is 200': (r) => r.status === 200,
    'product detail has product data': (r) => {
      try {
        const product = JSON.parse(r.body);
        return product.id && product.title && product.price;
      } catch (e) {
        return false;
      }
    },
  });
  
  errorRate.add(!success);
}

function authenticateUser() {
  const user = testUsers[Math.floor(Math.random() * testUsers.length)];
  const startTime = Date.now();
  
  const loginPayload = JSON.stringify({
    email: user.email,
    password: user.password
  });
  
  const response = http.post(`${API_BASE}/auth/login`, loginPayload, {
    headers: { 'Content-Type': 'application/json' },
  });
  
  const duration = Date.now() - startTime;
  authDuration.add(duration);
  
  const success = check(response, {
    'login status is 200': (r) => r.status === 200,
    'login returns token': (r) => {
      try {
        const authData = JSON.parse(r.body);
        return authData.accessToken && authData.refreshToken;
      } catch (e) {
        return false;
      }
    },
  });
  
  errorRate.add(!success);
  
  // Store token for potential use in other scenarios
  if (success) {
    try {
      const authData = JSON.parse(response.body);
      authTokens[user.email] = authData.accessToken;
    } catch (e) {
      // Ignore parsing errors
    }
  }
}

function adminOperations() {
  // Only run admin operations if we have an admin token
  const adminEmail = 'admin@ozdilek.com';
  let adminToken = authTokens[adminEmail];
  
  // If we don't have an admin token, try to get one
  if (!adminToken) {
    const loginPayload = JSON.stringify({
      email: adminEmail,
      password: 'admin123'
    });
    
    const loginResponse = http.post(`${API_BASE}/auth/login`, loginPayload, {
      headers: { 'Content-Type': 'application/json' },
    });
    
    if (loginResponse.status === 200) {
      try {
        const authData = JSON.parse(loginResponse.body);
        adminToken = authData.accessToken;
        authTokens[adminEmail] = adminToken;
      } catch (e) {
        return; // Skip admin operations if we can't get token
      }
    } else {
      return; // Skip admin operations if login fails
    }
  }
  
  // Random admin operation
  const operation = Math.random();
  
  if (operation < 0.5) {
    // Get admin metrics
    const response = http.get(`${API_BASE}/admin/metrics`, {
      headers: { 
        'Authorization': `Bearer ${adminToken}`,
        'Content-Type': 'application/json' 
      },
    });
    
    check(response, {
      'admin metrics status is 200': (r) => r.status === 200,
    });
  } else {
    // Get seed status
    const response = http.get(`${API_BASE}/admin/seed/status`, {
      headers: { 
        'Authorization': `Bearer ${adminToken}`,
        'Content-Type': 'application/json' 
      },
    });
    
    check(response, {
      'seed status is 200': (r) => r.status === 200,
    });
  }
}

export function teardown(data) {
  console.log('🏁 Load test completed');
  console.log(`📊 Tested with ${productIds.length} products`);
}
