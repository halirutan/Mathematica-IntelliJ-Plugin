/*
 * Copyright (c) 2017 Patrick Scheibe
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 *
 */

package de.halirutan.mathematica.codeinsight.inspections.bugs;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import de.halirutan.mathematica.codeinsight.completion.util.SymbolVersionProvider;
import de.halirutan.mathematica.codeinsight.inspections.AbstractInspection;
import de.halirutan.mathematica.file.MathematicaFileType;
import de.halirutan.mathematica.lang.psi.MathematicaVisitor;
import de.halirutan.mathematica.lang.psi.api.FunctionCall;
import de.halirutan.mathematica.lang.psi.api.Symbol;
import de.halirutan.mathematica.lang.psi.api.lists.Association;
import de.halirutan.mathematica.lang.psi.impl.LightBuiltInSymbol;
import de.halirutan.mathematica.module.MathematicaLanguageLevelModuleExtensionImpl;
import de.halirutan.mathematica.module.MathematicaModuleType;
import de.halirutan.mathematica.sdk.MathematicaLanguageLevel;
import de.halirutan.mathematica.sdk.MathematicaSdkType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashMap;

import static de.halirutan.mathematica.codeinsight.inspections.InspectionBundle.message;

/**
 * Provides warnings when you are using Mathematica symbols that are introduces later than the version you are using.
 *
 * @author halirutan
 */
public class UnsupportedVersion extends AbstractInspection {

  @SuppressWarnings({"InstanceVariableNamingConvention", "WeakerAccess"})
  public MathematicaLanguageLevel languageLevel = MathematicaLanguageLevel.HIGHEST;

  @SuppressWarnings({"InstanceVariableNamingConvention", "WeakerAccess"})
  public boolean useSDKLanguageLevelOrHighest = true;
  public boolean useModuleLanguageLevelOrHighest = true;

  /**
   * Sets the correct text for the info label in the inspection settings page
   *
   * @param label label to set the text
   */
  private void setLabelTextToVersion(JLabel label) {
    if (useSDKLanguageLevelOrHighest) {
      label.setText("Use language version from Project SDK");
    } else {
      label.setText(languageLevel.getPresentableText());
    }
  }

  @Nullable
  @Override
  public JComponent createOptionsPanel() {
    final JPanel mainPanel = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP));
    final JCheckBox useModuleCheckbox = new JCheckBox("Language Level of Module has priority");
    final JCheckBox useSDKCheckbox = new JCheckBox("Use Project SDK Language Level");
    final JLabel infoLabel = new JLabel();
    //noinspection Since15
    final ComboBox<MathematicaLanguageLevel> versionComboBox = new ComboBox<>();

    for (MathematicaLanguageLevel level : MathematicaLanguageLevel.values()) {
      //noinspection unchecked
      versionComboBox.addItem(level);
    }
    versionComboBox.setSelectedItem(languageLevel);
    versionComboBox.setEditable(false);
    //noinspection unchecked
    versionComboBox.addActionListener(e -> {
      final MathematicaLanguageLevel selectedItem = (MathematicaLanguageLevel) versionComboBox.getSelectedItem();
      if (selectedItem != null) {
        languageLevel = selectedItem;
        setLabelTextToVersion(infoLabel);
      }
    });

    useModuleCheckbox.setSelected(useModuleLanguageLevelOrHighest);
    useModuleCheckbox.addActionListener(e ->
        useModuleLanguageLevelOrHighest = useModuleCheckbox.isSelected()
    );

    useSDKCheckbox.setSelected(useSDKLanguageLevelOrHighest);
    useSDKCheckbox.addActionListener(e -> {
      useSDKLanguageLevelOrHighest = useSDKCheckbox.isSelected();
      versionComboBox.setVisible(!useSDKLanguageLevelOrHighest);
      if (!useSDKLanguageLevelOrHighest) {
        languageLevel = (MathematicaLanguageLevel) versionComboBox.getSelectedItem();
      }
      setLabelTextToVersion(infoLabel);
    });

    setLabelTextToVersion(infoLabel);
    versionComboBox.setVisible(!useSDKLanguageLevelOrHighest);
    mainPanel.add(useModuleCheckbox);
    mainPanel.add(useSDKCheckbox);
    mainPanel.add(versionComboBox);

    return mainPanel;
  }

  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return message("bugs.unsupported.version.name");
  }

  @Nullable
  @Override
  public String getStaticDescription() {
    return message("bugs.unsupported.version.description");
  }

  @Nls
  @NotNull
  @Override
  public String getGroupDisplayName() {
    return message("group.bugs");
  }

  @NotNull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
    if (session.getFile().getFileType() instanceof MathematicaFileType) {

      // TODO: There must be a simpler way to find the module for a source file
      if (useModuleLanguageLevelOrHighest) {
        final PsiFile file = session.getFile();
        final ModuleManager instance = ModuleManager.getInstance(file.getProject());
        for (Module module : instance.getModules()) {
          if (ModuleType.is(module, MathematicaModuleType.getInstance())) {
            final ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
            final ModuleFileIndex fileIndex = moduleRootManager.getFileIndex();
            final VirtualFile virtualFile = file.getVirtualFile();
            if (fileIndex.isInContent(virtualFile)) {
              final MathematicaLanguageLevelModuleExtensionImpl languageLevelModuleExtension =
                  moduleRootManager.getModuleExtension(MathematicaLanguageLevelModuleExtensionImpl.class);
              if (languageLevelModuleExtension.getMathematicaLanguageLevel() != null) {
                return new WrongVersionVisitor(holder, languageLevelModuleExtension.getMathematicaLanguageLevel());
              }
            }
          }
        }
      }

      if (useSDKLanguageLevelOrHighest) {
        final ProjectRootManager manager = ProjectRootManager.getInstance(holder.getProject());
        final Sdk projectSdk = manager.getProjectSdk();
        if (projectSdk != null && projectSdk.getSdkType() instanceof MathematicaSdkType) {
          languageLevel = MathematicaLanguageLevel.createFromSdk(projectSdk);
        }
      }
      return new WrongVersionVisitor(holder, languageLevel);
    } else return PsiElementVisitor.EMPTY_VISITOR;
  }

  /**
   * This visitor just inspects all symbols in the file. For each symbol it checks whether it is in the list of built-in
   * symbols and if yes, if it is already defined in the Mathematica version the user specified
   */
  private static class WrongVersionVisitor extends MathematicaVisitor {

    private final HashMap<String, Double> mySymbolVersions = SymbolVersionProvider.getSymbolNames();
    private final ProblemsHolder myHolder;
    private MathematicaLanguageLevel myLanguageLevel;

    WrongVersionVisitor(final ProblemsHolder holder, final MathematicaLanguageLevel usedLanguageVersion) {
      this.myHolder = holder;
      myLanguageLevel = usedLanguageVersion;
    }

    private void registerProblem(final PsiElement element, final String message) {
      myHolder.registerProblem(
          element,
          TextRange.from(0, element.getTextLength()),
          message);
    }

    @Override
    public void visitFunctionCall(FunctionCall functionCall) {
      final PsiElement head = functionCall.getHead();
      if ("Association".equals(head.getText()) && myLanguageLevel.getVersionNumber() < 10) {
        registerProblem(functionCall,
            message("bugs.unsupported.version.association", myLanguageLevel.getPresentableText()));
      }

      if (functionCall.hasHead("With") && myLanguageLevel.getVersionNumber() < 10.3 &&
          functionCall.getArguments().size() > 3) {
        registerProblem(functionCall, message("bugs.unsupported.version.with", myLanguageLevel.getPresentableText()));

      }
    }

    @Override
    public void visitAssociation(Association association) {
      if (myLanguageLevel.getVersionNumber() < 10) {
        registerProblem(association,
            message("bugs.unsupported.version.association", myLanguageLevel.getPresentableText()));
      }
    }

    @Override
    public void visitSymbol(Symbol symbol) {
      final String symbolName = symbol.getSymbolName();
      if (Character.isLowerCase(symbolName.charAt(0))) {
        return;
      }

      final PsiElement resolve = symbol.resolve();
      if (resolve instanceof LightBuiltInSymbol) {
        String nameWithContext =
            symbol.getMathematicaContext().equals("") ? "System`" + symbol.getSymbolName() : symbol.getFullSymbolName();

        if (mySymbolVersions.containsKey(nameWithContext)) {
          double version = mySymbolVersions.get(nameWithContext);
          if (version > myLanguageLevel.getVersionNumber()) {
            registerProblem(symbol,
                "Mathematica " + version + " required. You are using " + myLanguageLevel.getPresentableText());
          }
        }
      }
    }
  }
}


