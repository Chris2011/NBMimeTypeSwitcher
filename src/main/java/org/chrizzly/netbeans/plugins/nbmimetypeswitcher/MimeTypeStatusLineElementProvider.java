package org.chrizzly.netbeans.plugins.nbmimetypeswitcher;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.editor.EditorRegistry;
import org.openide.awt.StatusLineElementProvider;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Chrizzly
 */
@ServiceProvider(service = StatusLineElementProvider.class)
public class MimeTypeStatusLineElementProvider implements StatusLineElementProvider {

    private static final JLabel MIME_TYPE_LABEL = new MimeTypeLabel();
    private static final Component COMPONENT = panelWithSeparator(MIME_TYPE_LABEL);
    private static final Logger LOGGER = Logger.getLogger(MimeTypeStatusLineElementProvider.class.getName());
    private static volatile boolean SHOWING_POPUP;

    static {
        // icon position: right
        MIME_TYPE_LABEL.setHorizontalTextPosition(SwingConstants.LEFT);

        // add listeners
        EditorRegistry.addPropertyChangeListener((PropertyChangeListener) MIME_TYPE_LABEL);
        MIME_TYPE_LABEL.addMouseListener(new MimeTypeLabelMouseAdapter());
    }

    private static void setMimeType(FileObject fileObject, String selectedMimeType) {
        DataObject fileDataObject;

        try {
            fileDataObject = DataObject.find(fileObject);
            EditorCookie lookup = fileDataObject.getLookup().lookup(EditorCookie.class);
            JEditorPane openedPane = lookup.getOpenedPanes()[0];

            String text = openedPane.getText();

            openedPane.setContentType(selectedMimeType);

            openedPane.setText(text);
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public Component getStatusLineElement() {
        return COMPONENT;
    }

    /**
     * Create Component(JPanel) and add separator and JLabel to it.
     *
     * @param label JLabel
     * @return panel
     */
    private static Component panelWithSeparator(JLabel label) {
        // create separator
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL) {
            private static final long serialVersionUID = -6385848933295984637L;

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(3, 3);
            }
        };

        separator.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        // create panel
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(separator, BorderLayout.WEST);
        panel.add(label, BorderLayout.EAST);

        return panel;
    }

    /**
     * Change the mimeType.
     * <b>NOTE:</b>The file is reopened.
     *
     * @param selectedMimeType the selected mimeType
     */
    @NbBundle.Messages("MimeTypeStatusLineElementProvider.message.modified.file=Please save the file once.")
    private static void changeMimeType(String selectedMimeType) {
        if (selectedMimeType == null) {
            UiUtils.requestFocusLastFocusedComponent();

            LOGGER.log(Level.WARNING, "The selected mimeType is null."); // NOI18N

            return;
        }

        FileObject fileObject = UiUtils.getLastFocusedFileObject();
        if (fileObject == null) {
            return;
        }

        // check whether the file is modified
        try {
            DataObject dataObject = DataObject.find(fileObject);
            if (dataObject.isModified()) {
                UiUtils.requestFocusLastFocusedComponent();
                UiUtils.showErrorMessage(Bundle.MimeTypeStatusLineElementProvider_message_modified_file());
                return;
            }
        } catch (DataObjectNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        // same mimeType?
        String mimeType = MimeTypeFinder.find(fileObject);
        String currentMimeType = mimeType;

        // #1 mimeType is empty when snippet is inserted with palette
        if (selectedMimeType.equals(currentMimeType) || selectedMimeType.isEmpty()) {
            UiUtils.requestFocusLastFocusedComponent();

            return;
        }

        setMimeType(fileObject, selectedMimeType);

        final DataObject dobj;

        try {
            dobj = DataObject.find(fileObject);

            MimeTypeOptions.getInstance().setLastSelectedMimeTypes(selectedMimeType);
            UiUtils.reopen(dobj);
        } catch (DataObjectNotFoundException | InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
// save selected mimeType to options
////
//            String mimeType1 = dobj.getPrimaryFile().getMIMEType();
//            System.out.println(mimeType1);

// save selected mimeType to options
////
//            String mimeType1 = dobj.getPrimaryFile().getMIMEType();
//            System.out.println(mimeType1);
    }

    //~ inner classes
    private static final class MimeTypeFinder {

        private MimeTypeFinder() {
        }

        private static final MimeTypeQueryImpl QUERY_IMPL = new MimeTypeQueryImpl();

        private static String find(FileObject fileObject) {
            String mimeType = QUERY_IMPL.getMimeType(fileObject);

            if (mimeType == null) {
                mimeType = "unknown";
            }

            return mimeType;
        }
    }

    private static class MimeTypeLabel extends JLabel implements PropertyChangeListener {

        private static final long serialVersionUID = 7553842743917776222L;

        @StaticResource
        public static final String UP_DOWN_ICON = "org/chrizzly/netbeans/plugins/nbmimetypeswitcher/updown.png"; // NOI18N

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            updateMimeType();
        }

        private void updateMimeType() {
            assert EventQueue.isDispatchThread();
            FileObject fileObject;

            if (SHOWING_POPUP) {
                fileObject = UiUtils.getLastFocusedFileObject();
            } else {
                fileObject = UiUtils.getFocusedFileObject();
            }

            if (fileObject != null) {
                String mimeType = getMimeType(fileObject);

                this.setText(String.format(" %s ", mimeType)); // NOI18N
                this.setIcon(ImageUtilities.loadImageIcon(UP_DOWN_ICON, false));

                return;
            }

            this.setText("MimeType"); // NOI18N
//            this.setIcon(null);
        }

        private String getMimeType(FileObject fileObject) {
            return MimeTypeFinder.find(fileObject);
        }
    }

    private static class MimeTypeLabelMouseAdapter extends MouseAdapter {

        public MimeTypeLabelMouseAdapter() {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            FileObject fileObject = UiUtils.getFocusedFileObject();
            if (fileObject == null) {
                return;
            }

            // set current mimeType
            String mimeType = MimeTypeFinder.find(fileObject);
            final MimeTypePanel mimeTypePanel = new MimeTypePanel(mimeType);
            Popup popup = getPopup(mimeTypePanel);

            if (popup == null) {
                return;
            }

            // add listener
            final MimeTypeListMouseAdapter encodingListMouseAdapter = new MimeTypeListMouseAdapter(mimeTypePanel, popup);
            mimeTypePanel.addMimeTypeListMouseListener(encodingListMouseAdapter);

            // hide popup
            final AWTEventListener eventListener = new AWTEventListener() {
                @Override
                public void eventDispatched(AWTEvent event) {
                    if (isHidable(event)) {
                        Object source = event.getSource();
                        popup.hide();
                        SHOWING_POPUP = false;
                        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
                        mimeTypePanel.shutdown();

                        if (!UiUtils.isMouseClicked(event)
                                || !mimeTypePanel.isMimeTypeList(source)) {
                            mimeTypePanel.removeMimeTypeListMouseListener(encodingListMouseAdapter);
                        }

                        if (UiUtils.isEscKey(event) || UiUtils.isEnterKey(event)) {
                            if (mimeTypePanel.isMimeTypeFilterField(source)
                                    || mimeTypePanel.isMimeTypeList(source)) {
                                UiUtils.requestFocusLastFocusedComponent();
                            }
                        }

                        if (!mimeTypePanel.isMimeTypeList(source)
                                && !mimeTypePanel.isMimeTypeFilterField(source)) {
                            return;
                        }

                        if (UiUtils.isEnterKey(event)) {
                            changeMimeType(mimeTypePanel.getSelectedMimeType());
                        }
                    }
                }

                private boolean isHidable(AWTEvent event) {
                    Object source = event.getSource();
                    if (UiUtils.isMouseClicked(event)
                            && !UiUtils.isComponentOfClass(MimeTypePanel.class, source)
                            && source != MIME_TYPE_LABEL) {
                        return true;

                    }
                    return UiUtils.isEscKey(event) || UiUtils.isEnterKey(event);
                }

            };
            Toolkit.getDefaultToolkit().addAWTEventListener(eventListener, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);

            popup.show();
            SHOWING_POPUP = true;
            mimeTypePanel.requestFocusMimeTypeList();
        }

        private Popup getPopup(final JPanel encodingPanel) throws IllegalArgumentException {
            Point labelStart = MIME_TYPE_LABEL.getLocationOnScreen();
            int x = Math.min(labelStart.x, labelStart.x + MIME_TYPE_LABEL.getSize().width - encodingPanel.getPreferredSize().width);
            int y = labelStart.y - encodingPanel.getPreferredSize().height;
            return PopupFactory.getSharedInstance().getPopup(MIME_TYPE_LABEL, encodingPanel, x, y);
        }
    }

    private static class MimeTypeListMouseAdapter extends MouseAdapter {

        private final MimeTypePanel mimeTypePanel;
        private final Popup popup;

        public MimeTypeListMouseAdapter(MimeTypePanel mimeTypePanel, Popup popup) {
            this.mimeTypePanel = mimeTypePanel;
            this.popup = popup;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            popup.hide();
            SHOWING_POPUP = false;
            mimeTypePanel.removeMimeTypeListMouseListener(this);

            changeMimeType(mimeTypePanel.getSelectedMimeType());
        }

    }
}
