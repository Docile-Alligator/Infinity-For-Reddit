package ml.docilealligator.infinityforreddit.markdown;

import androidx.annotation.NonNull;

import java.util.List;

import io.noties.markwon.AbstractMarkwonPlugin;

public class CodeRangeParserPlugin extends AbstractMarkwonPlugin {
    private final List<CodeRangeParser.CodeRange> codeRanges;
    private final CodeRangeParser parser;

    CodeRangeParserPlugin(@NonNull List<CodeRangeParser.CodeRange> codeRanges) {
        codeRanges.clear();
        this.codeRanges = codeRanges;
        this.parser = new CodeRangeParser();
    }

    public static CodeRangeParserPlugin create(List<CodeRangeParser.CodeRange> codeRanges) {
        return new CodeRangeParserPlugin(codeRanges);
    }

    @NonNull
    @Override
    public String processMarkdown(@NonNull String markdown) {
        codeRanges.addAll(parser.parse(markdown));
        return markdown;
    }
}
