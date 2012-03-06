class Defaults {
    static String DBPATH="/users/pwagstro/Documents/workspace/github/graph.20120210.db"
    static int MAX_DELTA = 2 * 1000 // 5 seconds max skew for github dates when brute forcing
    static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ"
    static List PROJECTS = ["rails/rails","mojombo/jekyll","defunkt/resque","mxcl/homebrew","tinkerpop/gremlin","tinkerpop/blueprints","tinkerpop/pipes","tinkerpop/rexster","tinkerpop/frames","rack/rack","sinatra/sinatra"]
    static List EVENT_TYPES = ["closed", "referenced", "merged", "mentioned", "unsubscribed", "assigned", "reopened"]
}