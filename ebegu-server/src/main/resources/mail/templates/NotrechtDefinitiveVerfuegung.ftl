<#-- @ftlvariable name="rueckforderungFormular" type="ch.dvbern.ebegu.entities.RueckforderungFormular" -->
<#-- @ftlvariable name="institutionStammdaten" type="ch.dvbern.ebegu.entities.InstitutionStammdaten" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${institutionStammdaten.mail}
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> – Corona-Finanzierung
	für Kitas und TFO: Verfügung / Coronavirus et accueil extrafamilial : décision</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
    ${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> – Verfügung / décision</title>

</head>

<body>

<div>
	<p>
		<b>
			Ausfallentschädigung im Bereich familienergänzende Kinderbetreuung für entgangene Betreuungsbeiträge
			infolge der Massnahmen zur Bekämpfung des Coronavirus (Covid-19)
		</b>
	</p>
	<p>
		Sehr geehrte Dame, sehr geehrter Herr
	</p>
	<p>
		Wir haben Ihr Gesuch um eine Ausfallentschädigung für entgangene Betreuungsbeiträge infolge der Massnahmen zur
		Bekämpfung des Coronavirus geprüft. Ihre Verfügung finden Sie unter <a href="<#if configuration
		.clientUsingHTTPS>https://<#else>http://</#if>${configuration
		.hostname}/corona-finanzierung/list/rueckforderung/${rueckforderungFormular.id}/verfuegungen">diesem Link</a>.
	</p>
	<p>
		Freundliche Grüsse
	</p>
	<p>
		Abteilung Familie Kanton Bern</p>
	<p>
		Amt für Integration und Soziales<br>
		<a href="mailto:info.fam@be.ch">info.fam@be.ch</a><br>
		031 633 78 91
	</p>
    <#if configuration.isDevmode>
		<p>
			<b>Hierbei handelt es sich um eine Nachricht von einem Testsystem. Dieses Testsystem wird für Schulungen
				verwendet. Über dieses System abgehandelte Gesuche verfügen über keine Zahlungsberechtigung!</b><br><br>
		</p>
    </#if>
</div>
<div>
	<p>
		<b>
			Indemnités pour pertes financières en faveur des institutions d’accueil extra-familial pour enfants en
			compensation des contributions de garde non perçues en raison des mesures de lutte contre le coronavirus (COVID-19)
		</b>
	</p>
	<p>
		Mesdames, Messieurs,
	</p>
	<p>
		Votre demande d’indemnités pour pertes financières nous est bien parvenue et a retenu toute notre attention.
		Nous avons le plaisir de vous faire parvenir <a href="<#if configuration
		.clientUsingHTTPS>https://<#else>http://</#if>${configuration
		.hostname}/corona-finanzierung/list/rueckforderung/${rueckforderungFormular.id}/verfuegungen">la décision</a>.
	</p>
	<p>
		En vous remerciant de votre engagement en cette période de pandémie, nous vous prions d’agréer,
		Mesdames, Messieurs, nos salutations distinguées.
	</p>
	<p>
		Meilleurs salutations
	</p>
	<p>
		La division Famille</p>
	<p>
		Office de l’intégration et de l’action sociale
		Courriel : <a href="mailto:info.fam@be.ch">info.fam@be.ch</a>
		Tél : 031 633 78 91
	</p>
    <#if configuration.isDevmode>
		<p>
			<b>Le présent message est envoyé par un système test utilisé pour les tutoriels. Les demandes via ce système
				ne donnent pas droit à un versement.</b><br><br>
		</p>
    </#if>
</div>
</body>
</html>
