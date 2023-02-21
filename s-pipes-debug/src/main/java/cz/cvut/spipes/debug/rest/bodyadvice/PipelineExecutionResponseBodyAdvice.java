package cz.cvut.spipes.debug.rest.bodyadvice;

import static cz.cvut.spipes.debug.util.TypeUtils.getListElementType;

import java.util.List;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import cz.cvut.spipes.debug.dto.PipelineExecutionDto;
import cz.cvut.spipes.debug.service.RelatedResourceService;

@ControllerAdvice
public class PipelineExecutionResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final RelatedResourceService relatedResourceService;

    public PipelineExecutionResponseBodyAdvice(RelatedResourceService relatedResourceService) {
        this.relatedResourceService = relatedResourceService;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return List.class.isAssignableFrom(returnType.getParameterType())
                && PipelineExecutionDto.class.isAssignableFrom(getListElementType(returnType))
                || PipelineExecutionDto.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof List && PipelineExecutionDto.class.isAssignableFrom(getListElementType(returnType))) {
            List<PipelineExecutionDto> pipelineExecutionDtos = (List<PipelineExecutionDto>) body;
            for (PipelineExecutionDto pipelineExecutionDto : pipelineExecutionDtos) {
                relatedResourceService.addPipelineExecutionResources(pipelineExecutionDto);
            }
        } else if (body instanceof PipelineExecutionDto) {
            PipelineExecutionDto pipelineExecutionDto = (PipelineExecutionDto) body;
            relatedResourceService.addPipelineExecutionResources(pipelineExecutionDto);
        }
        return body;
    }

}