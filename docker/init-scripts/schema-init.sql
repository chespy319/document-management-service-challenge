--The script to initialize the schema was sourced from the Spring Batch Core dependency: org.springframework.batch.core.

CREATE SCHEMA document_schema;
SET SCHEMA 'document_schema';

-- table users
CREATE TABLE document_schema.users (
    id BIGSERIAL PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- table documents
CREATE TABLE document_schema.documents (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES document_schema.users(id) ON DELETE CASCADE,
    document_name TEXT NOT NULL,
    minio_path TEXT UNIQUE NOT NULL,
    file_size BIGINT NOT NULL,
    file_type TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_documents_user_id ON document_schema.documents(user_id);
CREATE INDEX idx_documents_name ON document_schema.documents(document_name);
CREATE INDEX idx_documents_created_at ON document_schema.documents(created_at DESC);
CREATE UNIQUE INDEX idx_documents_user_name_unique ON document_schema.documents(user_id, document_name);

-- table tags
CREATE TABLE document_schema.tags (
    id BIGSERIAL PRIMARY KEY,
    name TEXT UNIQUE NOT NULL
);

-- table documents-tags
CREATE TABLE document_schema.documents_tags (
    document_id BIGINT NOT NULL REFERENCES document_schema.documents(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES document_schema.tags(id) ON DELETE CASCADE,
    PRIMARY KEY (document_id, tag_id)
);
CREATE INDEX idx_documents_tags_tag_id ON document_schema.documents_tags(tag_id);
