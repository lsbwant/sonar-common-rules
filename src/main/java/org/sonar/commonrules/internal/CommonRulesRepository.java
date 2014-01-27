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

import org.sonar.api.server.rule.RuleDefinitions;
import org.sonar.commonrules.api.CommonRule;

import java.util.List;
import java.util.Map;

public class CommonRulesRepository implements RuleDefinitions {

  private List<CommonRule> rules;
  private String languageKey;

  public CommonRulesRepository(String languageKey, List<CommonRule> rules) {
    this.languageKey = languageKey;
    this.rules = rules;
  }

  public void define(Context context) {
    NewRepository repo = context.newRepository(CommonRulesConstants.REPO_KEY_PREFIX + languageKey, languageKey)
      .setName("Common SonarQube");
    for (CommonRule commonRule : rules) {
      NewRule newRule = repo.loadAnnotatedClass(commonRule.getCheckClass());
      for (Map.Entry<String, String> overridenParam : commonRule.getOverridenDefaultParams().entrySet()) {
        NewParam param = newRule.param(overridenParam.getKey());
        if (param == null) {
          throw new IllegalStateException("Parameter '" + overridenParam.getKey() + "' on rule '" + newRule.key() + "' does not exist.");
        }
        param.setDefaultValue(overridenParam.getValue());
      }
    }
    repo.done();
  }

}
