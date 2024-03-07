package ml.docilealligator.infinityforreddit.markdown;

import androidx.annotation.Nullable;

import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.CustomBlock;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Document;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;
import org.commonmark.node.Visitor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RichTextJSONConverter implements Visitor {
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

    private final Map<String, Integer> formatMap;
    private final JSONArray document;

    public RichTextJSONConverter() {
        formatMap = new HashMap<>();
        formatMap.put(StrongEmphasis.class.getName(), BOLD);
        formatMap.put(Emphasis.class.getName(), ITALICS);
        formatMap.put(Strikethrough.class.getName(), STRIKETHROUGH);
        formatMap.put(Superscript.class.getName(), SUPERSCRIPT);
        formatMap.put(Code.class.getName(), INLINE_CODE);

        document = new JSONArray();
    }

    public JSONObject constructRichTextJSON(List<Node> nodes) throws JSONException {
        JSONObject richText = new JSONObject();

        for (Node n : nodes) {
            n.accept(this);
        }

        richText.put(DOCUMENT, document);

        return richText;
    }

    @Nullable
    private JSONArray getFormatArray(Node node, StringBuilder stringBuilder,
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

    @Override
    public void visit(BlockQuote blockQuote) {

    }

    @Override
    public void visit(BulletList bulletList) {

    }

    @Override
    public void visit(Code code) {

    }

    @Override
    public void visit(Document document) {

    }

    @Override
    public void visit(Emphasis emphasis) {

    }

    @Override
    public void visit(FencedCodeBlock fencedCodeBlock) {

    }

    @Override
    public void visit(HardLineBreak hardLineBreak) {

    }

    @Override
    public void visit(Heading heading) {

    }

    @Override
    public void visit(ThematicBreak thematicBreak) {

    }

    @Override
    public void visit(HtmlInline htmlInline) {

    }

    @Override
    public void visit(HtmlBlock htmlBlock) {

    }

    @Override
    public void visit(Image image) {

    }

    @Override
    public void visit(IndentedCodeBlock indentedCodeBlock) {

    }

    @Override
    public void visit(Link link) {

    }

    @Override
    public void visit(ListItem listItem) {

    }

    @Override
    public void visit(OrderedList orderedList) {

    }

    @Override
    public void visit(Paragraph paragraph) {
        try {
            JSONObject nodeJSON = new JSONObject();
            nodeJSON.put(TYPE, PARAGRAPH_E);

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
            nodeJSON.put(CONTENT, cArray);

            document.put(nodeJSON);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(SoftLineBreak softLineBreak) {

    }

    @Override
    public void visit(StrongEmphasis strongEmphasis) {

    }

    @Override
    public void visit(Text text) {

    }

    @Override
    public void visit(LinkReferenceDefinition linkReferenceDefinition) {

    }

    @Override
    public void visit(CustomBlock customBlock) {

    }

    @Override
    public void visit(CustomNode customNode) {

    }
}
