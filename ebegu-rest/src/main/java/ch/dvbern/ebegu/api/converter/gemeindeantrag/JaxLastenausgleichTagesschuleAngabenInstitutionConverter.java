/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.converter.gemeindeantrag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

import ch.dvbern.ebegu.api.converter.AbstractBaseSonderConverter;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxLastenausgleichTagesschuleAngabenInstitution;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxLastenausgleichTagesschuleAngabenInstitutionContainer;
import ch.dvbern.ebegu.dto.gemeindeantrag.OeffnungszeitenTagesschuleDTO;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitution;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionContainer;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.util.EbeguUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hibernate.StaleObjectStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

@Dependent
public class JaxLastenausgleichTagesschuleAngabenInstitutionConverter extends
	AbstractBaseSonderConverter {
	private static final Logger LOGGER = LoggerFactory.getLogger(
		JaxLastenausgleichTagesschuleAngabenInstitutionConverter.class
	);

	@Inject
	private Persistence persistence;
	@Inject
	private InstitutionService institutionService;

	@Nonnull
	protected Set<JaxLastenausgleichTagesschuleAngabenInstitutionContainer> lastenausgleichTagesschuleAngabenInstitutionContainerListToJax(
		@Nullable final Set<LastenausgleichTagesschuleAngabenInstitutionContainer> institutionContainerList
	) {
		if (institutionContainerList == null) {
			return Collections.emptySet();
		}
		return institutionContainerList.stream()
			.map(
				this::lastenausgleichTagesschuleAngabenInstitutionContainerToJax
			)
			.collect(Collectors.toSet());
	}

	@Nonnull
	public JaxLastenausgleichTagesschuleAngabenInstitutionContainer lastenausgleichTagesschuleAngabenInstitutionContainerToJax(
		@Nonnull final LastenausgleichTagesschuleAngabenInstitutionContainer institutionContainer
	) {
		JaxLastenausgleichTagesschuleAngabenInstitutionContainer jaxInstitutionContainer =
			new JaxLastenausgleichTagesschuleAngabenInstitutionContainer();
		convertAbstractFieldsToJAX(
			institutionContainer,
			jaxInstitutionContainer
		);

		jaxInstitutionContainer.setStatus(institutionContainer.getStatus());
		jaxInstitutionContainer.setInstitution(
			institutionToJAX(institutionContainer.getInstitution())
		);
		if (institutionContainer.getAngabenDeklaration() != null) {
			jaxInstitutionContainer.setAngabenDeklaration(
				lastenausgleichTagesschuleAngabenInstitutionToJax(
					institutionContainer.getAngabenDeklaration()
				)
			);
		}
		if (institutionContainer.getAngabenKorrektur() != null) {
			jaxInstitutionContainer.setAngabenKorrektur(
				lastenausgleichTagesschuleAngabenInstitutionToJax(
					institutionContainer.getAngabenKorrektur()
				)
			);
		}
		return jaxInstitutionContainer;
	}

	@Nonnull
	protected LastenausgleichTagesschuleAngabenInstitutionContainer lastenausgleichTagesschuleAngabenInstitutionContainerToStorableEntity(
		@Nonnull JaxLastenausgleichTagesschuleAngabenInstitutionContainer jaxInstitutionContainer,
		boolean performOptimisticLockCheck
	) {
		LastenausgleichTagesschuleAngabenInstitutionContainer institutionContainerToMergeWith =
			persistence.find(
				LastenausgleichTagesschuleAngabenInstitutionContainer.class,
				jaxInstitutionContainer.getId()
			);
		if (institutionContainerToMergeWith == null) {
			institutionContainerToMergeWith =
				new LastenausgleichTagesschuleAngabenInstitutionContainer();
		}
		return lastenausgleichTagesschuleAngabenInstitutionContainerToEntity(
			jaxInstitutionContainer,
			institutionContainerToMergeWith,
			false
		);
	}

	@Nonnull
	public LastenausgleichTagesschuleAngabenInstitutionContainer lastenausgleichTagesschuleAngabenInstitutionContainerToEntity(
		@Nonnull JaxLastenausgleichTagesschuleAngabenInstitutionContainer jaxInstitutionContainer,
		@Nonnull LastenausgleichTagesschuleAngabenInstitutionContainer institutionContainer,
		boolean performOptimisticLockCheck
	) {

		requireNonNull(jaxInstitutionContainer.getInstitution().getId());
		convertAbstractFieldsToJAX(
			institutionContainer,
			jaxInstitutionContainer
		);

		// status darf nicht vom client übernommen werden

		// Die Institution darf nie vom Client uebernommen werden
		institutionService.findInstitution(
			jaxInstitutionContainer.getInstitution().getId(),
			false
		)
			.ifPresent(institutionContainer::setInstitution);

		if (jaxInstitutionContainer.getAngabenDeklaration() != null) {
			if (institutionContainer.getAngabenDeklaration() != null) {
				institutionContainer.setAngabenDeklaration(
					lastenausgleichTagesschuleAngabenInstitutionToEntity(
						jaxInstitutionContainer.getAngabenDeklaration(),
						institutionContainer.getAngabenDeklaration(),
						performOptimisticLockCheck
					)
				);
			} else {
				institutionContainer.setAngabenDeklaration(
					new LastenausgleichTagesschuleAngabenInstitution()
				);
			}
		}
		if (jaxInstitutionContainer.getAngabenKorrektur() != null) {
			if (institutionContainer.getAngabenKorrektur() != null) {
				institutionContainer.setAngabenKorrektur(
					lastenausgleichTagesschuleAngabenInstitutionToEntity(
						jaxInstitutionContainer.getAngabenKorrektur(),
						institutionContainer.getAngabenKorrektur(),
						performOptimisticLockCheck
					)
				);
			} else {
				institutionContainer.setAngabenKorrektur(
					new LastenausgleichTagesschuleAngabenInstitution()
				);
			}
		}
		return institutionContainer;
	}

	@Nonnull
	private JaxLastenausgleichTagesschuleAngabenInstitution lastenausgleichTagesschuleAngabenInstitutionToJax(
		@Nonnull final LastenausgleichTagesschuleAngabenInstitution angabenInstitution
	) {
		// OptimisticLocking: Version richtig behandeln
		flush();

		JaxLastenausgleichTagesschuleAngabenInstitution jaxAngabenInstitution =
			new JaxLastenausgleichTagesschuleAngabenInstitution();
		convertAbstractFieldsToJAX(angabenInstitution, jaxAngabenInstitution);

		// A: Informationen zur Tagesschule
		jaxAngabenInstitution.setIsLehrbetrieb(
			angabenInstitution.getIsLehrbetrieb()
		);
		// B: Quantitative Angaben
		jaxAngabenInstitution.setAnzahlEingeschriebeneKinder(
			angabenInstitution.getAnzahlEingeschriebeneKinder()
		);
		jaxAngabenInstitution.setAnzahlEingeschriebeneKinderKindergarten(
			angabenInstitution.getAnzahlEingeschriebeneKinderKindergarten()
		);
		jaxAngabenInstitution.setAnzahlEingeschriebeneKinderSekundarstufe(
			angabenInstitution.getAnzahlEingeschriebeneKinderSekundarstufe()
		);
		jaxAngabenInstitution.setAnzahlEingeschriebeneKinderPrimarstufe(
			angabenInstitution.getAnzahlEingeschriebeneKinderPrimarstufe()
		);
		jaxAngabenInstitution
			.setAnzahlEingeschriebeneKinderMitBesonderenBeduerfnissen(
				angabenInstitution
					.getAnzahlEingeschriebeneKinderMitBesonderenBeduerfnissen()
			);
		jaxAngabenInstitution.setAnzahlEingeschriebeneKinderVolksschulangebot(
			angabenInstitution.getAnzahlEingeschriebeneKinderVolksschulangebot()
		);
		jaxAngabenInstitution.setAnzahlEingeschriebeneKinderBasisstufe(
			angabenInstitution.getAnzahlEingeschriebeneKinderBasisstufe()
		);
		jaxAngabenInstitution.setDurchschnittKinderProTagFruehbetreuung(
			angabenInstitution.getDurchschnittKinderProTagFruehbetreuung()
		);
		jaxAngabenInstitution.setDurchschnittKinderProTagMittag(
			angabenInstitution.getDurchschnittKinderProTagMittag()
		);
		jaxAngabenInstitution.setDurchschnittKinderProTagNachmittag1(
			angabenInstitution.getDurchschnittKinderProTagNachmittag1()
		);
		jaxAngabenInstitution.setDurchschnittKinderProTagNachmittag2(
			angabenInstitution.getDurchschnittKinderProTagNachmittag2()
		);
		jaxAngabenInstitution
			.setBetreuungsstundenEinschliesslichBesondereBeduerfnisse(
				angabenInstitution
					.getBetreuungsstundenEinschliesslichBesondereBeduerfnisse()
			);
		// C: Qualitative Vorgaben der Tagesschuleverordnung
		jaxAngabenInstitution.setSchuleAufBasisOrganisatorischesKonzept(
			angabenInstitution.getSchuleAufBasisOrganisatorischesKonzept()
		);
		jaxAngabenInstitution.setSchuleAufBasisPaedagogischesKonzept(
			angabenInstitution.getSchuleAufBasisPaedagogischesKonzept()
		);
		jaxAngabenInstitution.setRaeumlicheVoraussetzungenEingehalten(
			angabenInstitution.getRaeumlicheVoraussetzungenEingehalten()
		);
		jaxAngabenInstitution.setBetreuungsverhaeltnisEingehalten(
			angabenInstitution.getBetreuungsverhaeltnisEingehalten()
		);
		jaxAngabenInstitution.setErnaehrungsGrundsaetzeEingehalten(
			angabenInstitution.getErnaehrungsGrundsaetzeEingehalten()
		);
		// Bemerkungen
		jaxAngabenInstitution.setBemerkungen(
			angabenInstitution.getBemerkungen()
		);

		// Oeffnungszeiten
		jaxAngabenInstitution.setOeffnungszeiten(
			angabenInstitution.getOeffnungszeiten() != null
				&& angabenInstitution.getOeffnungszeiten().length() > 2 ?
					new ArrayList<>(
						Arrays.asList(
							convert(angabenInstitution.getOeffnungszeiten())
						)
					) :
					new ArrayList<>()
		);

		return jaxAngabenInstitution;
	}

	@Nonnull
	private static OeffnungszeitenTagesschuleDTO[] convert(
		@Nonnull String oeffnungszeiten
	) {
		try {
			return EbeguUtil.convertOeffnungszeiten(oeffnungszeiten);
		} catch (JsonProcessingException e) {
			LOGGER.warn(
				"Problem converting Oeffnungszeiten: {}",
				e.getMessage()
			);
			return new OeffnungszeitenTagesschuleDTO[] {};
		}
	}

	@Nonnull
	private LastenausgleichTagesschuleAngabenInstitution lastenausgleichTagesschuleAngabenInstitutionToEntity(
		@Nonnull JaxLastenausgleichTagesschuleAngabenInstitution jaxAngabenInstitution,
		@Nonnull LastenausgleichTagesschuleAngabenInstitution angabenInstitution,
		boolean performOptimisticLockCheck
	) {
		if (performOptimisticLockCheck
			&& angabenInstitution.getVersion()
				!= jaxAngabenInstitution.getVersion()) {
			throw new WebApplicationException(
				new StaleObjectStateException(
					"Die LastenausgleichTagesschuleAngabenInstitution Versionen stimmen nicht",
					angabenInstitution.getId()
				),
				Status.CONFLICT
			);
		}

		convertAbstractFieldsToEntity(
			jaxAngabenInstitution,
			angabenInstitution
		);

		// A: Informationen zur Tagesschule
		angabenInstitution.setIsLehrbetrieb(
			jaxAngabenInstitution.getIsLehrbetrieb()
		);
		// B: Quantitative Angaben
		angabenInstitution.setAnzahlEingeschriebeneKinder(
			jaxAngabenInstitution.getAnzahlEingeschriebeneKinder()
		);
		angabenInstitution.setAnzahlEingeschriebeneKinderKindergarten(
			jaxAngabenInstitution.getAnzahlEingeschriebeneKinderKindergarten()
		);
		angabenInstitution.setAnzahlEingeschriebeneKinderSekundarstufe(
			jaxAngabenInstitution.getAnzahlEingeschriebeneKinderSekundarstufe()
		);
		angabenInstitution.setAnzahlEingeschriebeneKinderPrimarstufe(
			jaxAngabenInstitution.getAnzahlEingeschriebeneKinderPrimarstufe()
		);
		angabenInstitution
			.setAnzahlEingeschriebeneKinderMitBesonderenBeduerfnissen(
				jaxAngabenInstitution
					.getAnzahlEingeschriebeneKinderMitBesonderenBeduerfnissen()
			);
		angabenInstitution.setAnzahlEingeschriebeneKinderVolksschulangebot(
			jaxAngabenInstitution
				.getAnzahlEingeschriebeneKinderVolksschulangebot()
		);
		angabenInstitution.setAnzahlEingeschriebeneKinderBasisstufe(
			jaxAngabenInstitution.getAnzahlEingeschriebeneKinderBasisstufe()
		);
		angabenInstitution.setDurchschnittKinderProTagFruehbetreuung(
			jaxAngabenInstitution.getDurchschnittKinderProTagFruehbetreuung()
		);
		angabenInstitution.setDurchschnittKinderProTagMittag(
			jaxAngabenInstitution.getDurchschnittKinderProTagMittag()
		);
		angabenInstitution.setDurchschnittKinderProTagNachmittag1(
			jaxAngabenInstitution.getDurchschnittKinderProTagNachmittag1()
		);
		angabenInstitution.setDurchschnittKinderProTagNachmittag2(
			jaxAngabenInstitution.getDurchschnittKinderProTagNachmittag2()
		);
		angabenInstitution
			.setBetreuungsstundenEinschliesslichBesondereBeduerfnisse(
				jaxAngabenInstitution
					.getBetreuungsstundenEinschliesslichBesondereBeduerfnisse()
			);
		// C: Qualitative Vorgaben der Tagesschuleverordnung
		angabenInstitution.setSchuleAufBasisOrganisatorischesKonzept(
			jaxAngabenInstitution.getSchuleAufBasisOrganisatorischesKonzept()
		);
		angabenInstitution.setSchuleAufBasisPaedagogischesKonzept(
			jaxAngabenInstitution.getSchuleAufBasisPaedagogischesKonzept()
		);
		angabenInstitution.setRaeumlicheVoraussetzungenEingehalten(
			jaxAngabenInstitution.getRaeumlicheVoraussetzungenEingehalten()
		);
		angabenInstitution.setBetreuungsverhaeltnisEingehalten(
			jaxAngabenInstitution.getBetreuungsverhaeltnisEingehalten()
		);
		angabenInstitution.setErnaehrungsGrundsaetzeEingehalten(
			jaxAngabenInstitution.getErnaehrungsGrundsaetzeEingehalten()
		);
		// Bemerkungen
		angabenInstitution.setBemerkungen(
			jaxAngabenInstitution.getBemerkungen()
		);
		// Oeffnungszeiten
		angabenInstitution.setOeffnungszeiten(
			toOeffnungszeiten(jaxAngabenInstitution.getOeffnungszeiten())
				.toString()
		);

		return angabenInstitution;
	}

	@Nonnull
	private ArrayNode toOeffnungszeiten(
		@Nullable List<OeffnungszeitenTagesschuleDTO> oeffnungszeitenTagesschuleDTOS
	) {
		ObjectMapper mapper = new ObjectMapper();
		if (oeffnungszeitenTagesschuleDTOS == null) {
			return mapper.createArrayNode();
		}

		List<ObjectNode> mapped = oeffnungszeitenTagesschuleDTOS.stream()
			.map(this::toOeffnungszeit)
			.collect(Collectors.toList());

		return mapper.createArrayNode()
			.addAll(mapped);
	}

	@Nonnull
	private ObjectNode toOeffnungszeit(
		@Nonnull OeffnungszeitenTagesschuleDTO oeffnungszeitenTagesschuleDTO
	) {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.createObjectNode()
			.put("type", oeffnungszeitenTagesschuleDTO.getType().name())
			.put("montag", oeffnungszeitenTagesschuleDTO.isMontag())
			.put("dienstag", oeffnungszeitenTagesschuleDTO.isDienstag())
			.put("mittwoch", oeffnungszeitenTagesschuleDTO.isMittwoch())
			.put("donnerstag", oeffnungszeitenTagesschuleDTO.isDonnerstag())
			.put("freitag", oeffnungszeitenTagesschuleDTO.isFreitag());
	}

}
