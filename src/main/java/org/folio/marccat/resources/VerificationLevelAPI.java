package org.folio.marccat.resources;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.folio.marccat.Global;
import org.folio.marccat.ModMarccat;
import org.folio.marccat.business.codetable.Avp;
import org.folio.marccat.resources.domain.VerificationLevel;
import org.folio.marccat.resources.domain.VerificationLevelCollection;
import org.springframework.web.bind.annotation.*;

import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.folio.marccat.integration.CatalogingHelper.doGet;

/**
 * Verification Levels RESTful APIs.
 *
 * @author natasciab
 * @since 1.0
 */
@RestController
@Api(value = "modcat-api", description = "Verification level resource API")
@RequestMapping(value = ModMarccat.BASE_URI, produces = "application/json")
public class VerificationLevelAPI extends BaseResource {

  private Function <Avp <String>, VerificationLevel> toVerificationLevel = source -> {
    final VerificationLevel verificationLevel = new VerificationLevel();
    verificationLevel.setCode(source.getValue());
    verificationLevel.setDescription(source.getLabel());
    return verificationLevel;
  };

  @ApiOperation(value = "Returns all levels associated with a given language")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Method successfully returned the requested levels."),
    @ApiResponse(code = 400, message = "Bad Request"),
    @ApiResponse(code = 414, message = "Request-URI Too Long"),
    @ApiResponse(code = 500, message = "System internal failure occurred.")
  })
  @GetMapping("/verification-levels")
  public VerificationLevelCollection getVerificationLevels(
    @RequestParam final String lang,
    @RequestHeader(Global.OKAPI_TENANT_HEADER_NAME) final String tenant) {
    return doGet((storageService, configuration) -> {
      final VerificationLevelCollection container = new VerificationLevelCollection();
      container.setVerificationLevels(
        storageService.getVerificationLevels(lang)
          .stream()
          .map(toVerificationLevel)
          .collect(toList()));
      return container;
    }, tenant, configurator);
  }
}