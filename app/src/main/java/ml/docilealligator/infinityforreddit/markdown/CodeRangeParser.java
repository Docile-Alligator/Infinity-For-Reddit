package ml.docilealligator.infinityforreddit.markdown;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import kotlin.text.MatchGroup;
import kotlin.text.MatchResult;
import kotlin.text.Regex;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class CodeRangeParser {
    private static final Regex FENCE_OPEN = new Regex(Pattern.compile("^ {0,3}(([\\`\\~])\\2{2,})[^\\`\\n]*(?:\\n|$)"));
    private static final Regex FENCE_CLOSE = new Regex(Pattern.compile("^ {0,3}(([\\`\\~])\\2{2,})\\s*$"));

    private int position;
    private Integer lastNonParagraphLine = null;

    public final ArrayList<CodeRange> parse(String input) {
        ArrayList<CodeRange> allCodeRanges = new ArrayList<>();

        if (TextUtils.isEmpty(input)) {
            return allCodeRanges;
        }

        LineIterator<String> lines;
        lines = new LineIterator<>(new ArrayList<>(Arrays.asList(input.split("\n"))).listIterator());

        lines.moveNext();

        while (!lines.isEnd()) {
            CodeRange codeRange = blockParse(lines);
            if (codeRange == null && !lines.isEnd()) {
                String lineTrimmed = lines.getCurrent().trim();
                if (TextUtils.isEmpty(lineTrimmed) || lineTrimmed.startsWith("#")) {
                    lastNonParagraphLine = lines.getIndex();
                }
                position += lines.getCurrent().length() + 1;
                lines.moveNext();
            } else if (codeRange != null && codeRange.end != 0) {
                lastNonParagraphLine = lines.getIndex() - 1;
                int end = codeRange.end == -1 ? input.length() : codeRange.end;
                allCodeRanges.add(new CodeRange(codeRange.start, end));
            }
        }

        position = 0;
        lastNonParagraphLine = null;

        ArrayList<CodeRange> inlineCodeRanges = new ArrayList<>();
        if (allCodeRanges.size() > 0) {
            for (int i = 0; i < allCodeRanges.size(); i++) {
                CodeRange codeRange = allCodeRanges.get(i);
                int prev = codeRange.end + 1;
                int next;
                if ((i + 1) < allCodeRanges.size()) {
                    next = allCodeRanges.get(i + 1).start;
                } else {
                    next = input.length();
                }

                if (prev < input.length() && next > prev) {
                    for (CodeRange inline : inlineParse(input, prev, next)) {
                        inlineCodeRanges.add(new CodeRange(inline.start, inline.end));
                    }
                }
            }
        } else {
            for (CodeRange inline : inlineParse(input, null, null)) {
                inlineCodeRanges.add(new CodeRange(inline.start, inline.end));
            }
        }

        allCodeRanges.addAll(inlineCodeRanges);

        return allCodeRanges;
    }

    private CodeRange blockParse(LineIterator<String> lines) {
        if (lines.isEnd()) {
            return null;
        }

        String line = lines.getCurrent();

        if (FENCE_OPEN.matches(line)) {
            return parseBackticks(lines);
        }

        if (lines.getIndex() == 0) {
            if (matchBlockPrefix(line) != null) {
                return parseIndent(lines);
            }
        } else if (lastNonParagraphLine != null && lastNonParagraphLine == lines.getIndex() - 1) {
            if (matchBlockPrefix(line) != null) {
                return parseIndent(lines);
            }
        }

        return null;
    }

    private CodeRange parseBackticks(LineIterator<String> lines) {
        String line = lines.getCurrent();

        MatchResult matches = FENCE_OPEN.matchEntire(line);
        MatchGroup fenceMatch;
        String fence;
        int start;
        int end = -1;

        if (matches != null
                && matches.getGroups().size() >= 2
                && (fenceMatch = matches.getGroups().get(1)) != null) {
            fence = fenceMatch.getValue().trim();
            start = position + fenceMatch.getRange().getStart();
        } else {
            return null;
        }

        lines.moveNext();
        while (true) {
            position += line.length() + 1;
            if (lines.isEnd()) {
                break;
            }
            line = lines.getCurrent();
            lines.moveNext();

            MatchResult close_match = FENCE_CLOSE.matchEntire(line);
            MatchGroup close_fence;
            if (close_match != null
                    && close_match.getGroups().size() >= 1
                    && (close_fence = close_match.getGroups().get(0)) != null) {
                String close_fence_trimmed = close_fence.getValue().trim();
                if (close_fence_trimmed.charAt(0) == fence.charAt(0) && close_fence_trimmed.length() >= fence.length()) {
                    end = position + close_fence.getRange().getStart() + Utils.trimTrailingWhitespace(close_fence.getValue()).length() - 1;
                    position += line.length() + 1;
                    break;
                }
            }
        }

        return new CodeRange(start, end);
    }

    private CodeRange parseIndent(LineIterator<String> lines) {
        Integer start = null;
        int end = 0;

        while (!lines.isEnd()) {
            String line = lines.getCurrent();

            Integer prefix_length = matchBlockPrefix(line);
            if (prefix_length == null) {
                break;
            }

            if (start == null) {
                start = position;
            }

            position += line.length() + 1;
            end = position - 1;

            lines.moveNext();
        }

        return new CodeRange(start != null ? start : 0, end);
    }

    private static Integer matchBlockPrefix(String line) {
        int characters = 0;
        int indents = 0;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (indents == 4) {
                break;
            }

            if (c == ' ') {
                characters++;
                indents++;
            } else if (c == '\t') {
                characters++;
                indents = 4;
            } else {
                break;
            }
        }

        if (indents == 4) {
            return characters;
        }

        return null;
    }

    private static List<CodeRange> inlineParse(String input, @Nullable Integer start, @Nullable Integer end) {
        List<CodeRange> codeRanges = new ArrayList<>();
        int i = start == null ? 0 : start;
        int length = end == null ? input.length() : end;
        int openingStart;
        int openingLength;

        while (i < length) {
            InlineCodeStart match = matchInlinePrefix(input, i, length);
            if (match != null) {
                openingStart = match.start;
                openingLength = match.backticks;
            } else {
                break;
            }

            i = openingStart + openingLength;

            if (openingStart < 1 && openingLength < 1) {
                break;
            }

            Integer closingEnd = matchInlineClosing(input, i, length, openingLength);
            if (closingEnd != null && closingEnd > 0) {
                i = closingEnd + 1;
                codeRanges.add(new CodeRange(openingStart, closingEnd));
            }
        }

        return codeRanges;
    }

    @SuppressLint("UnsafeOptInUsageError")
    private static InlineCodeStart matchInlinePrefix(String input, int startIndex, int length) {
        Integer start = null;
        int backticks = 0;
        int indents = 0;
        int escapes = 0;
        int newLines = 0;
        int first_new_line_position = startIndex;
        Integer last_new_line_position = null;
        boolean skipToNextLine = false;

        for (int i = startIndex; i < length; i++) {
            char c = input.charAt(i);
            if (indents == 4) {
                skipToNextLine = true;
                backticks = 0;
                indents = 0;
                escapes = 0;
                newLines = 0;
            }

            if (skipToNextLine) {
                if (c == '\n') {
                    skipToNextLine = false;
                } else {
                    continue;
                }
            }

            if (c == ' ') {
                indents++;
            } else if (c == '\n') {
                newLines++;
                if (last_new_line_position != null) {
                    first_new_line_position = last_new_line_position;
                }
                last_new_line_position = i;
            } else if (c == '\t') {
                indents = 4;
            } else if (c == '\\') {
                escapes++;
            } else if (c == '`' && escapes % 2 == 0) {
                if (start == null) {
                    start = i;
                }
                backticks++;
                indents = 0;
                newLines = 0;
            }

            if (c != '`') {
                int matchStartIndex = last_new_line_position == null ? first_new_line_position : last_new_line_position;
                if (start != null && backticks >= 3 && FENCE_OPEN.matchesAt(input, matchStartIndex)) {
                    return null;
                } else if (start != null && backticks > 0 && !(matchStartIndex == start - 1 && backticks >= 3 && newLines >= 1)) {
                    return new InlineCodeStart(start, backticks);
                }
                start = null;
                backticks = 0;
                if (c != ' ' && c != '\t') {
                    indents = 0;
                }
                if (c != '\n') {
                    newLines = 0;
                }
                if (c != '\\') {
                    escapes = 0;
                }
            }
        }

        if (start != null && backticks > 0) {
            return new InlineCodeStart(start, backticks);
        }

        return null;
    }

    private static Integer matchInlineClosing(String input, int startIndex, int endIndex, int openingLength) {
        int backticks = 0;
        int newLines = 0;
        int indents = 0;
        for (int i = startIndex; i <= endIndex; i++) {
            char c = peek(i, input);

            if (openingLength >= 3 && indents == 4) {
                break;
            }

            if (c == '\n') {
                newLines++;
                if (backticks == openingLength && !(openingLength >= 3 && newLines > 1)) {
                    return i - 1;
                } else if (newLines > 1) {
                    break;
                }
            } else if (c == ' ') {
                indents++;
            } else if (c == '\t') {
                indents = 4;
            } else if (c == '\0') {
                return (backticks == openingLength) ? i - 1 : null;
            } else if (c == '`') {
                backticks++;
            }

            if (c != '`') {
                if (openingLength != 0 && backticks == openingLength) {
                    return i - 1;
                }
                backticks = 0;
                if (c != '\n') {
                    newLines = 0;
                }
                if (c != ' ' && c != '\t') {
                    indents = 0;
                }
            }
        }

        return null;
    }

    private static char peek(int index, String line) {
        return index >= 0 && index < line.length() ? line.charAt(index) : '\0';
    }

    public static class CodeRange {
        public final int start;
        public final int end;

        CodeRange(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    private static class InlineCodeStart {
        final int start;
        final int backticks;

        InlineCodeStart(int start, int backticks) {
            this.start = start;
            this.backticks = backticks;
        }
    }

    private static class LineIterator<E> {
        private final ListIterator<E> iterator;
        private int index;
        private E current;
        private boolean isEnd;

        private LineIterator(ListIterator<E> iterator) {
            this.iterator = iterator;
        }

        @SuppressWarnings("UnusedReturnValue")
        private boolean moveNext() {
            if (iterator.hasNext()) {
                index = iterator.nextIndex();
                current = iterator.next();
                isEnd = false;
                return true;
            }
            isEnd = true;
            return false;
        }

        private int getIndex() {
            return index;
        }

        private E getCurrent() {
            return current;
        }

        private boolean isEnd() {
            return isEnd;
        }
    }
}
