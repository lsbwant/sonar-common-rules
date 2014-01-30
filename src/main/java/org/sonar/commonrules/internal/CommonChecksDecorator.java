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

import org.sonar.api.batch.*;
import org.sonar.api.checks.AnnotationCheckFactory;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.ResourceUtils;
import org.sonar.commonrules.internal.checks.CommonCheck;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@DependsUpon(DecoratorBarriers.START_VIOLATIONS_GENERATION)
@DependedUpon(DecoratorBarriers.END_OF_VIOLATIONS_GENERATION)
public abstract class CommonChecksDecorator implements Decorator {

  private final ProjectFileSystem fs;
  private final String language;
  private final RulesProfile qProfile;
  private AnnotationCheckFactory checkFactory;
  private Collection<CommonCheck> activeChecks = Collections.emptyList();

  public CommonChecksDecorator(ProjectFileSystem fs, RulesProfile qProfile, String language) {
    this.fs = fs;
    this.qProfile = qProfile;
    this.language = language;
  }

  public String language() {
    return language;
  }

  @DependsUpon
  public List<Metric> dependsUponMetrics() {
    return Arrays.asList(CoreMetrics.LINE_COVERAGE, CoreMetrics.COMMENT_LINES_DENSITY);
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    boolean hasLangFiles = !fs.mainFiles(language).isEmpty() || !fs.testFiles(language).isEmpty();
    if (hasLangFiles) {
      checkFactory = AnnotationCheckFactory.create(qProfile, DefaultCommonRulesRepository.keyForLanguage(language), CommonRulesConstants.CLASSES);
      activeChecks = checkFactory.getChecks();
    }
    return !activeChecks.isEmpty();
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public void decorate(Resource resource, DecoratorContext context) {
    // assume that all checks relate to files, not directories nor modules
    if (ResourceUtils.isEntity(resource) && resource.getLanguage() != null && resource.getLanguage().getKey().equals(language)) {
      for (CommonCheck check : activeChecks) {
        check.checkResource(resource, context, checkFactory.getActiveRule(check).getRule());
      }
    }
  }

  @Override
  public String toString() {
    return "SonarQube Common Rules for " + language;
  }
}
