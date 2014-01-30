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

import com.google.common.base.Preconditions;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleParam;

/**
 * Sonar Common Rule that will be activated, and on which parameter default values can be specified.
 */
public class CommonRule {

  private Rule rule;

  CommonRule(Rule rule) {
    this.rule = rule;
  }

  Rule getRule() {
    return rule;
  }

  /**
   * Specifies the default value for the given rule parameter.
   * 
   * @param paramKey the key of the parameter
   * @param paramValue the default value for this parameter
   * @return the Sonar Common Rule
   */
  public CommonRule withParameter(String paramKey, String paramValue) {
    Preconditions.checkNotNull(paramKey, "The parameter key can't be null.");
    Preconditions.checkNotNull(paramValue, "The parameter value can't be null.");

    RuleParam param = rule.getParam(paramKey);
    if (param == null) {
      throw new IllegalStateException("Parameter '" + paramKey + "' on rule '" + rule.getKey() + "' does not exist.");
    }
    param.setDefaultValue(paramValue);
    return this;
  }

}
