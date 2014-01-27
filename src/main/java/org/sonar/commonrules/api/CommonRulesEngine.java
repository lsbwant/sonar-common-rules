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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.sonar.commonrules.internal.CommonChecksDecorator;
import org.sonar.commonrules.internal.CommonRulesRepository;
import org.sonar.commonrules.internal.checks.BranchCoverageCheck;
import org.sonar.commonrules.internal.checks.CommentDensityCheck;
import org.sonar.commonrules.internal.checks.CommonCheck;
import org.sonar.commonrules.internal.checks.DuplicatedBlocksCheck;
import org.sonar.commonrules.internal.checks.FailedUnitTestsCheck;
import org.sonar.commonrules.internal.checks.LineCoverageCheck;
import org.sonar.commonrules.internal.checks.SkippedUnitTestsCheck;

import java.util.List;

/**
 * Sonar Common Rules Engine on which rules can be activated.
 */
public class CommonRulesEngine {

  private String languageKey;
  private List<CommonRule> activatedRules = Lists.newArrayList();

  CommonRulesEngine(String languageKey) {
    Preconditions.checkNotNull(languageKey, "The language key can't be null.");
    this.languageKey = languageKey;
  }

  public CommonRule activateBranchCoverageCheck() {
    return createCommonRule(BranchCoverageCheck.class);
  }

  public CommonRule activateCommentDensityCheck() {
    return createCommonRule(CommentDensityCheck.class);
  }

  public CommonRule activateDuplicatedBlocksCheck() {
    return createCommonRule(DuplicatedBlocksCheck.class);
  }

  public CommonRule activateFailedUnitTestsCheck() {
    return createCommonRule(FailedUnitTestsCheck.class);
  }

  public CommonRule activateLineCoverageCheck() {
    return createCommonRule(LineCoverageCheck.class);
  }

  public CommonRule activateSkippedUnitTestsCheck() {
    return createCommonRule(SkippedUnitTestsCheck.class);
  }

  private CommonRule createCommonRule(Class<? extends CommonCheck> clazz) {
    CommonRule commonRule = new CommonRule(clazz);
    activatedRules.add(commonRule);
    return commonRule;
  }

  @VisibleForTesting
  @SuppressWarnings({"rawtypes", "unchecked"})
  List<?> getExtensions() {
    List extensions = Lists.newArrayList();

    // the rule repository created based on the configured rules
    extensions.add(new CommonRulesRepository(languageKey, activatedRules));

    // and the decorator which runs the checks
    extensions.add(CommonChecksDecorator.class);

    return extensions;
  }

  public List<CommonRule> getDeclaredCheck() {
    return activatedRules;
  }

}
