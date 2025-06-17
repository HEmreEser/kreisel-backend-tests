# Geschäftsfunktionen des Kreisel-Backend-Systems

## Benutzer- und Authentifizierungsverwaltung
1. **Benutzerregistrierung**
   - Registrierung neuer Benutzer mit HM-E-Mail-Adressen
   - Automatische Rollenzuweisung (ADMIN für E-Mails, die mit "admin" beginnen)
   - Passwort-Hashing für Sicherheit

2. **Benutzeranmeldung**
   - Authentifizierung von Benutzern mit E-Mail und Passwort
   - Rückgabe von Benutzerinformationen und Rolle

3. **Benutzerverwaltung**
   - Abrufen aller Benutzer (Admin-Funktion)
   - Abrufen von Benutzern nach ID oder E-Mail
   - Aktualisieren von Benutzerinformationen
   - Löschen von Benutzern

## Artikelverwaltung
1. **Artikelsuche und -filterung**
   - Filterung nach Standort (Pflichtfeld)
   - Optionale Filter: Verfügbarkeit, Textsuche, Geschlecht, Kategorie, Unterkategorie, Größe
   - Suche in Name, Beschreibung und Marke

2. **Artikelverwaltung**
   - Abrufen von Artikeln nach ID
   - Erstellen neuer Artikel (Admin-Funktion)
   - Aktualisieren von Artikelinformationen (Admin-Funktion)
   - Löschen von Artikeln (Admin-Funktion)

## Ausleihverwaltung
1. **Ausleihe von Artikeln**
   - Ausleihe eines Artikels für einen bestimmten Zeitraum
   - Validierung: Artikel muss verfügbar sein
   - Validierung: Benutzer darf maximal 5 aktive Ausleihen haben
   - Validierung: Ausleihdauer maximal 90 Tage

2. **Verlängerung von Ausleihen**
   - Einmalige Verlängerung um 30 Tage
   - Validierung: Ausleihe darf noch nicht zurückgegeben sein
   - Validierung: Ausleihe darf noch nicht verlängert worden sein
   - Validierung: Gesamtausleihdauer darf 120 Tage nicht überschreiten

3. **Rückgabe von Artikeln**
   - Markierung einer Ausleihe als zurückgegeben
   - Aktualisierung des Artikelstatus auf verfügbar

4. **Ausleihverwaltung**
   - Abrufen aller Ausleihen
   - Abrufen von Ausleihen nach Benutzer
   - Abrufen aktiver Ausleihen nach Benutzer
   - Abrufen historischer Ausleihen nach Benutzer
   - Abrufen überfälliger Ausleihen

## Bewertungssystem
1. **Bewertungserstellung**
   - Erstellen einer Bewertung für eine Ausleihe
   - Validierung: Artikel muss zurückgegeben sein
   - Validierung: Nur eine Bewertung pro Ausleihe

2. **Bewertungsverwaltung**
   - Aktualisieren einer Bewertung (nur durch den Ersteller)
   - Löschen einer Bewertung (nur durch den Ersteller)
   - Abrufen von Bewertungen nach Artikel, Benutzer oder Ausleihe

3. **Bewertungsstatistiken**
   - Berechnung der durchschnittlichen Bewertung für einen Artikel
   - Zählung der Bewertungen für einen Artikel
   - Verteilung der Bewertungen für einen Artikel (1-5 Sterne)
   - Abrufen der am besten bewerteten Artikel

## Möglicherweise redundante oder unnötige Funktionen

1. **Abmeldung ohne Session-Invalidierung**
   - Die Logout-Funktion im AuthController gibt nur eine Erfolgsmeldung zurück, ohne tatsächlich eine Session zu invalidieren

2. **Doppelte Benutzerrollenzuweisung**
   - Sowohl AuthService als auch UserService haben Logik zur automatischen Zuweisung der Admin-Rolle

3. **Übermäßige Filteroptionen für Artikel**
   - Die Filterung von Artikeln könnte vereinfacht werden, indem weniger Filter angeboten werden

4. **Redundante Bewertungsstatistiken**
   - Einige Bewertungsstatistiken könnten zusammengefasst werden, um die API zu vereinfachen