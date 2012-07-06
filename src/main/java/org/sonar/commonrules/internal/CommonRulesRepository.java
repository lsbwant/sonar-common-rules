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

import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleRepository;

import java.util.List;

public class CommonRulesRepository extends RuleRepository {

  private List<Rule> rules;

  public CommonRulesRepository(String languageKey, List<Rule> rules) {
    super(CommonRulesConstants.REPO_KEY_PREFIX + languageKey, languageKey);
    setName("Common Sonar");
    this.rules = rules;
  }

  @Override
  public List<Rule> createRules() {
    return rules;
  }

}
