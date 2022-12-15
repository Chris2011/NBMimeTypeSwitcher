package org.chrizzly.netbeans.plugins.nbmimetypeswitcher;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.windows.WindowManager;

/**
 *
 * @author Chrizzly
 */
final public class MimeTypePanel extends JPanel {
    private static final Collection<? extends String> MIME_TYPES = MimeTypeQueryImpl.getMimeTypes();
    private static final long serialVersionUID = -7350298167289629517L;

    private DefaultListModel<String> mimeTypeListModel = new DefaultListModel<>();
    // listeners
    private KeyListener mimeTypeFilterKeyListener = new MimeTypeFilterKeyListener();
    private KeyListener mimeTypeListKeyListener = new MimeTypeListKeyAdapter();
    private DocumentListener mimeTypeFilterDocumentListener = new MimeTypeFilterDocumentListener();

    /**
     * Creates new form MimeTypePanel
     */
    public MimeTypePanel(String origMimeType) {
        initComponents();
        List<String> selectedMimeTypes = MimeTypeOptions.getInstance().getLastSelectedMimeTypes();
        // add last 5 selected mimeTypes to top of the list by default
        selectedMimeTypes.forEach(e -> {
            if (!e.equals(origMimeType)) {
                mimeTypeListModel.addElement(e);
            }
        });

        // add the current mimeType to top of the list
        MIME_TYPES.forEach((mimeType) -> {
            if (mimeType.equals(mimeType)) {
                mimeTypeListModel.add(0, mimeType);
            } else {
                if (!selectedMimeTypes.contains(mimeType)) {
                    mimeTypeListModel.addElement(mimeType);
                }
            }
        });
        mimeTypeList.setModel(mimeTypeListModel);
        mimeTypeList.setSelectedValue(origMimeType, true);
        mimeTypeFilterTextField.getDocument().addDocumentListener(mimeTypeFilterDocumentListener);
        mimeTypeFilterTextField.addKeyListener(mimeTypeFilterKeyListener);
        mimeTypeList.addKeyListener(mimeTypeListKeyListener);
        mimeTypeFilterTextField.setVisible(false);
        // set Preferred size
        int preferredWidth = mimeTypeListScrollPane.getPreferredSize().width;
        int preferredHeight = WindowManager.getDefault().getMainWindow().getSize().height / 3;
        mimeTypeListScrollPane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));

    }

    /**
     * Clean up listeners.
     */
    void shutdown() {
        // remove listeners
        mimeTypeFilterTextField.removeKeyListener(mimeTypeFilterKeyListener);
        mimeTypeList.removeKeyListener(mimeTypeListKeyListener);
        mimeTypeFilterTextField.getDocument().removeDocumentListener(mimeTypeFilterDocumentListener);
        mimeTypeFilterKeyListener = null;
        mimeTypeListKeyListener = null;
        mimeTypeFilterDocumentListener = null;
    }

    /**
     * Invoke when something is typed in the text field.
     */
    private void fireChange() {
        assert EventQueue.isDispatchThread();
        mimeTypeListModel.clear();

        // check all keywords separated by whitespaces
        String filterText = mimeTypeFilterTextField.getText();
        String[] filters = filterText.split("\\s"); // NOI18N

        MIME_TYPES.forEach(mimeType -> {
            boolean addItem = true;
            for (String filter : filters) {
                if (!mimeType.toLowerCase().contains(filter.toLowerCase())) {
                    addItem = false;
                }
            }
            if (addItem) {
                mimeTypeListModel.addElement(mimeType);
            }
        });

        if (mimeTypeListModel.size() > 0) {
            String element = mimeTypeListModel.getElementAt(0);
            mimeTypeList.setSelectedValue(element, true);
        }

        // show the text field
        if (filterText.isEmpty()) {
            mimeTypeList.requestFocusInWindow();
            mimeTypeFilterTextField.setVisible(false);
            revalidate();
        }
    }

    void addMimeTypeListMouseListener(MouseListener listener) {
        mimeTypeList.addMouseListener(listener);
    }

    void removeMimeTypeListMouseListener(MouseListener listener) {
        mimeTypeList.removeMouseListener(listener);
    }

    boolean isMimeTypePanelComponent(Object object) {
        return object == this
                || isMimeTypeList(object)
                || isMimeTypeListScrollBar(object)
                || isMimeTypeFilterField(object);
    }

    boolean isMimeTypeListScrollBar(Object object) {
        return mimeTypeListScrollPane.getVerticalScrollBar() == object;
    }

    boolean isMimeTypeList(Object object) {
        return mimeTypeList == object;
    }

    boolean isMimeTypeFilterField(Object object) {
        return mimeTypeFilterTextField == object;
    }

    void requestFocusMimeTypeFilter() {
        mimeTypeFilterTextField.requestFocusInWindow();
    }

    void requestFocusMimeTypeList() {
        mimeTypeList.requestFocusInWindow();
    }

    /**
     * Get the selected mimeType.
     *
     * @return the selected mimeType
     */
    public String getSelectedMimeType() {
        return mimeTypeList.getSelectedValue();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mimeTypeFilterTextField = new javax.swing.JTextField();
        mimeTypeListScrollPane = new javax.swing.JScrollPane();
        mimeTypeList = new javax.swing.JList<>();

        mimeTypeFilterTextField.setText(org.openide.util.NbBundle.getMessage(MimeTypePanel.class, "EncodingPanel.encodingFilterTextField.text")); // NOI18N

        mimeTypeListScrollPane.setViewportView(mimeTypeList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mimeTypeFilterTextField)
            .addComponent(mimeTypeListScrollPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(mimeTypeListScrollPane)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mimeTypeFilterTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField mimeTypeFilterTextField;
    private javax.swing.JList<String> mimeTypeList;
    private javax.swing.JScrollPane mimeTypeListScrollPane;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes
    private class MimeTypeFilterKeyListener implements KeyListener {

        public MimeTypeFilterKeyListener() {
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            // change a selected item
            int size = mimeTypeListModel.getSize();
            if (size > 0) {
                int selectedIndex = mimeTypeList.getSelectedIndex();
                String element;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        if (selectedIndex == 0) {
                            element = mimeTypeListModel.getElementAt(size - 1);
                        } else {
                            element = mimeTypeListModel.getElementAt(selectedIndex - 1);
                        }
                        break;
                    case KeyEvent.VK_DOWN:
                        if (selectedIndex == size - 1) {
                            element = mimeTypeListModel.getElementAt(0);
                        } else {
                            element = mimeTypeListModel.getElementAt(selectedIndex + 1);
                        }
                        break;
                    default:
                        element = null;
                        break;
                }

                if (element != null) {
                    mimeTypeList.setSelectedValue(element, true);
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }
    }

    private class MimeTypeFilterDocumentListener implements DocumentListener {

        public MimeTypeFilterDocumentListener() {
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            processUpdate();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            processUpdate();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            processUpdate();
        }

        private void processUpdate() {
            fireChange();
        }
    }

    private class MimeTypeListKeyAdapter extends KeyAdapter {

        public MimeTypeListKeyAdapter() {
        }

        @Override
        public void keyTyped(KeyEvent e) {
            // XXX validate key char
            switch (e.getKeyChar()) {
                case '\b': // backspace no break
                case '\u007f': // delete
                    return;
                default:
                    mimeTypeFilterTextField.setVisible(true);
                    mimeTypeFilterTextField.requestFocusInWindow();
                    revalidate();
                    mimeTypeFilterTextField.setText(String.valueOf(e.getKeyChar()));
                    break;
            }
        }
    }
}