package net.wagstrom.research.github;

import java.util.Date;

import com.github.api.v2.schema.User;

/**
 * This is a filler class because the GitHub API doesn't
 * distinguish between objects in a discussion, thus there
 * isn't a way to make this a new object.
 * 
 * @author patrick
 *
 */
public class PullRequestReviewComment {
    private String diffHunk;
    private String body;
    private String path;
    private int position;
    private String commitId;
    private String originalCommitId;
    private User user;
    private Date createdAt;
    private Date updatedAt;

    public String getDiffHunk() {
        return diffHunk;
    }
    public void setDiffHunk(String diffHunk) {
        this.diffHunk = diffHunk;
    }
    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
    public String getCommitId() {
        return commitId;
    }
    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }
    public String getOriginalCommitId() {
        return originalCommitId;
    }
    public void setOriginalCommitId(String originalCommitId) {
        this.originalCommitId = originalCommitId;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    public Date getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
