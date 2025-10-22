/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.av.AVClient;
import ch.dvbern.ebegu.api.converter.JaxDokumentConverter;
import ch.dvbern.ebegu.api.converter.JaxFerienbetreuungConverter;
import ch.dvbern.ebegu.api.converter.JaxSozialdienstConverter;
import ch.dvbern.ebegu.api.dtos.JaxDokumentGrund;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungDokument;
import ch.dvbern.ebegu.api.dtos.sozialdienst.JaxSozialdienstFallDokument;
import ch.dvbern.ebegu.api.resource.util.MultipartFormToFileConverter;
import ch.dvbern.ebegu.api.resource.util.TransferFile;
import ch.dvbern.ebegu.api.util.RestUtil;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.einstellung.ApplicationPropertyService;
import ch.dvbern.ebegu.entities.Dokument;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungDokument;
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstFall;
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstFallDokument;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.gesuch.freigabe.GesuchValidatorService;
import ch.dvbern.ebegu.reporting.ReportKinderMitZemisNummerService;
import ch.dvbern.ebegu.services.DokumentGrundService;
import ch.dvbern.ebegu.services.FallService;
import ch.dvbern.ebegu.services.FileSaverService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.services.SozialdienstFallDokumentService;
import ch.dvbern.ebegu.services.gemeindeantrag.FerienbetreuungDokumentService;
import ch.dvbern.ebegu.services.gemeindeantrag.FerienbetreuungService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.DokumenteUtil;
import ch.dvbern.ebegu.util.UploadFileInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.tika.Tika;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.ejb3.annotation.TransactionTimeout;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.api.resource.util.ResourceConstants.PART_FILE;
import static ch.dvbern.ebegu.api.resource.util.ResourceConstants.UPLOAD_WARNING;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_FERIENBETREUUNG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_FERIENBETREUUNG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * REST Resource zum Upload von Dokumenten
 */
@SuppressWarnings("OverlyBroadCatchBlock")
@Path("upload")
@Stateless
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class UploadResource {

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	private GesuchService gesuchService;
	@Inject
	private DokumentGrundService dokumentGrundService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private ReportKinderMitZemisNummerService reportKinderMitZemisNummerService;

	@Inject
	private JaxDokumentConverter converter;

	@Inject
	private JaxSozialdienstConverter sozialdienstConverter;

	@Inject
	private JaxFerienbetreuungConverter ferienbetreuungConverter;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private FerienbetreuungService ferienbetreuungService;

	@Inject
	private FerienbetreuungDokumentService ferienbetreuungDokumentService;

	@Inject
	private SozialdienstFallDokumentService sozialdienstFallDokumentService;

	@Inject
	private FallService fallService;

	@Inject
	private AVClient avClient;

	@Inject
	private PrincipalBean principal;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Inject
	private GesuchValidatorService gesuchValidatorService;

	private static final String PART_DOKUMENT_GRUND = "dokumentGrund";

	private static final String FILENAME_HEADER = "x-filename";
	private static final String GESUCHID_HEADER = "x-gesuchID";

	private static final String FILENAME_WARNING = "filename must be given";

	private static final String CONTENT_TYPE = "*/*; charset=UTF-8";

	private static final Logger LOG = LoggerFactory.getLogger(
		UploadResource.class
	);

	@Operation(
		summary = "Speichert ein oder mehrere Dokumente in der Datenbank")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@PermitAll
	public Response uploadFiles(
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo,
		MultipartFormDataInput input
	)
		throws IOException, MimeTypeParseException {

		request.setAttribute(
			InputPart.DEFAULT_CONTENT_TYPE_PROPERTY,
			CONTENT_TYPE
		);

		String[] encodedFilenames = getFilenamesFromHeader(request);

		// check if filenames available
		if (encodedFilenames == null || encodedFilenames.length == 0) {
			final String problemString = FILENAME_WARNING;
			LOG.error(problemString);
			return Response.serverError().entity(problemString).build();
		}

		// Get GesuchId from header
		String gesuchId = request.getHeader(GESUCHID_HEADER);
		UUID.fromString(gesuchId); //will throw illegalArgumentException if no valid UUID
		if (StringUtils.isEmpty(gesuchId)) {
			final String problemString = "a valid gesuchID must be given";
			LOG.error(problemString);
			return Response.serverError().entity(problemString).build();
		}

		// Get DokumentGrund Object from form-paramter
		List<InputPart> inputPartsDG = input.getFormDataMap()
			.get(PART_DOKUMENT_GRUND);
		if (inputPartsDG == null
			|| !inputPartsDG.stream().findAny().isPresent()) {
			final String problemString =
				"form-parameter 'inputPartsDG' not found";
			LOG.error(problemString);
			return Response.serverError().entity(problemString).build();
		}

		// Convert DokumentGrund from inputStream
		JaxDokumentGrund jaxDokumentGrund;
		try (InputStream dokGrund = input.getFormDataPart(
			PART_DOKUMENT_GRUND,
			InputStream.class,
			null
		)) {
			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			jaxDokumentGrund =
				mapper.readValue(
					IOUtils.toString(dokGrund, StandardCharsets.UTF_8),
					JaxDokumentGrund.class
				);
		} catch (IOException e) {
			final String problemString =
				"Can't parse DokumentGrund from Jax to object";
			LOG.error(problemString, e);
			return Response.serverError().entity(problemString).build();
		}

		if (jaxDokumentGrund == null) {
			final String problemString =
				"\"Can't parse DokumentGrund from Jax to object";
			LOG.error(problemString);
			return Response.serverError().entity(problemString).build();
		}

		// jaxDokumentGrund ist jetzt u.U. noch in einem falschen Zustand. Wir müssen es neu von der Datenbank lesen
		// Die neu hochgeladenen Files gehen nicht verloren, sie befinden sich im "input"
		DokumentGrund dokumentGrundToMerge = new DokumentGrund();
		if (jaxDokumentGrund.getId() != null) {
			Optional<DokumentGrund> existingDokumentGrundOptional =
				dokumentGrundService.findDokumentGrund(
					jaxDokumentGrund.getId()
				);
			if (existingDokumentGrundOptional.isPresent()) {
				dokumentGrundToMerge = existingDokumentGrundOptional.get();
				jaxDokumentGrund = converter.dokumentGrundToJax(
					dokumentGrundToMerge
				);
				if (!dokumentGrundToMerge.getGesuch()
					.getId()
					.equals(gesuchId)) {
					final String problemString =
						"Gesuch zu ueberschreiben ist nicht erlaubt";
					LOG.error(problemString);
					return Response.serverError().entity(problemString).build();
				}
			}
		}

		DokumentGrund convertedDokumentGrund = converter.dokumentGrundToEntity(
			jaxDokumentGrund,
			dokumentGrundToMerge
		);

		extractFilesFromInput(
			input,
			encodedFilenames,
			gesuchId,
			dokumentGrundToMerge
		);

		Optional<Gesuch> gesuch = gesuchService.findGesuch(gesuchId);
		if (gesuch.isEmpty()) {
			final String problemString = "Can't find Gesuch on DB";
			LOG.error(problemString);
			return Response.serverError().entity(problemString).build();
		}

		gesuchValidatorService.validateGesuchDocumentUpload(gesuch.get());

		dokumentGrundToMerge.getDokumente()
			.forEach(
				dokument -> DokumenteUtil.validateDokumentDirectory(
					dokument.getFilepfad(),
					ebeguConfiguration.getDocumentFilePath()
				)
			);

		convertedDokumentGrund.setGesuch(gesuch.get());

		// Bereits beim Upload auf Viren scannen
		convertedDokumentGrund.getDokumente()
			.forEach(dokument -> avClient.scan(dokument));

		// save modified Dokument to DB
		DokumentGrund persistedDokumentGrund = dokumentGrundService
			.saveDokumentGrund(convertedDokumentGrund);

		final JaxDokumentGrund jaxDokumentGrundToReturn = converter
			.dokumentGrundToJax(persistedDokumentGrund);

		URI uri = uriInfo.getBaseUriBuilder()
			.path(UploadResource.class)
			.path('/' + persistedDokumentGrund.getId())
			.build();

		return Response.created(uri).entity(jaxDokumentGrundToReturn).build();
	}

	@Operation(
		summary = "Speichert ein oder mehrere SozialdienstFall Vollmacht Dokument in der Datenbank"
	)
	@Path("/uploadSozialdienstFallsDokument/{fallId}")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS,
		SACHBEARBEITER_TS, ADMIN_GEMEINDE,
		SACHBEARBEITER_GEMEINDE, ADMIN_SOZIALDIENST,
		SACHBEARBEITER_SOZIALDIENST })
	public Response uploadSozialdienstFallsDokument(
		@Nonnull @NotNull @PathParam("fallId") JaxId fallJAXPId,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo,
		MultipartFormDataInput input
	)
		throws IOException, MimeTypeParseException {

		request.setAttribute(
			InputPart.DEFAULT_CONTENT_TYPE_PROPERTY,
			CONTENT_TYPE
		);

		String[] encodedFilenames = getFilenamesFromHeader(request);

		// check if filenames available
		if (encodedFilenames == null || encodedFilenames.length == 0) {
			final String problemString = FILENAME_WARNING;
			LOG.error(problemString);
			return Response.serverError().entity(problemString).build();
		}

		// Get SozialdienstFallId from request Parameter
		String fallId = converter.toEntityId(fallJAXPId);

		Optional<Fall> fall = fallService.findFall(fallId);
		if (fall.isEmpty() || fall.get().getSozialdienstFall() == null) {
			final String problemString =
				"Can't find Fall on DB or not a SozialdienstFall";
			LOG.error(problemString);
			return Response.serverError().entity(problemString).build();
		}

		JaxSozialdienstFallDokument jaxSozialdienstFallDokuments =
			extractFileFromInputAndCreateVollmachtDokumenten(
				encodedFilenames,
				input,
				fall.get().getSozialdienstFall()
			);

		URI uri = uriInfo.getBaseUriBuilder()
			.path(UploadResource.class)
			.path('/' + fall.get().getSozialdienstFall().getId())
			.build();

		return Response.created(uri)
			.entity(jaxSozialdienstFallDokuments)
			.build();
	}

	@Operation(
		summary = "Speichert ein oder mehrere FerienbetreuungDokumente in der Datenbank"
	)
	@Path("/ferienbetreuungDokumente/{ferienbetreuungContainerId}")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS,
		ADMIN_FERIENBETREUUNG,
		SACHBEARBEITER_FERIENBETREUUNG })
	public Response uploadFerienbetreuungDokumente(
		@Nonnull
		@NotNull
		@PathParam("ferienbetreuungContainerId") JaxId ferienbetreuungContainerJAXPId,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo,
		MultipartFormDataInput input
	)
		throws IOException, MimeTypeParseException {

		request.setAttribute(
			InputPart.DEFAULT_CONTENT_TYPE_PROPERTY,
			CONTENT_TYPE
		);

		String[] encodedFilenames = getFilenamesFromHeader(request);

		// check if filenames available
		if (encodedFilenames == null || encodedFilenames.length == 0) {
			final String problemString = FILENAME_WARNING;
			LOG.error(problemString);
			return Response.serverError().entity(problemString).build();
		}

		String ferienbetreuungContainerId = converter.toEntityId(
			ferienbetreuungContainerJAXPId
		);

		FerienbetreuungAngabenContainer container =
			ferienbetreuungService.findFerienbetreuungAngabenContainer(
				ferienbetreuungContainerId
			)
				.orElseThrow(
					() -> new EbeguRuntimeException(
						"uploadFerienbetreuungDokumente",
						ferienbetreuungContainerId
					)
				);

		// for every file create a new FerienbetreuungDokument linked with the given FerienbetreuungContainer
		List<JaxFerienbetreuungDokument> jaxFerienbetreuungDokumente =
			extractFilesFromInputAndCreateFerienbetreuungDokumente(
				encodedFilenames,
				input,
				container
			);

		URI uri = uriInfo.getBaseUriBuilder()
			.path(UploadResource.class)
			.path('/' + container.getId())
			.build();

		return Response.created(uri)
			.entity(jaxFerienbetreuungDokumente)
			.build();
	}

	@Operation(
		summary = "Stores the Erlaeuterungen zu Verfuegung pdf of the Gesuchsperiode with the given id and Sprache")
	@POST
	@Path("/gesuchsperiodeDokument/{sprache}/{periodeId}/{dokumentTyp}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(SUPER_ADMIN)
	public Response saveGesuchsperiodeDokument(
		@Nonnull @NotNull @PathParam("sprache") Sprache sprache,
		@Nonnull @NotNull @PathParam("periodeId") String periodeId,
		@Nonnull @NotNull @PathParam("dokumentTyp") DokumentTyp dokumentTyp,
		@Nonnull @NotNull MultipartFormDataInput input
	) {

		List<TransferFile> fileList = MultipartFormToFileConverter.parse(input);
		Validate.notEmpty(fileList, UPLOAD_WARNING);
		final TransferFile transferFile = fileList.get(0);

		// Bereits beim Upload auf Viren scannen
		avClient.scan(transferFile.getContent(), transferFile.getFilename());

		gesuchsperiodeService.uploadGesuchsperiodeDokument(
			periodeId,
			sprache,
			dokumentTyp,
			transferFile.getContent()
		);

		return Response.ok().build();
	}

	@Operation(
		summary = "Stores Dokument of Typ dokumentTyp  for a Gemeinde and a Gesuchsperiode")
	@POST
	@Path("/gemeindeGesuchsperiodeDoku/{gemeindeId}/{gesuchsperiodeId}/{sprache}/{dokumentTyp}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE,
		SACHBEARBEITER_BG, SACHBEARBEITER_TS,
		SACHBEARBEITER_GEMEINDE, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response uploadGemeindeGesuchsperiodeDokument(
		@Nonnull @NotNull @PathParam("gemeindeId") JaxId gemeindeJAXPId,
		@Nonnull
		@NotNull
		@PathParam("gesuchsperiodeId") JaxId gesuchsperiodeJAXPId,
		@Nonnull @PathParam("sprache") Sprache sprache,
		@Nonnull @PathParam("dokumentTyp") DokumentTyp dokumentTyp,
		@Nonnull @NotNull MultipartFormDataInput input
	) {

		List<TransferFile> fileList = MultipartFormToFileConverter.parse(input);

		Validate.notEmpty(fileList, UPLOAD_WARNING);

		String gemeindeId = converter.toEntityId(gemeindeJAXPId);
		String gesuchsperiodeId = converter.toEntityId(gesuchsperiodeJAXPId);
		TransferFile file = fileList.get(0);

		// Bereits beim Upload auf Viren scannen
		avClient.scan(file.getContent(), file.getFilename());

		gemeindeService.uploadGemeindeGesuchsperiodeDokument(
			gemeindeId,
			gesuchsperiodeId,
			sprache,
			dokumentTyp,
			file.getContent()
		);

		return Response.ok().build();
	}

	@Operation(
		summary = "Stores and processes Excel containing a list of children with a zemis number. Sets flag "
			+ "'keinSelbstbehaltFuerGemeinde' for every child of this list.")
	@POST
	@Path("/zemisExcel")
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES,
		unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response uploadZemisExcelAndSetFlag(
		@Nonnull @NotNull MultipartFormDataInput input
	) throws IOException {

		List<TransferFile> fileList = MultipartFormToFileConverter.parse(input);
		Validate.notEmpty(fileList, UPLOAD_WARNING);
		TransferFile file = fileList.get(0);

		// Bereits beim Upload auf Viren scannen
		avClient.scan(file.getContent(), file.getFilename());

		reportKinderMitZemisNummerService.setFlagAndSaveZemisExcel(
			file.getContent()
		);

		return Response.ok().build();
	}

	@Nullable
	private String[] getFilenamesFromHeader(
		@Context HttpServletRequest request
	) {
		String filenamesJson = request.getHeader(FILENAME_HEADER);
		String[] filenames = null;
		if (!StringUtils.isEmpty(filenamesJson)) {
			filenames = filenamesJson.split(";");
		}
		return filenames;
	}

	private void extractFilesFromInput(
		MultipartFormDataInput input,
		String[] encodedFilenames,
		String gesuchId,
		DokumentGrund dokumentGrund
	) throws MimeTypeParseException, IOException {

		int filecounter = 0;
		String partrileName = PART_FILE + '[' + filecounter + ']';

		// do for every file:
		List<InputPart> inputParts = input.getFormDataMap().get(partrileName);
		while (inputParts != null
			&& inputParts.stream().findAny().isPresent()) {

			UploadFileInfo fileInfo = extractFileInfo(
				inputParts,
				encodedFilenames[filecounter],
				partrileName,
				input
			);

			// safe File to Filesystem, if we just analyze the input stream tika classifies all files as octet streams
			fileSaverService.save(fileInfo, gesuchId);
			checkFiletypeAllowed(fileInfo);

			// add the new file to DokumentGrund object
			addFileToDokumentGrund(dokumentGrund, fileInfo);

			filecounter++;
			partrileName = PART_FILE + '[' + filecounter + ']';
			inputParts = input.getFormDataMap().get(partrileName);
		}
	}

	@Nullable
	private JaxSozialdienstFallDokument extractFileFromInputAndCreateVollmachtDokumenten(
		@Nonnull String[] encodedFilenames,
		@Nonnull MultipartFormDataInput input,
		@Nonnull SozialdienstFall sozialdienstFall
	) throws MimeTypeParseException, IOException {
		String partrileName = PART_FILE;

		JaxSozialdienstFallDokument sozialdienstFallJaxDokuments = null;

		// do for every file:
		List<InputPart> inputParts = input.getFormDataMap().get(partrileName);
		if (inputParts != null && inputParts.stream().findAny().isPresent()) {

			UploadFileInfo fileInfo = extractFileInfo(
				inputParts,
				encodedFilenames[0],
				partrileName,
				input
			);

			// safe File to Filesystem, if we just analyze the input stream tika classifies all files as octet streams
			fileSaverService.save(fileInfo, sozialdienstFall.getId());
			checkFiletypeAllowed(fileInfo);

			SozialdienstFallDokument sozialdienstFallDokument =
				new SozialdienstFallDokument();
			sozialdienstFallDokument.setSozialdienstFall(sozialdienstFall);
			sozialdienstFallDokument.setFilepfad(fileInfo.getPathAsString());
			sozialdienstFallDokument.setFilename(fileInfo.getFilename());
			sozialdienstFallDokument.setFilesize(fileInfo.getSizeString());

			// Bereits beim Upload auf Viren scannen
			avClient.scan(sozialdienstFallDokument);

			SozialdienstFallDokument documentFromDB =
				sozialdienstFallDokumentService.saveVollmachtDokument(
					sozialdienstFallDokument
				);

			sozialdienstFallJaxDokuments = sozialdienstConverter
				.sozialdienstFallDokumentToJax(documentFromDB);
		}

		return sozialdienstFallJaxDokuments;
	}

	private List<JaxFerienbetreuungDokument> extractFilesFromInputAndCreateFerienbetreuungDokumente(
		@Nonnull String[] encodedFilenames,
		@Nonnull MultipartFormDataInput input,
		@Nonnull FerienbetreuungAngabenContainer container
	) throws MimeTypeParseException, IOException {

		int filecounter = 0;
		String partrileName = PART_FILE + '[' + filecounter + ']';

		List<JaxFerienbetreuungDokument> jaxFerienbetreuungDokumente =
			new ArrayList<>();

		// do for every file:
		List<InputPart> inputParts = input.getFormDataMap().get(partrileName);
		while (inputParts != null
			&& inputParts.stream().findAny().isPresent()) {

			UploadFileInfo fileInfo = extractFileInfo(
				inputParts,
				encodedFilenames[filecounter],
				partrileName,
				input
			);

			// safe File to Filesystem, if we just analyze the input stream tika classifies all files as octet streams
			fileSaverService.save(fileInfo, container.getId());
			checkFiletypeAllowed(fileInfo);

			// create and add the new file to FerienbetreuungDokument object and persist it
			FerienbetreuungDokument ferienbetreuungDokument =
				new FerienbetreuungDokument();
			ferienbetreuungDokument.setFerienbetreuungAngabenContainer(
				container
			);
			ferienbetreuungDokument.setFilepfad(fileInfo.getPathAsString());
			ferienbetreuungDokument.setFilename(fileInfo.getFilename());
			ferienbetreuungDokument.setFilesize(fileInfo.getSizeString());
			ferienbetreuungDokument.setTimestampUpload(LocalDateTime.now());

			// Bereits beim Upload auf Viren scannen
			avClient.scan(ferienbetreuungDokument);

			FerienbetreuungDokument documentFromDB =
				ferienbetreuungDokumentService.saveDokument(
					ferienbetreuungDokument
				);

			jaxFerienbetreuungDokumente.add(
				ferienbetreuungConverter.ferienbetreuungDokumentToJax(
					documentFromDB
				)
			);

			filecounter++;
			partrileName = PART_FILE + '[' + filecounter + ']';
			inputParts = input.getFormDataMap().get(partrileName);
		}

		return jaxFerienbetreuungDokumente;
	}

	private UploadFileInfo extractFileInfo(
		List<InputPart> inputParts,
		String encodedFilename,
		String partrileName,
		MultipartFormDataInput input
	)
		throws IOException, MimeTypeParseException {
		UploadFileInfo fileInfo =
			RestUtil.parseUploadFile(
				inputParts.stream()
					.findAny()
					.orElseThrow(
						() -> new IOException(
							"No InputParts to parse"
						)
					)
			);

		// evil workaround, (Umlaute werden sonst nicht richtig übertragen!)
		if (encodedFilename != null) {
			String decodedFilenamesJson =
				new String(
					Base64.getDecoder().decode(encodedFilename),
					StandardCharsets.UTF_8
				);
			fileInfo.setFilename(decodedFilenamesJson);
		}

		try (InputStream fileInputStream = input.getFormDataPart(
			partrileName,
			InputStream.class,
			null
		)) {
			fileInfo.setBytes(IOUtils.toByteArray(fileInputStream));
		}
		return fileInfo;
	}

	private void checkFiletypeAllowed(UploadFileInfo fileInfo) {
		//we dont purly trust the filetype set in the header, so we perform our own content-type guessing
		java.nio.file.Path filePath = fileInfo.getPath();
		try {
			Tika tika = new Tika();
			String contentType = tika.detect(filePath);
			final MimeType mimeType = fileInfo.getContentType();
			if (!contentType.equals(mimeType.toString())) {
				LOG.warn(
					"Content type from Header did not match content type returned from probing. "
						+ "\n\t header:   {} \n\t probing:  {}",
					mimeType,
					contentType
				);
			}
			checkTypeAllowed(fileInfo, contentType);
			checkTypeAllowed(fileInfo, mimeType.toString());

		} catch (IOException e) {
			LOG.warn(
				"Could not probe file for its content-type, check was omitted",
				e
			);
		}
	}

	private void checkTypeAllowed(UploadFileInfo fileInfo, String type) {
		final Collection<String> mimeTypeWhitelist = applicationPropertyService
			.readMimeTypeWhitelist(
				principal.getMandant()
			);
		if (!mimeTypeWhitelist.contains(type)) {
			fileSaverService.remove(fileInfo.getPathAsString());
			String message =
				"Blocked upload of filetype that is not in whitelist: "
					+ type;
			throw new EbeguRuntimeException(
				KibonLogLevel.INFO,
				"checkFiletypeAllowed",
				message,
				ErrorCodeEnum.ERROR_UPLOAD_INVALID_FILETYPE,
				type
			);
		}
	}

	private void addFileToDokumentGrund(
		DokumentGrund dokumentGrund,
		UploadFileInfo uploadFileInfo
	) {
		Objects.requireNonNull(dokumentGrund.getDokumente());

		for (Dokument dokument : dokumentGrund.getDokumente()) {

			if (null == dokument.getFilename()
				||
				dokument.getFilename().isEmpty()) {

				//set to existing
				dokument.setFilename(uploadFileInfo.getFilename());
				dokument.setFilepfad(uploadFileInfo.getPathAsString());
				dokument.setFilesize(uploadFileInfo.getSizeString());
				LOG.info(
					"Replace placeholder on {} by file {}",
					dokumentGrund.getDokumentTyp(),
					uploadFileInfo.getFilename()
				);
				return;
			}
		}

		//add new
		Dokument dokument = new Dokument();
		dokument.setFilename(uploadFileInfo.getFilename());
		dokument.setFilepfad(uploadFileInfo.getPathAsString());
		dokument.setFilesize(uploadFileInfo.getSizeString());
		dokument.setDokumentGrund(dokumentGrund);
		dokument.setTimestampUpload(LocalDateTime.now());
		dokumentGrund.getDokumente().add(dokument);
		LOG.info(
			"Add on {} file {}",
			dokumentGrund.getDokumentTyp(),
			uploadFileInfo.getFilename()
		);
	}

}
