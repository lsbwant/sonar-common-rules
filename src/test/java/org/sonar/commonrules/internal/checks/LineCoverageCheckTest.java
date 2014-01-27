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
import org.sonar.api.batch.rule.ModuleRule;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issuable.IssueBuilder;
import org.sonar.api.issue.Issue;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.Scopes;
import org.sonar.api.rule.RuleKey;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LineCoverageCheckTest {

  private LineCoverageCheck check;
  private Resource resource;
  private DecoratorContext context;

  private ResourcePerspectives perspectives;
  private Issuable issuable;
  private ModuleRule rule;
  private IssueBuilder issueBuilder;

  @Before
  public void before() {
    check = new LineCoverageCheck();
    resource = mock(Resource.class);
    context = mock(DecoratorContext.class);

    perspectives = mock(ResourcePerspectives.class);
    issuable = mock(Issuable.class);
    issueBuilder = mock(Issuable.IssueBuilder.class);
    when(issueBuilder.ruleKey(any(RuleKey.class))).thenReturn(issueBuilder);
    when(issueBuilder.effortToFix(anyDouble())).thenReturn(issueBuilder);
    when(issueBuilder.message(anyString())).thenReturn(issueBuilder);
    when(issuable.newIssueBuilder()).thenReturn(issueBuilder);
    when(perspectives.as(Issuable.class, resource)).thenReturn(issuable);
    rule = mock(ModuleRule.class);
  }

  @Test
  public void checkShouldNotGenerateViolationOnFileWithGoodLineCoverage() {
    when(resource.getScope()).thenReturn(Scopes.FILE);
    when(context.getMeasure(CoreMetrics.LINE_COVERAGE)).thenReturn(new Measure(CoreMetrics.LINE_COVERAGE, 85.0));

    check.checkResource(resource, context, perspectives, rule);

    verify(issuable, times(0)).addIssue(any(Issue.class));
  }

  @Test
  public void checkShouldNotGenerateViolationOnFileWithoutLineCoverage() {
    when(resource.getScope()).thenReturn(Scopes.FILE);
    when(context.getMeasure(CoreMetrics.LINE_COVERAGE)).thenReturn(null);

    check.checkResource(resource, context, perspectives, rule);

    verify(issuable, times(0)).addIssue(any(Issue.class));
  }

  @Test
  public void checkShouldGenerateViolationOnFileWithBadLineCoverage() {
    check.setMinimumLineCoverageRatio(60);
    when(resource.getScope()).thenReturn(Scopes.FILE);
    when(context.getMeasure(CoreMetrics.LINE_COVERAGE)).thenReturn(new Measure(CoreMetrics.LINE_COVERAGE, 20.0));
    when(context.getMeasure(CoreMetrics.LINES_TO_COVER)).thenReturn(new Measure(CoreMetrics.LINES_TO_COVER, 100.0));
    when(context.getMeasure(CoreMetrics.UNCOVERED_LINES)).thenReturn(new Measure(CoreMetrics.UNCOVERED_LINES, 80.0));

    check.checkResource(resource, context, perspectives, rule);

    verify(issueBuilder, times(1)).effortToFix(40d);
    verify(issuable, times(1)).addIssue(any(Issue.class));
  }

}
