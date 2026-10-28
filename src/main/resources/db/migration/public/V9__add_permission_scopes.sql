CREATE TABLE role_permission_scopes
(
    role_id    UUID         NOT NULL,
    permission VARCHAR(100) NOT NULL,
    scope      VARCHAR(20)  NOT NULL,
    CONSTRAINT pk_role_permission_scopes PRIMARY KEY (role_id, permission),
    CONSTRAINT fk_role_permission_scopes_role
        FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT ck_role_permission_scope
        CHECK (scope IN ('ASSIGNED', 'STORE', 'ALL'))
);

CREATE TABLE user_permission_scopes
(
    user_id    UUID         NOT NULL,
    permission VARCHAR(100) NOT NULL,
    scope      VARCHAR(20)  NOT NULL,
    CONSTRAINT pk_user_permission_scopes PRIMARY KEY (user_id, permission),
    CONSTRAINT fk_user_permission_scopes_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_user_permission_scope
        CHECK (scope IN ('ASSIGNED', 'STORE', 'ALL'))
);

-- Preserve current behavior: all existing grants begin at ALL. At runtime,
-- account category still caps drivers at ASSIGNED and store users at STORE.
INSERT INTO role_permission_scopes (role_id, permission, scope)
SELECT role_id, permission, 'ALL'
FROM role_permissions;

INSERT INTO user_permission_scopes (user_id, permission, scope)
SELECT user_id, permission, 'ALL'
FROM user_permissions;
