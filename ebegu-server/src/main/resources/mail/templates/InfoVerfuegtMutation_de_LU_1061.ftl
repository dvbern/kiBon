<#-- @ftlvariable name="gesuch" type="ch.dvbern.ebegu.entities.Gesuch" -->
<#-- @ftlvariable name="senderFullName" type="java.lang.String" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="gesuchsteller" type="ch.dvbern.ebegu.entities.Gesuchsteller" -->
<#-- @ftlvariable name="isSozialdienst" type="java.lang.Boolean" -->
<#-- @ftlvariable name="hostname" type="java.lang.String" -->
<#-- @ftlvariable name="gruss" type="java.lang.String" -->
<#-- @ftlvariable name="fall" type="ch.dvbern.ebegu.entities.Fall"-->
<#-- @ftlvariable name="gesuchsperiode" type="ch.dvbern.ebegu.entities.Gesuchsperiode"-->
From: ${configuration.senderAddress}
To: <@base64Header>${senderFullName}</@base64Header> <${empfaengerMail}>
Subject: ${fall.getPaddedFallnummer()}, ${gesuchsperiode.getGesuchsperiodeString()}, <@base64Header>kiBon <#if configuration.isDevmode>Testsystem</#if> – <#if isSozialdienst>Die Mutation für den Antrag von ${gesuchsteller.fullName}<#else>Eine Mutation</#if> wurde bearbeitet</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Testsystem</#if> – <#if isSozialdienst>Die Mutation für den Antrag von ${gesuchsteller.fullName}<#else>Ihre Mutation</#if> wurde bearbeitet</title>

</head>

<body>

<div>
	<p>
		Sehr geehrte Familie
	</p>
	<p>
		Am ${gesuch.getEingangsdatumFormated()} wurde via kiBon eine Mutation erstellt. Wir haben die Mutation<#if isSozialdienst> für den Antrag von ${gesuchsteller.fullName}</#if> bearbeitet und Sie können das Ergebnis
		<a href="${frontendUrl}/gesuch/verfuegen/${gesuch.id}">hier</a>
        einsehen.
	</p>
	<p>
		<b>
			Bitte beachten Sie folgende wichtige Informationen:
		</b>
	</p>
	<p>
	<ul>
		<li>
			Die Dienstabteilung teilt Ihnen den Anspruch und die Höhe der Betreuungsgutscheine mit. Nach dieser Mitteilung kann innert 10 Tagen nach deren Zustellung ein beschwerdefähiger Entscheid bei der zuständigen Dienstabteilung verlangt werden.
		</li>
		<li>
			Jede Änderung der Erwerbstätigkeit, des massgebenden Einkommens um mehr als +/- 25 %, des Betreuungsumfangs sowie die Beendigung des Betreuungsverhältnisses oder der Wegzug aus der Stadt Luzern muss innert einer Woche nach Eintritt der Änderung an die zuständige Dienstabteilung gemeldet werden.
		</li>
		<li>
			Die Betreuungsgutscheine müssen vor Beginn, spätestens im Vormonat des Starts des Betreuungsvertrags beantragt werden. Der Antrag muss für jedes Schuljahr spätestens bis zum 31.07. neu gestellt werden. Ein Anspruch kann nicht rückwirkend geltend gemacht werden.
		</li>
	</ul>
	</p>
	<p>
		Freundliche Grüsse <br/>
		${gruss}
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
