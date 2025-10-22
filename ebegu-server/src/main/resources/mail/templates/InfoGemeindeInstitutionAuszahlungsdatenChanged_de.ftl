<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="institutionName" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${empfaengerMail}
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem</#if> – Institution hat Zahlungsangaben angepasst</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Testsystem</#if> – Institution hat Zahlungsangaben angepasst </title>

</head>

<body>

<div>
	<p>
		Guten Tag,
	</p>
	<p>
		Die Institution ${institutionName} hat ihre Zahlungsangaben (IBAN-Nummer und/oder Kontoinhaber/in) angepasst.
        Bitte überprüfen Sie und passen Sie gegebenenfalls die Infoma Kreditorennummer und den Infoma Bankcode der Institution in den Stammdaten Ihrer Gemeinde an.
    </p>
	<p>
        Freundliche Grüsse<br/>
        kiBon - Team
	</p>
    <p>
        <#if configuration.isDevmode>
            <b>Hierbei handelt es sich um eine Nachricht von einem Testsystem. Dieses Testsystem wird für Schulungen verwendet. Über dieses System abgehandelte Anträge verfügen über keine Zahlungsberechtigung!</b><br><br>
        </#if>
        Dies ist eine automatisch versendete E-Mail. Bitte antworten Sie nicht auf diese Nachricht.
    </p>
</div>

</body>

</html>
