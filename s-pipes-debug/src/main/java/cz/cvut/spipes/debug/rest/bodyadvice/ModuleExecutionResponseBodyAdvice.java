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

import cz.cvut.spipes.debug.dto.ModuleExecutionDto;
import cz.cvut.spipes.debug.service.RelatedResourceService;

@ControllerAdvice
public class ModuleExecutionResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final RelatedResourceService relatedResourceService;

    public ModuleExecutionResponseBodyAdvice(RelatedResourceService relatedResourceService) {
        this.relatedResourceService = relatedResourceService;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return List.class.isAssignableFrom(returnType.getParameterType())
                && ModuleExecutionDto.class.isAssignableFrom(getListElementType(returnType))
                || ModuleExecutionDto.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof List && ModuleExecutionDto.class.isAssignableFrom(getListElementType(returnType))) {
            List<ModuleExecutionDto> moduleExecutionDtos = (List<ModuleExecutionDto>) body;
            for (ModuleExecutionDto moduleExecutionDto : moduleExecutionDtos) {
                relatedResourceService.addModuleExecutionResources(moduleExecutionDto);
            }
        } else if (body instanceof ModuleExecutionDto) {
            ModuleExecutionDto moduleExecutionDto = (ModuleExecutionDto) body;
            relatedResourceService.addModuleExecutionResources(moduleExecutionDto);
        }
        return body;
    }
}
