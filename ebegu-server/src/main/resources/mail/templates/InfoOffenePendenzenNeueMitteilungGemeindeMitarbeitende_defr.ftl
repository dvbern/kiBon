<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="offenePendenzen" type="java.lang.Boolean" -->
<#-- @ftlvariable name="ungelesendeMitteilung" type="java.lang.Boolean" -->
<#-- @ftlvariable name="hostname" type="java.lang.String" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="gemeindeNamen" type="java.lang.String" -->
<#-- @ftlvariable name="gemeindeNamenMitteilung" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${empfaengerMail}
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> –  <#if offenePendenzen>Offene Pendenzen</#if><#if ungelesendeMitteilung && offenePendenzen> und</#if><#if ungelesendeMitteilung><#if offenePendenzen> neue<#else> Neue</#if> Mitteilungen</#if> / <#if offenePendenzen>dossiers en suspens</#if><#if ungelesendeMitteilung && offenePendenzen> et</#if><#if ungelesendeMitteilung><#if offenePendenzen> nouveau<#else> Nouveau</#if> message</#if></@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if>– <#if offenePendenzen>Offene Pendenzen</#if><#if ungelesendeMitteilung && offenePendenzen> und</#if><#if ungelesendeMitteilung><#if offenePendenzen> neue<#else> Neue</#if> Mitteilungen</#if>  / <#if offenePendenzen>dossiers en suspens</#if><#if ungelesendeMitteilung && offenePendenzen> et</#if><#if ungelesendeMitteilung><#if offenePendenzen> nouveau<#else> Nouveau</#if> message</#if></title>

</head>

<body>

<div>
	<p>
		Guten Tag
	</p>
    <#if offenePendenzen>
	<p>
		Wir möchten Sie darüber informieren, dass für Ihre Gemeinde (${gemeindeNamen}) in kiBon Pendenzen offen sind. <br>
		Sie können diese <a href="${frontendUrl}/pendenzen">hier</a> einsehen.
	</p>
    </#if>
    <#if ungelesendeMitteilung>
	<p>
		Wir möchten Sie <#if offenePendenzen> ausserdem </#if>darüber informieren, dass Sie für Ihre Gemeinde (${gemeindeNamenMitteilung}) ungelesene Nachrichten im Posteingang haben.
		Sie können diese <a href="${frontendUrl}/posteingang">hier</a> einsehen.
	</p>
	</#if>
	<p>
		<#if configuration.isDevmode>
		<b>Hierbei handelt es sich um eine Nachricht von einem Testsystem. Dieses Testsystem wird für Schulungen verwendet. Über dieses System abgehandelte Anträge verfügen über keine Zahlungsberechtigung!</b><br><br>
		</#if>
		Dies ist eine automatisch versendete E-Mail. Bitte antworten Sie nicht auf diese Nachricht.
	</p>

	<hr>

	<p>
		Bonjour,
	</p>
    <#if offenePendenzen>
	<p>
        Nous tenons à vous informer que des dossiers en attente sont enregistrés dans kiBon pour votre commune (${gemeindeNamen}).
		Vous pouvez les consulter
		<a href="${frontendUrl}/pendenzen">ici</a>.
	</p>
    </#if>
    <#if ungelesendeMitteilung>
	<p>
        Nous tenons <#if offenePendenzen> également </#if> à vous informer que vous avez des messages non lus dans la boîte de réception concernant votre commune (${gemeindeNamenMitteilung}).
		Vous pouvez les consulter
		<a href="${frontendUrl}/posteingang">ici</a>.
	</p>
    </#if>
	<p>
        <#if configuration.isDevmode>
			<b>Le présent message est envoyé par un système test utilisé pour les tutoriels. Les demandes via ce
				système ne donnent pas droit à un versement.</b><br><br>
        </#if>
		Merci de ne pas répondre à ce message automatique.
	</p>

</div>

</body>

</html>
