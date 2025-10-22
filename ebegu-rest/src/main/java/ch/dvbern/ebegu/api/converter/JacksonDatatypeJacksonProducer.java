package ch.dvbern.ebegu.api.converter;

import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Provider
public class JacksonDatatypeJacksonProducer implements
	ContextResolver<ObjectMapper> {

	@Override
	public ObjectMapper getContext(Class<?> objectType) {
		return JsonMapper.builder()
			.build()
			.registerModule(new Jdk8Module()) // to handle types like Optional<> correctly
			.registerModule(new JavaTimeModule())
			.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
	}
}
