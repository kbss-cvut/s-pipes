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

import cz.cvut.spipes.debug.model.ModuleExecution;
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
                && ModuleExecution.class.isAssignableFrom(getListElementType(returnType))
                || ModuleExecution.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof List && ModuleExecution.class.isAssignableFrom(getListElementType(returnType))) {
            List<ModuleExecution> moduleExecutions = (List<ModuleExecution>) body;
            for (ModuleExecution moduleExecution : moduleExecutions) {
                relatedResourceService.addModuleExecutionResources(moduleExecution);
            }
        } else if (body instanceof ModuleExecution) {
            ModuleExecution moduleExecution = (ModuleExecution) body;
            relatedResourceService.addModuleExecutionResources(moduleExecution);
        }
        return body;
    }
}
