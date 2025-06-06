package cz.cvut.spipes.rest.util;

import cz.cvut.spipes.util.RDFMimeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(responseCode = "200", description = "RDF data",
        content = {
                @Content(mediaType = RDFMimeType.LD_JSON_STRING, schema = @Schema(hidden = true)),
                @Content(mediaType = RDFMimeType.N_TRIPLES_STRING, schema = @Schema(hidden = true)),
                @Content(mediaType = RDFMimeType.RDF_XML_STRING, schema = @Schema(hidden = true)),
                @Content(mediaType = RDFMimeType.TURTLE_STRING, schema = @Schema(hidden = true))
        }
)
public @interface RdfApiResponse {
}