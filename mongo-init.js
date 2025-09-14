// MongoDB initialization script
db = db.getSiblingDB('ecommerce');

// Create collections with validation
db.createCollection('users', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['email', 'passwordHash', 'name'],
      properties: {
        email: {
          bsonType: 'string',
          pattern: '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$',
          description: 'Email must be a valid email address'
        },
        passwordHash: {
          bsonType: 'string',
          description: 'Password hash is required'
        },
        name: {
          bsonType: 'string',
          description: 'Name is required'
        },
        roles: {
          bsonType: 'array',
          items: {
            bsonType: 'string',
            enum: ['USER', 'ADMIN', 'STAFF']
          },
          description: 'Roles must be valid role types'
        }
      }
    }
  }
});

db.createCollection('products', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['sku', 'title', 'slug', 'price'],
      properties: {
        sku: {
          bsonType: 'string',
          description: 'SKU is required'
        },
        title: {
          bsonType: 'string',
          description: 'Title is required'
        },
        slug: {
          bsonType: 'string',
          description: 'Slug is required'
        },
        price: {
          bsonType: 'decimal',
          description: 'Price is required'
        },
        currency: {
          bsonType: 'string',
          enum: ['TRY', 'USD', 'EUR'],
          description: 'Currency must be valid'
        },
        stock: {
          bsonType: 'int',
          minimum: 0,
          description: 'Stock must be non-negative'
        }
      }
    }
  }
});

db.createCollection('categories');
db.createCollection('carts');
db.createCollection('orders');
db.createCollection('refresh_tokens');

// Create indexes for better performance
print('Creating indexes...');

// Users indexes
db.users.createIndex({ email: 1 }, { unique: true });
db.users.createIndex({ lastLoginAt: 1 });

// Products indexes
db.products.createIndex({ sku: 1 }, { unique: true });
db.products.createIndex({ slug: 1 }, { unique: true });
db.products.createIndex({ title: 'text', description: 'text' });
db.products.createIndex({ categories: 1, price: 1 });
db.products.createIndex({ available: 1, stock: 1 });
db.products.createIndex({ createdAt: -1 });

// Categories indexes
db.categories.createIndex({ slug: 1 }, { unique: true });
db.categories.createIndex({ parentId: 1, sortOrder: 1 });
db.categories.createIndex({ active: 1 });

// Carts indexes
db.carts.createIndex({ userId: 1 });
db.carts.createIndex({ sessionId: 1 });
db.carts.createIndex({ updatedAt: 1 });

// Orders indexes
db.orders.createIndex({ userId: 1, createdAt: -1 });
db.orders.createIndex({ status: 1 });

// Refresh tokens indexes
db.refresh_tokens.createIndex({ tokenHash: 1 });
db.refresh_tokens.createIndex({ userId: 1 });
db.refresh_tokens.createIndex({ expiresAt: 1 }, { expireAfterSeconds: 0 });

print('Database initialization completed successfully!');
print('Collections created: users, products, categories, carts, orders, refresh_tokens');
print('Indexes created for optimal performance');
