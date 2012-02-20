package net.wagstrom.research.github;

import com.github.api.v2.services.GitHubException;
import com.google.gson.Gson;

public class GitHubErrorPrimative {
    String error;

    public GitHubErrorPrimative() {
    }

    public String getError() {
        return error;
    }

    public static GitHubErrorPrimative createGitHubErrorPrimative(GitHubException e) {
        Gson gson = new Gson();
        GitHubErrorPrimative primative = gson.fromJson(e.getMessage(), GitHubErrorPrimative.class);
        return primative;
    }
}
