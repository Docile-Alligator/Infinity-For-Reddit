package ml.docilealligator.infinityforreddit.customviews;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import ml.docilealligator.infinityforreddit.R;

public class ColorPickerDialog extends AlertDialog {
    private View colorView;
    private EditText colorValueEditText;
    private SeekBar seekBarA;
    private SeekBar seekBarR;
    private SeekBar seekBarG;
    private SeekBar seekBarB;
    private Button cancelButton;
    private Button okButton;
    private int colorValue;
    private boolean changeColorValueEditText = true;
    private ColorPickerListener colorPickerListener;

    public interface ColorPickerListener {
        void onColorPicked(int color);
    }

    public ColorPickerDialog(Context context, int color, ColorPickerListener colorPickerListener) {
        super(context);

        View rootView = getLayoutInflater().inflate(R.layout.color_picker, null);
        colorView = rootView.findViewById(R.id.color_view_color_picker);
        colorValueEditText = rootView.findViewById(R.id.color_edit_text_color_picker);
        seekBarA = rootView.findViewById(R.id.a_seek_bar_color_picker);
        seekBarR = rootView.findViewById(R.id.r_seek_bar_color_picker);
        seekBarG = rootView.findViewById(R.id.g_seek_bar_color_picker);
        seekBarB = rootView.findViewById(R.id.b_seek_bar_color_picker);
        cancelButton = rootView.findViewById(R.id.cancel_button_color_picker);
        okButton = rootView.findViewById(R.id.ok_button_color_picker);

        colorView.setBackgroundColor(color);
        colorValueEditText.setText(Integer.toHexString(color).toUpperCase());
        colorValueEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = editable.toString();
                if (s.length() == 6) {
                    try {
                        changeColorValueEditText = false;
                        colorValue = Color.parseColor("#" + s);
                        colorView.setBackgroundColor(colorValue);
                        seekBarA.setProgress(255);
                        seekBarR.setProgress(Integer.parseInt(s.substring(0, 2), 16));
                        seekBarG.setProgress(Integer.parseInt(s.substring(2, 4), 16));
                        seekBarB.setProgress(Integer.parseInt(s.substring(4, 6), 16));
                        changeColorValueEditText = true;
                    } catch (IllegalArgumentException ignored) {

                    }
                } else if (s.length() == 8) {
                    try {
                        changeColorValueEditText = false;
                        colorValue = Color.parseColor("#" + s);
                        colorView.setBackgroundColor(colorValue);
                        seekBarA.setProgress(Integer.parseInt(s.substring(0, 2), 16));
                        seekBarR.setProgress(Integer.parseInt(s.substring(2, 4), 16));
                        seekBarG.setProgress(Integer.parseInt(s.substring(4, 6), 16));
                        seekBarB.setProgress(Integer.parseInt(s.substring(6, 8), 16));
                        changeColorValueEditText = true;
                    } catch (IllegalArgumentException ignored) {

                    }
                }
            }
        });

        String colorHex = Integer.toHexString(color);
        if (colorHex.length() == 8) {
            colorValue = Color.parseColor("#" + colorHex);
            seekBarA.setProgress(Integer.parseInt(colorHex.substring(0, 2), 16));
            seekBarR.setProgress(Integer.parseInt(colorHex.substring(2, 4), 16));
            seekBarG.setProgress(Integer.parseInt(colorHex.substring(4, 6), 16));
            seekBarB.setProgress(Integer.parseInt(colorHex.substring(6, 8), 16));
        } else if (colorHex.length() == 6) {
            colorValue = Color.parseColor("#" + colorHex);
            seekBarA.setProgress(255);
            seekBarR.setProgress(Integer.parseInt(colorHex.substring(0, 2), 16));
            seekBarG.setProgress(Integer.parseInt(colorHex.substring(2, 4), 16));
            seekBarB.setProgress(Integer.parseInt(colorHex.substring(4, 6), 16));
        }
        setOnSeekBarChangeListener(seekBarA);
        setOnSeekBarChangeListener(seekBarR);
        setOnSeekBarChangeListener(seekBarG);
        setOnSeekBarChangeListener(seekBarB);

        cancelButton.setOnClickListener(view -> dismiss());
        okButton.setOnClickListener(view -> {
            try {
                colorValue = Color.parseColor("#" + colorValueEditText.getText().toString());
                colorPickerListener.onColorPicked(colorValue);
                dismiss();
            } catch (IllegalArgumentException e) {
                Toast.makeText(context, R.string.invalid_color, Toast.LENGTH_SHORT).show();
            }
        });

        setView(rootView);
    }

    private void setOnSeekBarChangeListener(SeekBar seekBar) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (changeColorValueEditText) {
                    int aValue = seekBarA.getProgress();
                    int rValue = seekBarR.getProgress();
                    int gValue = seekBarG.getProgress();
                    int bValue = seekBarB.getProgress();
                    String colorHex = String.format("%02x%02x%02x%02x", aValue, rValue, gValue, bValue).toUpperCase();
                    colorValue = Color.parseColor("#" + colorHex);
                    colorView.setBackgroundColor(colorValue);
                    colorValueEditText.setText(colorHex);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
