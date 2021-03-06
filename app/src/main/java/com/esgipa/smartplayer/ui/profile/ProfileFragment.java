package com.esgipa.smartplayer.ui.profile;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.esgipa.smartplayer.MainActivity;
import com.esgipa.smartplayer.R;
import com.esgipa.smartplayer.data.model.User;
import com.esgipa.smartplayer.music.MusicPlayerService;
import com.esgipa.smartplayer.ui.music.MusicFragment;
import com.esgipa.smartplayer.utils.UserProfileManager;
import com.esgipa.smartplayer.ui.authentication.SigninActivity;

public class ProfileFragment extends Fragment {
    private TextView name, username, email;

    private MusicPlayerService musicPlayerService;
    private MainActivity mainActivity;

    public ProfileFragment() {}

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        mainActivity = (MainActivity) requireActivity();
        musicPlayerService = mainActivity.getMusicPlayerService();
        name = root.findViewById(R.id.name);
        username = root.findViewById(R.id.username);
        email = root.findViewById(R.id.email);
        Button logout = root.findViewById(R.id.logout_button);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        updateUserInfor();
        return root;
    }

    private void updateUserInfor() {
        User user = UserProfileManager.getUserInfo(requireContext());
        name.setText(user.getName());
        username.setText(user.getUsername());
        email.setText(user.getEmail());
    }

    private void logout() {
        musicPlayerService.stopMedia();
        musicPlayerService.stopSelf();
        MusicFragment.stopRunnable();
        UserProfileManager.deleteUserInfo(requireContext());
        startActivity(new Intent(requireContext(), SigninActivity.class));
        requireActivity().finish();
    }
}
