package ml.docilealligator.infinityforreddit.markdown;

import android.content.Context;

import androidx.annotation.Nullable;

import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TableBody;
import org.commonmark.ext.gfm.tables.TableCell;
import org.commonmark.ext.gfm.tables.TableHead;
import org.commonmark.ext.gfm.tables.TableRow;
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
import java.util.Stack;

import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonReducer;
import ml.docilealligator.infinityforreddit.UploadedImage;

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
    private static final String LIST_ITEM_E = "li";
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
    private static final String URL = "u";
    private static final String LEVEL = "l";
    private static final String IS_ORDERED_LIST = "o";
    private static final String TABLE_HEADER_CONTENT = "h";
    private static final String TABLE_CELL_ALIGNMENT = "a";
    private static final String TABLE_CELL_ALIGNMENT_LEFT = "l";
    private static final String TABLE_CELL_ALIGNMENT_CENTER = "c";
    private static final String TABLE_CELL_ALIGNMENT_RIGHT = "r";
    private static final String IMAGE_ID = "id";
    private static final String DOCUMENT = "document";

    private final Map<String, Integer> formatMap;
    private final JSONArray document;
    private StringBuilder textSB;
    private List<JSONArray> formats;
    private Stack<JSONArray> contentArrayStack;

    public RichTextJSONConverter() {
        formatMap = new HashMap<>();
        formatMap.put(StrongEmphasis.class.getName(), BOLD);
        formatMap.put(Emphasis.class.getName(), ITALICS);
        formatMap.put(Strikethrough.class.getName(), STRIKETHROUGH);
        formatMap.put(Superscript.class.getName(), SUPERSCRIPT);
        formatMap.put(Code.class.getName(), INLINE_CODE);

        document = new JSONArray();
        textSB = new StringBuilder();
        formats = new ArrayList<>();
        contentArrayStack = new Stack<>();

        contentArrayStack.push(document);
    }

    public String constructRichTextJSON(Context context, String markdown,
                                            List<UploadedImage> uploadedImages) throws JSONException {
        UploadedImagePlugin uploadedImagePlugin = new UploadedImagePlugin();
        uploadedImagePlugin.setUploadedImages(uploadedImages);
        Markwon markwon = MarkdownUtils.createContentSubmissionRedditMarkwon(
                context, uploadedImagePlugin);

        List<Node> nodes = MarkwonReducer.directChildren().reduce(markwon.parse(markdown));

        JSONObject richText = new JSONObject();

        for (Node n : nodes) {
            n.accept(this);
        }

        richText.put(DOCUMENT, document);
        return richText.toString();
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
    private JSONArray getFormatArray(Node node) {
        int formatNum = 0;
        while (node != null && node.getFirstChild() != null) {
            String className = node.getClass().getName();
            if (formatMap.containsKey(className)) {
                formatNum += formatMap.get(className);
            }
            node = node.getFirstChild();
        }
        if (node instanceof Text) {
            int start = textSB.length();
            textSB.append(((Text) node).getLiteral());
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

    private String getAllText(Node node) {
        node = node.getFirstChild();
        while (node != null) {
            Node next = node;
            while (next != null && next.getFirstChild() != null) {
                next = next.getFirstChild();
            }

            if (next instanceof Text) {
                textSB.append(((Text) next).getLiteral());
            } else if (next instanceof Code) {
                textSB.append(((Code) next).getLiteral());
            }

            node = node.getNext();
        }

        String text = textSB.toString();
        textSB.delete(0, text.length());
        return text;
    }

    private void convertToRawTextJSONObject(JSONArray contentArray) throws JSONException {
        for (int i = 0; i < contentArray.length(); i++) {
            JSONObject content = contentArray.getJSONObject(i);
            if (TEXT_E.equals(content.get(TYPE))) {
                content.put(TYPE, RAW_E);
            }
        }
    }

    @Override
    public void visit(BlockQuote blockQuote) {
        try {
            JSONObject nodeJSON = new JSONObject();
            nodeJSON.put(TYPE, BLOCKQUOTE_E);

            contentArrayStack.push(new JSONArray());

            Node child = blockQuote.getFirstChild();
            while (child != null) {
                child.accept(this);
                child = child.getNext();
            }

            JSONArray cArray = contentArrayStack.pop();

            nodeJSON.put(CONTENT, cArray);
            contentArrayStack.peek().put(nodeJSON);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(BulletList bulletList) {
        try {
            JSONObject nodeJSON = new JSONObject();
            nodeJSON.put(TYPE, LIST_E);

            contentArrayStack.push(new JSONArray());

            Node child = bulletList.getFirstChild();
            while (child != null) {
                child.accept(this);
                child = child.getNext();
            }

            JSONArray cArray = contentArrayStack.pop();

            nodeJSON.put(CONTENT, cArray);
            nodeJSON.put(IS_ORDERED_LIST, false);
            contentArrayStack.peek().put(nodeJSON);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(Code code) {
        JSONArray format = new JSONArray();
        format.put(INLINE_CODE);
        format.put(textSB.length());
        format.put(code.getLiteral().length());
        formats.add(format);

        textSB.append(code.getLiteral());
    }

    @Override
    public void visit(Document document) {
        //Ignore
    }

    @Override
    public void visit(Emphasis emphasis) {
        JSONArray format = getFormatArray(emphasis);
        if (format != null) {
            formats.add(format);
        }
    }

    @Override
    public void visit(FencedCodeBlock fencedCodeBlock) {
        try {
            JSONObject nodeJSON = new JSONObject();
            nodeJSON.put(TYPE, CODE_BLOCK_E);

            JSONArray cArray = new JSONArray();
            String codeLiteral = fencedCodeBlock.getLiteral();

            String[] codeLines = codeLiteral.split("\n");
            for (String c : codeLines) {
                JSONObject contentJSONObject = new JSONObject();
                contentJSONObject.put(TYPE, RAW_E);
                contentJSONObject.put(TEXT, c);
                cArray.put(contentJSONObject);
            }

            nodeJSON.put(CONTENT, cArray);
            contentArrayStack.peek().put(nodeJSON);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(HardLineBreak hardLineBreak) {
        //Ignore
    }

    @Override
    public void visit(Heading heading) {
        try {
            JSONObject nodeJSON = new JSONObject();
            nodeJSON.put(TYPE, HEADING_E);
            nodeJSON.put(LEVEL, heading.getLevel());

            contentArrayStack.push(new JSONArray());

            Node child = heading.getFirstChild();
            while (child != null) {
                child.accept(this);
                child = child.getNext();
            }

            JSONArray cArray = contentArrayStack.pop();

            if (textSB.length() > 0) {
                JSONObject content = new JSONObject();
                content.put(TYPE, RAW_E);
                content.put(TEXT, textSB.toString());

                cArray.put(content);
            }

            convertToRawTextJSONObject(cArray);
            nodeJSON.put(CONTENT, cArray);
            contentArrayStack.peek().put(nodeJSON);

            formats = new ArrayList<>();
            textSB.delete(0, textSB.length());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(ThematicBreak thematicBreak) {
        //Not supported by Reddit
    }

    @Override
    public void visit(HtmlInline htmlInline) {
        //Not supported by Reddit
    }

    @Override
    public void visit(HtmlBlock htmlBlock) {
        //Not supported by Reddit
    }

    @Override
    public void visit(Image image) {

    }

    @Override
    public void visit(IndentedCodeBlock indentedCodeBlock) {
        try {
            JSONObject nodeJSON = new JSONObject();
            nodeJSON.put(TYPE, CODE_BLOCK_E);

            JSONArray cArray = new JSONArray();
            String codeLiteral = indentedCodeBlock.getLiteral();

            String[] codeLines = codeLiteral.split("\n");
            for (String c : codeLines) {
                JSONObject contentJSONObject = new JSONObject();
                contentJSONObject.put(TYPE, RAW_E);
                contentJSONObject.put(TEXT, c);
                cArray.put(contentJSONObject);
            }

            nodeJSON.put(CONTENT, cArray);
            contentArrayStack.peek().put(nodeJSON);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(Link link) {
        try {
            if (textSB.length() > 0) {
                JSONObject content = new JSONObject();
                content.put(TYPE, TEXT_E);
                content.put(TEXT, textSB.toString());
                if (!formats.isEmpty()) {
                    JSONArray formatsArray = new JSONArray();
                    for (JSONArray f : formats) {
                        formatsArray.put(f);
                    }
                    content.put(FORMAT, formatsArray);
                }

                contentArrayStack.peek().put(content);

                formats = new ArrayList<>();
                textSB.delete(0, textSB.length());
            }

            //Construct link object
            JSONObject nodeJSON = new JSONObject();
            nodeJSON.put(TYPE, LINK_E);

            Node child = link.getFirstChild();
            while (child != null) {
                child.accept(this);
                child = child.getNext();
            }

            nodeJSON.put(TEXT, textSB.toString());
            //It will automatically escape the string.
            nodeJSON.put(URL, link.getDestination());

            if (!formats.isEmpty()) {
                JSONArray formatsArray = new JSONArray();
                for (JSONArray f : formats) {
                    formatsArray.put(f);
                }
                nodeJSON.put(FORMAT, formatsArray);
            }

            contentArrayStack.peek().put(nodeJSON);

            formats = new ArrayList<>();
            textSB.delete(0, textSB.length());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(ListItem listItem) {
        try {
            JSONObject nodeJSON = new JSONObject();
            nodeJSON.put(TYPE, LIST_ITEM_E);

            contentArrayStack.push(new JSONArray());

            Node child = listItem.getFirstChild();
            while (child != null) {
                child.accept(this);
                child = child.getNext();
            }

            JSONArray cArray = contentArrayStack.pop();

            nodeJSON.put(CONTENT, cArray);
            contentArrayStack.peek().put(nodeJSON);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(OrderedList orderedList) {
        try {
            JSONObject nodeJSON = new JSONObject();
            nodeJSON.put(TYPE, LIST_E);

            contentArrayStack.push(new JSONArray());

            Node child = orderedList.getFirstChild();
            while (child != null) {
                child.accept(this);
                child = child.getNext();
            }

            JSONArray cArray = contentArrayStack.pop();

            nodeJSON.put(CONTENT, cArray);
            nodeJSON.put(IS_ORDERED_LIST, true);
            contentArrayStack.peek().put(nodeJSON);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(Paragraph paragraph) {
        try {
            JSONObject nodeJSON = new JSONObject();
            nodeJSON.put(TYPE, PARAGRAPH_E);

            contentArrayStack.push(new JSONArray());

            Node child = paragraph.getFirstChild();
            while (child != null) {
                child.accept(this);
                child = child.getNext();
            }

            JSONArray cArray = contentArrayStack.pop();

            if (textSB.length() > 0) {
                JSONObject content = new JSONObject();
                content.put(TYPE, TEXT_E);
                content.put(TEXT, textSB.toString());
                if (!formats.isEmpty()) {
                    JSONArray formatsArray = new JSONArray();
                    for (JSONArray f : formats) {
                        formatsArray.put(f);
                    }
                    content.put(FORMAT, formatsArray);
                }

                cArray.put(content);
            }

            nodeJSON.put(CONTENT, cArray);
            contentArrayStack.peek().put(nodeJSON);

            formats = new ArrayList<>();
            textSB.delete(0, textSB.length());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(SoftLineBreak softLineBreak) {
        //Ignore
    }

    @Override
    public void visit(StrongEmphasis strongEmphasis) {
        JSONArray format = getFormatArray(strongEmphasis);
        if (format != null) {
            formats.add(format);
        }
    }

    @Override
    public void visit(Text text) {
        textSB.append(text.getLiteral());
    }

    @Override
    public void visit(LinkReferenceDefinition linkReferenceDefinition) {
        //Not supported by Reddit
    }

    @Override
    public void visit(CustomBlock customBlock) {
        if (customBlock instanceof TableBlock) {
            try {
                JSONObject nodeJSON = new JSONObject();
                nodeJSON.put(TYPE, TABLE_E);

                Node child = customBlock.getFirstChild();
                while (child != null) {
                    if (child instanceof TableHead) {
                        contentArrayStack.push(new JSONArray());

                        child.accept(this);

                        JSONArray hArray = contentArrayStack.pop();
                        nodeJSON.put(TABLE_HEADER_CONTENT, hArray);
                    } else if (child instanceof TableBody) {
                        contentArrayStack.push(new JSONArray());

                        child.accept(this);

                        JSONArray cArray = contentArrayStack.pop();
                        nodeJSON.put(CONTENT, cArray);
                    }
                    child = child.getNext();
                }


                contentArrayStack.peek().put(nodeJSON);

                formats = new ArrayList<>();
                textSB.delete(0, textSB.length());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else if (customBlock instanceof UploadedImageBlock) {
            //Nothing is allowed inside this block.
            try {
                JSONObject nodeJSON = new JSONObject();
                nodeJSON.put(TYPE, IMAGE_E);
                nodeJSON.put(IMAGE_ID, ((UploadedImageBlock) customBlock).uploadeImage.imageUrlOrKey);
                nodeJSON.put(CONTENT, ((UploadedImageBlock) customBlock).uploadeImage.getCaption());

                document.put(nodeJSON);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void visit(CustomNode customNode) {
        if (customNode instanceof Superscript) {
            /*
                Superscript can still has inline spans, thus checking children's next node until the end.
                Superscript must use ^(), not ^ right now.
            */
            Node child = customNode.getFirstChild();
            if (child == null) {
                //It may be ^Superscript
            } else {
                while (child != null) {
                    JSONArray format = getFormatArray(customNode);
                    if (format != null) {
                        formats.add(format);
                    }

                    Node next = child.getNext();
                    child.unlink();
                    child = next;
                }
            }
        } else if (customNode instanceof SpoilerNode) {
            //Spoiler cannot have styles
            try {
                JSONObject nodeJSON = new JSONObject();
                nodeJSON.put(TYPE, SPOILER_E);

                JSONArray cArray = new JSONArray();

                JSONObject contentJSONObject = new JSONObject();
                contentJSONObject.put(TYPE, TEXT_E);
                contentJSONObject.put(TEXT, getAllText(customNode));

                cArray.put(contentJSONObject);
                nodeJSON.put(CONTENT, cArray);
                contentArrayStack.peek().put(nodeJSON);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else if (customNode instanceof TableHead) {
            Node child = customNode.getFirstChild();
            while (child != null) {
                child.accept(this);
                child = child.getNext();
            }
        } else if (customNode instanceof TableBody) {
            Node child = customNode.getFirstChild();
            while (child != null) {
                if (child instanceof TableRow) {
                    contentArrayStack.push(new JSONArray());

                    child.accept(this);

                    JSONArray array = contentArrayStack.pop();
                    contentArrayStack.peek().put(array);
                }
                child = child.getNext();
            }
        } else if(customNode instanceof TableRow) {
            Node child = customNode.getFirstChild();
            while (child != null) {
                child.accept(this);
                child = child.getNext();
            }
        } else if (customNode instanceof TableCell) {
            try {
                JSONObject nodeJSON = new JSONObject();

                contentArrayStack.push(new JSONArray());

                Node child = customNode.getFirstChild();
                while (child != null) {
                    child.accept(this);
                    child = child.getNext();
                }

                JSONArray cArray = contentArrayStack.pop();

                if (textSB.length() > 0) {
                    JSONObject content = new JSONObject();
                    content.put(TYPE, TEXT_E);
                    content.put(TEXT, textSB.toString());
                    if (!formats.isEmpty()) {
                        JSONArray formatsArray = new JSONArray();
                        for (JSONArray f : formats) {
                            formatsArray.put(f);
                        }
                        content.put(FORMAT, formatsArray);
                    }

                    cArray.put(content);
                }

                nodeJSON.put(CONTENT, cArray);
                if (((TableCell) customNode).getAlignment() == null) {
                    nodeJSON.put(TABLE_CELL_ALIGNMENT, TABLE_CELL_ALIGNMENT_LEFT);
                } else {
                    switch (((TableCell) customNode).getAlignment()) {
                        case CENTER:
                            nodeJSON.put(TABLE_CELL_ALIGNMENT, TABLE_CELL_ALIGNMENT_CENTER);
                            break;
                        case RIGHT:
                            nodeJSON.put(TABLE_CELL_ALIGNMENT, TABLE_CELL_ALIGNMENT_RIGHT);
                            break;
                        default:
                            nodeJSON.put(TABLE_CELL_ALIGNMENT, TABLE_CELL_ALIGNMENT_LEFT);
                            break;
                    }
                }
                contentArrayStack.peek().put(nodeJSON);

                formats = new ArrayList<>();
                textSB.delete(0, textSB.length());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
