-- ==============================================================================
-- V2__seed_initial_data.sql
-- Dữ liệu mẫu khởi tạo cho môi trường phát triển & kiểm thử PayCore (Zefra).
--
-- Lưu ý bảo mật:
--   Mật khẩu mẫu cho toàn bộ tài khoản demo là: PayCore@2026
--   (Hash BCrypt cost=10 chỉ dùng cho dữ liệu seed dev/staging).
--   Dữ liệu sổ cái (ledger_entries) đảm bảo cân bằng tuyệt đối: SUM(DEBIT) = SUM(CREDIT).
-- ==============================================================================

-- 1. Người dùng mẫu (System, Admin, Compliance Officer, 2 Customers, 1 Merchant)
INSERT INTO users (id, username, email, password_hash, full_name, role, status, kyc_level)
VALUES
    ('10000000-0000-0000-0000-000000000001', 'system.treasury', 'treasury@paycore.local',
     '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5L9q0Y8mJ8p3z5wK1gL2Q4W6X8Y2.', 'PayCore System Treasury', 'ADMIN', 'ACTIVE', 3),

    ('10000000-0000-0000-0000-000000000002', 'admin.paycore', 'admin@paycore.local',
     '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5L9q0Y8mJ8p3z5wK1gL2Q4W6X8Y2.', 'Quản trị viên Hệ thống', 'ADMIN', 'ACTIVE', 3),

    ('10000000-0000-0000-0000-000000000003', 'compliance.officer', 'compliance@paycore.local',
     '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5L9q0Y8mJ8p3z5wK1gL2Q4W6X8Y2.', 'Chuyên viên Tuân thủ AML', 'COMPLIANCE_OFFICER', 'ACTIVE', 3),

    ('10000000-0000-0000-0000-000000000004', 'nguyen.van.an', 'an.nguyen@example.com',
     '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5L9q0Y8mJ8p3z5wK1gL2Q4W6X8Y2.', 'Nguyễn Văn An', 'USER', 'ACTIVE', 2),

    ('10000000-0000-0000-0000-000000000005', 'tran.thi.binh', 'binh.tran@example.com',
     '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5L9q0Y8mJ8p3z5wK1gL2Q4W6X8Y2.', 'Trần Thị Bình', 'USER', 'ACTIVE', 2),

    ('10000000-0000-0000-0000-000000000006', 'zefra.mart', 'billing@zeframart.vn',
     '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5L9q0Y8mJ8p3z5wK1gL2Q4W6X8Y2.', 'Zefra Mart Merchant', 'MERCHANT', 'ACTIVE', 3)
ON CONFLICT (id) DO NOTHING;

-- 2. Tài khoản hệ thống & ví người dùng
-- Tài khoản CLEARING (đối ứng nạp/rút) được phép ghi âm (allow_negative = TRUE)
-- để phản ánh tổng lượng tiền đã phát hành vào hệ thống (Zero-sum ledger).
INSERT INTO accounts (id, user_id, account_number, account_type, currency, balance, available_balance, allow_negative, status)
VALUES
    -- Tài khoản bù trừ (Clearing) & Thu phí hệ thống - VND
    ('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001',
     'SYS-CLR-VND-0001', 'CLEARING', 'VND', -75000000.0000, -75000000.0000, TRUE, 'ACTIVE'),

    ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001',
     'SYS-FEE-VND-0001', 'SYSTEM_FEE', 'VND', 11000.0000, 11000.0000, FALSE, 'ACTIVE'),

    -- Tài khoản bù trừ (Clearing) & Thu phí hệ thống - USD
    ('20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001',
     'SYS-CLR-USD-0001', 'CLEARING', 'USD', -5000.0000, -5000.0000, TRUE, 'ACTIVE'),

    ('20000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000001',
     'SYS-FEE-USD-0001', 'SYSTEM_FEE', 'USD', 0.0000, 0.0000, FALSE, 'ACTIVE'),

    -- Ví khách hàng Nguyễn Văn An (VND & USD)
    ('20000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000004',
     'ZEF-VND-10000001', 'WALLET', 'VND', 48489000.0000, 48489000.0000, FALSE, 'ACTIVE'),

    ('20000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000004',
     'ZEF-USD-10000001', 'WALLET', 'USD', 5000.0000, 5000.0000, FALSE, 'ACTIVE'),

    -- Ví khách hàng Trần Thị Bình (VND)
    ('20000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000005',
     'ZEF-VND-10000002', 'WALLET', 'VND', 25000000.0000, 25000000.0000, FALSE, 'ACTIVE'),

    -- Tài khoản đối soát Merchant Zefra Mart (VND)
    ('20000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000006',
     'MRC-VND-90000001', 'MERCHANT_SETTLEMENT', 'VND', 1500000.0000, 1500000.0000, FALSE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- Kiểm chứng nhanh tổng số dư VND:
--   -75,000,000 (Clearing) + 11,000 (Fee) + 48,489,000 (An) + 25,000,000 (Bình) + 1,500,000 (Zefra Mart) = 0 VND!
-- Kiểm chứng tổng số dư USD:
--   -5,000 (Clearing) + 5,000 (An) = 0 USD!

-- 3. Giao dịch chuyển tiền mẫu
INSERT INTO transfers (
    id, reference_code, idempotency_key,
    source_account_id, destination_account_id,
    amount, fee_amount, currency, transfer_type, status,
    risk_score, description, initiated_by, created_at, completed_at
)
VALUES
    -- Nạp 50,000,000 VND vào ví Nguyễn Văn An
    ('30000000-0000-0000-0000-000000000001', 'TRF-20261126-0001', 'seed-idem-deposit-an-vnd',
     '20000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000005',
     50000000.0000, 0.0000, 'VND', 'DEPOSIT', 'COMPLETED',
     5, 'Nạp tiền khởi tạo từ ngân hàng liên kết', '10000000-0000-0000-0000-000000000004',
     NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),

    -- Nạp 25,000,000 VND vào ví Trần Thị Bình
    ('30000000-0000-0000-0000-000000000002', 'TRF-20261126-0002', 'seed-idem-deposit-binh-vnd',
     '20000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000007',
     25000000.0000, 0.0000, 'VND', 'DEPOSIT', 'COMPLETED',
     5, 'Nạp tiền khởi tạo từ ngân hàng liên kết', '10000000-0000-0000-0000-000000000005',
     NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),

    -- Nạp 5,000 USD vào ví Nguyễn Văn An
    ('30000000-0000-0000-0000-000000000003', 'TRF-20261126-0003', 'seed-idem-deposit-an-usd',
     '20000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000006',
     5000.0000, 0.0000, 'USD', 'DEPOSIT', 'COMPLETED',
     10, 'Nạp tiền USD qua SWIFT', '10000000-0000-0000-0000-000000000004',
     NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),

    -- Nguyễn Văn An thanh toán đơn hàng 1,500,000 VND cho Zefra Mart (Phí 11,000 VND)
    ('30000000-0000-0000-0000-000000000004', 'TRF-20261126-0004', 'seed-idem-merchant-pay-01',
     '20000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000008',
     1500000.0000, 11000.0000, 'VND', 'MERCHANT_PAYMENT', 'COMPLETED',
     18, 'Thanh toán đơn hàng #ZM-202611-889 tại Zefra Mart', '10000000-0000-0000-0000-000000000004',
     NOW() - INTERVAL '6 hours', NOW() - INTERVAL '6 hours')
ON CONFLICT (id) DO NOTHING;

-- 4. Chứng từ sổ cái (ledger_transactions)
INSERT INTO ledger_transactions (id, transfer_id, transaction_reference, transaction_type, description, posted_at, created_by)
VALUES
    ('40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001',
     'LTX-20261126-0001', 'TRANSFER', 'Hạch toán nạp tiền VND cho Nguyễn Văn An', NOW() - INTERVAL '2 days', 'system'),

    ('40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002',
     'LTX-20261126-0002', 'TRANSFER', 'Hạch toán nạp tiền VND cho Trần Thị Bình', NOW() - INTERVAL '2 days', 'system'),

    ('40000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000003',
     'LTX-20261126-0003', 'TRANSFER', 'Hạch toán nạp tiền USD cho Nguyễn Văn An', NOW() - INTERVAL '1 day', 'system'),

    ('40000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000004',
     'LTX-20261126-0004', 'TRANSFER', 'Hạch toán thanh toán merchant Zefra Mart', NOW() - INTERVAL '6 hours', 'system'),

    ('40000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000004',
     'LTX-20261126-0005', 'FEE', 'Hạch toán thu phí giao dịch merchant', NOW() - INTERVAL '6 hours', 'system')
ON CONFLICT (id) DO NOTHING;

-- 5. Bút toán kép (ledger_entries) - Mỗi giao dịch có cặp DEBIT / CREDIT cân bằng
INSERT INTO ledger_entries (id, ledger_transaction_id, account_id, entry_direction, amount, currency, balance_before, balance_after, created_at)
VALUES
    -- LTX-0001: Nạp 50M VND cho An
    ('50000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001',
     '20000000-0000-0000-0000-000000000001', 'DEBIT', 50000000.0000, 'VND', 0.0000, -50000000.0000, NOW() - INTERVAL '2 days'),
    ('50000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000001',
     '20000000-0000-0000-0000-000000000005', 'CREDIT', 50000000.0000, 'VND', 0.0000, 50000000.0000, NOW() - INTERVAL '2 days'),

    -- LTX-0002: Nạp 25M VND cho Bình
    ('50000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000002',
     '20000000-0000-0000-0000-000000000001', 'DEBIT', 25000000.0000, 'VND', -50000000.0000, -75000000.0000, NOW() - INTERVAL '2 days'),
    ('50000000-0000-0000-0000-000000000004', '40000000-0000-0000-0000-000000000002',
     '20000000-0000-0000-0000-000000000007', 'CREDIT', 25000000.0000, 'VND', 0.0000, 25000000.0000, NOW() - INTERVAL '2 days'),

    -- LTX-0003: Nạp 5,000 USD cho An
    ('50000000-0000-0000-0000-000000000005', '40000000-0000-0000-0000-000000000003',
     '20000000-0000-0000-0000-000000000003', 'DEBIT', 5000.0000, 'USD', 0.0000, -5000.0000, NOW() - INTERVAL '1 day'),
    ('50000000-0000-0000-0000-000000000006', '40000000-0000-0000-0000-000000000003',
     '20000000-0000-0000-0000-000000000006', 'CREDIT', 5000.0000, 'USD', 0.0000, 5000.0000, NOW() - INTERVAL '1 day'),

    -- LTX-0004: Chuyển 1,500,000 VND từ An sang Zefra Mart
    ('50000000-0000-0000-0000-000000000007', '40000000-0000-0000-0000-000000000004',
     '20000000-0000-0000-0000-000000000005', 'DEBIT', 1500000.0000, 'VND', 50000000.0000, 48500000.0000, NOW() - INTERVAL '6 hours'),
    ('50000000-0000-0000-0000-000000000008', '40000000-0000-0000-0000-000000000004',
     '20000000-0000-0000-0000-000000000008', 'CREDIT', 1500000.0000, 'VND', 0.0000, 1500000.0000, NOW() - INTERVAL '6 hours'),

    -- LTX-0005: Thu phí 11,000 VND từ An vào tài khoản SYSTEM_FEE
    ('50000000-0000-0000-0000-000000000009', '40000000-0000-0000-0000-000000000005',
     '20000000-0000-0000-0000-000000000005', 'DEBIT', 11000.0000, 'VND', 48500000.0000, 48489000.0000, NOW() - INTERVAL '6 hours'),
    ('50000000-0000-0000-0000-000000000010', '40000000-0000-0000-0000-000000000005',
     '20000000-0000-0000-0000-000000000002', 'CREDIT', 11000.0000, 'VND', 0.0000, 11000.0000, NOW() - INTERVAL '6 hours')
ON CONFLICT (id) DO NOTHING;

-- 6. Đánh giá rủi ro gian lận mẫu
INSERT INTO fraud_assessments (id, transfer_id, risk_score, decision, triggered_rules, notes, created_at)
VALUES
    ('60000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001',
     5, 'ALLOW', '["TRUSTED_BANK_DEPOSIT"]'::jsonb, 'Giao dịch nạp tiền hợp lệ từ tài khoản đã xác thực KYC cấp 2', NOW() - INTERVAL '2 days'),
    ('60000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000004',
     18, 'ALLOW', '["MERCHANT_WHITELIST", "NORMAL_VELOCITY"]'::jsonb, 'Thanh toán merchant nằm trong ngưỡng an toàn', NOW() - INTERVAL '6 hours')
ON CONFLICT (id) DO NOTHING;

-- 7. Nhật ký kiểm toán mẫu
INSERT INTO audit_logs (id, actor_id, actor_username, action, resource_type, resource_id, ip_address, details)
VALUES
    ('70000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000002', 'admin.paycore',
     'SEED_DATABASE_INITIALIZED', 'SYSTEM', 'paycore-v2', '127.0.0.1',
     'Khởi tạo dữ liệu mẫu ban đầu thông qua Flyway migration V2')
ON CONFLICT (id) DO NOTHING;
