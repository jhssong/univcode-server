CREATE TABLE member (
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    affiliation VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)
);

CREATE TABLE api_key (
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    member_id  BIGINT       NOT NULL UNIQUE,
    key_value  VARCHAR(255) UNIQUE,
    purpose    VARCHAR(500) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    daily_call_count INT          NOT NULL DEFAULT 0,
    call_count_date  DATE,
    created_at      DATETIME(6)  NOT NULL,
    updated_at DATETIME(6),
    CONSTRAINT fk_api_key_member FOREIGN KEY (member_id) REFERENCES member (id)
);

CREATE TABLE api_key_call_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    api_key_id  BIGINT       NOT NULL,
    method      VARCHAR(10)  NOT NULL,
    path        VARCHAR(255) NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6),
    CONSTRAINT fk_api_key_call_log_api_key FOREIGN KEY (api_key_id) REFERENCES api_key (id)
);

CREATE INDEX idx_api_key_call_log_api_key_created_at ON api_key_call_log (api_key_id, created_at);
