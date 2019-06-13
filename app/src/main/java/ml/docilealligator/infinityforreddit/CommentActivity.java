package ml.docilealligator.infinityforreddit;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.styles.toolbar.IARE_Toolbar;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_AlignmentCenter;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_AlignmentLeft;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_AlignmentRight;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_At;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Bold;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Hr;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Image;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Italic;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Link;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_ListBullet;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_ListNumber;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Quote;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Strikethrough;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Subscript;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Superscript;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Underline;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Video;
import com.chinalwb.are.styles.toolitems.IARE_ToolItem;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CommentActivity extends AppCompatActivity {

    static final String COMMENT_PARENT_TEXT = "CPT";
    static final String EXTRA_COMMENT_DATA = "ECD";
    static final int WRITE_COMMENT_REQUEST_CODE = 1;

    @BindView(R.id.toolbar_comment_activity) Toolbar toolbar;
    @BindView(R.id.comment_parent_text_view_comment_activity) TextView commentParentTextView;
    @BindView(R.id.arEditText) AREditText commentEditor;
    @BindView(R.id.areToolbar) IARE_Toolbar editorToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        commentParentTextView.setText(getIntent().getExtras().getString(COMMENT_PARENT_TEXT));

        commentEditor.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        initToolbar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.comment_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_send_comment_activity:
                CommentData commentData = null;
                Intent returnIntent = new Intent();
                returnIntent.putExtra(EXTRA_COMMENT_DATA, commentData);
                setResult(RESULT_OK, returnIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        IARE_ToolItem bold = new ARE_ToolItem_Bold();
        IARE_ToolItem italic = new ARE_ToolItem_Italic();
        IARE_ToolItem underline = new ARE_ToolItem_Underline();
        IARE_ToolItem strikethrough = new ARE_ToolItem_Strikethrough();
        IARE_ToolItem quote = new ARE_ToolItem_Quote();
        IARE_ToolItem listNumber = new ARE_ToolItem_ListNumber();
        IARE_ToolItem listBullet = new ARE_ToolItem_ListBullet();
        IARE_ToolItem hr = new ARE_ToolItem_Hr();
        IARE_ToolItem link = new ARE_ToolItem_Link();
        IARE_ToolItem subscript = new ARE_ToolItem_Subscript();
        IARE_ToolItem superscript = new ARE_ToolItem_Superscript();
        IARE_ToolItem left = new ARE_ToolItem_AlignmentLeft();
        IARE_ToolItem center = new ARE_ToolItem_AlignmentCenter();
        IARE_ToolItem right = new ARE_ToolItem_AlignmentRight();
        IARE_ToolItem image = new ARE_ToolItem_Image();
        IARE_ToolItem video = new ARE_ToolItem_Video();
        IARE_ToolItem at = new ARE_ToolItem_At();
        editorToolbar.addToolbarItem(bold);
        editorToolbar.addToolbarItem(italic);
        editorToolbar.addToolbarItem(underline);
        editorToolbar.addToolbarItem(strikethrough);
        editorToolbar.addToolbarItem(quote);
        editorToolbar.addToolbarItem(listNumber);
        editorToolbar.addToolbarItem(listBullet);
        editorToolbar.addToolbarItem(hr);
        editorToolbar.addToolbarItem(link);
        editorToolbar.addToolbarItem(subscript);
        editorToolbar.addToolbarItem(superscript);
        editorToolbar.addToolbarItem(left);
        editorToolbar.addToolbarItem(center);
        editorToolbar.addToolbarItem(right);
        editorToolbar.addToolbarItem(image);
        editorToolbar.addToolbarItem(video);
        editorToolbar.addToolbarItem(at);

        commentEditor.setToolbar(editorToolbar);

        /*setHtml();

        initToolbarArrow();*/
    }
}
