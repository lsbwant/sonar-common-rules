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

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.sonar.api.rules.Violation;


public class ViolationCostMatcher extends BaseMatcher<Violation> {
  
  private final double expectedCost;
  
  public ViolationCostMatcher(double expectedCost){
    this.expectedCost = expectedCost;
  }

  public boolean matches(Object violation) {
    double cost = ((Violation)violation).getCost();
    return cost == expectedCost;
  }

  public void describeTo(Description desc) {
    desc.appendText("Cost == " + expectedCost);
  }

}
