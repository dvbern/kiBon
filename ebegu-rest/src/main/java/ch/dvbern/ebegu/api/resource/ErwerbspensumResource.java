package ch.dvbern.ebegu.api.resource;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxErwerbspensumContainer;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.resource.util.ResourceHelper;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.ErwerbspensumService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.GesuchstellerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.Validate;

/**
 * REST Resource fuer Erwerbspensum
 */
@Path("erwerbspensen")
@Stateless
@Api(description = "Resource welche zum bearbeiten des Erwerbspensums dient")
public class ErwerbspensumResource {

	@Inject
	private ErwerbspensumService erwerbspensumService;
	@Inject
	private GesuchstellerService gesuchstellerService;
	@Inject
	private GesuchService gesuchService;

	@SuppressWarnings("CdiInjectionPointsInspection")
	@Inject
	private JaxBConverter converter;

	@Inject
	private ResourceHelper resourceHelper;


	@ApiOperation("Create a new ErwerbspensumContainer in the database. The object also has a relations to Erwerbspensum data Objects, " +
		", those will be created as well")
	@Nonnull
	@PUT
	@Path("/{gesuchstellerId}/{gesuchId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveErwerbspensum(
		@Nonnull @NotNull @PathParam ("gesuchId") JaxId gesuchJAXPId,
		@Nonnull @NotNull @PathParam("gesuchstellerId") JaxId gesuchstellerId,
		@Nonnull @NotNull @Valid JaxErwerbspensumContainer jaxErwerbspensumContainer,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) throws EbeguEntityNotFoundException {

		Gesuch gesuch = gesuchService.findGesuch(gesuchJAXPId.getId()).orElseThrow(() -> new EbeguEntityNotFoundException("saveErwerbspensum", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, "GesuchId invalid: " + gesuchJAXPId.getId()));
		// Sicherstellen, dass das dazugehoerige Gesuch ueberhaupt noch editiert werden darf fuer meine Rolle
		resourceHelper.assertGesuchStatusForBenutzerRole(gesuch);

		GesuchstellerContainer gesuchsteller = gesuchstellerService.findGesuchsteller(gesuchstellerId.getId()).orElseThrow(() -> new EbeguEntityNotFoundException("saveErwerbspensum", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, "GesuchstellerId invalid: " + gesuchstellerId.getId()));
		ErwerbspensumContainer convertedEwpContainer = converter.erwerbspensumContainerToStoreableEntity(jaxErwerbspensumContainer);
		convertedEwpContainer.setGesuchsteller(gesuchsteller);
		ErwerbspensumContainer storedEwpCont = this.erwerbspensumService.saveErwerbspensum(convertedEwpContainer, gesuch);

		URI uri = null;
		if (uriInfo != null) {
			uri = uriInfo.getBaseUriBuilder()
				.path(ErwerbspensumResource.class)
				.path('/' + storedEwpCont.getId())
				.build();
		}
		JaxErwerbspensumContainer jaxEwpCont = converter.erwerbspensumContainerToJAX(storedEwpCont);
		return Response.created(uri).entity(jaxEwpCont).build();
	}

	@ApiOperation("Returns the ErwerbspensumContainer with the specified ID ")
	@Nullable
	@Path("/{erwerbspensumContID}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxErwerbspensumContainer findErwerbspensum(
		@Nonnull @NotNull @PathParam("erwerbspensumContID") JaxId erwerbspensumContID) throws EbeguRuntimeException {

		Validate.notNull(erwerbspensumContID.getId());
		String entityID = converter.toEntityId(erwerbspensumContID);
		Optional<ErwerbspensumContainer> optional = erwerbspensumService.findErwerbspensum(entityID);

		if (!optional.isPresent()) {
			return null;
		}
		ErwerbspensumContainer erwerbspenCont = optional.get();
		return converter.erwerbspensumContainerToJAX(erwerbspenCont);
	}

	@ApiOperation("Returns all the ErwerbspensumContainer for the Gesuchsteller with the specified ID")
	@Nullable
	@GET
	@Path("/gesuchsteller/{gesuchstellerID}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<JaxErwerbspensumContainer> findErwerbspensumForGesuchsteller(
		@Nonnull @NotNull @PathParam("gesuchstellerID") JaxId gesuchstellerID) throws EbeguEntityNotFoundException {

		Validate.notNull(gesuchstellerID.getId());
		String gesEntityID = converter.toEntityId(gesuchstellerID);
		Optional<GesuchstellerContainer> gesuchsteller = gesuchstellerService.findGesuchsteller(gesEntityID);
		GesuchstellerContainer gs = gesuchsteller.orElseThrow(
			() -> new EbeguEntityNotFoundException("findErwerbspensumForGesuchsteller", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				"GesuchstellerId invalid: " + gesEntityID));
		Collection<ErwerbspensumContainer> pensen = erwerbspensumService.findErwerbspensenForGesuchsteller(gs);
		return pensen.stream()
			.map(erwerbspensumContainer -> converter.erwerbspensumContainerToJAX(erwerbspensumContainer))
			.collect(Collectors.toList());
	}

	@Nullable
	@DELETE
	@Path("/gesuchId/{gesuchId}/erwPenId/{erwerbspensumContID}")
	@Consumes(MediaType.WILDCARD)
	public Response removeErwerbspensum(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchJAXPId,
		@Nonnull @NotNull @PathParam("erwerbspensumContID") JaxId erwerbspensumContIDJAXPId,
		@Context HttpServletResponse response) {

		Validate.notNull(erwerbspensumContIDJAXPId.getId());
		Gesuch gesuch = gesuchService.findGesuch(gesuchJAXPId.getId()).orElseThrow(() -> new EbeguEntityNotFoundException("removeErwerbspensum", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, "GesuchId invalid: " + gesuchJAXPId.getId()));

		// Sicherstellen, dass das dazugehoerige Gesuch ueberhaupt noch editiert werden darf fuer meine Rolle
		resourceHelper.assertGesuchStatusForBenutzerRole(gesuch);

		erwerbspensumService.removeErwerbspensum(converter.toEntityId(erwerbspensumContIDJAXPId), gesuch);
		return Response.ok().build();
	}

}
