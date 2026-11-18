-- Seminar 7, Incalzire: interogari pe tabela movies (rulati dupa cinema.sql, in Query Tool)

-- (a) filmele aparute dupa 2010, ordonate dupa an
SELECT title, director, year
FROM movies
WHERE year > 2010
ORDER BY year;

-- (b) numarul de filme si durata medie pentru fiecare regizor
SELECT director, COUNT(*) AS movie_count, ROUND(AVG(duration_minutes)) AS avg_duration_minutes
FROM movies
GROUP BY director
ORDER BY movie_count DESC;

-- (c) inserarea unui film cu id-ul dat explicit: esueaza, id-ul este GENERATED ALWAYS
-- ERROR: cannot insert a non-DEFAULT value into column "id"
INSERT INTO movies (id, title, director, year, duration_minutes)
VALUES (100, 'Barbie', 'Greta Gerwig', 2023, 114);
