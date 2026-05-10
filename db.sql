CREATE TABLE IF NOT EXISTS entity_records (
                                              id            VARCHAR(36)  NOT NULL,
    entity_name   VARCHAR(64)  NOT NULL,
    data_json     LONGTEXT     NOT NULL,
    deleted       BIT(1)       NOT NULL,
    created_date  DATETIME(6)  NOT NULL,
    updated_date  DATETIME(6)  NOT NULL,
    created_by    VARCHAR(255) NULL,
    PRIMARY KEY (id),
    KEY idx_entity_records_entity_name_deleted (entity_name, deleted)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS products (
                                        id           BIGINT       NOT NULL AUTO_INCREMENT,
                                        name         VARCHAR(255) NOT NULL,
    sku          VARCHAR(255) NULL,
    description  TEXT         NULL,
    price        DECIMAL(19,2) NULL,
    status       VARCHAR(32)  NOT NULL,
    created_at   DATETIME(6)  NOT NULL,
    updated_at   DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_products_sku (sku)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS properties (
                                          id            VARCHAR(36)  NOT NULL,
    data_json     LONGTEXT     NOT NULL,
    deleted       BIT(1)       NOT NULL,
    created_date  DATETIME(6)  NOT NULL,
    updated_date  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_properties_deleted (deleted)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS room_types (
                                          id            VARCHAR(36)  NOT NULL,
    data_json     LONGTEXT     NOT NULL,
    deleted       BIT(1)       NOT NULL,
    created_date  DATETIME(6)  NOT NULL,
    updated_date  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_room_types_deleted (deleted)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS rooms (
                                     id            VARCHAR(36)  NOT NULL,
    data_json     LONGTEXT     NOT NULL,
    deleted       BIT(1)       NOT NULL,
    created_date  DATETIME(6)  NOT NULL,
    updated_date  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_rooms_deleted (deleted)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS guests (
                                      id            VARCHAR(36)  NOT NULL,
    data_json     LONGTEXT     NOT NULL,
    deleted       BIT(1)       NOT NULL,
    created_date  DATETIME(6)  NOT NULL,
    updated_date  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_guests_deleted (deleted)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reservations (
                                            id            VARCHAR(36)  NOT NULL,
    data_json     LONGTEXT     NOT NULL,
    deleted       BIT(1)       NOT NULL,
    created_date  DATETIME(6)  NOT NULL,
    updated_date  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_reservations_deleted (deleted)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS policies (
                                        id            VARCHAR(36)  NOT NULL,
    data_json     LONGTEXT     NOT NULL,
    deleted       BIT(1)       NOT NULL,
    created_date  DATETIME(6)  NOT NULL,
    updated_date  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_policies_deleted (deleted)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS rate_plans (
                                          id            VARCHAR(36)  NOT NULL,
    data_json     LONGTEXT     NOT NULL,
    deleted       BIT(1)       NOT NULL,
    created_date  DATETIME(6)  NOT NULL,
    updated_date  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_rate_plans_deleted (deleted)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS folio_transactions (
                                                  id            VARCHAR(36)  NOT NULL,
    data_json     LONGTEXT     NOT NULL,
    deleted       BIT(1)       NOT NULL,
    created_date  DATETIME(6)  NOT NULL,
    updated_date  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_folio_transactions_deleted (deleted)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_properties (
                                               id            VARCHAR(36)  NOT NULL,
    data_json     LONGTEXT     NOT NULL,
    deleted       BIT(1)       NOT NULL,
    created_date  DATETIME(6)  NOT NULL,
    updated_date  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_user_properties_deleted (deleted)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS license_transactions (
                                                    id            VARCHAR(36)  NOT NULL,
    data_json     LONGTEXT     NOT NULL,
    deleted       BIT(1)       NOT NULL,
    created_date  DATETIME(6)  NOT NULL,
    updated_date  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_license_transactions_deleted (deleted)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
                                     id            VARCHAR(36)  NOT NULL,
    data_json     LONGTEXT     NOT NULL,
    deleted       BIT(1)       NOT NULL,
    created_date  DATETIME(6)  NOT NULL,
    updated_date  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_users_deleted (deleted)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS invoices (
                                        id            VARCHAR(36)  NOT NULL,
    data_json     LONGTEXT     NOT NULL,
    deleted       BIT(1)       NOT NULL,
    created_date  DATETIME(6)  NOT NULL,
    updated_date  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_invoices_deleted (deleted)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;