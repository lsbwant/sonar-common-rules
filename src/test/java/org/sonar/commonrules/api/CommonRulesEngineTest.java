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

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.rules.Rule;
import org.sonar.commonrules.internal.CommonRulesConstants;
import org.sonar.commonrules.internal.CommonRulesRepository;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public final class CommonRulesEngineTest {

  @org.junit.Rule
  public ExpectedException thrown = ExpectedException.none();

  private CommonRulesEngine engine;

  @Before
  public void init() throws Exception {
    engine = new CommonRulesEngine("fake");
  }

  @Test
  public void shouldReturnTwoExtensionsWithNoCheckByDefault() throws Exception {
    List<?> extensions = engine.getExtensions();
    assertThat(extensions.size()).isEqualTo(2);

    CommonRulesRepository commonRulesRepository = (CommonRulesRepository) extensions.get(0);
    assertThat(commonRulesRepository.createRules().size()).isEqualTo(0);
  }

  @Test
  public void shouldReturnActivatedRules() throws Exception {
    engine.activateRule("InsufficientCommentDensity").withParameter("minimumCommentDensity", "80");
    List<Rule> declaredRules = engine.getDeclaredRules();
    assertThat(declaredRules.size()).isEqualTo(1);
    Rule rule = declaredRules.get(0);
    assertThat(rule.getKey()).isEqualTo("InsufficientCommentDensity");
    assertThat(rule.getRepositoryKey()).isEqualTo(CommonRulesConstants.REPO_KEY_PREFIX + "fake");
    assertThat(rule.getParam("minimumCommentDensity").getDefaultValue()).isEqualTo("80");
  }

  @Test
  public void shouldFailIfUnknowRule() throws Exception {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Sonar common rule 'Foo' does not exist.");

    engine.activateRule("Foo");
  }

  @Test
  public void shouldFailIfUnknowRuleParameter() throws Exception {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Parameter 'foo' on rule 'InsufficientCommentDensity' does not exist.");

    engine.activateRule("InsufficientCommentDensity").withParameter("foo", "80");
  }

}
