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

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.rules.Rule;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public final class CommonRulesRepositoryTest {

  private List<Rule> rules;

  private CommonRulesRepository repository;

  @Before
  public void init() throws Exception {
    rules = Lists.newArrayList(Rule.create("common-fake", "FakeRule"));

    repository = new CommonRulesRepository("fake", "Fake", rules);
  }

  @Test
  public void shouldHaveCorrectDefinition() throws Exception {
    assertThat(repository.getKey()).isEqualTo(CommonRulesConstants.REPO_KEY_PREFIX + "fake");
    assertThat(repository.getName()).isEqualTo("Common Fake");
    assertThat(repository.getLanguage()).isEqualTo("fake");
  }

  @Test
  public void shouldCreateRules() throws Exception {
    assertThat(repository.createRules()).isEqualTo(rules);
  }

}
