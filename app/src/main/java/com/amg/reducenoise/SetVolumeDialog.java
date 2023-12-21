package com.amg.reducenoise;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

/* loaded from: classes3.dex */
public class SetVolumeDialog extends DialogFragment {
    Button cancel;
    ImageButton decrease;
    EditText editInput;
    ImageButton increase;
    Button ok;

    public static SetVolumeDialog newInstance() {
        SetVolumeDialog dialog = new SetVolumeDialog();
        Bundle bundle = new Bundle();
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override // android.app.DialogFragment, android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override // android.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.volume_dialog, container, true);
        this.decrease = (ImageButton) view.findViewById(R.id.decrease);
        this.increase = (ImageButton) view.findViewById(R.id.increase);
        this.editInput = (EditText) view.findViewById(R.id.editInput);
        this.decrease.setOnClickListener(new View.OnClickListener() { // from class: com.amg.noisecleaner.SetVolumeDialog.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                int value = 1;
                try {
                    int value2 = Integer.parseInt(SetVolumeDialog.this.editInput.getText().toString());
                    value = value2 - 1;
                } catch (Exception e) {
                }
                SetVolumeDialog.this.editInput.setText(value + "");
            }
        });
        this.increase.setOnClickListener(new View.OnClickListener() { // from class: com.amg.noisecleaner.SetVolumeDialog.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                int value = 1;
                try {
                    int value2 = Integer.parseInt(SetVolumeDialog.this.editInput.getText().toString());
                    value = value2 + 1;
                } catch (Exception e) {
                }
                SetVolumeDialog.this.editInput.setText(value + "");
            }
        });
        Button button = (Button) view.findViewById(R.id.cancel);
        this.cancel = button;
        button.setOnClickListener(new View.OnClickListener() { // from class: com.amg.noisecleaner.SetVolumeDialog.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                SetVolumeDialog.this.dismiss();
            }
        });
        Button button2 = (Button) view.findViewById(R.id.ok);
        this.ok = button2;
        button2.setOnClickListener(new View.OnClickListener() { // from class: com.amg.noisecleaner.SetVolumeDialog.4
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                try {
                    MainActivity main = (MainActivity) SetVolumeDialog.this.getActivity();
                    int volume = Integer.parseInt(SetVolumeDialog.this.editInput.getText().toString());
                    //main.changeVolume(volume);
                } catch (Exception e) {
                }
            }
        });
        return view;
    }
}
