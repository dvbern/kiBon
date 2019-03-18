<#-- @ftlvariable name="gesuch" type="ch.dvbern.ebegu.entities.Gesuch" -->
<#-- @ftlvariable name="gesuchsteller" type="ch.dvbern.ebegu.entities.Gesuchsteller" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${gesuchsteller.fullName} <${empfaengerMail}>
Subject: <@base64Header>FR_kiBon – Ihre Mutation wurde bearbeitet</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>FR_kiBon – Ihre Mutation wurde bearbeitet</title>

</head>

<body>

<div>
    <p>
        FR_Sehr geehrte Familie
    </p>
    <p>
        FR_Am ${gesuch.getEingangsdatumFormated()} haben Sie via kiBon eine Mutation eingereicht.
        Wir haben die Mutation bearbeitet und Sie können das Ergebnis
        <a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/gesuch/verfuegen/${gesuch.id}">hier</a>
        einsehen.
    </p>
    <p>
        FR_Freundliche Grüsse <br/>
        Ihre Gemeinde ${gesuch.dossier.gemeinde.name}
    </p>
    <p>
        FR_Dies ist eine automatisch versendete E-Mail. Bitte antworten Sie nicht auf diese Nachricht.
    </p>
</div>

</body>

</html>
