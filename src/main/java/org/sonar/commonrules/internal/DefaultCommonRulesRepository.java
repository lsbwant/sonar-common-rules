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

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionAnnotationLoader;
import org.sonar.commonrules.api.CommonRulesRepository;
import org.sonar.commonrules.internal.checks.BranchCoverageCheck;
import org.sonar.commonrules.internal.checks.CommentDensityCheck;
import org.sonar.commonrules.internal.checks.DuplicatedBlocksCheck;
import org.sonar.commonrules.internal.checks.FailedUnitTestsCheck;
import org.sonar.commonrules.internal.checks.LineCoverageCheck;
import org.sonar.commonrules.internal.checks.SkippedUnitTestsCheck;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultCommonRulesRepository implements RulesDefinition, CommonRulesRepository {

  private List<Class> enabledChecks = new ArrayList<Class>();
  private String language;
  private Double minimumBranchCoverageRatio;
  private Double minimumLineCoverageRatio;
  private Double minimumCommentDensity;

  public DefaultCommonRulesRepository(String language) {
    this.language = language;
  }

  @Override
  public void define(Context context) {
    NewRepository repo = context.createRepository(keyForLanguage(language), language)
      .setName("Common SonarQube");
    new RulesDefinitionAnnotationLoader().load(repo, enabledChecks.toArray(new Class[0]));
    NewRule insufBranchCoverage = repo.rule(RULE_INSUFFICIENT_BRANCH_COVERAGE);
    if (insufBranchCoverage != null && minimumBranchCoverageRatio != null) {
      insufBranchCoverage.param(PARAM_MIN_BRANCH_COVERAGE).setDefaultValue("" + minimumBranchCoverageRatio);
    }
    NewRule insufLineCoverage = repo.rule(RULE_INSUFFICIENT_LINE_COVERAGE);
    if (insufLineCoverage != null && minimumLineCoverageRatio != null) {
      insufLineCoverage.param(PARAM_MIN_LINE_COVERAGE).setDefaultValue("" + minimumLineCoverageRatio);
    }
    NewRule insufCommentDensity = repo.rule(RULE_INSUFFICIENT_COMMENT_DENSITY);
    if (insufCommentDensity != null && minimumCommentDensity != null) {
      insufCommentDensity.param(PARAM_MIN_COMMENT_DENSITY).setDefaultValue("" + minimumCommentDensity);
    }
    repo.done();
  }

  public static String keyForLanguage(String language) {
    return CommonRulesConstants.REPO_KEY_PREFIX + language;
  }

  @Override
  public DefaultCommonRulesRepository enableInsufficientBranchCoverageRule(@Nullable Double minimumBranchCoverageRatio) {
    this.minimumBranchCoverageRatio = minimumBranchCoverageRatio;
    enabledChecks.add(BranchCoverageCheck.class);
    return this;
  }

  @Override
  public DefaultCommonRulesRepository enableInsufficientLineCoverageRule(@Nullable Double minimumLineCoverageRatio) {
    this.minimumLineCoverageRatio = minimumLineCoverageRatio;
    enabledChecks.add(LineCoverageCheck.class);
    return this;
  }

  @Override
  public DefaultCommonRulesRepository enableInsufficientCommentDensityRule(@Nullable Double minimumCommentDensity) {
    this.minimumCommentDensity = minimumCommentDensity;
    enabledChecks.add(CommentDensityCheck.class);
    return this;
  }

  @Override
  public DefaultCommonRulesRepository enableDuplicatedBlocksRule() {
    enabledChecks.add(DuplicatedBlocksCheck.class);
    return this;
  }

  @Override
  public DefaultCommonRulesRepository enableSkippedUnitTestsRule() {
    enabledChecks.add(SkippedUnitTestsCheck.class);
    return this;
  }

  @Override
  public DefaultCommonRulesRepository enableFailedUnitTestsRule() {
    enabledChecks.add(FailedUnitTestsCheck.class);
    return this;
  }

  private Map<String, String> toMap(String key, @Nullable Double value) {
    Map<String, String> map = new HashMap<String, String>();
    if (value != null) {
      map.put(key, String.valueOf(value));
    }
    return map;
  }
}
