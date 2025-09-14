import http from 'k6/http';
import { check, sleep } from 'k6';

// Configuration
const BASE_URL = 'http://localhost:8080';
const API_BASE = `${BASE_URL}/api`;

export const options = {
  vus: 10, // 10 virtual users
  duration: '2m', // 2 minutes
  thresholds: {
    http_req_duration: ['p(95)<1000'], // 95% of requests should be faster than 1s
    http_req_failed: ['rate<0.1'], // Error rate should be less than 10%
  },
};

export function setup() {
  console.log('🚀 Starting smoke test...');
  
  // Check if application is running
  const healthResponse = http.get(`${BASE_URL}/actuator/health`);
  if (healthResponse.status !== 200) {
    throw new Error('Application is not healthy');
  }
  
  console.log('✅ Application is healthy');
  return {};
}

export default function(data) {
  // Test basic endpoints
  testPublicEndpoints();
  
  sleep(1);
}

function testPublicEndpoints() {
  // Test categories endpoint
  const categoriesResponse = http.get(`${API_BASE}/categories`);
  check(categoriesResponse, {
    'categories endpoint returns 200': (r) => r.status === 200,
    'categories endpoint has data': (r) => {
      try {
        const categories = JSON.parse(r.body);
        return Array.isArray(categories) && categories.length > 0;
      } catch (e) {
        return false;
      }
    },
  });
  
  // Test products endpoint
  const productsResponse = http.get(`${API_BASE}/products?size=10`);
  check(productsResponse, {
    'products endpoint returns 200': (r) => r.status === 200,
    'products endpoint has data': (r) => {
      try {
        const products = JSON.parse(r.body);
        return products.content && Array.isArray(products.content);
      } catch (e) {
        return false;
      }
    },
  });
  
  // Test product search
  const searchResponse = http.get(`${API_BASE}/products?q=yatak&size=5`);
  check(searchResponse, {
    'search endpoint returns 200': (r) => r.status === 200,
  });
  
  // Test authentication
  const loginPayload = JSON.stringify({
    email: 'test1@ozdilek.com',
    password: 'test123'
  });
  
  const authResponse = http.post(`${API_BASE}/auth/login`, loginPayload, {
    headers: { 'Content-Type': 'application/json' },
  });
  
  check(authResponse, {
    'auth endpoint returns 200': (r) => r.status === 200,
    'auth endpoint returns token': (r) => {
      try {
        const authData = JSON.parse(r.body);
        return authData.accessToken && authData.refreshToken;
      } catch (e) {
        return false;
      }
    },
  });
}

export function teardown(data) {
  console.log('✅ Smoke test completed successfully');
}
