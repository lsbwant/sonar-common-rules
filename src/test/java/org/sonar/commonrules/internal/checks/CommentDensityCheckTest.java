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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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

public class CommentDensityCheckTest {

  private CommentDensityCheck check;
  private Resource resource;
  private DecoratorContext context;

  private ResourcePerspectives perspectives;
  private Issuable issuable;
  private ModuleRule rule;
  private IssueBuilder issueBuilder;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void before() {
    check = new CommentDensityCheck();
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
  public void checkShouldNotGenerateViolationOnFileWithGoodCommentDensity() {
    when(resource.getScope()).thenReturn(Scopes.FILE);
    when(context.getMeasure(CoreMetrics.COMMENT_LINES_DENSITY)).thenReturn(new Measure(CoreMetrics.COMMENT_LINES_DENSITY, 30.0));

    check.checkResource(resource, context, perspectives, rule);

    verify(issuable, times(0)).addIssue(any(Issue.class));
  }

  @Test
  public void checkShouldNotGenerateViolationOnFileWithoutCommentDensity() {
    when(resource.getScope()).thenReturn(Scopes.FILE);
    when(context.getMeasure(CoreMetrics.COMMENT_LINES_DENSITY)).thenReturn(null);

    check.checkResource(resource, context, perspectives, rule);

    verify(issuable, times(0)).addIssue(any(Issue.class));
  }

  @Test
  public void checkShoulGenerateViolationOnFileWithBadCommentDensity() {
    check.setMinimumCommentDensity(20);
    when(resource.getScope()).thenReturn(Scopes.FILE);
    when(context.getMeasure(CoreMetrics.COMMENT_LINES_DENSITY)).thenReturn(new Measure(CoreMetrics.COMMENT_LINES_DENSITY, 16.6));
    when(context.getMeasure(CoreMetrics.NCLOC)).thenReturn(new Measure(CoreMetrics.NCLOC, 100.0));
    when(context.getMeasure(CoreMetrics.COMMENT_LINES)).thenReturn(new Measure(CoreMetrics.COMMENT_LINES, 20.0));

    check.checkResource(resource, context, perspectives, rule);

    verify(issueBuilder, times(1)).effortToFix(5d);
    verify(issuable, times(1)).addIssue(any(Issue.class));
  }

  /**
   * SQALE-110
   */
  @Test
  public void shouldFailIfMinimumCommentDensitySetTo100() throws Exception {
    check.setMinimumCommentDensity(100);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("100.0 is not a valid value for minimum required comment density for rule 'CommentDensityCheck' (must be >= 0 and < 100).");

    check.checkResource(resource, context, perspectives, rule);
  }

  /**
   * SQALE-110
   */
  @Test
  public void shouldFailIfMinimumCommentDensitySetToNegative() throws Exception {
    check.setMinimumCommentDensity(-5);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("-5.0 is not a valid value for minimum required comment density for rule 'CommentDensityCheck' (must be >= 0 and < 100).");

    check.checkResource(resource, context, perspectives, rule);
  }

}
