package ml.docilealligator.infinityforreddit.markdown;

import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.node.Code;
import org.commonmark.node.Emphasis;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class RichTextUtils {
    private static final int BOLD = 1;
    private static final int ITALICS = 2;
    private static final int STRIKETHROUGH = 8;
    private static final int SUPERSCRIPT = 32;
    private static final int INLINE_CODE = 64;

    private static final String PARAGRAPH_E = "par";
    private static final String TEXT_E = "text";
    private static final String HEADING_E = "h";
    private static final String LINK_E = "link";
    private static final String LIST_E = "list";
    private static final String LI_E = "li";
    private static final String BLOCKQUOTE_E = "blockquote";
    private static final String CODE_BLOCK_E = "code";
    //For lines in code block
    private static final String RAW_E = "raw";
    private static final String SPOILER_E = "spoilertext";
    private static final String TABLE_E = "table";
    private static final String IMAGE_E = "img";

    private static final String TYPE = "e";
    private static final String CONTENT = "c";
    private static final String TEXT = "t";
    private static final String FORMAT = "f";
    private static final String DOCUMENT = "document";

    public static JSONObject constructRichTextJSON(List<Node> nodes) throws JSONException {
        Map<String, Integer> formatMap = new HashMap<>();
        formatMap.put(StrongEmphasis.class.getName(), BOLD);
        formatMap.put(Emphasis.class.getName(), ITALICS);
        formatMap.put(Strikethrough.class.getName(), STRIKETHROUGH);
        formatMap.put(Superscript.class.getName(), SUPERSCRIPT);
        formatMap.put(Code.class.getName(), INLINE_CODE);

        JSONObject richText = new JSONObject();
        JSONArray document = new JSONArray();

        for (Node n : nodes) {
            JSONObject nodeJSON = getRichTextObject(n, formatMap);
            if (nodeJSON != null) {
                document.put(nodeJSON);
            }
        }
        richText.put(DOCUMENT, document);

        return richText;
    }

    private static JSONObject getRichTextObject(Node node, Map<String, Integer> formatMap) throws JSONException {
        if (node instanceof Paragraph) {
            return getRichTextObject((Paragraph) node, formatMap);
        }
        return null;
    }

    private static JSONObject getRichTextObject(Paragraph paragraph, Map<String, Integer> formatMap) throws JSONException {
        JSONObject result = new JSONObject();
        result.put(TYPE, PARAGRAPH_E);

        JSONArray cArray = new JSONArray();
        JSONObject content = new JSONObject();
        content.put(TYPE, TEXT_E);

        StringBuilder stringBuilder = new StringBuilder();
        List<JSONArray> formats = new ArrayList<>();

        Node child = paragraph.getFirstChild();
        while (child != null) {
            JSONArray format = getFormatArray(child, stringBuilder, formatMap);
            if (format != null) {
                formats.add(format);
            }
            child = child.getNext();
        }
        content.put(TEXT, stringBuilder.toString());
        if (!formats.isEmpty()) {
            JSONArray formatsArray = new JSONArray();
            for (JSONArray f : formats) {
                formatsArray.put(f);
            }
            content.put(FORMAT, formatsArray);
        }

        cArray.put(content);
        result.put(CONTENT, cArray);

        return result;
    }

    @Nullable
    private static JSONArray getFormatArray(Node node, StringBuilder stringBuilder,
                                                                     Map<String, Integer> formatMap) {
        int formatNum = 0;
        while (node != null && node.getFirstChild() != null) {
            formatNum += formatMap.get(node.getClass().getName());
            node = node.getFirstChild();
        }
        if (node instanceof Text) {
            int start = stringBuilder.length();
            stringBuilder.append(((Text) node).getLiteral());
            if (formatNum > 0) {
                JSONArray format = new JSONArray();
                format.put(formatNum);
                format.put(start);
                format.put(((Text) node).getLiteral().length());
                return format;
            }
        }

        return null;
    }
}
