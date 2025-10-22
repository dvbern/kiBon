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

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

import ch.dvbern.ebegu.api.resource.util.EbeguSchemaOutputResolver;
import ch.dvbern.ebegu.dto.dataexport.v1.VerfuegungenExportDTO;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchemaGenerator;
import org.eclipse.microprofile.openapi.annotations.Operation;

/**
 * Resource for Exporting data
 */
@Path("export")
@Stateless
@PermitAll
public class ExportResource {

	@Operation(summary = "Exports a json Schema of the ExportDTOs")
	@Path("/meta/jsonschema")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJsonSchemaString() throws JsonMappingException {

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		// configure mapper, if necessary, then create schema generator
		JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
		JsonSchema schema = schemaGen.generateSchema(
			VerfuegungenExportDTO.class
		);
		return Response.ok(schema).build();

	}

	@Operation(summary = "Exports an xsd of the ExportDTOs")
	@Path("/meta/xsd")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getXmlSchemaString() throws JAXBException, IOException {
		JAXBContext jaxbContext = JAXBContext.newInstance(
			VerfuegungenExportDTO.class
		);
		EbeguSchemaOutputResolver sor = new EbeguSchemaOutputResolver();
		jaxbContext.generateSchema(sor);
		String schema = sor.getSchema();
		return Response.ok(schema).build();
	}
}
