package org.chrizzly.netbeans.plugins.nbmimetypeswitcher;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.netbeans.modules.csl.api.UiUtils;
import org.openide.util.RequestProcessor;

@ActionID(
        category = "File",
        id = "org.chrizzly.netbeans.plugins.nbmimetypeswitcher.MimeTypeSwitchAction"
)
@ActionRegistration(
        iconBase = "org/chrizzly/netbeans/plugins/nbmimetypeswitcher/updown.png",
        displayName = "#CTL_MimeTypeSwitchAction"
)
@ActionReference(path = "Toolbars/File", position = 0)
@Messages("CTL_MimeTypeSwitchAction=Mime Type Switcher")
public final class MimeTypeSwitchAction implements ActionListener {
    private static final RequestProcessor RP = new RequestProcessor(MimeTypeSwitchAction.class);
    private static final Logger LOGGER = Logger.getLogger(MimeTypeSwitchAction.class.getName());
    
    /**
     * Ordered set of all MIME types registered in system.
     */
    private final TreeSet<String> mimeTypes = new TreeSet<String>();

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
                    readMimeTypesFromLoaders();

            //        JOptionPane.showMessageDialog(null, mimeTypes);
            FileObject fo = getFocusedFileObject(true);
            setMimeType(fo, "text/html");

//            DataObject dobj = DataObject.find(fo);

//            close(dobj);
            // XXX java.lang.AssertionError is occurred
//            Thread.sleep(200);

//            open(dobj);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static void setMimeType(FileObject fileObject, String selectedMimeType) {
        try {
            DataObject fileDataObject;

            fileDataObject = DataObject.find(fileObject);
            EditorCookie ec = fileDataObject.getLookup().lookup(EditorCookie.class);
//            CloseCookie cc;
            JEditorPane openedPane = ec.getOpenedPanes()[0];
//
            JTextComponent focusedComponent = EditorRegistry.focusedComponent();
            Document document = focusedComponent.getDocument();
//
//
            BaseDocument abstractDocument = (BaseDocument)document;
            try {
                //            // Beginning of the Line where the caret is.
                System.out.println(String.format("Line beginning: %s", Utilities.getRowStart(focusedComponent, openedPane.getCaretPosition())));
                System.out.println(String.format("Line beginning indent: %s", Utilities.getRowIndent(abstractDocument, openedPane.getCaretPosition())));
                System.out.println(String.format("Line beginning after space: %s", LineDocumentUtils.getLineFirstNonWhitespace(abstractDocument, 0)));
//            EditorCaret name = (EditorCaret)focusedComponent.getCaret();
//            System.out.println(String.format("Caret position: %s", name.getDot()));
//            document.insertString(name.getDot(), "bla", null);

abstractDocument.putProperty("mimeType", selectedMimeType);

//Runtime.getRuntime().exec("''");

//            new ProcessBuilder("C:\\Projekte\\Netbeans Plugins\\NBMimeTypeSwitcher\\src\\main\\java\\org\\chrizzly\\netbeans\\plugins\\nbmimetypeswitcher\\MimeTypeSwitchAction.java").start();

//            abstractDocument.readLock();
//
//            String text = openedPane.getText();
//
//            openedPane.setContentType(selectedMimeType);
//
//            openedPane.setText(text);
//            
//            reopen(abstractDocument, fileObject, openedPane.getCaretPosition());

//            System.out.println(ec);
//            System.out.println(EditorCookie.class);

//            Method notifyModified = CloneableEditorSupport.class.getDeclaredMethod("callNotifyModified");
//
//            notifyModified.setAccessible(true);
//            notifyModified.invoke(ec);

//            abstractDocument.readUnlock();
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        } catch (IllegalArgumentException
                | SecurityException | DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private static void reopen(Document document, FileObject fileObject, int caretPosition) {
        DataObject dataObject = NbEditorUtilities.getDataObject(document);

        RP.schedule(() -> {
            try {
                dataObject.setValid(false);
                UiUtils.open(fileObject, caretPosition);
            } catch (PropertyVetoException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }, 200, TimeUnit.MILLISECONDS);
    }

    @CheckForNull
    private static FileObject getFocusedFileObject(boolean last) {
        JTextComponent component;
        if (last) {
            component = EditorRegistry.lastFocusedComponent();
        } else {
            component = EditorRegistry.focusedComponent();
        }
        if (component == null) {
            return null;
        }

        Document document = component.getDocument();
        if (document == null) {
            return null;
        }

        return NbEditorUtilities.getFileObject(document);
    }

    /**
     * Reads MIME types registered in Loaders folder and fills mimeTypes set.
     */
    private void readMimeTypesFromLoaders() {
        FileObject[] children = FileUtil.getConfigFile("Loaders").getChildren();  //NOI18N

        for (FileObject child : children) {
            String mime1 = child.getNameExt();
            FileObject[] subchildren = child.getChildren();

            for (FileObject subchild : subchildren) {
                FileObject factoriesFO = subchild.getFileObject("Factories");  //NOI18N

                if (factoriesFO != null && factoriesFO.getChildren().length > 0) {
                    // add only MIME types where some loader exists
                    mimeTypes.add(mime1 + "/" + subchild.getNameExt()); //NOI18N
                }
            }
        }

        mimeTypes.remove("content/unknown"); //NOI18N
    }
}
