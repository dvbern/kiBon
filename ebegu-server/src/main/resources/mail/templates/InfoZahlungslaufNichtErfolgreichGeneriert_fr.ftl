<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="footer" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${empfaengerMail}
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Système de test</#if> – cycle de paiement impossible à créer</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Système de test</#if> – cycle de paiement impossible à créer</title>

</head>

<body>

<div>

	<p>
		Bonjour,
	</p>
    <p>
        Votre cycle de paiement n’a pas pu être créé. Veuillez réessayer ultérieurement.
    </p>
    <p>
        En cas de problème persistant ou de question, n’hésitez pas à nous contacter par téléphone (<a href="tel:+41 31 378 24 33">+41 31 378 24 33</a>) ou par courriel (<a href="mailto:support@kibon.ch">support@kibon.ch</a>).
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
