package uni.jena.swep.ehealth.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import uni.jena.swep.ehealth.FirebaseInterface;
import uni.jena.swep.ehealth.R;
import uni.jena.swep.ehealth.UpdatePasswordActivity;

public class SettingsFragment extends Fragment {
    private SettingsViewModel settingsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        Button reset_btn = root.findViewById(R.id.settings_reset_pass_btn);
        Button update_btn = root.findViewById(R.id.btn_step_goal);
        final EditText input_field = root.findViewById(R.id.step_goal_input);

        update_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // get value from input field
                int new_value = Integer.parseInt(input_field.getText().toString());

                // get firebase
                FirebaseInterface fi = new FirebaseInterface(v.getContext());

                // update step goal value
                fi.uploadStepGoal(new_value);

                // clear step goal input field
                input_field.setText("");

                // close keyboard
                if (getActivity() != null) {
                    InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });

        reset_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // switch to password change activity
                Intent intent = new Intent(getActivity(), UpdatePasswordActivity.class);
                startActivity(intent);
            }
        });



        return root;
    }
}
