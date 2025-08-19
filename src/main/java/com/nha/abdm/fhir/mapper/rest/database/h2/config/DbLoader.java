/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.database.h2.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.*;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class DbLoader {
  private final ApplicationContext applicationContext;
  private final ObjectMapper dbMapper;
  private static final Logger log = LoggerFactory.getLogger(DbLoader.class);

  @Autowired private PlatformTransactionManager transactionManager;

  public DbLoader(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
    this.dbMapper = new ObjectMapper();
  }

  @PostConstruct
  public void loadData() throws IOException {
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    Resource[] resources = resolver.getResources("classpath:/snomed/*.json");

    if (resources == null || resources.length == 0) {
      throw new IllegalStateException(
          "No JSON files found in the 'snomed' folder on the classpath.");
    }

    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

    transactionTemplate.execute(
        status -> {
          try {
            for (Resource resource : resources) {
              processResource(resource);
            }
          } catch (Exception e) {
            status.setRollbackOnly();
            throw new RuntimeException("Error loading data", e);
          }
          return null;
        });
  }

  private void processResource(Resource resource) {
    try (InputStream inputStream = resource.getInputStream()) {
      String fileName = resource.getFilename();
      if (fileName == null || !fileName.endsWith(".json")) {
        log.warn("Skipping invalid resource: {}", fileName);
        return;
      }

      String entityName = fileName.replace(".json", "");
      String beanName = entityName.substring(0, 1).toLowerCase() + entityName.substring(1) + "Repo";

      if (applicationContext.containsBean(beanName)) {

        JpaRepository<Object, ?> repository =
            (JpaRepository<Object, ?>) applicationContext.getBean(beanName);

        List<Object> entities =
            (List<Object>) dbMapper.readValue(inputStream, getTypeReference(entityName));
        repository.saveAll(entities);

        log.info("Loaded {} records into {}", entities.size(), entityName);

        addIndexesTransactional(entityName);
      } else {
        log.info("No repository bean found for {}", entityName);
      }
    } catch (Exception e) {
      log.error("Error processing resource: {}", resource.getFilename(), e);
      throw new RuntimeException(e);
    }
  }

  private TypeReference<?> getTypeReference(String entityName) {
    return switch (entityName) {
      case "SnomedConditionProcedure" -> new TypeReference<List<SnomedConditionProcedure>>() {};
      case "SnomedDiagnostic" -> new TypeReference<List<SnomedDiagnostic>>() {};
      case "SnomedEncounter" -> new TypeReference<List<SnomedEncounter>>() {};
      case "SnomedMedicineRoute" -> new TypeReference<List<SnomedMedicineRoute>>() {};
      case "SnomedMedicine" -> new TypeReference<List<SnomedMedicine>>() {};
      case "SnomedObservation" -> new TypeReference<List<SnomedObservation>>() {};
      case "SnomedSpecimen" -> new TypeReference<List<SnomedSpecimen>>() {};
      case "SnomedVaccine" -> new TypeReference<List<SnomedVaccine>>() {};
      case "TypeChargeItem" -> new TypeReference<List<TypeChargeItem>>() {};
      case "TypeInvoice" -> new TypeReference<List<TypeChargeItem>>() {};
      default -> throw new IllegalArgumentException("Unknown entity name: " + entityName);
    };
  }

  @Transactional
  public void addIndexesTransactional(String entityName) {

    String tableName = "\"" + convertToSnakeCase(entityName) + "\"";
    String indexName = "idx_" + convertToSnakeCase(entityName) + "_display_code";
    String sql =
        switch (entityName) {
          case "SnomedEncounter" ->
              "CREATE INDEX " + indexName + " ON " + tableName + " (\"code\", \"display\");";
          case "SnomedDiagnostic" ->
              "CREATE INDEX " + indexName + " ON " + tableName + " (\"code\", \"display\");";
          case "SnomedConditionProcedure" ->
              "CREATE INDEX " + indexName + " ON " + tableName + " (\"code\", \"display\");";
          case "SnomedMedicineRoute" ->
              "CREATE INDEX " + indexName + " ON " + tableName + " (\"code\", \"display\");";
          case "SnomedMedicine" ->
              "CREATE INDEX " + indexName + " ON " + tableName + " (\"code\", \"display\");";
          case "SnomedObservation" ->
              "CREATE INDEX " + indexName + " ON " + tableName + " (\"code\", \"display\");";
          case "SnomedSpecimen" ->
              "CREATE INDEX " + indexName + " ON " + tableName + " (\"code\", \"display\");";
          case "SnomedVaccine" ->
              "CREATE INDEX " + indexName + " ON " + tableName + " (\"code\", \"display\");";
          case "TypeChargeItem" ->
              "CREATE INDEX " + indexName + " ON " + tableName + " (\"code\", \"display\");";
          case "TypeInvoice" ->
              "CREATE INDEX " + indexName + " ON " + tableName + " (\"code\", \"display\");";
          default -> null;
        };

    if (sql != null) {
      EntityManager em = applicationContext.getBean(EntityManager.class);
      em.createNativeQuery(sql).executeUpdate();
    }
  }

  private String convertToSnakeCase(String className) {
    return className.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
  }
}
