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
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;
import org.sonar.commonrules.internal.checks.ViolationCostMatcher;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class CommonChecksDecoratorTest {

  private static final String REPO_KEY = CommonRulesConstants.REPO_KEY_PREFIX + "fake";

  @Mock
  Resource resource;

  @Mock
  DecoratorContext context;

  CommonChecksDecorator decorator;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);

    RulesProfile profile = RulesProfile.create("My SQALE profile", "fake");
    Rule duplicatedBlocksRule = Rule.create(REPO_KEY, "DuplicatedBlocks", null);
    profile.activateRule(duplicatedBlocksRule, RulePriority.MAJOR);
    Rule lineCoverageRule = Rule.create(REPO_KEY, "InsufficientLineCoverage", null);
    profile.activateRule(lineCoverageRule, RulePriority.MAJOR);

    decorator = new CommonChecksDecorator(profile);
  }

  @Test
  public void testToString() throws Exception {
    assertThat(decorator.toString()).isEqualTo("Sonar common rules engine");
  }

  @Test
  public void verifyDependsUponMetrics() throws Exception {
    List<Metric> metrics = decorator.dependsUponMetrics();
    assertThat(metrics.size()).isEqualTo(2);
    assertThat(metrics).containsOnly(CoreMetrics.LINE_COVERAGE, CoreMetrics.COMMENT_LINES_DENSITY);
  }

  @Test
  public void shouldExecuteOnProject() {
    assertThat(decorator.shouldExecuteOnProject(null), is(true));
  }

  @Test
  public void shouldNotExecuteOnProject() {
    decorator = new CommonChecksDecorator(RulesProfile.create("My SQALE profile", "fake"));
    assertThat(decorator.shouldExecuteOnProject(null), is(false));
  }

  @Test
  public void shouldCreateViolationOnDuplicatedBlocksRule() {
    when(resource.getScope()).thenReturn(Resource.SCOPE_ENTITY);
    when(context.getMeasure(CoreMetrics.DUPLICATED_BLOCKS)).thenReturn(new Measure(CoreMetrics.DUPLICATED_BLOCKS, 2.0));

    decorator.decorate(resource, context);

    verify(context, times(1)).saveViolation(argThat(new ViolationCostMatcher(2)));
  }

}
