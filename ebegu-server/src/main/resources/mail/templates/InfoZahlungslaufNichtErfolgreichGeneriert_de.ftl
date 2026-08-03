<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="footer" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${empfaengerMail}
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem</#if> – Zahlungslauf konnte nicht erstellt werden</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Testsystem</#if> – Zahlungslauf konnte nicht erstellt werden</title>

</head>

<body>

<div>

	<p>
		Guten Tag
	</p>
	<p>
        Der Zahlungslauf konnte nicht erfolgreich erstellt werden. Bitte versuchen Sie es zu einem späteren Zeitpunkt erneut.
	</p>
    <p>
        Falls das Problem weiterhin besteht oder Sie Fragen haben, stehen Ihnen unsere Mitarbeitenden per Telefon (<a href="tel:+41 31 378 24 33">+41 31 378 24 33</a>) oder E-Mail (<a href="mailto:support@kibon.ch">support@kibon.ch</a>) zur Verfügung.
    </p>
    <p>
        ${footer}
    </p>
	<#if configuration.isDevmode>
		<p>
			<b>Hierbei handelt es sich um eine Nachricht von einem Testsystem. Dieses Testsystem wird für Schulungen verwendet. Über dieses System abgehandelte Anträge verfügen über keine Zahlungsberechtigung!</b><br><br>
		</p>
	</#if>
</div>

</body>

</html>
