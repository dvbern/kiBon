<#-- @ftlvariable name="gesuch" type="ch.dvbern.ebegu.entities.Gesuch" -->
<#-- @ftlvariable name="gesuchsteller" type="ch.dvbern.ebegu.entities.Gesuchsteller" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#assign datumGeloescht = .now>
From: ${configuration.senderAddress}
To: <@base64Header>${gesuchsteller.fullName}</@base64Header> <${gesuchsteller.mail}>
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Système de test</#if> – Votre demande a été supprimée</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Système de test</#if> – Votre demande a été supprimée</title>

</head>

<body>

<div>
	<p>
		Bonjour,
	</p>
	<p>
		Votre demande via  <a href="www.kibon.ch">www.kibon.ch</a> nous est bien parvenue mais elle n'a toujours pas été validée et il manque la confirmation
		des
		données. Vous avez déjà reçu un courriel à ce sujet.
	</p>
	<p>
		Vos données ont donc été automatiquement supprimées le ${datumGeloescht?date}.
	</p>
	<p>
		Nous vous présentons nos meilleures salutations.<br/>
		Votre commune ${gesuch.dossier.gemeinde.name}
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
