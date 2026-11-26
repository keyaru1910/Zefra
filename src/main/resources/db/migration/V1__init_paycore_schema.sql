-- ==============================================================================
-- V1__init_paycore_schema.sql
-- Thiết kế schema cho hệ thống PayCore (Zefra):
--   1. users              - Người dùng, merchant, nhân viên tuân thủ, quản trị viên
--   2. accounts           - Tài khoản / ví tiền đa tiền tệ (hỗ trợ optimistic lock)
--   3. transfers          - Lệnh chuyển tiền nghiệp vụ & theo dõi trạng thái
--   4. ledger_transactions- Chứng từ hạch toán tổng (bất biến - immutable)
--   5. ledger_entries     - Bút toán kép DEBIT / CREDIT gắn chặt với số dư trước/sau
--   6. fraud_assessments  - Kết quả đánh giá rủi ro gian lận & AML cho mỗi giao dịch
--   7. idempotency_records- Chống trùng lặp giao dịch khi client retry
--   8. audit_logs         - Nhật ký kiểm toán bảo mật & thao tác hệ thống
-- ==============================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ------------------------------------------------------------------------------
-- 1. Bảng người dùng (users)
-- ------------------------------------------------------------------------------
CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username      VARCHAR(64)  NOT NULL UNIQUE,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(150) NOT NULL,
    role          VARCHAR(32)  NOT NULL,
    status        VARCHAR(24)  NOT NULL DEFAULT 'ACTIVE',
    kyc_level     SMALLINT     NOT NULL DEFAULT 1,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_users_role CHECK (role IN ('USER', 'MERCHANT', 'COMPLIANCE_OFFICER', 'ADMIN')),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'FROZEN', 'SUSPENDED', 'CLOSED')),
    CONSTRAINT chk_users_kyc_level CHECK (kyc_level BETWEEN 0 AND 3)
);

CREATE INDEX idx_users_role_status ON users (role, status);

-- ------------------------------------------------------------------------------
-- 2. Bảng tài khoản / ví tiền (accounts)
-- Dùng NUMERIC(19, 4) để lưu trữ chính xác số tiền, tránh sai số dấu phẩy động.
-- allow_negative = TRUE chỉ áp dụng cho tài khoản hệ thống (CLEARING / SUSPENSE).
-- ------------------------------------------------------------------------------
CREATE TABLE accounts (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID           NOT NULL REFERENCES users (id),
    account_number    VARCHAR(34)    NOT NULL UNIQUE,
    account_type      VARCHAR(32)    NOT NULL,
    currency          CHAR(3)        NOT NULL,
    balance           NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    available_balance NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    allow_negative    BOOLEAN        NOT NULL DEFAULT FALSE,
    status            VARCHAR(24)    NOT NULL DEFAULT 'ACTIVE',
    version           BIGINT         NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_accounts_type CHECK (
        account_type IN ('WALLET', 'MERCHANT_SETTLEMENT', 'SYSTEM_FEE', 'SYSTEM_SUSPENSE', 'CLEARING')
    ),
    CONSTRAINT chk_accounts_currency CHECK (currency IN ('VND', 'USD', 'EUR', 'SGD', 'JPY')),
    CONSTRAINT chk_accounts_status CHECK (status IN ('ACTIVE', 'FROZEN', 'CLOSED')),
    CONSTRAINT chk_accounts_non_negative_balance CHECK (
        allow_negative OR (balance >= 0 AND available_balance >= 0)
    ),
    CONSTRAINT chk_accounts_available_le_balance CHECK (
        allow_negative OR available_balance <= balance
    )
);

CREATE INDEX idx_accounts_user_id ON accounts (user_id);
CREATE INDEX idx_accounts_currency_status ON accounts (currency, status);

-- ------------------------------------------------------------------------------
-- 3. Bảng lệnh chuyển tiền (transfers)
-- ------------------------------------------------------------------------------
CREATE TABLE transfers (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference_code         VARCHAR(64)    NOT NULL UNIQUE,
    idempotency_key        VARCHAR(128)   NOT NULL UNIQUE,
    source_account_id      UUID           NOT NULL REFERENCES accounts (id),
    destination_account_id UUID           NOT NULL REFERENCES accounts (id),
    amount                 NUMERIC(19, 4) NOT NULL,
    fee_amount             NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    currency               CHAR(3)        NOT NULL,
    transfer_type          VARCHAR(32)    NOT NULL,
    status                 VARCHAR(32)    NOT NULL DEFAULT 'INITIATED',
    risk_score             INTEGER,
    description            VARCHAR(255),
    failure_reason         TEXT,
    initiated_by           UUID           NOT NULL REFERENCES users (id),
    created_at             TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    completed_at           TIMESTAMPTZ,
    updated_at             TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_transfers_distinct_accounts CHECK (source_account_id <> destination_account_id),
    CONSTRAINT chk_transfers_positive_amount CHECK (amount > 0),
    CONSTRAINT chk_transfers_non_negative_fee CHECK (fee_amount >= 0),
    CONSTRAINT chk_transfers_currency CHECK (currency IN ('VND', 'USD', 'EUR', 'SGD', 'JPY')),
    CONSTRAINT chk_transfers_type CHECK (
        transfer_type IN ('P2P', 'DEPOSIT', 'WITHDRAWAL', 'MERCHANT_PAYMENT', 'FEE_COLLECTION', 'REVERSAL')
    ),
    CONSTRAINT chk_transfers_status CHECK (
        status IN (
            'INITIATED',
            'FRAUD_CHECK_PENDING',
            'APPROVED',
            'PROCESSING',
            'COMPLETED',
            'FAILED',
            'REVERSED',
            'BLOCKED'
        )
    ),
    CONSTRAINT chk_transfers_risk_score CHECK (risk_score IS NULL OR risk_score BETWEEN 0 AND 100)
);

CREATE INDEX idx_transfers_source_account ON transfers (source_account_id, created_at DESC);
CREATE INDEX idx_transfers_dest_account ON transfers (destination_account_id, created_at DESC);
CREATE INDEX idx_transfers_status_created ON transfers (status, created_at DESC);
CREATE INDEX idx_transfers_initiated_by ON transfers (initiated_by, created_at DESC);

-- ------------------------------------------------------------------------------
-- 4. Bảng chứng từ sổ cái (ledger_transactions)
-- Bất biến (append-only): không UPDATE/DELETE sau khi đã ghi sổ.
-- ------------------------------------------------------------------------------
CREATE TABLE ledger_transactions (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transfer_id           UUID         REFERENCES transfers (id),
    transaction_reference VARCHAR(64)  NOT NULL UNIQUE,
    transaction_type      VARCHAR(32)  NOT NULL,
    description           VARCHAR(255),
    posted_at             TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by            VARCHAR(100) NOT NULL,

    CONSTRAINT chk_ledger_tx_type CHECK (
        transaction_type IN ('TRANSFER', 'FEE', 'REVERSAL', 'ADJUSTMENT')
    )
);

CREATE INDEX idx_ledger_tx_transfer_id ON ledger_transactions (transfer_id);
CREATE INDEX idx_ledger_tx_posted_at ON ledger_transactions (posted_at DESC);

-- ------------------------------------------------------------------------------
-- 5. Bảng bút toán kép (ledger_entries)
-- Nguyên tắc kế toán kép: Với mỗi ledger_transaction_id và currency,
-- SUM(amount WHERE DEBIT) PHẢI BẰNG SUM(amount WHERE CREDIT).
-- ------------------------------------------------------------------------------
CREATE TABLE ledger_entries (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ledger_transaction_id UUID           NOT NULL REFERENCES ledger_transactions (id),
    account_id            UUID           NOT NULL REFERENCES accounts (id),
    entry_direction       VARCHAR(10)    NOT NULL,
    amount                NUMERIC(19, 4) NOT NULL,
    currency              CHAR(3)        NOT NULL,
    balance_before        NUMERIC(19, 4) NOT NULL,
    balance_after         NUMERIC(19, 4) NOT NULL,
    created_at            TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_ledger_entry_direction CHECK (entry_direction IN ('DEBIT', 'CREDIT')),
    CONSTRAINT chk_ledger_entry_positive_amount CHECK (amount > 0),
    CONSTRAINT chk_ledger_entry_currency CHECK (currency IN ('VND', 'USD', 'EUR', 'SGD', 'JPY'))
);

CREATE INDEX idx_ledger_entries_tx_id ON ledger_entries (ledger_transaction_id);
CREATE INDEX idx_ledger_entries_account_created ON ledger_entries (account_id, created_at DESC);

-- ------------------------------------------------------------------------------
-- 6. Bảng đánh giá gian lận (fraud_assessments)
-- ------------------------------------------------------------------------------
CREATE TABLE fraud_assessments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transfer_id     UUID        NOT NULL UNIQUE REFERENCES transfers (id),
    risk_score      INTEGER     NOT NULL,
    decision        VARCHAR(16) NOT NULL,
    triggered_rules JSONB       NOT NULL DEFAULT '[]'::jsonb,
    reviewer_id     UUID        REFERENCES users (id),
    reviewed_at     TIMESTAMPTZ,
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_fraud_risk_score CHECK (risk_score BETWEEN 0 AND 100),
    CONSTRAINT chk_fraud_decision CHECK (decision IN ('ALLOW', 'REVIEW', 'BLOCK'))
);

CREATE INDEX idx_fraud_decision_created ON fraud_assessments (decision, created_at DESC);

-- ------------------------------------------------------------------------------
-- 7. Bảng lưu khóa chống trùng lặp (idempotency_records)
-- ------------------------------------------------------------------------------
CREATE TABLE idempotency_records (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key VARCHAR(128) NOT NULL,
    user_id         UUID         NOT NULL REFERENCES users (id),
    request_hash    VARCHAR(64)  NOT NULL,
    response_status INTEGER,
    response_body   TEXT,
    locked_at       TIMESTAMPTZ,
    expires_at      TIMESTAMPTZ  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_idempotency_user_key UNIQUE (user_id, idempotency_key)
);

CREATE INDEX idx_idempotency_expires_at ON idempotency_records (expires_at);

-- ------------------------------------------------------------------------------
-- 8. Bảng nhật ký kiểm toán (audit_logs)
-- ------------------------------------------------------------------------------
CREATE TABLE audit_logs (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id       UUID,
    actor_username VARCHAR(100),
    action         VARCHAR(100) NOT NULL,
    resource_type  VARCHAR(50)  NOT NULL,
    resource_id    VARCHAR(64),
    ip_address     VARCHAR(45),
    details        TEXT,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_actor ON audit_logs (actor_id, created_at DESC);
CREATE INDEX idx_audit_logs_resource ON audit_logs (resource_type, resource_id);
