# Wir muessen allenfalls bestehende Zahlungsauftraege loeschen
DELETE FROM pain001dokument;
DELETE FROM pain001dokument_aud;
DELETE FROM zahlungsposition;
DELETE FROM zahlungsposition_aud;
DELETE FROM zahlung;
DELETE FROM zahlung_aud;
DELETE FROM zahlungsauftrag;
DELETE FROM zahlungsauftrag_aud;
UPDATE verfuegung_zeitabschnitt SET zahlungsstatus = 'NEU';

ALTER TABLE zahlungsauftrag ADD gemeinde_id BINARY(16) NOT NULL;
ALTER TABLE zahlungsauftrag_aud ADD gemeinde_id BINARY(16);

ALTER TABLE zahlungsauftrag
	ADD CONSTRAINT FK_zahlungsauftrag_gemeinde_id
FOREIGN KEY (gemeinde_id)
REFERENCES gemeinde (id);