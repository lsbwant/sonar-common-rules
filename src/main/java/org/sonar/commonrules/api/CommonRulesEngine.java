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

import org.sonar.api.BatchExtension;
import org.sonar.api.ExtensionProvider;
import org.sonar.api.ServerExtension;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.commonrules.internal.CommonChecksDecorator;
import org.sonar.commonrules.internal.DefaultCommonRulesRepository;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * This class should be extended by any plugin that wants to use some Common Rules.
 * </p>
 * <p>
 * <b>Important:</b> both constructors must be inherited in order to have a fully working extension on both server
 * and batch side
 * <p>
 * <p>
 * See JavaCommonRulesEngineProvider in the sonar-java-plugin to have an example of how this works.
 * </p>
 */
public abstract class CommonRulesEngine extends ExtensionProvider implements ServerExtension, BatchExtension {

  private final String language;

  public CommonRulesEngine(String language) {
    this.language = language;
  }

  protected abstract void doEnableRules(CommonRulesRepository repository);

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("rawtypes")
  @Override
  public List provide() {
    return Arrays.asList(LanguageDecorator.class, newRepository());
  }

  public CommonRulesRepository newRepository() {
    CommonRulesRepository repository = new DefaultCommonRulesRepository(language);
    doEnableRules(repository);
    return repository;
  }

  public class LanguageDecorator extends CommonChecksDecorator {
    public LanguageDecorator(ProjectFileSystem fs, RulesProfile qProfile) {
      super(fs, qProfile, language);
    }
  }
}
