package org.chrizzly.netbeans.plugins.nbmimetypeswitcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Chrizzly
 */
public class MimeTypeOptions {
    private static final int LAST_SELECTED_MIMETYPES_DEFAULT_MAX_SIZE = 5;
    private static final MimeTypeOptions INSTANCE = new MimeTypeOptions();
    private static final String MIMETYPE = "mimetype"; // NOI18N
    private static final String LAST_SELECTED_MIMETYPES = "last.selected.mimetypes"; // NOI18N
    private static final String LAST_SELECTED_MIMETYPES_MAX_SIZE = "last.selected.mimetypes.max.size"; // NOI18N
    private static final String DELIMITER = "|"; // NOI18N

    public static MimeTypeOptions getInstance() {
        return INSTANCE;
    }

    private MimeTypeOptions() {}

    public int getLastSelectedMimeTypesMaxSize() {
        return getPreferences().getInt(LAST_SELECTED_MIMETYPES_MAX_SIZE, LAST_SELECTED_MIMETYPES_DEFAULT_MAX_SIZE);
    }

    public void setLastSelectedMimeTypesMazSize(int max) {
        getPreferences().putInt(LAST_SELECTED_MIMETYPES_MAX_SIZE, max);
    }

    public List<String> getLastSelectedMimeTypes() {
        String mimeTypeString = getPreferences().get(LAST_SELECTED_MIMETYPES, ""); // NOI18N

        if (mimeTypeString.isEmpty()) {
            return Collections.emptyList();
        }

        String[] mimeTypes = mimeTypeString.split("\\" + DELIMITER); // NOI18N

        return Arrays.asList(mimeTypes);
    }

    public void setLastSelectedMimeTypes(String mimeType) {
        List<String> selectedMimeTypes = new ArrayList<>(getLastSelectedMimeTypes());

        if (selectedMimeTypes.contains(mimeType)) {
            selectedMimeTypes.remove(mimeType);
            selectedMimeTypes.add(0, mimeType);
        } else {
            if (selectedMimeTypes.size() >= getLastSelectedMimeTypesMaxSize()) {
                selectedMimeTypes.remove(getLastSelectedMimeTypesMaxSize() - 1);
            }

            selectedMimeTypes.add(0, mimeType);
        }

        String join = String.join(DELIMITER, selectedMimeTypes);
        getPreferences().put(LAST_SELECTED_MIMETYPES, join);
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(MimeTypeOptions.class).node(MIMETYPE);
    }
}