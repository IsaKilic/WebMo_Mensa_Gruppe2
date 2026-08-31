-- Testdaten: 2 Benutzer und die 10 geforderten Essen.
-- Ausfuehren nach 01_schema.sql:
--   Get-Content backend\sql\03_testdaten.sql | mysql -u mensa_app -p mensa
--
-- ACHTUNG: die Passwort-Hashes sind Platzhalter. Vor der Abgabe durch
-- echte BCrypt-Hashes ersetzen.

USE mensa;

INSERT INTO benutzer (benutzername, passwort_hash, rolle) VALUES
    ('admin', 'PLATZHALTER_ADMIN', 'ADMIN'),
    ('user',  'PLATZHALTER_USER',  'USER');

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
