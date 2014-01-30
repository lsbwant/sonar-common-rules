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

import org.sonar.api.rules.AnnotationRuleParser;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleParam;
import org.sonar.api.rules.RuleRepository;
import org.sonar.commonrules.api.CommonRulesRepository;

import javax.annotation.Nullable;
import java.util.*;

public class DefaultCommonRulesRepository extends RuleRepository implements CommonRulesRepository {

  private final Map<String, Rule> supportedRulesByKey;
  private final List<Rule> rules = new ArrayList<Rule>();

  public DefaultCommonRulesRepository(String language) {
    super(keyForLanguage(language), language);
    setName("Common SonarQube");

    supportedRulesByKey = new HashMap<String, Rule>();
    List<Rule> supportedRules = new AnnotationRuleParser().parse(keyForLanguage(CommonRulesConstants.REPO_KEY_PREFIX), CommonRulesConstants.CLASSES);
    for (Rule supportedRule : supportedRules) {
      supportedRulesByKey.put(supportedRule.getKey(), supportedRule);
    }
  }

  public static String keyForLanguage(String language) {
    return CommonRulesConstants.REPO_KEY_PREFIX + language;
  }

  @Override
  public DefaultCommonRulesRepository enableInsufficientBranchCoverageRule(@Nullable Double minimumBranchCoverageRatio) {
    enableRule(RULE_INSUFFICIENT_BRANCH_COVERAGE, toMap(PARAM_MIN_BRANCH_COVERAGE, minimumBranchCoverageRatio));
    return this;
  }

  @Override
  public DefaultCommonRulesRepository enableInsufficientLineCoverageRule(@Nullable Double minimumLineCoverageRatio) {
    enableRule(RULE_INSUFFICIENT_LINE_COVERAGE, toMap(PARAM_MIN_LINE_COVERAGE, minimumLineCoverageRatio));
    return this;
  }

  @Override
  public DefaultCommonRulesRepository enableInsufficientCommentDensityRule(@Nullable Double minimumCommentDensity) {
    enableRule(RULE_INSUFFICIENT_COMMENT_DENSITY, toMap(PARAM_MIN_COMMENT_DENSITY, minimumCommentDensity));
    return this;
  }

  @Override
  public DefaultCommonRulesRepository enableDuplicatedBlocksRule() {
    enableRule(RULE_DUPLICATED_BLOCKS, Collections.<String, String>emptyMap());
    return this;
  }

  @Override
  public DefaultCommonRulesRepository enableSkippedUnitTestsRule() {
    enableRule(RULE_SKIPPED_UNIT_TESTS, Collections.<String, String>emptyMap());
    return this;
  }

  @Override
  public DefaultCommonRulesRepository enableFailedUnitTestsRule() {
    enableRule(RULE_FAILED_UNIT_TESTS, Collections.<String, String>emptyMap());
    return this;
  }

  private Map<String, String> toMap(String key, @Nullable Double value) {
    Map<String, String> map = new HashMap<String, String>();
    if (value != null) {
      map.put(key, String.valueOf(value));
    }
    return map;
  }

  DefaultCommonRulesRepository enableRule(String ruleKey, Map<String, String> params) {
    Rule rule = supportedRulesByKey.get(ruleKey);
    if (rule == null) {
      throw new IllegalStateException("Unknown rule: " + ruleKey);
    }
    for (Map.Entry<String, String> entry : params.entrySet()) {
      String paramKey = entry.getKey();
      RuleParam param = rule.getParam(paramKey);
      if (param == null) {
        throw new IllegalStateException(String.format("Rule '%s' has no parameter named '%s'", ruleKey, paramKey));
      }
      param.setDefaultValue(entry.getValue());
    }
    rules.add(rule);
    return this;
  }


  @Override
  public List<Rule> createRules() {
    return rules();
  }

  @Override
  public List<Rule> rules() {
    return rules;
  }

  @Nullable
  @Override
  public Rule rule(String ruleKey) {
    for (Rule rule : rules) {
      if (rule.getKey().equals(ruleKey)) {
        return rule;
      }
    }
    return null;
  }
}
