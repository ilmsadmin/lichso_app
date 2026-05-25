// ============================================
// Zplus Base - MongoDB Initialization Script
// ============================================
// This script runs automatically when MongoDB
// container is created for the first time.
// ============================================

// Switch to application database
db = db.getSiblingDB('zplus_lichso_logs');

// ============================================
// Create Collections with Schema Validation
// ============================================

// Activity Logs Collection
db.createCollection('activity_logs', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['user_id', 'action', 'created_at'],
      properties: {
        user_id: {
          bsonType: 'string',
          description: 'UUID of the user who performed the action'
        },
        action: {
          bsonType: 'string',
          description: 'Action type (e.g., user.login, user.create)'
        },
        resource: {
          bsonType: 'string',
          description: 'Resource type (e.g., user, role, permission)'
        },
        resource_id: {
          bsonType: 'string',
          description: 'ID of the affected resource'
        },
        details: {
          bsonType: 'object',
          description: 'Additional details about the action'
        },
        ip_address: {
          bsonType: 'string',
          description: 'IP address of the client'
        },
        user_agent: {
          bsonType: 'string',
          description: 'User agent string'
        },
        created_at: {
          bsonType: 'date',
          description: 'Timestamp of the action'
        }
      }
    }
  }
});

// App Settings Collection
db.createCollection('app_settings', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['key', 'value'],
      properties: {
        key: {
          bsonType: 'string',
          description: 'Setting key (unique identifier)'
        },
        value: {
          description: 'Setting value (any type)'
        },
        group: {
          bsonType: 'string',
          description: 'Setting group (e.g., general, email, security)'
        },
        description: {
          bsonType: 'string',
          description: 'Setting description'
        },
        updated_by: {
          bsonType: 'string',
          description: 'UUID of the user who last updated'
        },
        updated_at: {
          bsonType: 'date',
          description: 'Last update timestamp'
        }
      }
    }
  }
});

// Media Collection
db.createCollection('media', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['filename', 'path', 'mime_type', 'uploaded_by', 'created_at'],
      properties: {
        filename: {
          bsonType: 'string',
          description: 'Original filename'
        },
        path: {
          bsonType: 'string',
          description: 'Storage path'
        },
        mime_type: {
          bsonType: 'string',
          description: 'MIME type of the file'
        },
        size: {
          bsonType: 'long',
          description: 'File size in bytes'
        },
        uploaded_by: {
          bsonType: 'string',
          description: 'UUID of the uploader'
        },
        created_at: {
          bsonType: 'date',
          description: 'Upload timestamp'
        }
      }
    }
  }
});

// Notifications Collection
db.createCollection('notifications', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['user_id', 'title', 'type', 'created_at'],
      properties: {
        user_id: {
          bsonType: 'string',
          description: 'Target user UUID'
        },
        title: {
          bsonType: 'string',
          description: 'Notification title'
        },
        message: {
          bsonType: 'string',
          description: 'Notification message'
        },
        type: {
          bsonType: 'string',
          enum: ['info', 'warning', 'error', 'success'],
          description: 'Notification type'
        },
        is_read: {
          bsonType: 'bool',
          description: 'Read status'
        },
        link: {
          bsonType: 'string',
          description: 'Related link'
        },
        created_at: {
          bsonType: 'date',
          description: 'Creation timestamp'
        }
      }
    }
  }
});

// ============================================
// Create Indexes
// ============================================

// Activity Logs Indexes
db.activity_logs.createIndex({ user_id: 1, created_at: -1 });
db.activity_logs.createIndex({ action: 1 });
db.activity_logs.createIndex({ resource: 1, resource_id: 1 });
db.activity_logs.createIndex(
  { created_at: 1 },
  { expireAfterSeconds: 7776000 } // Auto-delete after 90 days
);

// App Settings Indexes
db.app_settings.createIndex({ key: 1 }, { unique: true });
db.app_settings.createIndex({ group: 1 });

// Media Indexes
db.media.createIndex({ uploaded_by: 1 });
db.media.createIndex({ created_at: -1 });
db.media.createIndex({ mime_type: 1 });

// Notifications Indexes
db.notifications.createIndex({ user_id: 1, is_read: 1, created_at: -1 });
db.notifications.createIndex(
  { created_at: 1 },
  { expireAfterSeconds: 2592000 } // Auto-delete after 30 days
);

// ============================================
// Insert default app settings
// ============================================
db.app_settings.insertMany([
  {
    key: 'site_name',
    value: 'Zplus Base',
    group: 'general',
    description: 'Website name',
    updated_at: new Date()
  },
  {
    key: 'site_description',
    value: 'Admin Base Platform',
    group: 'general',
    description: 'Website description',
    updated_at: new Date()
  },
  {
    key: 'maintenance_mode',
    value: false,
    group: 'general',
    description: 'Enable maintenance mode',
    updated_at: new Date()
  },
  {
    key: 'max_login_attempts',
    value: 5,
    group: 'security',
    description: 'Maximum login attempts before lockout',
    updated_at: new Date()
  },
  {
    key: 'lockout_duration',
    value: 900,
    group: 'security',
    description: 'Account lockout duration in seconds (15 min)',
    updated_at: new Date()
  }
]);

print('✅ Zplus Base - MongoDB initialized successfully!');
