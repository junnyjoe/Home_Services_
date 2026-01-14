-- Initial Admin User (password: admin123)
-- Only inserted if not exists to avoid duplicate errors on re-run
INSERT INTO users (nom, email, password, telephone, role, verified, active)
SELECT 'Administrateur', 'admin@homeservices.ci', '$2a$10$8.UnVuG9shgY3W9.2t.Slu.2uG9shgY3W9.2t.Slu.2uG9shgY3W9', '+225 0700000000', 'ADMIN', true, true
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@homeservices.ci');
