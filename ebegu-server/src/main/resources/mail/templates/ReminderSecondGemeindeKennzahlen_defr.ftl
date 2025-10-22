<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="gemeinde" type="ch.dvbern.ebegu.entities.Gemeinde" -->
<#-- @ftlvariable name="angebotNameDe" type="java.lang.String" -->
<#-- @ftlvariable name="angebotNameFr" type="java.lang.String" -->
<#-- @ftlvariable name="hostname" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${empfaengerMail}
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem</#if> – : Erinnerung Aktualisierung Formular «Gemeinde-Kennzahlen Betreuungsgutscheine»/ Rappel : mise à jour du formulaire « Bons de garde : indicateurs de la commune »</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Testsystem</#if> – : Erinnerung Aktualisierung Formular «Gemeinde-Kennzahlen Betreuungsgutscheine»/ Rappel : mise à jour du formulaire « Bons de garde : indicateurs de la commune »</title>

</head>

<body>
<p><b>Français ci-dessous</b></p>
<div>
    <p>Sehr geehrte Damen und Herren</p>

    <p>Das Amt für Integration und Soziales erhebt einmal pro Jahr, wie die Gemeinden im Kanton Bern Betreuungsgutscheine ausgeben. Am 15. September wurden Sie per Mail aufgefordert, die Angaben in kiBon auf dem Formular «Gemeinde Kennzahlen Betreuungsgutscheine» bis am 15. Oktober zu ergänzen. Bis zum heutigen Zeitpunkt wurde das Formular nicht ausgefüllt.</p>

   <p> Wir bitten Sie daher erneut, bis spätestens am 31. Oktober das Formular unter folgendem Link auszufüllen: <a href="${frontendUrl}/gemeinde-antraege">Gemeinde Kennzahlen Betreuungsgutscheine</a></p>

    <p>Die Aktualisierung des Formulars dauert maximal fünf Minuten. Gutscheinausgabestellen füllen für jede Gemeinde ein eigenes Formular aus.</p>

    <p>Gerne steht Ihnen der Fachbereich Betreuungsgutscheine unter <a href="mailto:info.bg@be.ch">info.bg@be.ch</a> und <a href="tel:0316337883">031 633 78 83</a> für Rückfragen und Bemerkungen zur Verfügung.</p>

    <p>Wir danken Ihnen für die Kenntnisnahme und verbleiben mit freundlichen Grüssen.</p>

    <p class="signature"><b>Gesundheits-, Sozial- und Integrationsdirektion des Kantons Bern,</b><br />
    Amt für Integration und Soziales, Abteilung Behinderung, Familie und Opferhilfe (BFO)<br />
    Rathausplatz 1, Postfach, 3000 Bern 8<br />
		031 633 78 83, www.be.ch/gsi</p>
</div>

<hr>

<div>
    <p>Mesdames, Messieurs,</p>

    <p>Une fois par an, l’Office de l’intégration et de l’action sociale (OIAS) recense la manière dont les communes bernoises émettent les bons de garde. À cet effet, vous avez reçu le 15 septembre dernier un courriel vous demandant de compléter dans kiBon le formulaire « Indicateurs de la commune » d’ici le 15 octobre.</p>

	<p>Sans nouvelles de votre part à ce jour, nous vous prions de remplir le formulaire, disponible au lien suivant d’ici le 31 octobre au plus tard: <a href="${frontendUrl}/gemeinde-antraege">Bons de garde: indicateurs de la commune</a>.</p>

    <p>Il faut compter cinq minutes au maximum pour le mettre à jour. Il convient de compléter un seul formulaire par commune.</p>

    <p>N’hésitez pas à prendre contact avec la section Bons de garde en cas de question (<a href="mailto:info.bg@be.ch">info.bg@be.ch</a> ; <a href="tel:0316337883">031 633 78 83</a>).</p>

    <p>En espérant que ces indications vous seront utiles, nous vous adressons, Mesdames, Messieurs, nos salutations les meilleures.</p>

    <p class="signature"><b>Direction de la santé, des affaires sociales et de l’intégration du canton de Berne</b><br />
    Office de l’intégration et de l’action sociale, division Handicap, famille et aide aux victimes<br />
    Rathausplatz 1, case postale, 3000 Berne 8<br />
		031 633 78 83, www.be.ch/dssi</p>
</div>

</body>

</html>
