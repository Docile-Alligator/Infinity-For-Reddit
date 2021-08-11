package ml.docilealligator.infinityforreddit.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;

public class MarkdownBottomBarRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int BOLD = 0;
    public static final int ITALIC = 1;
    public static final int LINK = 2;
    public static final int STRIKE_THROUGH = 3;
    public static final int HEADER = 4;
    public static final int ORDERED_LIST = 5;
    public static final int UNORDERED_LIST = 6;
    public static final int SPOILER = 7;
    public static final int QUOTE = 8;
    public static final int CODE_BLOCK = 9;
    public static final int UPLOAD_IMAGE = 10;

    private static final int ITEM_COUNT = 10;

    private CustomThemeWrapper customThemeWrapper;
    private ItemClickListener itemClickListener;

    public interface ItemClickListener {
        void onClick(int item);
        void onUploadImage();
    }

    public MarkdownBottomBarRecyclerViewAdapter(CustomThemeWrapper customThemeWrapper,
                                                ItemClickListener itemClickListener) {
        this.customThemeWrapper = customThemeWrapper;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MarkdownBottomBarItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_markdown_bottom_bar, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MarkdownBottomBarItemViewHolder) {
            switch (position) {
                case BOLD:
                    ((MarkdownBottomBarItemViewHolder) holder).imageView.setImageResource(R.drawable.ic_bold_black_24dp);
                    break;
                case ITALIC:
                    ((MarkdownBottomBarItemViewHolder) holder).imageView.setImageResource(R.drawable.ic_italic_black_24dp);
                    break;
                case LINK:
                    ((MarkdownBottomBarItemViewHolder) holder).imageView.setImageResource(R.drawable.ic_link_round_black_24dp);
                    break;
                case STRIKE_THROUGH:
                    ((MarkdownBottomBarItemViewHolder) holder).imageView.setImageResource(R.drawable.ic_strikethrough_black_24dp);
                    break;
                case HEADER:
                    ((MarkdownBottomBarItemViewHolder) holder).imageView.setImageResource(R.drawable.ic_title_24dp);
                    break;
                case ORDERED_LIST:
                    ((MarkdownBottomBarItemViewHolder) holder).imageView.setImageResource(R.drawable.ic_ordered_list_black_24dp);
                    break;
                case UNORDERED_LIST:
                    ((MarkdownBottomBarItemViewHolder) holder).imageView.setImageResource(R.drawable.ic_unordered_list_black_24dp);
                    break;
                case SPOILER:
                    ((MarkdownBottomBarItemViewHolder) holder).imageView.setImageResource(R.drawable.ic_spoiler_black_24dp);
                    break;
                case QUOTE:
                    ((MarkdownBottomBarItemViewHolder) holder).imageView.setImageResource(R.drawable.ic_quote_24dp);
                    break;
                case CODE_BLOCK:
                    ((MarkdownBottomBarItemViewHolder) holder).imageView.setImageResource(R.drawable.ic_code_24dp);
                    break;
                case UPLOAD_IMAGE:
                    ((MarkdownBottomBarItemViewHolder) holder).imageView.setImageResource(R.drawable.ic_image_24dp);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return ITEM_COUNT;
    }

    public static void bindEditTextWithItemClickListener(Activity activity, EditText commentEditText, int item) {
        switch (item) {
            case MarkdownBottomBarRecyclerViewAdapter.BOLD: {
                int start = Math.max(commentEditText.getSelectionStart(), 0);
                int end = Math.max(commentEditText.getSelectionEnd(), 0);
                if (end != start) {
                    String currentSelection = commentEditText.getText().subSequence(start, end).toString();
                    commentEditText.getText().replace(Math.min(start, end), Math.max(start, end),
                            "**" + currentSelection + "**", 0, "****".length() + currentSelection.length());
                } else {
                    commentEditText.getText().replace(start, end,
                            "****", 0, "****".length());
                    commentEditText.setSelection(start + "**".length());
                }
                break;
            }
            case MarkdownBottomBarRecyclerViewAdapter.ITALIC: {
                int start = Math.max(commentEditText.getSelectionStart(), 0);
                int end = Math.max(commentEditText.getSelectionEnd(), 0);
                if (end != start) {
                    String currentSelection = commentEditText.getText().subSequence(start, end).toString();
                    commentEditText.getText().replace(Math.min(start, end), Math.max(start, end),
                            "*" + currentSelection + "*", 0, "**".length() + currentSelection.length());
                } else {
                    commentEditText.getText().replace(start, end,
                            "**", 0, "**".length());
                    commentEditText.setSelection(start + "*".length());
                }
                break;
            }
            case MarkdownBottomBarRecyclerViewAdapter.LINK: {
                View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_insert_link, null);
                TextInputEditText textEditText = dialogView.findViewById(R.id.edit_text_insert_link_dialog);
                TextInputEditText linkEditText = dialogView.findViewById(R.id.edit_link_insert_link_dialog);

                int start = Math.max(commentEditText.getSelectionStart(), 0);
                int end = Math.max(commentEditText.getSelectionEnd(), 0);
                if (end != start) {
                    String currentSelection = commentEditText.getText().subSequence(start, end).toString();
                    textEditText.setText(currentSelection);
                }

                new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.insert_link)
                        .setView(dialogView)
                        .setPositiveButton(R.string.ok, (editTextDialogInterface, i1)
                                -> {
                            String text = textEditText.getText().toString();
                            String link = linkEditText.getText().toString();
                            if (text.equals("")) {
                                text = link;
                            }

                            commentEditText.getText().replace(Math.min(start, end), Math.max(start, end),
                                    "[" + text + "](" + link + ")", 0, "[]()".length() + text.length() + link.length());
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                break;
            }
            case MarkdownBottomBarRecyclerViewAdapter.STRIKE_THROUGH: {
                int start = Math.max(commentEditText.getSelectionStart(), 0);
                int end = Math.max(commentEditText.getSelectionEnd(), 0);
                if (end != start) {
                    String currentSelection = commentEditText.getText().subSequence(start, end).toString();
                    commentEditText.getText().replace(Math.min(start, end), Math.max(start, end),
                            "~~" + currentSelection + "~~", 0, "~~~~".length() + currentSelection.length());
                } else {
                    commentEditText.getText().replace(start, end,
                            "~~~~", 0, "~~~~".length());
                    commentEditText.setSelection(start + "~~".length());
                }
                break;
            }
            case MarkdownBottomBarRecyclerViewAdapter.HEADER: {
                View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_select_header, null);
                Slider seekBar = dialogView.findViewById(R.id.seek_bar_dialog_select_header);
                new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.select_header_size)
                        .setView(dialogView)
                        .setPositiveButton(R.string.ok, (editTextDialogInterface, i1)
                                -> {
                            int start = Math.max(commentEditText.getSelectionStart(), 0);
                            int end = Math.max(commentEditText.getSelectionEnd(), 0);
                            String hashTags;
                            switch ((int) seekBar.getValue()) {
                                case 1:
                                    hashTags = "# ";
                                    break;
                                case 2:
                                    hashTags = "## ";
                                    break;
                                case 3:
                                    hashTags = "### ";
                                    break;
                                case 4:
                                    hashTags = "#### ";
                                    break;
                                case 5:
                                    hashTags = "##### ";
                                    break;
                                default:
                                    hashTags = "###### ";
                                    break;
                            }
                            if (end != start) {
                                String currentSelection = commentEditText.getText().subSequence(start, end).toString();
                                commentEditText.getText().replace(Math.min(start, end), Math.max(start, end),
                                        hashTags + currentSelection, 0, hashTags.length() + currentSelection.length());
                            } else {
                                commentEditText.getText().replace(start, end,
                                        hashTags, 0, hashTags.length());
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                break;
            }
            case MarkdownBottomBarRecyclerViewAdapter.ORDERED_LIST: {
                int start = Math.max(commentEditText.getSelectionStart(), 0);
                int end = Math.max(commentEditText.getSelectionEnd(), 0);
                if (end != start) {
                    String currentSelection = commentEditText.getText().subSequence(start, end).toString();
                    commentEditText.getText().replace(Math.min(start, end), Math.max(start, end),
                            "1. " + currentSelection, 0, "1. ".length() + currentSelection.length());
                } else {
                    commentEditText.getText().replace(start, end,
                            "1. ", 0, "1. ".length());
                }
                break;
            }
            case MarkdownBottomBarRecyclerViewAdapter.UNORDERED_LIST: {
                int start = Math.max(commentEditText.getSelectionStart(), 0);
                int end = Math.max(commentEditText.getSelectionEnd(), 0);
                if (end != start) {
                    String currentSelection = commentEditText.getText().subSequence(start, end).toString();
                    commentEditText.getText().replace(Math.min(start, end), Math.max(start, end),
                            "* " + currentSelection, 0, "* ".length() + currentSelection.length());
                } else {
                    commentEditText.getText().replace(start, end,
                            "* ", 0, "* ".length());
                }
                break;
            }
            case MarkdownBottomBarRecyclerViewAdapter.SPOILER: {
                int start = Math.max(commentEditText.getSelectionStart(), 0);
                int end = Math.max(commentEditText.getSelectionEnd(), 0);
                if (end != start) {
                    String currentSelection = commentEditText.getText().subSequence(start, end).toString();
                    commentEditText.getText().replace(Math.min(start, end), Math.max(start, end),
                            ">!" + currentSelection + "!<", 0, ">!!<".length() + currentSelection.length());
                } else {
                    commentEditText.getText().replace(start, end,
                            ">!!<", 0, ">!!<".length());
                    commentEditText.setSelection(start + ">!".length());
                }
                break;
            }
            case QUOTE: {
                int start = Math.max(commentEditText.getSelectionStart(), 0);
                int end = Math.max(commentEditText.getSelectionEnd(), 0);
                if (end != start) {
                    String currentSelection = commentEditText.getText().subSequence(start, end).toString();
                    commentEditText.getText().replace(Math.min(start, end), Math.max(start, end),
                            "> " + currentSelection + "\n\n", 0, "> \n\n".length() + currentSelection.length());
                } else {
                    commentEditText.getText().replace(start, end,
                            "> \n\n", 0, "> \n\n".length());
                    commentEditText.setSelection(start + "> ".length());
                }
                break;
            }
            case CODE_BLOCK: {
                int start = Math.max(commentEditText.getSelectionStart(), 0);
                int end = Math.max(commentEditText.getSelectionEnd(), 0);
                if (end != start) {
                    String currentSelection = commentEditText.getText().subSequence(start, end).toString();
                    commentEditText.getText().replace(Math.min(start, end), Math.max(start, end),
                            "```\n" + currentSelection + "\n```\n", 0, "```\n\n```\n".length() + currentSelection.length());
                } else {
                    commentEditText.getText().replace(start, end,
                            "```\n\n```\n", 0, "```\n\n```\n".length());
                    commentEditText.setSelection(start + "```\n".length());
                }
                break;
            }
        }
    }

    class MarkdownBottomBarItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public MarkdownBottomBarItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;
            itemView.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position == UPLOAD_IMAGE) {
                    itemClickListener.onUploadImage();
                } else {
                    itemClickListener.onClick(position);
                }
            });

            imageView.setColorFilter(customThemeWrapper.getPrimaryIconColor(), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }
}
