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
    property_id   VARCHAR(36)  NULL,
    data_json     LONGTEXT     NOT NULL,
    deleted       BIT(1)       NOT NULL,
    created_date  DATETIME(6)  NOT NULL,
    updated_date  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_room_types_deleted (deleted),
    KEY idx_room_types_property_id (property_id)
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
    property_id            VARCHAR(36)  NULL,
    cancellation_policy_id VARCHAR(36)  NULL,
    surcharge_policy_id    VARCHAR(36)  NULL,
    data_json     LONGTEXT     NOT NULL,
    deleted       BIT(1)       NOT NULL,
    created_date  DATETIME(6)  NOT NULL,
    updated_date  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_rate_plans_deleted (deleted),
    KEY idx_rate_plans_property_id (property_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS rate_plan_room_types (
    rate_plan_id  VARCHAR(36) NOT NULL,
    room_type_id  VARCHAR(36) NOT NULL,
    PRIMARY KEY (rate_plan_id, room_type_id),
    KEY idx_rprt_rate_plan_id (rate_plan_id),
    KEY idx_rprt_room_type_id (room_type_id)
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

CREATE TABLE IF NOT EXISTS ota_channels (
                                           id            VARCHAR(36)  NOT NULL,
    data_json     LONGTEXT     NOT NULL,
    deleted       BIT(1)       NOT NULL,
    created_date  DATETIME(6)  NOT NULL,
    updated_date  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_ota_channels_deleted (deleted)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS room_rates_monthly (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    property_id     VARCHAR(36) NOT NULL,
    room_type_id    VARCHAR(36) NOT NULL,
    rate_plan_id    VARCHAR(36) NOT NULL,
    in_year         INT NOT NULL,
    in_month        INT NOT NULL,
    col01 DECIMAL(19,2), col02 DECIMAL(19,2), col03 DECIMAL(19,2), col04 DECIMAL(19,2), col05 DECIMAL(19,2),
    col06 DECIMAL(19,2), col07 DECIMAL(19,2), col08 DECIMAL(19,2), col09 DECIMAL(19,2), col10 DECIMAL(19,2),
    col11 DECIMAL(19,2), col12 DECIMAL(19,2), col13 DECIMAL(19,2), col14 DECIMAL(19,2), col15 DECIMAL(19,2),
    col16 DECIMAL(19,2), col17 DECIMAL(19,2), col18 DECIMAL(19,2), col19 DECIMAL(19,2), col20 DECIMAL(19,2),
    col21 DECIMAL(19,2), col22 DECIMAL(19,2), col23 DECIMAL(19,2), col24 DECIMAL(19,2), col25 DECIMAL(19,2),
    col26 DECIMAL(19,2), col27 DECIMAL(19,2), col28 DECIMAL(19,2), col29 DECIMAL(19,2), col30 DECIMAL(19,2),
    col31 DECIMAL(19,2),
    UNIQUE KEY uk_rates_monthly (room_type_id, rate_plan_id, in_year, in_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS room_allotments_monthly (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    property_id     VARCHAR(36) NOT NULL,
    room_type_id    VARCHAR(36) NOT NULL,
    in_year         INT NOT NULL,
    in_month        INT NOT NULL,
    col01 INT, col02 INT, col03 INT, col04 INT, col05 INT,
    col06 INT, col07 INT, col08 INT, col09 INT, col10 INT,
    col11 INT, col12 INT, col13 INT, col14 INT, col15 INT,
    col16 INT, col17 INT, col18 INT, col19 INT, col20 INT,
    col21 INT, col22 INT, col23 INT, col24 INT, col25 INT,
    col26 INT, col27 INT, col28 INT, col29 INT, col30 INT,
    col31 INT,
    UNIQUE KEY uk_allotments_monthly (room_type_id, in_year, in_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS room_restrictions_monthly (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    property_id       VARCHAR(36) NOT NULL,
    room_type_id      VARCHAR(36) NOT NULL,
    rate_plan_id      VARCHAR(36),
    restriction_type  VARCHAR(32) NOT NULL,
    in_year           INT NOT NULL,
    in_month          INT NOT NULL,
    col01 VARCHAR(16), col02 VARCHAR(16), col03 VARCHAR(16), col04 VARCHAR(16), col05 VARCHAR(16),
    col06 VARCHAR(16), col07 VARCHAR(16), col08 VARCHAR(16), col09 VARCHAR(16), col10 VARCHAR(16),
    col11 VARCHAR(16), col12 VARCHAR(16), col13 VARCHAR(16), col14 VARCHAR(16), col15 VARCHAR(16),
    col16 VARCHAR(16), col17 VARCHAR(16), col18 VARCHAR(16), col19 VARCHAR(16), col20 VARCHAR(16),
    col21 VARCHAR(16), col22 VARCHAR(16), col23 VARCHAR(16), col24 VARCHAR(16), col25 VARCHAR(16),
    col26 VARCHAR(16), col27 VARCHAR(16), col28 VARCHAR(16), col29 VARCHAR(16), col30 VARCHAR(16),
    col31 VARCHAR(16),
    UNIQUE KEY uk_restrictions_monthly (room_type_id, rate_plan_id, restriction_type, in_year, in_month)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
