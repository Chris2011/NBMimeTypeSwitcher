package org.chrizzly.netbeans.plugins.nbmimetypeswitcher;

import java.awt.AWTEvent;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author Chrl
 */
public final class UiUtils {
    private UiUtils() {
    }

    @CheckForNull
    public static FileObject getLastFocusedFileObject() {
        return getFocusedFileObject(true);
    }

    @CheckForNull
    public static FileObject getFocusedFileObject() {
        return getFocusedFileObject(false);
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

    public static void reopen(DataObject dobj) throws InterruptedException {
        close(dobj);

        // XXX java.lang.AssertionError is occurred
        Thread.sleep(200);

        open(dobj);
    }

    public static void close(DataObject dobj) {
        CloseCookie cc = dobj.getLookup().lookup(CloseCookie.class);
        EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);

        if (cc != null) {
            cc.close();
        }
        if (ec != null) {
            ec.close();
        }
    }

    public static void open(DataObject dobj) {
        OpenCookie oc = dobj.getLookup().lookup(OpenCookie.class);
        if (oc != null) {
            oc.open();
        }
    }

    public static boolean isMouseClicked(AWTEvent event) {
        return event instanceof MouseEvent && ((MouseEvent) event).getClickCount() > 0;
    }

    public static boolean isEscKey(AWTEvent event) {
        return event instanceof KeyEvent && ((KeyEvent) event).getKeyCode() == KeyEvent.VK_ESCAPE;
    }

    public static boolean isEnterKey(AWTEvent event) {
        return event instanceof KeyEvent && ((KeyEvent) event).getKeyCode() == KeyEvent.VK_ENTER;
    }

    /**
     * Focus the last focused editor pane.
     */
    public static void requestFocusLastFocusedComponent() {
        JTextComponent lastFocusedComponent = EditorRegistry.lastFocusedComponent();

        if (lastFocusedComponent != null) {
            lastFocusedComponent.requestFocusInWindow();
        }
    }

    /**
     * Check whether the object is a component of the specific class.
     *
     * @param object the object
     * @return {@code true} if the object is a component of the specific class,
     * otherwise {@code false}
     */
    public static boolean isComponentOfClass(Class<?> clazz, Object object) {
        if (object instanceof JComponent) {
            JComponent component = (JComponent) object;
            Container parent = SwingUtilities.getAncestorOfClass(clazz, component);

            return parent != null;
        }
        
        return false;
    }

    /**
     * Show the error message.
     *
     * @param message the error message
     */
    public static void showErrorMessage(String message) {
        showMessage(message, NotifyDescriptor.ERROR_MESSAGE);
    }

    private static void showMessage(String message, int messageType) {
        NotifyDescriptor descriptor = new NotifyDescriptor.Message(
                message,
                messageType
        );
        DialogDisplayer.getDefault().notifyLater(descriptor);
    }
}