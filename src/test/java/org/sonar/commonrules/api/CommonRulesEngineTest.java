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
package org.sonar.commonrules.api;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.server.rule.RuleDefinitions;
import org.sonar.commonrules.internal.CommonRulesConstants;
import org.sonar.commonrules.internal.CommonRulesRepository;
import org.sonar.commonrules.internal.checks.CommentDensityCheck;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public final class CommonRulesEngineTest {

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
    RuleDefinitions.Context context = new RuleDefinitions.Context();
    commonRulesRepository.define(context);
    assertThat(context.repository("common-fake")).isNotNull();
    assertThat(context.repository("common-fake").rules()).hasSize(0);
  }

  @Test
  public void shouldReturnActivatedRules() throws Exception {
    engine.activateCommentDensityCheck().withParameter("minimumCommentDensity", "80");
    List<CommonRule> declaredRules = engine.getDeclaredCheck();
    assertThat(declaredRules.size()).isEqualTo(1);
    CommonRule rule = declaredRules.get(0);
    assertThat(rule.getCheckClass()).isEqualTo(CommentDensityCheck.class);
    assertThat(rule.getOverridenDefaultParams().get("minimumCommentDensity")).isEqualTo("80");
  }

  @Test
  public void shouldEnableAllCheck() throws Exception {
    engine.activateCommentDensityCheck();
    engine.activateBranchCoverageCheck();
    engine.activateDuplicatedBlocksCheck();
    engine.activateFailedUnitTestsCheck();
    engine.activateLineCoverageCheck();
    engine.activateSkippedUnitTestsCheck();
    assertThat(Collections2.transform(engine.getDeclaredCheck(), new Function<CommonRule, Class>() {
      public Class apply(CommonRule input) {
        return input.getCheckClass();
      }
    })).containsOnly(CommonRulesConstants.CLASSES.toArray());
  }

}
