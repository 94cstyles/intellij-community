package com.intellij.structuralsearch.plugin.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.structuralsearch.MatchOptions;
import com.intellij.structuralsearch.plugin.replace.ui.ReplaceConfiguration;

import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Maxim.Mossienko
 * Date: Apr 23, 2004
 * Time: 5:03:52 PM
 * To change this template use File | Settings | File Templates.
 */
class SelectTemplateDialog extends DialogWrapper {
  private boolean showHistory;
  private Editor searchPatternEditor;
  private Editor replacePatternEditor;
  private boolean replace;
  private Project project;
  protected final ExistingTemplatesComponent existingTemplatesComponent;

  private MySelectionListener selectionListener;

  class MySelectionListener implements TreeSelectionListener, ListSelectionListener {
    public void valueChanged(TreeSelectionEvent e) {
      if (e.getNewLeadSelectionPath()!=null) {
        setPatternFromNode(
          ((DefaultMutableTreeNode)e.getNewLeadSelectionPath().getLastPathComponent())
        );
      }
    }

    public void valueChanged(ListSelectionEvent e) {
      if (e.getValueIsAdjusting() || e.getLastIndex()==-1) return;
      int selectionIndex = existingTemplatesComponent.getHistoryList().getSelectedIndex();
      if (selectionIndex!=-1) {
        setPatternFromList( selectionIndex );
      }
    }
  }

  SelectTemplateDialog(Project _project, boolean _showHistory, boolean _replace) {
    super(_project,false);

    project = _project;
    showHistory = _showHistory;
    replace = _replace;
    existingTemplatesComponent = ExistingTemplatesComponent.getInstance(project);

    setTitle((showHistory)?"Used Templates History":"Existing Templates");

    getOKAction().putValue(Action.MNEMONIC_KEY,new Integer('O'));
    init();

    if (showHistory) {
      final int selection = existingTemplatesComponent.getHistoryList().getSelectedIndex();
      if (selection!=-1) {
        setPatternFromList(selection);
      }
    } else {
      final TreePath selection = existingTemplatesComponent.getPatternTree().getSelectionPath();
      if (selection!=null) {
        setPatternFromNode((DefaultMutableTreeNode)selection.getLastPathComponent());
      }
    }

    setupListeners();
  }

  private void setPatternFromList(int index) {
    setSearchPatternFromConfiguration(
      (Configuration)existingTemplatesComponent.getHistoryList().getModel().getElementAt( index )
    );
  }

  protected JComponent createCenterPanel() {
    final JPanel centerPanel = new JPanel( new BorderLayout() );
    Splitter splitter;

    centerPanel.add(BorderLayout.CENTER,splitter = new Splitter(false,0.3f));
    centerPanel.add(splitter);

    final JPanel panel;
    splitter.setFirstComponent(
      showHistory?
        existingTemplatesComponent.getHistoryPanel():
        existingTemplatesComponent.getTemplatesPanel()
    );
    splitter.setSecondComponent(
      panel = new JPanel( new BorderLayout() )
    );

    searchPatternEditor = UIUtil.createEditor(
      EditorFactory.getInstance().createDocument(""),
      project,
      false,
      true
    );

    JComponent centerComponent;

    if (replace) {
      replacePatternEditor = UIUtil.createEditor(
        EditorFactory.getInstance().createDocument(""),
        project,
        false,
        true
      );
      centerComponent = new Splitter(true);
      ((Splitter)centerComponent).setFirstComponent( searchPatternEditor.getComponent() );
      ((Splitter)centerComponent).setSecondComponent( replacePatternEditor.getComponent() );
    } else {
      centerComponent = searchPatternEditor.getComponent();
    }

    panel.add(BorderLayout.CENTER,centerComponent);

    panel.add(
      BorderLayout.NORTH, new JLabel("Template preview:")
    );
    return centerPanel;
  }

  protected void dispose() {
    EditorFactory.getInstance().releaseEditor(searchPatternEditor);
    removeListeners();
    super.dispose();
  }

  public JComponent getPreferredFocusedComponent() {
    return (showHistory)?
      (JComponent)existingTemplatesComponent.getHistoryList() :
      existingTemplatesComponent.getPatternTree();
  }

  protected String getDimensionServiceKey() {
    return "#com.intellij.structuralsearch.plugin.ui.SelectTemplateDialog";
  }

  private void setupListeners() {
    existingTemplatesComponent.setOwner(this);
    selectionListener = new MySelectionListener();

    if (showHistory) {
      existingTemplatesComponent.getHistoryList().getSelectionModel().addListSelectionListener(
        selectionListener
      );
    } else {
      existingTemplatesComponent.getPatternTree().getSelectionModel().addTreeSelectionListener(
        selectionListener
      );
    }
  }

  private void removeListeners() {
    existingTemplatesComponent.setOwner(null);
    if (showHistory) {
      existingTemplatesComponent.getHistoryList().getSelectionModel().removeListSelectionListener(
        selectionListener
      );
    } else {
      existingTemplatesComponent.getPatternTree().getSelectionModel().removeTreeSelectionListener(selectionListener);
    }
  }

  private void setPatternFromNode(DefaultMutableTreeNode node) {
    if (node == null) return;
    final Object userObject = node.getUserObject();
    final Configuration configuration;

    // root could be without saerch template
    if (userObject instanceof PredefinedConfiguration) {
      final PredefinedConfiguration config = (PredefinedConfiguration)userObject;
      configuration = config.getConfiguration();
    } else if (userObject instanceof Configuration) {
      configuration = (Configuration)userObject;
    } else {
      return;
    }

    setSearchPatternFromConfiguration(configuration);

  }

  private void setSearchPatternFromConfiguration(final Configuration configuration) {
    final MatchOptions matchOptions = configuration.getMatchOptions();

    UIUtil.setContent(
      searchPatternEditor,
      matchOptions.getSearchPattern(),
      0,
      searchPatternEditor.getDocument().getTextLength(),
      project
    );

    searchPatternEditor.putUserData(SubstitutionShortInfoHandler.CURRENT_CONFIGURATION_KEY,configuration);

    if (replace) {
      String replacement;

      if (configuration instanceof ReplaceConfiguration) {
        replacement = ((ReplaceConfiguration)configuration).getOptions().getReplacement();
      } else {
        replacement = configuration.getMatchOptions().getSearchPattern();
      }

      UIUtil.setContent(
        replacePatternEditor,
        replacement,
        0,
        replacePatternEditor.getDocument().getTextLength(),
        project
      );

      replacePatternEditor.putUserData(SubstitutionShortInfoHandler.CURRENT_CONFIGURATION_KEY,configuration);
    }
  }
}
