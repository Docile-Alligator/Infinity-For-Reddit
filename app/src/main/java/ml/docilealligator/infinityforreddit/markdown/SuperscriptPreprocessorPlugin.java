package ml.docilealligator.infinityforreddit.markdown;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.noties.markwon.AbstractMarkwonPlugin;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class SuperscriptPreprocessorPlugin extends AbstractMarkwonPlugin {
    private final CodeRangeParser parser;

    SuperscriptPreprocessorPlugin() {
        this.parser = new CodeRangeParser();
    }

    public static SuperscriptPreprocessorPlugin create() {
        return new SuperscriptPreprocessorPlugin();
    }

    @NonNull
    @Override
    public String processMarkdown(@NonNull String markdown) {
        ArrayList<CodeRangeParser.CodeRange> codeRanges = parser.parse(markdown);
        return Utils.fixSuperScript(markdown, codeRanges);
    }
}
