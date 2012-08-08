#
# Get the projects with the most overlap with rails
#
DATA_FILE <- file.path(Sys.getenv("HOME"), "Google Drive", "Ecosystem Research",
                       "Data", "rails.db.20120505.coreMemberIntersections.txt")

# read in the data
data <- read.table(DATA_FILE, header=T, sep=",")
data <- data[with(data, order(-numUsers, destinationReponame)), ]

# at this point we've got a sorted data Frame. It just happens that if we set the
# cutoff to greater than 20 co-active participants we're also about 1% of the
# total projects. This provides us with 145 different projects

most.overlap <- data[data$numUsers > 20, ]
most.overlap
