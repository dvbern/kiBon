<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="downloadurl" type="java.lang.String" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="footer" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${empfaengerMail}
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Système de test</#if> – Statistique disponible</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Système de test</#if> – Statistique disponible</title>

</head>

<body>

<div>

	<p>
		Bonjour,
	</p>
	<p>
		La statistique vous concernant est disponible et peut être téléchargée <a href="${downloadurl}">ici</a>.
	</p>
	<p>
	${footer}
	</p>
	<#if configuration.isDevmode>
		<p>
			<b>Le présent message est envoyé par un système test utilisé pour les tutoriels. Les demandes via ce système ne donnent pas droit à un versement.</b><br><br>
		</p>
	</#if>
</div>

</body>

</html>
