/*
 * Sonar Common Rules
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.commonrules.internal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.batch.ModuleLanguages;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.rule.ModuleRule;
import org.sonar.api.batch.rule.ModuleRules;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.AbstractLanguage;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.Scopes;
import org.sonar.api.rule.RuleKey;
import org.sonar.commonrules.internal.checks.CommonCheck;

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class CommonChecksDecoratorTest {

  @Mock
  Resource resource;

  @Mock
  DecoratorContext context;

  CommonChecksDecorator decorator;

  private ModuleLanguages languages;

  private ModuleRules moduleRules;

  private ModuleRule javaCommonRule1;
  private ModuleRule javaCommonRule2;
  private ModuleRule cobolCommonRule1;
  private CheckFactory checkFactory;

  private CommonCheck javaCommonCheck1;

  private CommonCheck javaCommonCheck2;

  private CommonCheck cobolCommonCheck1;

  private AbstractLanguage java;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);

    moduleRules = mock(ModuleRules.class);
    languages = mock(ModuleLanguages.class);
    checkFactory = mock(CheckFactory.class);

    javaCommonRule1 = mock(ModuleRule.class);
    javaCommonCheck1 = mock(CommonCheck.class);
    javaCommonRule2 = mock(ModuleRule.class);
    javaCommonCheck2 = mock(CommonCheck.class);
    cobolCommonRule1 = mock(ModuleRule.class);
    cobolCommonCheck1 = mock(CommonCheck.class);

    Checks javaChecks = mock(Checks.class);
    when(checkFactory.create("common-java")).thenReturn(javaChecks)
      // Checks construction should be done only once
      .thenThrow(new IllegalStateException());
    when(javaChecks.addAnnotatedChecks(CommonRulesConstants.CLASSES)).thenReturn(javaChecks);
    when(javaChecks.all()).thenReturn(Arrays.asList(javaCommonCheck1, javaCommonCheck2));

    when(javaChecks.ruleKey(javaCommonCheck1)).thenReturn(RuleKey.of("common-java", "rule1"));
    when(javaChecks.ruleKey(javaCommonCheck2)).thenReturn(RuleKey.of("common-java", "rule2"));
    when(moduleRules.find(RuleKey.of("common-java", "rule1"))).thenReturn(javaCommonRule1);
    when(moduleRules.find(RuleKey.of("common-java", "rule2"))).thenReturn(javaCommonRule2);
    when(moduleRules.find(RuleKey.of("common-cobol", "rule1"))).thenReturn(cobolCommonRule1);

    Checks cobolChecks = mock(Checks.class);
    when(checkFactory.create("common-cobol")).thenReturn(cobolChecks)
      // Checks construction should be done only once
      .thenThrow(new IllegalStateException());
    when(cobolChecks.addAnnotatedChecks(CommonRulesConstants.CLASSES)).thenReturn(cobolChecks);
    when(cobolChecks.all()).thenReturn(Arrays.asList(cobolCommonCheck1));
    when(cobolChecks.ruleKey(cobolCommonCheck1)).thenReturn(RuleKey.of("common-cobol", "rule1"));

    decorator = new CommonChecksDecorator(moduleRules, languages, checkFactory, null);

    java = new AbstractLanguage("java") {

      public String[] getFileSuffixes() {
        return null;
      }
    };
  }

  @Test
  public void testToString() throws Exception {
    assertThat(decorator.toString()).isEqualTo("SonarQube common rules engine");
  }

  @Test
  public void verifyDependsUponMetrics() throws Exception {
    List<Metric> metrics = decorator.dependsUponMetrics();
    assertThat(metrics.size()).isEqualTo(2);
    assertThat(metrics).containsOnly(CoreMetrics.LINE_COVERAGE, CoreMetrics.COMMENT_LINES_DENSITY);
  }

  @Test
  public void shouldExecuteOnProjectIfAtLeastOneActiveCommonRule() {
    when(languages.keys()).thenReturn(Arrays.asList("java", "cobol"));
    when(moduleRules.findByRepository("common-java")).thenReturn(Arrays.asList(javaCommonRule1));
    assertThat(decorator.shouldExecuteOnProject(null)).isTrue();
  }

  @Test
  public void shouldNotExecuteOnProjectWhenNoActiveCommonRule() {
    when(languages.keys()).thenReturn(Arrays.asList("java", "cobol"));
    assertThat(decorator.shouldExecuteOnProject(null)).isFalse();
  }

  @Test
  public void shouldNotExecuteCommonCheckWhenResourceHasNoLanguage() {
    when(languages.keys()).thenReturn(Arrays.asList("java", "cobol"));

    when(moduleRules.findByRepository("common-java")).thenReturn(Arrays.asList(javaCommonRule1, javaCommonRule2));
    when(moduleRules.findByRepository("common-cobol")).thenReturn(Arrays.asList(cobolCommonRule1));
    when(resource.getScope()).thenReturn(Scopes.FILE);
    when(resource.getLanguage()).thenReturn(null);

    decorator.decorate(resource, context);

    verify(javaCommonCheck1, never()).checkResource(any(Resource.class), any(DecoratorContext.class), any(ResourcePerspectives.class), any(ModuleRule.class));
    verify(javaCommonCheck2, never()).checkResource(any(Resource.class), any(DecoratorContext.class), any(ResourcePerspectives.class), any(ModuleRule.class));
    verify(cobolCommonCheck1, never()).checkResource(any(Resource.class), any(DecoratorContext.class), any(ResourcePerspectives.class), any(ModuleRule.class));

  }

  @Test
  public void shouldExecuteAllCommonCheckForLanguageOfTheResource() {
    when(languages.keys()).thenReturn(Arrays.asList("java", "cobol"));

    when(moduleRules.findByRepository("common-java")).thenReturn(Arrays.asList(javaCommonRule1, javaCommonRule2));
    when(moduleRules.findByRepository("common-cobol")).thenReturn(Arrays.asList(cobolCommonRule1));

    when(resource.getScope()).thenReturn(Scopes.FILE);
    when(resource.getLanguage()).thenReturn(java);

    decorator.decorate(resource, context);

    verify(javaCommonCheck1, times(1)).checkResource(eq(resource), any(DecoratorContext.class), any(ResourcePerspectives.class), any(ModuleRule.class));
    verify(javaCommonCheck2, times(1)).checkResource(eq(resource), any(DecoratorContext.class), any(ResourcePerspectives.class), any(ModuleRule.class));
    verify(cobolCommonCheck1, never()).checkResource(eq(resource), any(DecoratorContext.class), any(ResourcePerspectives.class), any(ModuleRule.class));

  }

  @Test
  public void shouldReuseChecks() {
    when(languages.keys()).thenReturn(Arrays.asList("java", "cobol"));

    when(moduleRules.findByRepository("common-java")).thenReturn(Arrays.asList(javaCommonRule1, javaCommonRule2));
    when(moduleRules.findByRepository("common-cobol")).thenReturn(Arrays.asList(cobolCommonRule1));

    when(resource.getScope()).thenReturn(Scopes.FILE);
    when(resource.getLanguage()).thenReturn(java);

    decorator.decorate(resource, context);

    Resource anotherJavaResource = mock(Resource.class);
    when(anotherJavaResource.getScope()).thenReturn(Scopes.FILE);
    when(anotherJavaResource.getLanguage()).thenReturn(java);

    decorator.decorate(anotherJavaResource, context);

    verify(javaCommonCheck1, times(2)).checkResource(any(Resource.class), any(DecoratorContext.class), any(ResourcePerspectives.class), any(ModuleRule.class));
    verify(javaCommonCheck2, times(2)).checkResource(any(Resource.class), any(DecoratorContext.class), any(ResourcePerspectives.class), any(ModuleRule.class));
    verify(cobolCommonCheck1, never()).checkResource(any(Resource.class), any(DecoratorContext.class), any(ResourcePerspectives.class), any(ModuleRule.class));

  }
}
