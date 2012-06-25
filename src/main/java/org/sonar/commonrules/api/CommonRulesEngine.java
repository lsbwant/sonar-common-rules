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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.sonar.api.resources.Language;
import org.sonar.api.rules.AnnotationRuleParser;
import org.sonar.api.rules.Rule;
import org.sonar.commonrules.internal.CommonChecksDecorator;
import org.sonar.commonrules.internal.CommonRulesConstants;
import org.sonar.commonrules.internal.CommonRulesRepository;

import java.util.List;
import java.util.Map;

public class CommonRulesEngine {

  private Language language;
  private Map<String, Rule> availableRulesbyKey = Maps.newHashMap();
  private List<CommonRule> commonRules = Lists.newArrayList();

  public CommonRulesEngine(Language language) {
    Preconditions.checkNotNull(language, "The language can't be null.");

    this.language = language;
    List<Rule> availableRules = new AnnotationRuleParser().parse(CommonRulesConstants.REPO_KEY_PREFIX + language.getKey(), CommonRulesConstants.CLASSES);
    for (Rule rule : availableRules) {
      availableRulesbyKey.put(rule.getKey(), rule);
    }
  }

  public CommonRule activateRule(String ruleKey) {
    Preconditions.checkNotNull(ruleKey, "The rule key can't be null.");

    Rule rule = availableRulesbyKey.get(ruleKey);
    if (rule == null) {
      throw new IllegalStateException("Sonar common rule '" + ruleKey + "' does not exist.");
    }
    CommonRule commonRule = new CommonRule(rule);
    commonRules.add(commonRule);
    return commonRule;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public List<?> getExtensions() {
    List extensions = Lists.newArrayList();

    // the rule repository created based on the configured rules
    extensions.add(new CommonRulesRepository(language, getDeclaredRules()));

    // and the decorator which runs the checks
    extensions.add(CommonChecksDecorator.class);

    return extensions;
  }

  @VisibleForTesting
  List<Rule> getDeclaredRules() {
    List<Rule> declaredRules = Lists.newArrayList();
    for (CommonRule commonRule : commonRules) {
      declaredRules.add(commonRule.getRule());
    }
    return declaredRules;
  }
}
