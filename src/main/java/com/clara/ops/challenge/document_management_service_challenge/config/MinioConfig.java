package com.clara.ops.challenge.document_management_service_challenge.config;

import io.minio.MinioClient;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.springframework.aot.hint.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

/** Provides configuration for MinIO client integration. */
@Configuration
@RequiredArgsConstructor
@ImportRuntimeHints(MinioConfig.MinioRuntimeHintsRegistrar.class)
public class MinioConfig {

  /**
   * Creates and configures a MinIO client using provided properties.
   *
   * @param properties MinIO connection properties
   * @return configured MinioClient instance
   */
  @Bean
  public MinioClient minioClient(MinioProperties properties) {
    return MinioClient.builder()
        .endpoint(properties.getUrl())
        .credentials(properties.getAccessKey(), properties.getSecretKey())
        .build();
  }

  /** Registers runtime hints for reflection and serialization of MinIO-related classes. */
  public static class MinioRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

    List<String> packagesToScan = List.of("io.minio", "org.simpleframework.xml.core");

    private final BindingReflectionHintsRegistrar bindingReflectionHintsRegistrar =
        new BindingReflectionHintsRegistrar();

    @Override
    public void registerHints(RuntimeHints hint, ClassLoader classLoader) {
      for (String packageName : packagesToScan) {
        registerPackage(hint, packageName);
      }
    }

    /**
     * Scans a package and registers reflection and serialization hints for its types.
     *
     * @param hint runtime hints container
     * @param packageName package to scan
     */
    private void registerPackage(RuntimeHints hint, String packageName) {
      Reflections reflections =
          new Reflections(packageName, Scanners.SubTypes.filterResultsBy(s -> true));
      Set<Class<?>> allTypes = reflections.getSubTypesOf(Object.class);
      allTypes.forEach(
          type -> bindingReflectionHintsRegistrar.registerReflectionHints(hint.reflection(), type));
      reflections.getSubTypesOf(Serializable.class).stream()
          .filter(it -> it.getCanonicalName() != null)
          .forEach(type -> hint.serialization().registerType(type));
    }
  }
}
