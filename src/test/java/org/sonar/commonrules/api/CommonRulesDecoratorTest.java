/*
 * SonarQube Common Rules
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
package org.sonar.commonrules.api;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.*;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;
import org.sonar.commonrules.internal.CommonRulesConstants;
import org.sonar.commonrules.internal.checks.ViolationCostMatcher;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

public final class CommonRulesDecoratorTest {

  private static final String REPO_KEY = CommonRulesConstants.REPO_KEY_PREFIX + "java";

  Resource resource = mock(Resource.class);
  DecoratorContext context = mock(DecoratorContext.class);
  ProjectFileSystem fs = mock(ProjectFileSystem.class);
  RulesProfile profile = RulesProfile.create("My SQALE profile", "java");
  CommonRulesDecorator decorator = new CommonRulesDecorator("java", fs, profile) {
  };


  @Test
  public void test_metadata() throws Exception {
    assertThat(decorator.toString()).isEqualTo("Common Rules for java");
    assertThat(decorator.language()).isEqualTo("java");
  }

  @Test
  public void verifyDependsUponMetrics() throws Exception {
    List<Metric> metrics = decorator.dependsUponMetrics();
    assertThat(metrics.size()).isEqualTo(2);
    assertThat(metrics).containsOnly(CoreMetrics.LINE_COVERAGE, CoreMetrics.COMMENT_LINES_DENSITY);
  }

  @Test
  public void do_not_execute_if_no_source_files() {
    assertThat(decorator.shouldExecuteOnProject(null)).isFalse();
  }

  @Test
  public void do_execute_if_source_files_and_active_rules() {
    when(fs.mainFiles("java")).thenReturn(Lists.newArrayList(mock(InputFile.class)));
    Rule duplicatedBlocksRule = Rule.create(REPO_KEY, CommonRulesRepository.RULE_DUPLICATED_BLOCKS, null);
    profile.activateRule(duplicatedBlocksRule, RulePriority.MAJOR);
    Rule lineCoverageRule = Rule.create(REPO_KEY, CommonRulesRepository.RULE_INSUFFICIENT_LINE_COVERAGE, null);
    profile.activateRule(lineCoverageRule, RulePriority.MAJOR);

    assertThat(decorator.shouldExecuteOnProject(null)).isTrue();
  }

  @Test
  public void do_execute_if_test_files_and_active_rules() {
    when(fs.testFiles("java")).thenReturn(Lists.newArrayList(mock(InputFile.class)));
    Rule duplicatedBlocksRule = Rule.create(REPO_KEY, CommonRulesRepository.RULE_DUPLICATED_BLOCKS, null);
    profile.activateRule(duplicatedBlocksRule, RulePriority.MAJOR);
    Rule lineCoverageRule = Rule.create(REPO_KEY, CommonRulesRepository.RULE_INSUFFICIENT_LINE_COVERAGE, null);
    profile.activateRule(lineCoverageRule, RulePriority.MAJOR);

    assertThat(decorator.shouldExecuteOnProject(null)).isTrue();
  }

  @Test
  public void do_not_execute_if_no_active_rules() {
    when(fs.mainFiles("java")).thenReturn(Lists.newArrayList(mock(InputFile.class)));
    // Q profile is empty
    assertThat(decorator.shouldExecuteOnProject(null)).isFalse();
  }

  @Test
  public void create_violation() {
    when(fs.mainFiles("java")).thenReturn(Lists.newArrayList(mock(InputFile.class)));
    when(resource.getScope()).thenReturn(Resource.SCOPE_ENTITY);
    when(resource.getLanguage()).thenReturn(Java.INSTANCE);
    when(context.getMeasure(CoreMetrics.DUPLICATED_BLOCKS)).thenReturn(new Measure(CoreMetrics.DUPLICATED_BLOCKS, 2.0));

    Rule duplicatedBlocksRule = Rule.create(REPO_KEY, CommonRulesRepository.RULE_DUPLICATED_BLOCKS, null);
    profile.activateRule(duplicatedBlocksRule, RulePriority.MAJOR);

    // ugly, this method initializes the decorator
    decorator.shouldExecuteOnProject(null);
    decorator.decorate(resource, context);

    verify(context, times(1)).saveViolation(argThat(new ViolationCostMatcher(2)));
  }

  @Test
  public void do_not_decorate_other_languages() {
    when(fs.mainFiles("java")).thenReturn(Lists.newArrayList(mock(InputFile.class)));
    when(resource.getScope()).thenReturn(Resource.SCOPE_ENTITY);
    when(resource.getLanguage()).thenReturn(new Php());
    when(context.getMeasure(CoreMetrics.DUPLICATED_BLOCKS)).thenReturn(new Measure(CoreMetrics.DUPLICATED_BLOCKS, 2.0));

    Rule duplicatedBlocksRule = Rule.create(REPO_KEY, CommonRulesRepository.RULE_DUPLICATED_BLOCKS, null);
    profile.activateRule(duplicatedBlocksRule, RulePriority.MAJOR);

    // ugly, this method initializes the decorator
    decorator.shouldExecuteOnProject(null);
    decorator.decorate(resource, context);

    verifyZeroInteractions(context);
  }

  @Test
  public void do_not_decorate_directories() {
    when(fs.mainFiles("java")).thenReturn(Lists.newArrayList(mock(InputFile.class)));
    when(resource.getScope()).thenReturn(Resource.SCOPE_SPACE);
    when(resource.getLanguage()).thenReturn(Java.INSTANCE);
    when(context.getMeasure(CoreMetrics.DUPLICATED_BLOCKS)).thenReturn(new Measure(CoreMetrics.DUPLICATED_BLOCKS, 2.0));

    Rule duplicatedBlocksRule = Rule.create(REPO_KEY, CommonRulesRepository.RULE_DUPLICATED_BLOCKS, null);
    profile.activateRule(duplicatedBlocksRule, RulePriority.MAJOR);

    // ugly, this method initializes the decorator
    decorator.shouldExecuteOnProject(null);
    decorator.decorate(resource, context);

    verifyZeroInteractions(context);
  }

  @Test
  public void do_not_decorate_if_missing_file_language() {
    when(fs.mainFiles("java")).thenReturn(Lists.newArrayList(mock(InputFile.class)));
    when(resource.getScope()).thenReturn(Resource.SCOPE_ENTITY);
    when(resource.getLanguage()).thenReturn(null);
    when(context.getMeasure(CoreMetrics.DUPLICATED_BLOCKS)).thenReturn(new Measure(CoreMetrics.DUPLICATED_BLOCKS, 2.0));

    Rule duplicatedBlocksRule = Rule.create(REPO_KEY, CommonRulesRepository.RULE_DUPLICATED_BLOCKS, null);
    profile.activateRule(duplicatedBlocksRule, RulePriority.MAJOR);

    // ugly, this method initializes the decorator
    decorator.shouldExecuteOnProject(null);
    decorator.decorate(resource, context);

    verifyZeroInteractions(context);
  }

  static class Php implements Language {
    @Override
    public String getKey() {
      return "php";
    }

    @Override
    public String getName() {
      return "PHP";
    }

    @Override
    public String[] getFileSuffixes() {
      return new String[0];
    }
  }
}
