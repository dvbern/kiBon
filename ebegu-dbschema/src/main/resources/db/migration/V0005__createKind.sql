CREATE TABLE kind (
  id                             VARCHAR(36)  NOT NULL,
  timestamp_erstellt             DATETIME     NOT NULL,
  timestamp_mutiert              DATETIME     NOT NULL,
  user_erstellt                  VARCHAR(36)  NOT NULL,
  user_mutiert                   VARCHAR(36)  NOT NULL,
  version                        BIGINT       NOT NULL,
  geburtsdatum                   DATE         NOT NULL,
  geschlecht                     VARCHAR(255) NOT NULL,
  nachname                       VARCHAR(255) NOT NULL,
  vorname                        VARCHAR(255) NOT NULL,
  bemerkungen                    VARCHAR(1000),
  betreuungspensum_fachstelle    INTEGER,
  familien_ergaenzende_betreuung BIT,
  muttersprache_deutsch          BIT,
  unterstuetzungspflicht         BIT,
  wohnhaft_im_gleichen_haushalt  INTEGER,
  fachstelle_id                  VARCHAR(36),
  CONSTRAINT PK_kind PRIMARY KEY (id)
);

CREATE TABLE kind_aud (
  id                             VARCHAR(36) NOT NULL,
  rev                            INTEGER     NOT NULL,
  revtype                        TINYINT,
  timestamp_erstellt             DATETIME,
  timestamp_mutiert              DATETIME,
  user_erstellt                  VARCHAR(36),
  user_mutiert                   VARCHAR(36),
  geburtsdatum                   DATE,
  geschlecht                     VARCHAR(255),
  nachname                       VARCHAR(255),
  vorname                        VARCHAR(255),
  bemerkungen                    VARCHAR(1000),
  betreuungspensum_fachstelle    INTEGER,
  familien_ergaenzende_betreuung BIT,
  muttersprache_deutsch          BIT,
  unterstuetzungspflicht         BIT,
  wohnhaft_im_gleichen_haushalt  INTEGER,
  fachstelle_id                  VARCHAR(36),
  CONSTRAINT PK_kind_aud PRIMARY KEY (id, rev)
);

ALTER TABLE kind
  ADD CONSTRAINT FK_kind_fachstelle_id
FOREIGN KEY (fachstelle_id)
REFERENCES fachstelle (id);

ALTER TABLE kind_aud
  ADD CONSTRAINT FK_kind_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo (rev);
