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

import org.sonar.api.batch.Decorator;
import org.sonar.api.batch.DecoratorBarriers;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.batch.DependedUpon;
import org.sonar.api.batch.DependsUpon;
import org.sonar.api.batch.ModuleLanguages;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.rule.ModuleRules;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.commonrules.internal.checks.CommonCheck;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DependedUpon(DecoratorBarriers.ISSUES_ADDED)
public class CommonChecksDecorator implements Decorator {

  private final ModuleRules moduleRules;
  private final ModuleLanguages languages;
  private final CheckFactory checkFactory;
  private final ResourcePerspectives resourcePerspectives;
  private Map<String, Checks<CommonCheck>> checksByLanguage;

  @DependsUpon
  public List<Metric> dependsUponMetrics() {
    return Arrays.asList(CoreMetrics.LINE_COVERAGE, CoreMetrics.COMMENT_LINES_DENSITY);
  }

  public CommonChecksDecorator(ModuleRules moduleRules, ModuleLanguages languages, CheckFactory checkFactory, ResourcePerspectives p) {
    this.moduleRules = moduleRules;
    this.languages = languages;
    this.checkFactory = checkFactory;
    this.resourcePerspectives = p;
  }

  public boolean shouldExecuteOnProject(Project project) {
    // IS there at least one common rule enabled for one of the languages of the project?
    for (String languageKey : languages.keys()) {
      if (!moduleRules.findByRepository(CommonRulesConstants.REPO_KEY_PREFIX + languageKey).isEmpty()) {
        return true;
      }
    }
    return false;
  }

  public void decorate(Resource resource, DecoratorContext context) {
    // Only files have a language
    if (resource.getLanguage() == null) {
      return;
    }
    String languageKey = resource.getLanguage().getKey();
    Checks<CommonCheck> checks = getChecksByLanguage(languageKey);
    for (CommonCheck check : checks.all()) {
      check.checkResource(resource, context, resourcePerspectives, moduleRules.find(checks.ruleKey(check)));
    }
  }

  public Checks<CommonCheck> getChecksByLanguage(String languageKey) {
    if (checksByLanguage == null) {
      checksByLanguage = new HashMap<String, Checks<CommonCheck>>();
      for (String langKey : languages.keys()) {
        Checks<CommonCheck> checks = checkFactory.<CommonCheck>create(CommonRulesConstants.REPO_KEY_PREFIX + langKey)
          .addAnnotatedChecks(CommonRulesConstants.CLASSES);
        checksByLanguage.put(langKey, checks);
      }
    }
    return checksByLanguage.get(languageKey);
  }

  @Override
  public String toString() {
    return "SonarQube common rules engine";
  }
}
