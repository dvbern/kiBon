<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="gemeinde" type="ch.dvbern.ebegu.entities.Gemeinde" -->
<#-- @ftlvariable name="angebotNameDe" type="java.lang.String" -->
<#-- @ftlvariable name="angebotNameFr" type="java.lang.String" -->
<#-- @ftlvariable name="hostname" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${empfaengerMail}
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem</#if> – : Aktualisierung Formular «Gemeinde-Kennzahlen Betreuungsgutscheine» / mise à jour du formulaire « Bons de garde : indicateurs de la commune » </@base64Header>
Content-Type: text/html;charset=utf-8

<html xmlns="http://www.w3.org/1999/html">
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Testsystem</#if> – : Aktualisierung Formular «Gemeinde-Kennzahlen Betreuungsgutscheine» / mise à jour du formulaire « Bons de garde : indicateurs de la commune »</title>

</head>

<body>
<p><b>Français ci-dessous</b></p>
<div>
    <p>Sehr geehrte Damen und Herren</p>

    <p>Einmal im Jahr erhebt das Amt für Integration und Soziales, wie die Gemeinden im Kanton Bern Betreuungsgutscheine ausgeben. Dazu sind alle Gemeinden aufgefordert, Angaben über eine allfällige Begrenzung der Gutscheine in der aktuellen Periode (ab dem 1. August) zu machen. Auch wenn Ihre Gemeinde aktuell keine Kostenlenkungsmassnahmen hat, bitten wir Sie um entsprechende Angaben.</p>

    <p>Bitte füllen Sie in kiBon das Formular «Gemeinde Kennzahlen Betreuungsgutscheine» bis am 15. Oktober aus. Sie finden das Formular unter der Rubrik «Gemeindeanträge» und unter folgendem Link: <a href="${frontendUrl}/gemeinde-antraege">Gemeinde Kennzahlen Betreuungsgutscheine</a></p>

    <p>Die Aktualisierung des Formulars dauert maximal fünf Minuten. Gutscheinausgabestellen füllen für jede Gemeinde ein eigenes Formular aus.</p>

    <p>Gerne steht Ihnen der Fachbereich Betreuungsgutscheine unter <a href="mailto:info.bg@be.ch">info.bg@be.ch</a> und <a href="tel:0316337883">031 633 78 83</a> für Rückfragen und Bemerkungen zur Verfügung.</p>

    <p>Wir danken Ihnen für die Kenntnisnahme und verbleiben mit freundlichen Grüssen.</p>

    <p class="signature"><b>Gesundheits-, Sozial- und Integrationsdirektion des Kantons Bern, </b><br />
    Amt für Integration und Soziales, Abteilung Behinderung, Familie und Opferhilfe (BFO)<br />
    Rathausplatz 1, Postfach, 3000 Bern 8<br />
		031 633 78 83, www.be.ch/gsi</p>

</div>

<hr>

<div>
    <p>Mesdames, Messieurs,

    <p>Une fois par an, l’Office de l’intégration et de l’action sociale (OIAS) recense la manière dont les communes bernoises émettent les bons de garde. À cet effet, toutes les communes sont invitées à fournir des informations sur un éventuel contingentement des bons pendant la période en cours (à compter du 1er août) et ce, même si elles n’appliquent actuellement aucune mesure de limitation des coûts.</p>

    <p>Veuillez remplir dans kiBon le formulaire « Indicateurs de la commune » prévu à cet effet d’ici le 15 octobre, disponible à la rubrique « Décomptes de la commune » et au lien suivant: <a href="${frontendUrl}/gemeinde-antraege">Bons de garde: indicateurs de la commune</a>.</p>

    <p>Il faut compter cinq minutes au maximum pour le mettre à jour. Il convient de compléter un seul formulaire par commune.</p>

    <p>N’hésitez pas à prendre contact avec la section Bons de garde en cas de question (<a href="mailto:info.bg@be.ch">info.bg@be.ch</a> ; <a href="tel:0316337883">031 633 78 83</a>).</p>

    <p>En espérant que ces indications vous seront utiles, nous vous adressons, Mesdames, Messieurs, nos salutations les meilleures.</p>

    <p class="signature"><b>Direction de la santé, des affaires sociales et de l’intégration du canton de Berne</b><br />
    Office de l’intégration et de l’action sociale, division Handicap, famille et aide aux victimes<br />
    Rathausplatz 1, case postale, 3000 Berne 8<br />
		031 633 78 83, www.be.ch/dssi<br /></p>

</div>

</body>

</html>
