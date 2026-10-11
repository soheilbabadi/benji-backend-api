-- V001__create_consultation_schema.sql

-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ENUMS

CREATE TYPE consultation_status AS ENUM (
    'DRAFT', 'PAYMENT_PENDING', 'SUBMITTED', 'UNDER_REVIEW',
    'WAITING_FOR_USER', 'ANSWERED', 'CLOSED', 'CANCELLED', 'PAYMENT_FAILED'
);

CREATE TYPE payment_status AS ENUM (
    'PENDING', 'SUCCESS', 'FAILED', 'REFUNDED', 'CANCELLED'
);

CREATE TYPE consultation_category AS ENUM (
    'GENERAL_HEALTH', 'NUTRITION', 'BEHAVIOR',
    'GROOMING_AND_CARE', 'TRAINING', 'OTHER'
);

CREATE TYPE urgency_level AS ENUM (
    'NO_URGENT_ACTION', 'MONITOR', 'IN_PERSON_VISIT_RECOMMENDED', 'URGENT_VETERINARY_VISIT'
);

CREATE TYPE pet_data_type AS ENUM (
    'BASIC_INFO', 'AGE', 'BREED', 'GENDER', 'WEIGHT_HISTORY',
    'MEDICAL_HISTORY', 'ALLERGIES', 'MEDICATIONS', 'VACCINATIONS', 'RECENT_EVENTS', 'TIMELINE'
);

CREATE TYPE expert_verification_status AS ENUM (
    'PENDING', 'VERIFIED', 'REJECTED'
);

-- TABLES

-- Expert Profiles
CREATE TABLE expert_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE, -- Links to existing User table
    verification_status expert_verification_status NOT NULL DEFAULT 'PENDING',
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    bio TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Expert Specialties
CREATE TABLE expert_specialties (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    expert_id UUID NOT NULL REFERENCES expert_profiles(id) ON DELETE CASCADE,
    specialty VARCHAR(50) NOT NULL, -- Matches consultation_category values or custom
    UNIQUE(expert_id, specialty)
);

-- Consultations
CREATE TABLE consultations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_id UUID NOT NULL, -- Links to existing User table
    pet_id UUID NOT NULL,   -- Links to existing Pet table
    category consultation_category NOT NULL,
    subject VARCHAR(255) NOT NULL,
    question TEXT NOT NULL,
    status consultation_status NOT NULL DEFAULT 'DRAFT',
    price_amount BIGINT NOT NULL, -- Stored in cents
    currency CHAR(3) NOT NULL DEFAULT 'USD',
    emergency_disclaimer_accepted BOOLEAN NOT NULL DEFAULT FALSE,
    assigned_expert_id UUID REFERENCES expert_profiles(id),
    version BIGINT NOT NULL DEFAULT 0, -- Optimistic locking
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    closed_at TIMESTAMP WITH TIME ZONE,
    answered_at TIMESTAMP WITH TIME ZONE
);

-- Shared Pet Data Snapshot (What the owner allowed to share)
CREATE TABLE consultation_shared_pet_data (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    consultation_id UUID NOT NULL REFERENCES consultations(id) ON DELETE CASCADE,
    data_type pet_data_type NOT NULL,
    content_snapshot JSONB NOT NULL, -- Stores the actual data at the time of sharing
    UNIQUE(consultation_id, data_type)
);

-- Consultation Attachments
CREATE TABLE consultation_attachments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    consultation_id UUID NOT NULL REFERENCES consultations(id) ON DELETE CASCADE,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_key VARCHAR(255) NOT NULL, -- Path/Key in storage
    uploaded_by UUID NOT NULL, -- User ID
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Consultation Messages
CREATE TABLE consultation_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    consultation_id UUID NOT NULL REFERENCES consultations(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL, -- User ID (Owner or Expert)
    sender_role VARCHAR(20) NOT NULL, -- 'OWNER', 'EXPERT', 'SYSTEM'
    content TEXT NOT NULL,
    is_system_message BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Consultation Answers (Final Structured Answer)
CREATE TABLE consultation_answers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    consultation_id UUID NOT NULL UNIQUE REFERENCES consultations(id) ON DELETE CASCADE,
    expert_id UUID NOT NULL REFERENCES expert_profiles(id),
    assessment TEXT NOT NULL,
    recommended_actions TEXT,
    warning_signs TEXT,
    in_person_visit_recommended BOOLEAN NOT NULL DEFAULT FALSE,
    urgency urgency_level NOT NULL,
    submitted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Consultation Payments
CREATE TABLE consultation_payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    consultation_id UUID NOT NULL UNIQUE REFERENCES consultations(id) ON DELETE CASCADE,
    provider_payment_id VARCHAR(255), -- ID from Stripe/PayPal
    status payment_status NOT NULL DEFAULT 'PENDING',
    amount BIGINT NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'USD',
    idempotency_key VARCHAR(255) UNIQUE, -- For idempotent callbacks
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    paid_at TIMESTAMP WITH TIME ZONE
);

-- INDEXES

CREATE INDEX idx_consultations_owner ON consultations(owner_id);
CREATE INDEX idx_consultations_status ON consultations(status);
CREATE INDEX idx_consultations_expert ON consultations(assigned_expert_id);
CREATE INDEX idx_messages_consultation ON consultation_messages(consultation_id, created_at);
CREATE INDEX idx_expert_specialties ON expert_specialties(specialty);
CREATE INDEX idx_expert_profiles_active ON expert_profiles(is_active, verification_status);
