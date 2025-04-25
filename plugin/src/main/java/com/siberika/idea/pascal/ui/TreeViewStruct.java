package com.siberika.idea.pascal.ui;

import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.util.Filter;
import consulo.project.Project;
import consulo.ui.ex.awt.DialogWrapper;
import consulo.ui.ex.awt.ScrollPaneFactory;
import consulo.ui.ex.awt.UIUtil;
import consulo.ui.ex.awt.event.DoubleClickListener;
import consulo.ui.ex.awt.speedSearch.TreeSpeedSearch;
import consulo.ui.ex.awt.tree.Tree;
import consulo.util.collection.SmartList;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 26/11/2015
 */
public class TreeViewStruct extends DialogWrapper {
    private final Collection<PasEntityScope> structs;
    private final Filter<PasField> filter;
    private Tree myTree;
    private List<PasField> selected = new SmartList<PasField>();

    public TreeViewStruct(Project project, String title, Collection<PasEntityScope> structs, Filter<PasField> filter) {
        super(project, true);
        setTitle(title);
        this.structs = structs;
        this.filter = filter;
        init();
    }

    @Override
    protected JComponent createCenterPanel() {
        final DefaultTreeModel model = new DefaultTreeModel(new DefaultMutableTreeNode());

        MutableTreeNode root = new DefaultMutableTreeNode();
        model.setRoot(root);
        for (PasEntityScope struct : structs) {
            MutableTreeNode child = new DefaultMutableTreeNode(struct);
            for (PasField field : struct.getAllFields()) {
                if (filter.allow(field)) {
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(field);
                    child.insert(node, child.getChildCount());
                }
            }
            if (child.getChildCount() > 0) {
                model.insertNodeInto(child, root, root.getChildCount());
            }
        }

        myTree = new Tree(model);
        myTree.setRootVisible(false);
        myTree.expandRow(0);
        myTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        myTree.setCellRenderer(new FieldRenderer());
        UIUtil.setLineStyleAngled(myTree);

        final JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(myTree);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        myTree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (KeyEvent.VK_ENTER == e.getKeyCode()) {
                    doOKAction();
                }
            }
        });

        new DoubleClickListener() {
            @Override
            protected boolean onDoubleClick(MouseEvent e) {
                final TreePath path = myTree.getPathForLocation(e.getX(), e.getY());
                if (path != null && myTree.isPathSelected(path)) {
                    doOKAction();
                    return true;
                }
                return false;
            }
        }.installOn(myTree);

        myTree.addTreeSelectionListener(
                new TreeSelectionListener() {
                    @Override
                    public void valueChanged(final TreeSelectionEvent e) {
                        handleSelectionChanged();
                    }
                }
        );

        new TreeSpeedSearch(myTree);

        return scrollPane;
    }

    private void handleSelectionChanged(){
        setOKActionEnabled(true);
    }

    @Override
    protected void doOKAction() {
        MutableTreeNode[] nodes = myTree.getSelectedNodes(MutableTreeNode.class, null);
        for (MutableTreeNode node : nodes) {
            selected.add((PasField) ((DefaultMutableTreeNode) node).getUserObject());
        }
        super.doOKAction();
    }

    @Override
    public void doCancelAction() {
        selected.clear();
        super.doCancelAction();
    }

    public List<PasField> getSelected() {
        return selected;
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return myTree;
    }

    @Override
    protected String getDimensionServiceKey() {
        return "#com.siberika.pascal.TreeViewStruct";
    }
}
