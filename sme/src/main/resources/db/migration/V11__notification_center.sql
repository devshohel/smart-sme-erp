CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    type ENUM('INFO','SUCCESS','WARNING','ERROR') NOT NULL DEFAULT 'INFO',
    severity ENUM('LOW','MEDIUM','HIGH','CRITICAL') NOT NULL DEFAULT 'LOW',
    entity_type VARCHAR(80) NULL,
    entity_id BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(120) NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at DATETIME NULL,
    target_user_id BIGINT NULL,
    target_role_id BIGINT NULL,
    action_url VARCHAR(300) NULL,
    PRIMARY KEY (id),
    INDEX idx_notifications_created_at (created_at),
    INDEX idx_notifications_read_created (is_read, created_at),
    INDEX idx_notifications_target_user (target_user_id, is_read),
    INDEX idx_notifications_target_role (target_role_id, is_read),
    INDEX idx_notifications_type_severity (type, severity),
    CONSTRAINT fk_notifications_target_user FOREIGN KEY (target_user_id) REFERENCES users(id),
    CONSTRAINT fk_notifications_target_role FOREIGN KEY (target_role_id) REFERENCES roles(id)
);

INSERT INTO permissions (name, module, action, description, created_at)
SELECT 'NOTIFICATION_VIEW', 'NOTIFICATION', 'VIEW', 'NOTIFICATION VIEW', NOW()
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'NOTIFICATION_VIEW');

INSERT INTO permissions (name, module, action, description, created_at)
SELECT 'NOTIFICATION_MANAGE', 'NOTIFICATION', 'MANAGE', 'NOTIFICATION MANAGE', NOW()
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'NOTIFICATION_MANAGE');
