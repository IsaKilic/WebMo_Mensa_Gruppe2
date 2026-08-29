-- Stammdaten der Mensen. Diese aendern sich praktisch nie und werden
-- deshalb nicht vom Importer gepflegt, sondern einmalig eingespielt.

USE mensa;

INSERT INTO mensa (id, name, adresse, breitengrad, laengengrad) VALUES
    (1, 'Mensa Campus',        'Scharnhorststraße 1, 21335 Lüneburg', 53.2285, 10.4012),
    (2, 'Mensa Campus abends', 'Scharnhorststraße 1, 21335 Lüneburg', 53.2285, 10.4012)
ON DUPLICATE KEY UPDATE
    name        = VALUES(name),
    adresse     = VALUES(adresse),
    breitengrad = VALUES(breitengrad),
    laengengrad = VALUES(laengengrad);
