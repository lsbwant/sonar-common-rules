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
package org.sonar.commonrules.internal.checks;

import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.MeasureUtils;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.ResourceUtils;
import org.sonar.api.rules.Violation;
import org.sonar.check.Priority;
import org.sonar.check.Rule;

@Rule(
  key = "SkippedUnitTests",
  name = "Skipped unit tests",
  priority = Priority.MAJOR,
  description = "<p>Skipped unit tests are considered as dead code. " +
    "Either they should be activated again (and updated) or they should be removed.</p>")
public class SkippedUnitTestsCheck extends CommonCheck {

  @SuppressWarnings("rawtypes")
  @Override
  public void checkResource(Resource resource, DecoratorContext context, org.sonar.api.rules.Rule rule) {
    double skippedTests = MeasureUtils.getValue(context.getMeasure(CoreMetrics.SKIPPED_TESTS), 0.0);
    if (ResourceUtils.isUnitTestClass(resource) && skippedTests > 0) {
      Violation violation = Violation.create(rule, resource).setCost(skippedTests);
      violation.setMessage("Some tests are skipped. You should activate them or remove them.");
      context.saveViolation(violation);
    }
  }

}
