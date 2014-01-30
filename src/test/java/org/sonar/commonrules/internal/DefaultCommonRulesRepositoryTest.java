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
package org.sonar.commonrules.internal;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.sonar.api.rules.Rule;
import org.sonar.commonrules.api.CommonRulesRepository;

import java.util.Collections;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

public final class DefaultCommonRulesRepositoryTest {

  DefaultCommonRulesRepository repository = new DefaultCommonRulesRepository("java");

  @Test
  public void validate_all_rules() {
    repository
      .enableFailedUnitTestsRule()
      .enableDuplicatedBlocksRule()
      .enableInsufficientCommentDensityRule(42.0)
      .enableInsufficientBranchCoverageRule(42.0)
      .enableInsufficientLineCoverageRule(42.0)
      .enableSkippedUnitTestsRule();
    assertThat(repository.rules()).hasSize(CommonRulesConstants.CLASSES.size());
  }

  @Test
  public void test_metadata() throws Exception {
    assertThat(repository.getKey()).isEqualTo(CommonRulesConstants.REPO_KEY_PREFIX + "java");
    assertThat(repository.getName()).isEqualTo("Common SonarQube");
    assertThat(repository.getLanguage()).isEqualTo("java");
  }

  @Test
  public void all_rules_are_disabled() {
    assertThat(repository.createRules()).isEmpty();
    assertThat(repository.rules()).isEmpty();
    assertThat(repository.rule("xxx")).isNull();
  }

  @Test
  public void enable_rule() {
    repository.enableDuplicatedBlocksRule();

    assertThat(repository.createRules()).hasSize(1);
    assertThat(repository.rules()).hasSize(1);
    assertThat(repository.rule(CommonRulesRepository.RULE_DUPLICATED_BLOCKS)).isNotNull();
  }

  @Test
  public void enable_rule_with_default_param_value() {
    repository.enableInsufficientCommentDensityRule(null);

    assertThat(repository.createRules()).hasSize(1);
    assertThat(repository.rules()).hasSize(1);
    Rule rule = repository.rule(CommonRulesRepository.RULE_INSUFFICIENT_COMMENT_DENSITY);
    assertThat(rule).isNotNull();
    assertThat(Double.parseDouble(rule.getParam(CommonRulesRepository.PARAM_MIN_COMMENT_DENSITY).getDefaultValue())).isEqualTo(25.0);
  }

  @Test
  public void override_default_param_value() {
    repository.enableInsufficientCommentDensityRule(42.0);

    assertThat(repository.createRules()).hasSize(1);
    assertThat(repository.rules()).hasSize(1);
    Rule rule = repository.rule(CommonRulesRepository.RULE_INSUFFICIENT_COMMENT_DENSITY);
    assertThat(rule).isNotNull();
    assertThat(Double.parseDouble(rule.getParam(CommonRulesRepository.PARAM_MIN_COMMENT_DENSITY).getDefaultValue())).isEqualTo(42.0);
  }

  @Test
  public void fail_if_rule_does_not_exist() {
    // typo
    try {
      repository.enableRule("xxx", Collections.<String, String>emptyMap());
      fail();
    } catch (IllegalStateException e) {
      assertThat(e).hasMessage("Unknown rule: xxx");
    }
  }

  @Test
  public void fail_if_rule_param_does_not_exist() {
    // typo
    try {
      repository.enableRule(CommonRulesRepository.RULE_INSUFFICIENT_LINE_COVERAGE, ImmutableMap.of("xxx", "yyy"));
      fail();
    } catch (IllegalStateException e) {
      assertThat(e).hasMessage("Rule 'InsufficientLineCoverage' has no parameter named 'xxx'");
    }
  }
}
