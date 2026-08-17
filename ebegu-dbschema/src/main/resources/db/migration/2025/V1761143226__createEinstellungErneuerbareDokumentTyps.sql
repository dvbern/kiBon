/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
                         einstellung_key, value, gesuchsperiode_id, erklaerung)
    (SELECT UNHEX(REPLACE(UUID(), '-', ''))                                                        AS id,
            NOW()                                                                                  AS timestamp_erstellt,
            NOW()                                                                                  AS timestamp_muiert,
            'ebegu'                                                                                AS user_erstellt,
            'ebegu'                                                                                AS user_mutiert,
            '0'                                                                                    AS version,
            'ERNEUERBARE_DOKUMENT_TYPS'                                                            AS einstellungkey,
            ''                                                                                     AS value,
            id                                                                                     AS gesuchsperiode_id,
            'Welche Dokumente können aus der Vorperiode in den aktuellen Antrag übernommen werden' AS erklaerung
     FROM gesuchsperiode);

ALTER TABLE einstellung
    MODIFY value TEXT NOT NULL;
ALTER TABLE einstellung_aud
    MODIFY value TEXT NULL;

SET @gesuchsperiode_id_26_27 = (SELECT gesuchsperiode.id
                          FROM gesuchsperiode
                                   INNER JOIN mandant ON gesuchsperiode.mandant_id = mandant.id
                          WHERE mandant_identifier = 'BERN'
                            AND gueltig_ab = '2026-08-01');

UPDATE einstellung SET value = 'NACHWEIS_GETEILTE_OBHUT,NACHWEIS_UNTERHALTSVEREINBARUNG,BESTAETIGUNG_AUSSERORDENTLICHER_BETREUUNGSAUFWAND,ERFOLGSRECHNUNGEN_JAHR,ERFOLGSRECHNUNGEN_JAHR_MINUS1,ERFOLGSRECHNUNGEN_JAHR_MINUS2,NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR,NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR_MINUS1,NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR_MINUS2' WHERE einstellung_key = 'ERNEUERBARE_DOKUMENT_TYPS' AND gesuchsperiode_id = @gesuchsperiode_id_26_27;
