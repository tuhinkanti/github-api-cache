package com.github.api.cache;

public final class CacheServiceConstants {
    public static final String BASE_URL = "/";
    public static final String NETFLIX_ORG_URL = "/orgs/Netflix";
    public static final String NETFLIX_ORG_REPOS_URL = NETFLIX_ORG_URL + "/repos";
    public static final String NETFLIX_ORG_REPOS_TOP_N_FORKS_URL = "/view/top/{N}/forks";
    public static final String NETFLIX_ORG_REPOS_TOP_N_LAST_UPDATED_URL = "/view/top/{N}/last_updated";
    public static final String NETFLIX_ORG_REPOS_TOP_N_OPEN_ISSUES_URL = "/view/top/{N}/open_issues";
    public static final String NETFLIX_ORG_REPOS_TOP_N_STARS_URL = "/view/top/{N}/stars";
    public static final String NETFLIX_ORG_MEMBERS_URL = NETFLIX_ORG_URL + "/members";
    public static final String OTHER_URL = "/**";
}
