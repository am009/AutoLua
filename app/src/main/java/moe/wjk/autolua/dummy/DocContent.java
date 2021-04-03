package moe.wjk.autolua.dummy;

import android.content.Context;
import android.content.res.Resources;
import android.media.audiofx.AcousticEchoCanceler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import moe.wjk.autolua.R;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DocContent {

    /**
     * An array of sample (dummy) items.
     */
    private static final List<DocItem> ITEMS = new ArrayList<DocItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    private static final Map<String, DocItem> ITEM_MAP = new HashMap<String, DocItem>();

    public static int COUNT = -1;

    public static List<DocItem> getItems(Context context) {
        if (COUNT != -1) {
            return ITEMS;
        }
        Resources res = context.getResources();
        String[] doc_titles = res.getStringArray(R.array.doc_titles);
        String[] doc_urls = res.getStringArray(R.array.doc_urls);
        COUNT = Math.max(doc_titles.length, doc_urls.length);
        for (int pos=0; pos < COUNT; pos ++)
        {
            addItem(new DocItem(String.valueOf(pos + 1), doc_titles[pos], doc_urls[pos]));
        }
        return ITEMS;
    }

    private static void addItem(DocItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DocItem {
        public final String id;
        public final String title;
        public final String url;

        public DocItem(String id, String title, String url) {
            this.id = id;
            this.title = title;
            this.url = url;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}