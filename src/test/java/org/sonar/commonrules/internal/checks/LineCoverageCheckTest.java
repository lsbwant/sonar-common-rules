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
package org.sonar.commonrules.internal.checks;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.Violation;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LineCoverageCheckTest {

  private LineCoverageCheck check;
  private Resource resource;
  private DecoratorContext context;

  @Before
  public void before() {
    check = new LineCoverageCheck();
    resource = mock(Resource.class);
    context = mock(DecoratorContext.class);
  }

  @Test
  public void checkShouldNotGenerateViolationOnFileWithGoodLineCoverage() {
    when(resource.getScope()).thenReturn(Resource.SCOPE_ENTITY);
    when(context.getMeasure(CoreMetrics.LINE_COVERAGE)).thenReturn(new Measure(CoreMetrics.LINE_COVERAGE, 85.0));

    check.checkResource(resource, context, null);

    verify(context, times(0)).saveViolation(any(Violation.class));
  }

  @Test
  public void checkShouldNotGenerateViolationOnFileWithoutLineCoverage() {
    when(resource.getScope()).thenReturn(Resource.SCOPE_ENTITY);
    when(context.getMeasure(CoreMetrics.LINE_COVERAGE)).thenReturn(null);

    check.checkResource(resource, context, null);

    verify(context, times(0)).saveViolation(any(Violation.class));
  }

  @Test
  public void checkShoulGenerateViolationOnFileWithBadLineCoverage() {
    check.setMinimumLineCoverageRatio(60);
    when(resource.getScope()).thenReturn(Resource.SCOPE_ENTITY);
    when(context.getMeasure(CoreMetrics.LINE_COVERAGE)).thenReturn(new Measure(CoreMetrics.LINE_COVERAGE, 20.0));
    when(context.getMeasure(CoreMetrics.LINES_TO_COVER)).thenReturn(new Measure(CoreMetrics.LINES_TO_COVER, 100.0));
    when(context.getMeasure(CoreMetrics.UNCOVERED_LINES)).thenReturn(new Measure(CoreMetrics.UNCOVERED_LINES, 80.0));

    check.checkResource(resource, context, null);

    verify(context, times(1)).saveViolation(argThat(new ViolationCostMatcher(40)));
  }

}
