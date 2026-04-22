/* (C) 2024 */
package in.nha.abdm.fhir.mapper.rest.database.h2.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.nha.abdm.fhir.mapper.rest.common.constants.LogMessageConstants;
import in.nha.abdm.fhir.mapper.rest.database.h2.tables.*;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
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
        log.warn(LogMessageConstants.SKIPPING_INVALID_RESOURCE, fileName);
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

        log.info(LogMessageConstants.LOADED_RECORDS, entities.size(), entityName);

        addIndexesTransactional(entityName);
      } else {
        log.info(LogMessageConstants.NO_REPOSITORY_FOUND, entityName);
      }
    } catch (Exception e) {
      log.error(LogMessageConstants.ERROR_PROCESSING_RESOURCE, resource.getFilename(), e);
      throw new RuntimeException(e);
    }
  }

  private TypeReference<?> getTypeReference(String entityName) {
    Map<String, TypeReference<?>> typeMap =
        Map.of(
            "SnomedConditionProcedure", new TypeReference<List<SnomedConditionProcedure>>() {},
            "SnomedDiagnostic", new TypeReference<List<SnomedDiagnostic>>() {},
            "SnomedEncounter", new TypeReference<List<SnomedEncounter>>() {},
            "SnomedMedicineRoute", new TypeReference<List<SnomedMedicineRoute>>() {},
            "SnomedMedicine", new TypeReference<List<SnomedMedicine>>() {},
            "SnomedObservation", new TypeReference<List<SnomedObservation>>() {},
            "SnomedSpecimen", new TypeReference<List<SnomedSpecimen>>() {},
            "SnomedVaccine", new TypeReference<List<SnomedVaccine>>() {},
            "TypeChargeItem", new TypeReference<List<TypeChargeItem>>() {},
            "TypeInvoice", new TypeReference<List<TypeChargeItem>>() {});
    TypeReference<?> typeRef = typeMap.get(entityName);
    if (typeRef == null) {
      throw new IllegalArgumentException("Unknown entity name: " + entityName);
    }
    return typeRef;
  }

  @Transactional
  public void addIndexesTransactional(String entityName) {
    String tableName = "\"" + convertToSnakeCase(entityName) + "\"";
    String indexName = "idx_" + convertToSnakeCase(entityName) + "_display_code";
    String sql = "CREATE INDEX " + indexName + " ON " + tableName + " (\"code\", \"display\");";

    EntityManager em = applicationContext.getBean(EntityManager.class);
    em.createNativeQuery(sql).executeUpdate();
  }

  private String convertToSnakeCase(String className) {
    return className.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
  }
}
