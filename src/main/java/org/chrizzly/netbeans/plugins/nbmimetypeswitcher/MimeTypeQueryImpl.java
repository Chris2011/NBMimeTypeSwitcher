package org.chrizzly.netbeans.plugins.nbmimetypeswitcher;

import java.util.ArrayList;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Chrizzly
 */
@ServiceProvider(service = MimeTypeQueryImpl.class, position = 10)
public final class MimeTypeQueryImpl {
    public static String getMimeType(FileObject file) {
        assert file != null;

        return file.getMIMEType();
    }

    public static List<String> getMimeTypes() {
        FileObject[] children = FileUtil.getConfigFile("Loaders").getChildren();  //NOI18N
        List<String> mimeTypes = new ArrayList<>();

        for (FileObject child : children) {
            String mime1 = child.getNameExt();
            FileObject[] subchildren = child.getChildren();

            for (FileObject subchild : subchildren) {
                FileObject factoriesFO = subchild.getFileObject("Factories");  //NOI18N

                if(factoriesFO != null && factoriesFO.getChildren().length > 0) {
                    // add only MIME types where some loader exists
                    mimeTypes.add(mime1 + "/" + subchild.getNameExt()); //NOI18N
                }
            }
        }

        mimeTypes.remove("content/unknown"); //NOI18N

        return mimeTypes;
    }
}