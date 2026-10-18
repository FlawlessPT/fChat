CREATE TABLE IF NOT EXISTS fchat_prefs (
    uuid CHAR(36) CHARACTER SET ascii NOT NULL,
    default_channel VARCHAR(8) NOT NULL,
    PRIMARY KEY (uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
