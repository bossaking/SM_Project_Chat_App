package com.example.sm_project.AlertDialogues;

import android.graphics.Point;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.sm_project.R;

import java.util.Objects;

public class LoadingDialog extends DialogFragment {

    public static final String LOADING_DIALOG_TAG = "Loading";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.loading_dialog_layout, null);
        setCancelable(false);
        return view;
    }


}
