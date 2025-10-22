# Mailing

kiBon versendet bei gewissen Ereignissen Mails, beispielsweise wenn ein Online-Antrag verfügt wurde oder wenn alle
Plätze eines Online-Antrags bestätigt wurde. Die Mails werden dabei queued.

## Queueing

Wenn eine Mail versendet wird, wird sie in
eine [Outbox-Tabelle](./../ebegu-shared/src/main/java/ch/dvbern/ebegu/mailing/OutboxMail.java) geschrieben.
Aus dieser Tabelle werden vo einem scheduled Task
im [OutboxMailSender](./../ebegu-server/src/main/java/ch/dvbern/ebegu/mailing/OutboxMailSender.java) 100
Einträge ausgelesen und als Mails versendet. Dies geschieht alle 30 Sekunden zur vollen und halben Minute.
