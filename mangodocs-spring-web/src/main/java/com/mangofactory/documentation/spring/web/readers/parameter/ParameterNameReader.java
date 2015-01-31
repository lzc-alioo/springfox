package com.mangofactory.documentation.spring.web.readers.parameter;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.mangofactory.documentation.spi.DocumentationType;
import com.mangofactory.documentation.spi.service.ParameterBuilderPlugin;
import com.mangofactory.documentation.spi.service.contexts.ParameterContext;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.annotation.Annotation;
import java.util.List;

import static com.google.common.base.Strings.*;
import static com.google.common.collect.FluentIterable.*;
import static com.google.common.collect.Lists.*;
import static java.lang.String.*;

@Component
public class ParameterNameReader implements ParameterBuilderPlugin {

  @Override
  public void apply(ParameterContext context) {
    MethodParameter methodParameter = context.methodParameter();
    String name = findParameterNameFromAnnotations(methodParameter);
    String parameterName = methodParameter.getParameterName();
    if (isNullOrEmpty(name)) {
      name = isNullOrEmpty(parameterName) ? format("param%s", methodParameter.getParameterIndex()) : parameterName;
    }
    context.parameterBuilder().name(name);
  }

  @Override
  public boolean supports(DocumentationType delimiter) {
    return true;
  }

  private String findParameterNameFromAnnotations(MethodParameter methodParameter) {
    List<Annotation> methodAnnotations = newArrayList(methodParameter.getParameterAnnotations());
    return from(methodAnnotations)
            .filter(PathVariable.class).first().transform(pathVariableValue())
            .or(first(methodAnnotations, ModelAttribute.class).transform(modelAttributeValue()))
            .or(first(methodAnnotations, RequestParam.class).transform(requestParamValue()))
            .or(first(methodAnnotations, RequestHeader.class).transform(requestHeaderValue()))
            .orNull();
  }

  private <T> Optional<T> first(List<Annotation> methodAnnotations, Class<T> ofType) {
    return from(methodAnnotations).filter(ofType).first();
  }

  private Function<RequestHeader, String> requestHeaderValue() {
    return new Function<RequestHeader, String>() {
      @Override
      public String apply(RequestHeader input) {
        return input.value();
      }
    };
  }

  private Function<RequestParam, String> requestParamValue() {
    return new Function<RequestParam, String>() {
      @Override
      public String apply(RequestParam input) {
        return input.value();
      }
    };
  }

  private Function<ModelAttribute, String> modelAttributeValue() {
    return new Function<ModelAttribute, String>() {
      @Override
      public String apply(ModelAttribute input) {
        return input.value();
      }
    };
  }

  private Function<PathVariable, String> pathVariableValue() {
    return new Function<PathVariable, String>() {
      @Override
      public String apply(PathVariable input) {
        return input.value();
      }
    };
  }

}
