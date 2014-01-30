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

import org.sonar.commonrules.internal.checks.BranchCoverageCheck;
import org.sonar.commonrules.internal.checks.CommentDensityCheck;
import org.sonar.commonrules.internal.checks.DuplicatedBlocksCheck;
import org.sonar.commonrules.internal.checks.FailedUnitTestsCheck;
import org.sonar.commonrules.internal.checks.LineCoverageCheck;
import org.sonar.commonrules.internal.checks.SkippedUnitTestsCheck;

import java.util.Arrays;
import java.util.List;

/**
 * Constants used in the Common Rules lib.
 */
public final class CommonRulesConstants {

  private CommonRulesConstants() {
  }

  /**
   * The prefix used to create the rule repository for a given language (using its key).
   * For instance : "common-java".
   */
  public static final String REPO_KEY_PREFIX = "common-";

  /**
   * List of existing checks.
   */
  @SuppressWarnings("rawtypes")
  public static final List<Class> CLASSES = Arrays.<Class> asList(
      DuplicatedBlocksCheck.class,
      LineCoverageCheck.class,
      BranchCoverageCheck.class,
      CommentDensityCheck.class,
      SkippedUnitTestsCheck.class,
      FailedUnitTestsCheck.class);

}
