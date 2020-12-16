CREATE TABLE gesuchsperiode_email_candidate (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	status varchar(255) not null,
	dossier_id binary(16) not null,
	last_gesuchsperiode_id binary(16) not null,
	next_gesuchsperiode_id binary(16) not null,
	primary key (id)
);

CREATE TABLE gesuchsperiode_email_candidate_aud (
	id binary(16) not null,
	rev integer not null,
	revtype tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert datetime,
	user_erstellt varchar(255),
	user_mutiert varchar(255),
	status varchar(255),
	dossier_id binary(16),
	last_gesuchsperiode_id binary(16),
	next_gesuchsperiode_id binary(16),
	primary key (id, rev)
);

