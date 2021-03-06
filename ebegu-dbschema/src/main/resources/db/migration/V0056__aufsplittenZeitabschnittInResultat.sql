create table bgcalculation_result (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	abzug_fam_groesse decimal(19,2),
	anspruchspensum_prozent integer not null,
	anspruchspensum_zeiteinheit decimal(19,2) not null,
	besondere_beduerfnisse_bestaetigt bit not null,
	betreuungspensum_prozent decimal(19,2) not null,
	betreuungspensum_zeiteinheit decimal(19,2) not null,
	bg_pensum_zeiteinheit decimal(19,2) not null,
	einkommensjahr integer not null,
	elternbeitrag decimal(19,2) not null,
	fam_groesse decimal(19,2),
	massgebendes_einkommen_vor_abzug_famgr decimal(19,2) not null,
	minimaler_elternbeitrag decimal(19,2) not null,
	minimaler_elternbeitrag_gekuerzt decimal(19,2),
	minimales_ewp_unterschritten bit not null,
	temp_id_zeitabschnitt binary(16),
	verguenstigung decimal(19,2) not null,
	verguenstigung_ohne_beruecksichtigung_minimalbeitrag decimal(19,2) not null,
	verguenstigung_ohne_beruecksichtigung_vollkosten decimal(19,2) not null,
	vollkosten decimal(19,2) not null,
	zeiteinheit varchar(100) not null,
	zu_spaet_eingereicht bit not null,
	primary key (id)
);

create table bgcalculation_result_aud (
	id binary(16) not null,
	rev integer not null,
	revtype tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert datetime,
	user_erstellt varchar(255),
	user_mutiert varchar(255),
	abzug_fam_groesse decimal(19,2),
	anspruchspensum_prozent integer,
	anspruchspensum_zeiteinheit decimal(19,2),
	besondere_beduerfnisse_bestaetigt bit,
	betreuungspensum_prozent decimal(19,2),
	betreuungspensum_zeiteinheit decimal(19,2),
	bg_pensum_zeiteinheit decimal(19,2),
	einkommensjahr integer,
	elternbeitrag decimal(19,2),
	fam_groesse decimal(19,2),
	massgebendes_einkommen_vor_abzug_famgr decimal(19,2),
	minimaler_elternbeitrag decimal(19,2),
	minimaler_elternbeitrag_gekuerzt decimal(19,2),
	minimales_ewp_unterschritten bit,
	temp_id_zeitabschnitt binary(16),
	verguenstigung decimal(19,2),
	verguenstigung_ohne_beruecksichtigung_minimalbeitrag decimal(19,2),
	verguenstigung_ohne_beruecksichtigung_vollkosten decimal(19,2),
	vollkosten decimal(19,2),
	zeiteinheit varchar(100),
	zu_spaet_eingereicht bit,
	primary key (id, rev)
);

alter table verfuegung_zeitabschnitt add bg_calculation_result_asiv_id binary(16);
alter table verfuegung_zeitabschnitt add bg_calculation_result_gemeinde_id binary(16);
alter table verfuegung_zeitabschnitt_aud add bg_calculation_result_asiv_id binary(16);
alter table verfuegung_zeitabschnitt_aud add bg_calculation_result_gemeinde_id binary(16);

alter table bgcalculation_result_aud
	add constraint FK_bgcalculation_result_aud_revinfo
foreign key (rev)
references revinfo (rev);

alter table verfuegung_zeitabschnitt
	add constraint FK_verfuegungZeitabschnitt_resultatAsiv
foreign key (bg_calculation_result_asiv_id)
references bgcalculation_result (id);

alter table verfuegung_zeitabschnitt
	add constraint FK_verfuegungZeitabschnitt_resultatGemeinde
foreign key (bg_calculation_result_gemeinde_id)
references bgcalculation_result (id);


INSERT INTO bgcalculation_result (
	id,
	timestamp_erstellt,
	timestamp_mutiert,
	user_erstellt,
	user_mutiert,
	version,
	temp_id_zeitabschnitt,
	anspruchspensum_prozent,
	anspruchspensum_zeiteinheit,
	betreuungspensum_prozent,
	betreuungspensum_zeiteinheit,
	bg_pensum_zeiteinheit,
	elternbeitrag,
	minimaler_elternbeitrag_gekuerzt,
	verguenstigung,
	verguenstigung_ohne_beruecksichtigung_minimalbeitrag,
	verguenstigung_ohne_beruecksichtigung_vollkosten,
	vollkosten,
	zeiteinheit,
	abzug_fam_groesse,
	besondere_beduerfnisse_bestaetigt,
	einkommensjahr,
	fam_groesse,
	massgebendes_einkommen_vor_abzug_famgr,
	minimales_ewp_unterschritten,
	zu_spaet_eingereicht,
	minimaler_elternbeitrag)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 gp.timestamp_erstellt   as timestamp_erstellt,
			 gp.timestamp_mutiert    as timestamp_mutiert,
			 gp.user_erstellt        as user_erstellt,
			 gp.user_mutiert         as user_mutiert,
			 0                       as version,
			 gp.id 					as temp_id_zeitabschnitt,
			 gp.anspruchberechtigtes_pensum 				as anspruchspensum_prozent,
			 gp.anspruchsberechtigte_anzahl_zeiteinheiten 	as anspruchspensum_zeiteinheit,
			 gp.betreuungspensum_prozent 					as betreuungspensum_prozent,
			 gp.betreuungspensum_zeiteinheit 				as betreuungspensum_zeiteinheit,
			 gp.verfuegte_anzahl_zeiteinheiten 				as bg_pensum_zeiteinheit,
			 gp.elternbeitrag 		as elternbeitrag,
			 null 					as minimaler_elternbeitrag_gekuerzt,
			 gp.verguenstigung 		as verguenstigung,
			 gp.verguenstigung_ohne_beruecksichtigung_minimalbeitrag 	as verguenstigung_ohne_beruecksichtigung_minimalbeitrag,
			 gp.verguenstigung_ohne_beruecksichtigung_vollkosten 		as verguenstigung_ohne_beruecksichtigung_vollkosten,
			 gp.vollkosten 			as vollkosten,
			 gp.zeiteinheit 		as zeiteinheit,
			 gp.abzug_fam_groesse 	as abzug_fam_groesse,
			 gp.besondere_beduerfnisse_bestaetigt 			as besondere_beduerfnisse_bestaetigt,
			 gp.einkommensjahr 			as einkommensjahr,
			 gp.fam_groesse 			as fam_groesse,
			 gp.massgebendes_einkommen_vor_abzug_famgr 		as massgebendes_einkommen_vor_abzug_famgr,
			 gp.minimales_ewp_unterschritten 				as minimales_ewp_unterschritten,
			 gp.zu_spaet_eingereicht 						as zu_spaet_eingereicht,
			 gp.minimaler_elternbeitrag						as minimaler_elternbeitrag
	  from verfuegung_zeitabschnitt as gp) as tmp;

update verfuegung_zeitabschnitt v
inner join bgcalculation_result b
set v.bg_calculation_result_asiv_id = b.id
where b.temp_id_zeitabschnitt = v.id;

alter table verfuegung_zeitabschnitt modify column bg_calculation_result_asiv_id binary(16) not null;

alter table verfuegung_zeitabschnitt drop minimaler_elternbeitrag;
alter table verfuegung_zeitabschnitt drop anspruchberechtigtes_pensum;
alter table verfuegung_zeitabschnitt drop anspruchsberechtigte_anzahl_zeiteinheiten;
alter table verfuegung_zeitabschnitt drop betreuungspensum_prozent;
alter table verfuegung_zeitabschnitt drop betreuungspensum_zeiteinheit;
alter table verfuegung_zeitabschnitt drop elternbeitrag;
alter table verfuegung_zeitabschnitt drop verfuegte_anzahl_zeiteinheiten;
alter table verfuegung_zeitabschnitt drop verguenstigung;
alter table verfuegung_zeitabschnitt drop verguenstigung_ohne_beruecksichtigung_minimalbeitrag;
alter table verfuegung_zeitabschnitt drop verguenstigung_ohne_beruecksichtigung_vollkosten;
alter table verfuegung_zeitabschnitt drop vollkosten;
alter table verfuegung_zeitabschnitt drop zeiteinheit;
alter table verfuegung_zeitabschnitt drop abzug_fam_groesse;
alter table verfuegung_zeitabschnitt drop besondere_beduerfnisse_bestaetigt;
alter table verfuegung_zeitabschnitt drop einkommensjahr;
alter table verfuegung_zeitabschnitt drop fam_groesse;
alter table verfuegung_zeitabschnitt drop massgebendes_einkommen_vor_abzug_famgr;
alter table verfuegung_zeitabschnitt drop minimales_ewp_unterschritten;
alter table verfuegung_zeitabschnitt drop zu_spaet_eingereicht;

alter table verfuegung_zeitabschnitt_aud drop minimaler_elternbeitrag;
alter table verfuegung_zeitabschnitt_aud drop anspruchberechtigtes_pensum;
alter table verfuegung_zeitabschnitt_aud drop anspruchsberechtigte_anzahl_zeiteinheiten;
alter table verfuegung_zeitabschnitt_aud drop betreuungspensum_prozent;
alter table verfuegung_zeitabschnitt_aud drop betreuungspensum_zeiteinheit;
alter table verfuegung_zeitabschnitt_aud drop elternbeitrag;
alter table verfuegung_zeitabschnitt_aud drop verfuegte_anzahl_zeiteinheiten;
alter table verfuegung_zeitabschnitt_aud drop verguenstigung;
alter table verfuegung_zeitabschnitt_aud drop verguenstigung_ohne_beruecksichtigung_minimalbeitrag;
alter table verfuegung_zeitabschnitt_aud drop verguenstigung_ohne_beruecksichtigung_vollkosten;
alter table verfuegung_zeitabschnitt_aud drop vollkosten;
alter table verfuegung_zeitabschnitt_aud drop zeiteinheit;
alter table verfuegung_zeitabschnitt_aud drop abzug_fam_groesse;
alter table verfuegung_zeitabschnitt_aud drop besondere_beduerfnisse_bestaetigt;
alter table verfuegung_zeitabschnitt_aud drop einkommensjahr;
alter table verfuegung_zeitabschnitt_aud drop fam_groesse;
alter table verfuegung_zeitabschnitt_aud drop massgebendes_einkommen_vor_abzug_famgr;
alter table verfuegung_zeitabschnitt_aud drop minimales_ewp_unterschritten;
alter table verfuegung_zeitabschnitt_aud drop zu_spaet_eingereicht;