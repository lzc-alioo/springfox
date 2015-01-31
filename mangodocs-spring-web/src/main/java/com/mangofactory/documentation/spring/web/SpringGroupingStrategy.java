package com.mangofactory.documentation.spring.web;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.mangofactory.documentation.service.model.ResourceGroup;
import com.mangofactory.documentation.spi.DocumentationType;
import com.mangofactory.documentation.spi.service.ResourceGroupingStrategy;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.util.Set;

import static com.google.common.base.Strings.*;
import static com.google.common.collect.Sets.*;
import static com.mangofactory.documentation.spring.web.Paths.*;
import static java.util.Arrays.*;

/**
 * TODO - fix or remove
 * There are a lot of cases this strategy does not cover.
 * - request mappings of varying path depths,
 * - Paths beginning with path variables
 * - Controllers without top level request mappings
 */
public class SpringGroupingStrategy implements ResourceGroupingStrategy {
  @Override
  public Set<ResourceGroup> getResourceGroups(RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod) {
    return groups(handlerMethod);
  }

  @Override
  public String getResourceDescription(RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod) {
    return getDescription(handlerMethod);
  }

  @Override
  public Integer getResourcePosition(RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod) {
    return 0;
  }

  @Override
  public boolean supports(DocumentationType delimiter) {
    return true;
  }

  private Set<ResourceGroup> groups(HandlerMethod handlerMethod) {
    Class<?> controllerClass = handlerMethod.getBeanType();
    String defaultGroup = String.format("%s", splitCamelCase(controllerClass.getSimpleName(), "-"));

    Optional<RequestMapping> requestMapping
            = Optional.fromNullable(AnnotationUtils.findAnnotation(controllerClass, RequestMapping.class));
    if (requestMapping.isPresent()) {
      Set<ResourceGroup> groups = newHashSet();
      Iterable<String> groupNames = FluentIterable.from(asList(requestMapping.get().value()))
              .filter(notNullOrEmpty());
      for (String each : groupNames) {
        String groupName = maybeChompLeadingSlash(firstPathSegment(each));
        groups.add(new ResourceGroup(groupName, handlerMethod.getBeanType()));
      }
      if (groups.size() > 0) {
        return groups;
      }
    }
    return newHashSet(new ResourceGroup(maybeChompLeadingSlash(defaultGroup.toLowerCase()),
            handlerMethod.getBeanType()));
  }

  private Predicate<String> notNullOrEmpty() {
    return new Predicate<String>() {
      @Override
      public boolean apply(String input) {
        return !isNullOrEmpty(input);
      }
    };
  }

  private String getDescription(HandlerMethod handlerMethod) {
    Class<?> controllerClass = handlerMethod.getBeanType();
    return splitCamelCase(controllerClass.getSimpleName(), " ");
  }
}
