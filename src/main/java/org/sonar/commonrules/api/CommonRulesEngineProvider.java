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

import org.sonar.api.BatchExtension;
import org.sonar.api.ExtensionProvider;
import org.sonar.api.ServerExtension;
import org.sonar.api.resources.Project;

import java.util.Collections;
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
public abstract class CommonRulesEngineProvider extends ExtensionProvider implements ServerExtension, BatchExtension {

  private Project project = null;

  /**
   * Constructor that will be called on server side
   */
  public CommonRulesEngineProvider() {
  }

  /**
   * Constructor that will be called on batch side
   */
  public CommonRulesEngineProvider(Project project) {
    this.project = project;
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("rawtypes")
  @Override
  public final List provide() {
    if (project == null || project.getLanguageKey().equals(getLanguageKey())) {
      CommonRulesEngine engine = new CommonRulesEngine(getLanguageKey());

      doActivation(engine);

      return engine.getExtensions();
    }
    return Collections.emptyList();
  }

  /**
   * Implement this method to activate rule (with optional parameters) on the rule engine.
   * 
   * @param engine the rule engine.
   */
  protected abstract void doActivation(CommonRulesEngine engine);

  /**
   * The key of the language
   * 
   * @return the language key
   */
  protected abstract String getLanguageKey();

}
