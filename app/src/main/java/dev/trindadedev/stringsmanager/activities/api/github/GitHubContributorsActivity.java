package dev.trindadedev.stringsmanager.activities.api.github;

import android.os.Bundle;
import android.util.Log;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import static dev.trindadedev.stringsmanager.StringsCreatorApp.Repo;
import dev.trindadedev.stringsmanager.StringsCreatorAppLog;
import dev.trindadedev.stringsmanager.adapters.ContributorsAdapter;
import dev.trindadedev.stringsmanager.classes.api.github.Contributor;
import dev.trindadedev.stringsmanager.classes.api.github.GitHubService;
import dev.trindadedev.stringsmanager.classes.api.github.User;
import dev.trindadedev.stringsmanager.databinding.GithubContributorsBinding;
import dev.trindadedev.stringsmanager.utils.ThemedActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GitHubContributorsActivity extends ThemedActivity {

    private static final String BASE_URL = "https://api.github.com/";
    private final StringsCreatorAppLog appLogger = new StringsCreatorAppLog();
    private GithubContributorsBinding binding;
    private ArrayList<HashMap<String, Object>> contributorsList = new ArrayList<>();
    private GitHubService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = GithubContributorsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(GitHubService.class);

        fetchContributors();
    }

    private void fetchContributors() {
        Call<List<Contributor>> call = service.getContributors(Repo.ONWER, Repo.NAME);

        call.enqueue(new Callback<List<Contributor>>() {
            @Override
            public void onResponse(Call<List<Contributor>> call, Response<List<Contributor>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Contributor> contributors = response.body();
                    for (Contributor contributor : contributors) {
                        fetchUserBio(contributor.getLogin(), contributor.getAvatarUrl());
                    }
                } else {
                    Log.e("GitHub API", "Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Contributor>> call, Throwable t) {
                Log.e("GitHub API", "Error: " + t.getMessage());
            }
        });
    }

    private void fetchUserBio(String username, String avatarUrl) {
        Call<User> userCall = service.getUser(username);
        userCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    String bio = user.getBio();
                    addContributor(username, bio, avatarUrl);
                } else {
                    Log.e("GitHub API", "Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("GitHub API", "Error: " + t.getMessage());
            }
        });
    }

    private void addContributor(String username, String bio, String avatarUrl) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("login", username);
        map.put("bio", bio);
        map.put("avatar-url", avatarUrl);
        contributorsList.add(map);
        binding.listCon.setAdapter(new ContributorsAdapter(GitHubContributorsActivity.this, contributorsList, binding.listCon));
    }
}