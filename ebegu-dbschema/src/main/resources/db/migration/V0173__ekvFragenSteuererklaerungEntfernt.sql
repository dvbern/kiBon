ALTER TABLE einkommensverschlechterung DROP COLUMN steuererklaerung_ausgefuellt;
ALTER TABLE einkommensverschlechterung DROP COLUMN steuerveranlagung_erhalten;

ALTER TABLE einkommensverschlechterung_aud DROP COLUMN steuererklaerung_ausgefuellt;
ALTER TABLE einkommensverschlechterung_aud DROP COLUMN steuerveranlagung_erhalten;

ALTER TABLE einkommensverschlechterung_info DROP COLUMN gemeinsame_steuererklaerung_bjp1;
ALTER TABLE einkommensverschlechterung_info DROP COLUMN gemeinsame_steuererklaerung_bjp2;

ALTER TABLE einkommensverschlechterung_info_aud DROP COLUMN gemeinsame_steuererklaerung_bjp1;
ALTER TABLE einkommensverschlechterung_info_aud DROP COLUMN gemeinsame_steuererklaerung_bjp2;