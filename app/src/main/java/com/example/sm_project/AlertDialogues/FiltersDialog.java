package com.example.sm_project.AlertDialogues;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;
import com.example.sm_project.R;

import java.util.*;

public class FiltersDialog extends DialogFragment {

    TextView languagesTextView;
    boolean[] selectedLanguage;
    List<Integer> languagesList = new ArrayList<>();
    String[] languagesArray;

    Button applyFiltersButton, cancelButton;
    SwitchCompat observedFirstSwitch;

    private filtersDialogListener filtersDialogListener;

    private boolean observedFirst;
    private final List<String> languages;

    public FiltersDialog(boolean observedFirst, List<String> languages){
        this.observedFirst = observedFirst;
        this.languages = languages;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        filtersDialogListener = (filtersDialogListener)context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.filters_dialog_layout, null);

        observedFirstSwitch = view.findViewById(R.id.observed_first_switch_compat);
        observedFirstSwitch.setChecked(observedFirst);

        languagesTextView = view.findViewById(R.id.languages_text_view);
        languagesArray = getResources().getStringArray(R.array.languages);
        selectedLanguage = new boolean[languagesArray.length];

        for(int i = 0; i < languagesArray.length; i++){
            if(languages.contains(languagesArray[i])){
                selectedLanguage[i] = true;
                languagesList.add(i);
                Collections.sort(languagesList);
            }
        }

        showLanguagesInTitle();

        languagesTextView.setOnClickListener(view1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.select_languages);
            builder.setCancelable(false);
            builder.setMultiChoiceItems(languagesArray, selectedLanguage, (dialogInterface, i, b) -> {
               if(b) {
                   languagesList.add(i);
                   Collections.sort(languagesList);
               }else{
                   languagesList.remove((Integer) i);
               }

                showLanguagesInTitle();

            });

            builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                dialogInterface.dismiss();
            });

            builder.show();

        });

        applyFiltersButton = view.findViewById(R.id.apply_filters_button);
        applyFiltersButton.setOnClickListener(view1 -> {
            observedFirst = observedFirstSwitch.isChecked();
            List<String> languages = new ArrayList<>();
            for (Integer integer : languagesList) {
                languages.add(languagesArray[integer]);
            }
            filtersDialogListener.applyFilters(observedFirst, languages);
            dismiss();
        });
        cancelButton = view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(view1 -> dismiss());

        return view;
    }

    private void showLanguagesInTitle(){
        StringBuilder stringBuilder = new StringBuilder();
        for(int j = 0; j < languagesList.size(); j++){
            stringBuilder.append(languagesArray[languagesList.get(j)]);
            if(j != languagesList.size() - 1){
                stringBuilder.append(", ");
            }
        }
        languagesTextView.setText(stringBuilder.toString());
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = Objects.requireNonNull(getDialog()).getWindow();

        int width = Resources.getSystem().getDisplayMetrics().widthPixels;

        window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
    }

    public interface filtersDialogListener{
        void applyFilters(boolean observedFirst, List<String> languages);
    }
}
