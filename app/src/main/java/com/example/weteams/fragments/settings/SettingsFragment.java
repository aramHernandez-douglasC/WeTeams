package com.example.weteams.fragments.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.example.weteams.R;
import com.example.weteams.logic.Callbacks;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsFragment extends Fragment {
    private FirebaseUser user;
    private TextView textUsername;
    private ListView listViewSetting;
    private View dialogView;
    private ActionBar actionBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        textUsername = view.findViewById(R.id.textUsername);
        listViewSetting = view.findViewById(R.id.listViewSetting);

        user = FirebaseAuth.getInstance().getCurrentUser();
        textUsername.setText(user.getDisplayName());

        listViewSetting.setAdapter(new SettingListViewAdapter());

        listViewSetting.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                switch (position) {
                    case 1: //CHANGE USER NAME
                        dialogView = getLayoutInflater().inflate(R.layout.custom_dia, null);
                        builder.setView(dialogView);

                        builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText editText = dialogView.findViewById(R.id.editText);
                                final String name = editText.getText().toString();
                                if (TextUtils.isEmpty(name)) {
                                    Toast.makeText(getContext(), "Display name cannot be empty!", Toast.LENGTH_LONG).show();
                                    return;
                                } else {
                                    try {
                                        updateUserProfile(user, name, new Callbacks<FirebaseUser>() {
                                            @Override
                                            public void onSuccess(FirebaseUser user) {
                                                textUsername.setText(name);
                                                Toast.makeText(getContext(), "Display name changed!", Toast.LENGTH_LONG).show();
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                            }
                                        });
                                    } catch (Exception e) {
                                        Toast.makeText(getContext(), "Error!\nDisplay name is not set!", Toast.LENGTH_LONG).show();
                                        Log.wtf("CHANGE USER NAME", e.getMessage());
                                    }
                                }
                            }
                        });

                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.show();
                        break;

                    case 2: //CHANGE PASSWORD
                        dialogView = getLayoutInflater().inflate(R.layout.dia_change_pass, null);
                        builder.setView(dialogView);

                        builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText curPass, newPass, reNewPass;
                                curPass = dialogView.findViewById(R.id.editOldPass);
                                newPass = dialogView.findViewById(R.id.editNewPass);
                                reNewPass = dialogView.findViewById(R.id.editReNewPass);

                                String curPassText = curPass.getText().toString();
                                String newPassText = newPass.getText().toString();
                                String reNewPassText = reNewPass.getText().toString();

                                if (TextUtils.isEmpty(curPassText) || TextUtils.isEmpty(newPassText)) {
                                    Toast.makeText(getContext(), "Please enter a password", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (curPassText.equals(newPassText)) {
                                    Toast.makeText(getContext(), "New password cannot be the same as current", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (!newPassText.equals(reNewPassText)) {
                                    Toast.makeText(getContext(), "The passwords do not match", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                try {
                                    updateUserPassword(user, curPassText, newPassText, new Callbacks<FirebaseUser>() {
                                        @Override
                                        public void onSuccess(FirebaseUser user) {
                                            Toast.makeText(getContext(), "Password changed", Toast.LENGTH_LONG).show();
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            if (e instanceof FirebaseAuthInvalidCredentialsException)
                                                Toast.makeText(getContext(), "Wrong current password", Toast.LENGTH_LONG).show();
                                            e.printStackTrace();
                                        }
                                    });
                                } catch (Exception e) {

                                    Log.wtf("CHANGE PASSWORD", e.getMessage());
                                }
                            }
                        });

                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.show();
                        break;

                    case 3: //SIGN OUT
                        new AlertDialog.Builder(getContext()).setTitle("Sign Out").setMessage("Do you want to sign out?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        FirebaseAuth.getInstance().signOut();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).show();
                        break;
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle("Settings");
    }

    class SettingListViewAdapter extends BaseAdapter {
        List<Integer> pics;
        List<String> text;

        public SettingListViewAdapter() {
            pics = new ArrayList<>(Arrays.asList(R.drawable.email_icon, R.drawable.user_icon, R.drawable.password_icon, R.drawable.signout_icon));
            text = new ArrayList<>(Arrays.asList(user.getEmail(), "Change Username", "Change Password", "Sign Out"));
        }

        @Override
        public int getCount() {
            return text.size();
        }

        @Override
        public Object getItem(int position) {
            return text.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout, null);

            Drawable d = AppCompatResources.getDrawable(convertView.getContext(), pics.get(position));
            TextView text = convertView.findViewById(R.id.listText);
            ImageView i = convertView.findViewById(R.id.listImg);

            i.setImageDrawable(d);
            text.setText(this.text.get(position));

            return convertView;
        }
    }

    public static void updateUserProfile(
            final FirebaseUser user,
            final String displayName,
            final Callbacks<FirebaseUser> callbacks
    ) {
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();
        user.updateProfile(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        updateUserDocument(user, displayName, callbacks);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        callbacks.onFailure(e);
                    }
                });
    }

    public static void updateUserPassword(
            final FirebaseUser user,
            final String oldPassword,
            final String newPassword,
            final Callbacks<FirebaseUser> callbacks
    ) {
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
        user.reauthenticate(credential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                user.updatePassword(newPassword)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                callbacks.onSuccess(user);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                                callbacks.onFailure(e);
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callbacks.onFailure(e);
            }
        });

    }

    public static void updateUserDocument(
            final FirebaseUser user,
            String displayName,
            final Callbacks<FirebaseUser> callbacks
    ) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("email", user.getEmail());
        profile.put("displayName", displayName);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid()).set(profile)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callbacks.onSuccess(user);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        callbacks.onFailure(e);
                    }
                });
    }
}
