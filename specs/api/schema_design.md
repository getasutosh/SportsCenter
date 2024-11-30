# SportsCenter Database Schema Design

## Overview
This document outlines the database schema design for the SportsCenter platform. The schema is designed to support all core functionalities while maintaining data integrity, scalability, and performance.

## Database Technology
- Primary Database: PostgreSQL
- Cache Layer: Redis
- File Storage: AWS S3

## Core Entities

### 1. Users
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('athlete', 'parent', 'coach', 'mentor', 'admin')),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    date_of_birth DATE,
    profile_image_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(20) DEFAULT 'active',
    verification_status BOOLEAN DEFAULT false
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
```

### 2. Athletes
```sql
CREATE TABLE athletes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    parent_id UUID REFERENCES users(id),
    current_level VARCHAR(50),
    primary_sport VARCHAR(100) NOT NULL,
    secondary_sports TEXT[],
    height_cm INTEGER,
    weight_kg DECIMAL(5,2),
    goals TEXT[],
    medical_conditions TEXT,
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_athletes_parent_id ON athletes(parent_id);
CREATE INDEX idx_athletes_primary_sport ON athletes(primary_sport);
```

### 3. Coaches
```sql
CREATE TABLE coaches (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    specializations TEXT[],
    certification_ids TEXT[],
    years_experience INTEGER,
    bio TEXT,
    hourly_rate DECIMAL(10,2),
    availability JSONB,
    rating DECIMAL(3,2),
    total_reviews INTEGER DEFAULT 0,
    verification_documents JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_coaches_specializations ON coaches USING GIN(specializations);
CREATE INDEX idx_coaches_rating ON coaches(rating);
```

### 4. Training Sessions
```sql
CREATE TABLE training_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    coach_id UUID REFERENCES coaches(id),
    athlete_id UUID REFERENCES athletes(id),
    session_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'scheduled',
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    location JSONB,
    price DECIMAL(10,2),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sessions_coach_id ON training_sessions(coach_id);
CREATE INDEX idx_sessions_athlete_id ON training_sessions(athlete_id);
CREATE INDEX idx_sessions_start_time ON training_sessions(start_time);
```

### 5. Performance Metrics
```sql
CREATE TABLE performance_metrics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    athlete_id UUID REFERENCES athletes(id),
    metric_type VARCHAR(100) NOT NULL,
    value JSONB NOT NULL,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    recorded_by UUID REFERENCES users(id),
    session_id UUID REFERENCES training_sessions(id),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_metrics_athlete_id ON performance_metrics(athlete_id);
CREATE INDEX idx_metrics_type ON performance_metrics(metric_type);
```

### 6. Video Analysis
```sql
CREATE TABLE video_analysis (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    athlete_id UUID REFERENCES athletes(id),
    session_id UUID REFERENCES training_sessions(id),
    video_url TEXT NOT NULL,
    thumbnail_url TEXT,
    duration INTEGER,
    analysis_results JSONB,
    ai_feedback JSONB,
    coach_feedback TEXT,
    status VARCHAR(50) DEFAULT 'pending',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_video_athlete_id ON video_analysis(athlete_id);
CREATE INDEX idx_video_session_id ON video_analysis(session_id);
```

### 7. Training Programs
```sql
CREATE TABLE training_programs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    coach_id UUID REFERENCES coaches(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    difficulty_level VARCHAR(50),
    duration_weeks INTEGER,
    sport_type VARCHAR(100),
    target_skills TEXT[],
    program_content JSONB,
    price DECIMAL(10,2),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_programs_coach_id ON training_programs(coach_id);
CREATE INDEX idx_programs_sport_type ON training_programs(sport_type);
```

### 8. Nutrition Plans
```sql
CREATE TABLE nutrition_plans (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    athlete_id UUID REFERENCES athletes(id),
    created_by UUID REFERENCES users(id),
    plan_type VARCHAR(50),
    start_date DATE,
    end_date DATE,
    daily_plans JSONB,
    dietary_restrictions TEXT[],
    goals TEXT[],
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_nutrition_athlete_id ON nutrition_plans(athlete_id);
```

### 9. Payments
```sql
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payer_id UUID REFERENCES users(id),
    recipient_id UUID REFERENCES users(id),
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    status VARCHAR(50) DEFAULT 'pending',
    payment_type VARCHAR(50),
    session_id UUID REFERENCES training_sessions(id),
    program_id UUID REFERENCES training_programs(id),
    transaction_id VARCHAR(255),
    payment_method_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payments_payer ON payments(payer_id);
CREATE INDEX idx_payments_recipient ON payments(recipient_id);
CREATE INDEX idx_payments_status ON payments(status);
```

## Redis Cache Schema

### Key Patterns
1. User Sessions: `session:{userId}`
2. Rate Limiting: `ratelimit:{userId}:{endpoint}`
3. Performance Metrics Cache: `metrics:{athleteId}:latest`
4. Coach Availability Cache: `availability:{coachId}:{date}`
5. Video Processing Status: `video:status:{videoId}`

## File Storage Structure (S3)

### Bucket Organization
```
sports-center/
├── profile-images/
│   └── {userId}/
├── videos/
│   ├── raw/{userId}/{videoId}/
│   ├── processed/{userId}/{videoId}/
│   └── thumbnails/{userId}/{videoId}/
├── documents/
│   └── certifications/{userId}/
└── training-materials/
    └── {programId}/
```

## Notes on Schema Design

### 1. Performance Considerations
- Indexes on frequently queried fields
- JSONB for flexible data structures
- Partitioning strategy for large tables (videos, metrics)

### 2. Security Features
- Password hashing
- Role-based access control
- Audit trails for sensitive operations

### 3. Scalability Features
- UUID as primary keys for horizontal scaling
- Normalized design for core entities
- Denormalized data where appropriate for performance

### 4. Data Integrity
- Foreign key constraints
- Check constraints for enums
- Timestamp tracking for all records

### 5. Future Considerations
- Partition strategy for historical data
- Archival strategy for old videos/metrics
- Sharding strategy for multi-region deployment
