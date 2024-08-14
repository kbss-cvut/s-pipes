package cz.cvut.spipes.rest;

import cz.cvut.spipes.registry.StreamResourceRegistry;
import cz.cvut.spipes.rest.util.ResourceRegisterHelper;
import cz.cvut.spipes.util.RestUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@RestController
@EnableWebMvc
public class StreamResourceController {

    private final ResourceRegisterHelper resourceRegisterHelper;

    public StreamResourceController() {
        this.resourceRegisterHelper = new ResourceRegisterHelper();
    }

    @PostConstruct
    void init() {
//        String resourcesLocation = ServletUriComponentsBuilder
//                .fromCurrentContextPath().path("/resources")
//                .buildAndExpand("").toUriString();
//        StreamResourceRegistry.getInstance().registerResourcePrefix(resourcesLocation);

    }

    @RequestMapping(
            value = "/resources",
            method = RequestMethod.POST,
            consumes = {
                    MediaType.APPLICATION_XML_VALUE,
                    MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    "application/zip"
//                    MediaType.TEXT_XML_VALUE
            },
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<StreamResourceDTO> registerStreamResource(@RequestHeader(value = "Content-type") String contentType, InputStream body) {

        StreamResourceDTO res = resourceRegisterHelper.registerStreamResource(contentType, body);

        final HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/{id}", res.getId());
        return new ResponseEntity<StreamResourceDTO>(res, headers, HttpStatus.CREATED);
    }



    @RequestMapping(
            value = "/resources2",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<StreamResourceDTO> registerStreamResource2(@RequestHeader(value = "Content-type") String contentType, @RequestParam("file") MultipartFile file) throws IOException {
        InputStream fis = file.getInputStream();
        String body = IOUtils.toString(fis, StandardCharsets.UTF_8);
        IOUtils.closeQuietly(fis);

        StreamResourceDTO res = new StreamResourceDTO(
                UUID.randomUUID().toString(),
                StreamResourceRegistry.getInstance().getPERSISTENT_CONTEXT_PREFIX(),
                resourceRegisterHelper.getRegisteredResourceLocation()
        );

        log.info("Registering new stream resource with url {} " + res.getPersistentUri());
        StreamResourceRegistry.getInstance().registerResource(res.getId(), IOUtils.toByteArray(fis), contentType);
        //LOG.debug("Resource content : {}", body);

        final HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/{id}", res.getId());
        return new ResponseEntity<StreamResourceDTO>(res, headers, HttpStatus.CREATED);

    }


    /**
     * Receives uploaded E5X/E5F file and passes it to our E5X/E5F processing code.
     *
     * @param file The uploaded file
     * @return Created response with key to the imported report
     */
//    @RequestMapping(value = "/importE5", method = RequestMethod.POST)
//    public ResponseEntity<Void> importE5Report(@RequestParam("file") MultipartFile file) {
//        try {
//            final LogicalDocument result = reportService
//                    .importReportFromFile(file.getOriginalFilename(), file.getInputStream());
//            final HttpHeaders headers = RestUtils
//                    .createLocationHeaderFromContextPath("/reports/{key}", result.getKey());
//            return new ResponseEntity<>(headers, HttpStatus.CREATED);
//        } catch (IOException e) {
//            throw new BadRequestException("Unable to read the uploaded file.", e);
//        }
//    }


}
