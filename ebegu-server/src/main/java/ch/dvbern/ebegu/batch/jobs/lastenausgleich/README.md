# Batchjob Lastenausgleich
Der Lastenausgleich wird neu in einem Workjob ausgeführt. Dabei wird die Batchlet-Funktionalität von Java EE verwendet. Dies ermöglicht es die unterschiedlichen den Lastenausgleich in mehreren Steps zu Berechnen. Jeder Step wird dabei automatisch in einer eigenen Transaction ausgeführt. Die Definition des Batchlets ist im lastenausgleichbatch.xml zu finden.

## Batch - Steps
Der Lastenausgleich wird in die folgenden Steps unterteilt:

### Step 1 - Lastenausgleich Creation
Erstellen der Lastenausgleich und Lastenausgleich Grundlagen Entität. Dabei wird auch geprüft, ob es bereits einen Lastenausgleich für das entsprechende Jahr gibt.

### Step 2 - Lastenausgleich Calculation
Es wird für jede Gemeinde die Berechnung des Lastenausgleichs durchgeführt. Die Berechnung beinhaltet die Berechnung der Regulären Abrechnung (Jahr des Lastenausgleichs) und die Berechnung der Korrekturzahlungen (Korrekturen werden 10 Jahre zurück berechnet).

Die Berechnung je Gemeinde wir in Chuncks aufgeteilt. Pro Chunck wird eine Transaction gestartet. Momentan ist die Cunck Grösse 10, heisst der Lastenausgleich wird für 10 Gemeinden in einer Transaction berechnet.

### Step 3 - Total Calculation
Nachdem die Berechnung für jede Gemeinde durchgeführt wurde, startet die Berechnung des Totals (Summe aus LAS aus allen Gemeinden)

### Step 4 - Send Mails to Gemeinden
Alle Gemeinden werden Informiert, dass die Berechnung des LAS durchgeführt wurde. (Alle Gemeinden, für welche in Step 2 eine Berechnung durchgeführt wurde)

## Ende des Prozesses
### Steps erfolgreich durchgeführt
Wenn alle 4 Steps fehlerfrei durchgeführt wurden, wird eine Erfolgsmeldung per Mail an den User gesendet, der den Lastenausgleich gestartet hat.

### Steps nicht erfolgreich durchgeführt
Wenn in einem der 4 Steps ein Fehler auftritt, wird eine Fehlermeldung per Mail an den User gesendet.

Wenn in Step 1 -3 ein Fehler auftritt wird zudem der Lastenausgleich gelöscht. Wenn nur die Mail nicht versendet werden konnte, aber der Lastenausgleich erfolgreich berechnet wurde, wird der Lastenausgleich nicht gelöscht.