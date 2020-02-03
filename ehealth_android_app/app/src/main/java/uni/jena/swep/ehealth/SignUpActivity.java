package uni.jena.swep.ehealth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    private EditText emailEt;
    private EditText passwordEt;

    private Button signUpBtn;
    private Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        emailEt = findViewById(R.id.email_edt_text);
        passwordEt = findViewById(R.id.pass_edt_text);

        loginBtn = findViewById(R.id.login_btn);
        signUpBtn = findViewById(R.id.signup_btn);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String email = emailEt.getText().toString();
                String password = passwordEt.getText().toString();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(SignUpActivity.this, "Please fill all the fields", Toast.LENGTH_LONG).show();
                } else {
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SignUpActivity.this, "Successfully Registered", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(SignUpActivity.this, "Registration Failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.v("firebase", e.getLocalizedMessage());
                            Toast.makeText(SignUpActivity.this, "Registration Failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
