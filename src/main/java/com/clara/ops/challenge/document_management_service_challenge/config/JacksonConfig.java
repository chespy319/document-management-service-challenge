package com.clara.ops.challenge.document_management_service_challenge.config;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Provides custom Jackson configuration for serialization and deserialization. */
@Configuration
public class JacksonConfig {

  private final DateTimeFormatter dateTimeFormat =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");

  private final OffsetDateTimeSerializer offsetDateTimeSerializer =
      new OffsetDateTimeSerializer(
          OffsetDateTimeSerializer.INSTANCE, false, dateTimeFormat, JsonFormat.Shape.STRING);

  // ~ METHOD(S) ------------------------------------------------------------

  /**
   * Customizes the Jackson ObjectMapper with specific serialization settings.
   *
   * @return Jackson2ObjectMapperBuilderCustomizer instance
   */
  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
    return builder ->
        builder
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .timeZone(TimeZone.getDefault())
            .failOnUnknownProperties(false)
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .serializers(offsetDateTimeSerializer);
  }

  /** Deserializer that converts JSON arrays into trimmed string lists, removing empty values. */
  public static class TrimmedStringList extends JsonDeserializer<List<String>> {
    @Override
    public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      List<String> values = ctxt.getParser().getCodec().readValue(p, new TypeReference<>() {});
      return values == null
          ? null
          : values.stream().map(JacksonConfig::trimToNull).filter(Objects::nonNull).toList();
    }
  }

  /** Deserializer that converts JSON strings into trimmed values, returning null if empty. */
  public static class TrimmedString extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      var value = p.getValueAsString();
      return trimToNull(value);
    }
  }

  /**
   * Trims a string and returns null if the result is empty.
   *
   * @param str input string
   * @return trimmed string or null
   */
  private static String trimToNull(final String str) {
    final String ts = str == null ? null : str.trim();
    return ts == null || ts.isEmpty() ? null : ts;
  }
}
