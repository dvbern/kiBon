<#-- @ftlvariable name="gesuch" type="ch.dvbern.ebegu.entities.Gesuch" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="fall" type="ch.dvbern.ebegu.entities.Fall"-->
<#-- @ftlvariable name="gesuchsperiode" type="ch.dvbern.ebegu.entities.Gesuchsperiode"-->
<#-- @ftlvariable name="verantwortlicherTS" type="ch.dvbern.ebegu.entities.Benutzer"-->
From: ${configuration.senderAddress}
To: <@base64Header>${verantwortlicherTS.vorname} ${verantwortlicherTS.nachname}</@base64Header> <${empfaengerMail}>
Subject: ${fall.getPaddedFallnummer()}, ${gesuchsperiode.getGesuchsperiodeString()}, <@base64Header>kiBon - Décision de bon de garde rendue</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
    ${templateConfiguration.mailCss}
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>kiBon - Décision de bon de garde rendue</title>

</head>

<body>

    <div>
        <p>
            Bonjour,
        </p>
        <p>
            Une décision a été rendue pour la demande portant le numéro de dossier <a href="${frontendUrl}/gesuch/verfuegen/${gesuch.id}">${gesuch.dossier.fall.fallNummer?string("#")}</a>. Les inscriptions aux écoles à journée continue peuvent désormais également être terminées.
        </p>
        <p>
            Avec nos salutations les meilleures,<br/>
            kiBon-Team
        </p>
        <p>
            <#if configuration.isDevmode>
                <b>Le présent message est envoyé par un système test utilisé pour les tutoriels. Les demandes via ce système ne donnent pas droit à un versement.</b><br><br>
            </#if>
            Merci de ne pas répondre à ce message automatique.
        </p>
    </div>

</body>

</html>
