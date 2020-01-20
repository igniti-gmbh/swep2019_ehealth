package uni.jena.swep.ehealth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.internal.WebDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class UpdatePasswordActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    private EditText passwordEt;

    private Button changePasswordBtn;
    private Button back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        passwordEt = findViewById(R.id.password_edt_text);

        changePasswordBtn = findViewById(R.id.reset_pass_btn);
        back = findViewById(R.id.back_btn);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = passwordEt.getText().toString();
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(UpdatePasswordActivity.this, "Please enter password", Toast.LENGTH_LONG).show();
                } else {
                    auth.getCurrentUser().updatePassword(password).addOnCompleteListener(UpdatePasswordActivity.this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(UpdatePasswordActivity.this, "Password changes successfully", Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                Toast.makeText(UpdatePasswordActivity.this, "password not changed", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
