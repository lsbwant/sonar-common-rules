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
package org.sonar.commonrules.api;

import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.containers.TransientPicoContainer;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.rules.Rule;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public final class CommonRulesEngineTest {

  static class JavaCommonRulesEngine extends CommonRulesEngine {
    public JavaCommonRulesEngine() {
      super("java");
    }

    @Override
    protected void doEnableRules(CommonRulesRepository repository) {
      repository.enableDuplicatedBlocksRule();

      // default value
      repository.enableInsufficientBranchCoverageRule(null);

      // override default value
      repository.enableInsufficientLineCoverageRule(82.0);
    }
  }


  @Test
  public void enable_rules() throws Exception {
    JavaCommonRulesEngine engine = new JavaCommonRulesEngine();
    CommonRulesRepository repo = engine.newRepository();

    assertThat(repo.rules()).hasSize(3);
    assertThat(repo.rule(CommonRulesRepository.RULE_DUPLICATED_BLOCKS)).isNotNull();
    assertThat(repo.rule(CommonRulesRepository.RULE_INSUFFICIENT_COMMENT_DENSITY)).isNull();

    // hardcoded default value
    Rule branchCoverage = repo.rule(CommonRulesRepository.RULE_INSUFFICIENT_BRANCH_COVERAGE);
    assertThat(branchCoverage).isNotNull();
    assertThat(Double.parseDouble(branchCoverage.getParam(CommonRulesRepository.PARAM_MIN_BRANCH_COVERAGE).getDefaultValue())).isEqualTo(65.0);

    Rule lineCoverage = repo.rule(CommonRulesRepository.RULE_INSUFFICIENT_LINE_COVERAGE);
    assertThat(lineCoverage).isNotNull();
    assertThat(Double.parseDouble(lineCoverage.getParam(CommonRulesRepository.PARAM_MIN_LINE_COVERAGE).getDefaultValue())).isEqualTo(82.0);
  }

  @Test
  public void provide_batch_decorator() throws Exception {
    JavaCommonRulesEngine engine = new JavaCommonRulesEngine();
    List extensions = engine.provide();

    assertThat(extensions).hasSize(2).contains(CommonRulesEngine.LanguageDecorator.class);

    TransientPicoContainer pico = new TransientPicoContainer();
    pico.as(Characteristics.CACHE).addComponent(engine);
    pico.as(Characteristics.CACHE).addComponent(mock(ProjectFileSystem.class));
    pico.as(Characteristics.CACHE).addComponent(mock(RulesProfile.class));
    for (Object extension : extensions) {
      pico.as(Characteristics.CACHE).addComponent(extension);
    }

    CommonRulesEngine.LanguageDecorator decorator = pico.getComponent(CommonRulesEngine.LanguageDecorator.class);
    assertThat(decorator.language()).isEqualTo("java");
    assertThat(decorator.toString()).isEqualTo("Commons Rules Decorator for java");
  }

  @Test
  public void provide_rule_definitions() throws Exception {
    JavaCommonRulesEngine engine = new JavaCommonRulesEngine();
    List extensions = engine.provide();

    assertThat(extensions.get(1)).isInstanceOf(CommonRulesRepository.class);
    CommonRulesRepository repo = (CommonRulesRepository) extensions.get(1);
    assertThat(repo.rules()).hasSize(3);
  }
}
