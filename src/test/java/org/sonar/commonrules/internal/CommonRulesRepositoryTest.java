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

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.server.rule.RuleDefinitions;
import org.sonar.commonrules.api.CommonRule;
import org.sonar.commonrules.internal.checks.CommentDensityCheck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

import static org.mockito.Mockito.mock;
import static org.fest.assertions.Assertions.assertThat;

public final class CommonRulesRepositoryTest {

  @org.junit.Rule
  public ExpectedException thrown = ExpectedException.none();

  private List<CommonRule> rules;

  private CommonRulesRepository repository;

  @Before
  public void init() throws Exception {
    rules = new ArrayList<CommonRule>();

    repository = new CommonRulesRepository("fake", rules);
  }

  @Test
  public void shouldHaveCorrectDefinition() throws Exception {
    RuleDefinitions.Context context = new RuleDefinitions.Context();
    repository.define(context);
    assertThat(context.repository(CommonRulesConstants.REPO_KEY_PREFIX + "fake")).isNotNull();
    assertThat(context.repository(CommonRulesConstants.REPO_KEY_PREFIX + "fake").name()).isEqualTo("Common SonarQube");
    assertThat(context.repository(CommonRulesConstants.REPO_KEY_PREFIX + "fake").language()).isEqualTo("fake");
  }

  @Test
  public void shouldCreateRulesAndOverrideParameters() throws Exception {
    CommonRule rule1 = mock(CommonRule.class);
    when(rule1.getCheckClass()).thenReturn((Class) CommentDensityCheck.class);
    when(rule1.getOverridenDefaultParams()).thenReturn(ImmutableMap.of("minimumCommentDensity", "80"));
    rules.add(rule1);

    RuleDefinitions.Context context = new RuleDefinitions.Context();
    repository.define(context);

    org.sonar.api.server.rule.RuleDefinitions.Rule rule = context.repositories().get(0).rule("InsufficientCommentDensity");
    assertThat(rule.name()).isEqualTo("Insufficient comment density");
    assertThat(rule.param("minimumCommentDensity").defaultValue()).isEqualTo("80");
  }

  @Test
  public void shouldFailIfUnknowRuleParameter() throws Exception {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Parameter 'foo' on rule 'InsufficientCommentDensity' does not exist.");

    CommonRule rule = mock(CommonRule.class);
    when(rule.getCheckClass()).thenReturn((Class) CommentDensityCheck.class);
    when(rule.getOverridenDefaultParams()).thenReturn(ImmutableMap.of("foo", "80"));
    new CommonRulesRepository("fake", Arrays.asList(rule)).define(new RuleDefinitions.Context());
  }

}
