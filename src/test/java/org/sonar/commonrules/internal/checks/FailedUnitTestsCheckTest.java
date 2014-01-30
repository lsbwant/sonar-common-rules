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

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.Violation;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FailedUnitTestsCheckTest {

  private FailedUnitTestsCheck check;
  private Resource<?> resource;
  private DecoratorContext context;

  @Before
  public void before() {
    check = new FailedUnitTestsCheck();
    resource = mock(Resource.class);
    context = mock(DecoratorContext.class);
  }

  @Test
  public void checkShouldNotGenerateViolationIfNotTest() {
    when(resource.getQualifier()).thenReturn(Qualifiers.FILE);
    check.checkResource(resource, context, null);
    verify(context, times(0)).saveViolation(any(Violation.class));
  }

  @Test
  public void checkShouldNotGenerateViolationIfNoFailedTests() {
    when(resource.getQualifier()).thenReturn(Qualifiers.UNIT_TEST_FILE);

    // this test has no "test_errors" or "test_failures" measure
    check.checkResource(resource, context, null);
    verify(context, times(0)).saveViolation(any(Violation.class));

    // this is the case of a test file that has only successful tests
    when(context.getMeasure(CoreMetrics.TEST_ERRORS)).thenReturn(new Measure(CoreMetrics.TEST_ERRORS, 0.0));
    when(context.getMeasure(CoreMetrics.TEST_FAILURES)).thenReturn(new Measure(CoreMetrics.TEST_FAILURES, 0.0));
    check.checkResource(resource, context, null);
    verify(context, times(0)).saveViolation(any(Violation.class));
  }

  @Test
  public void checkShouldGenerateViolationOnFileIfTestFailures() {
    when(resource.getQualifier()).thenReturn(Qualifiers.UNIT_TEST_FILE);
    when(context.getMeasure(CoreMetrics.TEST_ERRORS)).thenReturn(new Measure(CoreMetrics.TEST_ERRORS, 2.0));
    when(context.getMeasure(CoreMetrics.TEST_FAILURES)).thenReturn(new Measure(CoreMetrics.TEST_FAILURES, 3.0));

    check.checkResource(resource, context, null);

    verify(context, times(1)).saveViolation(argThat(new ViolationCostMatcher(5)));
  }

}
