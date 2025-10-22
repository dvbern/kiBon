<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="jahr" type="java.lang.String" -->
<#-- @ftlvariable name="hostname" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${empfaengerMail}
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem</#if> - Lastenausgleich verbucht - compensation des charges comptabilisée</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
    ${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>Lastenausgleich ${jahr} verbucht - compensation des charges pour l'année ${jahr} comptabilisée</title>

</head>

<body>

<div>
	<p>
		Guten Tag
	</p>
	<p>
		Der Lastenausgleich des Jahres ${jahr} wurde verbucht. Die Berechnungsresultate sind <a href="${frontendUrl}/lastenausgleich">hier</a> ersichtlich
	</p>
	<p>
		Freundliche Grüsse<br/>
		Ihr Kanton Bern
	</p>
	<p>
		<#if configuration.isDevmode>
			<b>Hierbei handelt es sich um eine Nachricht von einem Testsystem. Dieses Testsystem wird zu Testzwecken verwendet. Über dieses System abgehandelte Anträge verfügen über keine Zahlungsberechtigung!</b><br><br>
		</#if>
		Dies ist eine automatisch versendete E-Mail. Bitte antworten Sie nicht auf diese Nachricht.
	</p>
</div>

<div>
	<p>
		Bonjour,
	</p>
	<p>
		La compensation des charges pour l'année ${jahr} a été comptabilisée. Les résultats sont disponibles <a href="${frontendUrl}/lastenausgleich">ici</a>
	</p>
	<p>
		Nous vous présentons nos meilleures salutations.<br/>
		Votre canton
	</p>
	<p>
		<#if configuration.isDevmode>
			<b>Le présent message est envoyé par un système test utilisé à des fins d'essai. Les demandes via ce système ne donnent pas droit à un versement.</b><br><br>
		</#if>
		Merci de ne pas répondre à ce message automatique.
	</p>
</div>

</body>

</html>
