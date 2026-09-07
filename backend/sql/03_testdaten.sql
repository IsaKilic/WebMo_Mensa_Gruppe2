-- Testdaten: 2 Benutzer und die 10 geforderten Essen.
-- Ausfuehren nach 01_schema.sql:
--   Get-Content backend\sql\03_testdaten.sql | mysql -u mensa_app -p
--
-- Die Passwort-Hashes sind echte PBKDF2-Werte, erzeugt mit
-- PasswortHasher.main(...). Format: iterationen:salt:hash
--   admin / admin123
--   user  / user123
-- Aus dem Hash laesst sich das Passwort nicht zurueckrechnen.

USE mensa;

INSERT INTO benutzer (benutzername, passwort_hash, rolle) VALUES
    ('admin', '120000:VpAvMrH5DJIzU3zN//bdgw==:XWZB1QCoBjKC5jXmzFExEjhKgmkEN8rnEMP2xbl4X88=', 'ADMIN'),
    ('user',  '120000:xaUKYrIypJelS81UFx8p1Q==:AcWXgBThErCQrHYnkts/efR4DptsCMKvR8GhmlYv90g=',  'USER');

INSERT INTO essen (name, preis, art) VALUES
    ('Hähnchenbrust in Sesampanade',    2.85, 'MIT_FLEISCH'),
    ('Rindergulasch mit Rotkohl',       3.20, 'MIT_FLEISCH'),
    ('Seelachsfilet auf Blattspinat',   3.10, 'MIT_FLEISCH'),
    ('Schweineschnitzel mit Pommes',    2.95, 'MIT_FLEISCH'),
    ('Putengeschnetzeltes',             3.05, 'MIT_FLEISCH'),
    ('Gemüselasagne',                   2.40, 'VEGETARISCH'),
    ('Spinatknödel mit Salbeibutter',   2.50, 'VEGETARISCH'),
    ('Kichererbsen-Curry mit Reis',     2.30, 'VEGAN'),
    ('Ofengemüse mit Hirse und Tahin',  2.60, 'VEGAN'),
    ('Linsenbolognese mit Penne',       2.45, 'VEGAN');
