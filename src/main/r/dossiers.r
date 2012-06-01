
fullDossiers <- read.csv("~/Google Drive/Ecosystem Research/Data/rails.db.20120505.dossiers.rails.csv")

#names(fullDossiers)

# as suspected, most of these a low with the highest means at just under 4 (community followers, committer and commitauthor)
summary(fullDossiers)

cor(fullDossiers[, c("issueassignee", "issuecommentowner", "closed", "reopened", "subscribed", "merged", "referenced", "mentioned", "assigned", "issueowner", "pullrequestcommentowner", "communityfollowers", "committer", "commitauthor")])
#                        issueassignee issuecommentowner    closed    reopened subscribed        merged  referenced    mentioned      assigned    issueowner pullrequestcommentowner communityfollowers    committer commitauthor
#issueassignee            1.0000000000     -0.0001661826 0.5993305 0.656693316  0.5258512  0.5691781984 0.573709455 0.6185791148  0.6457639354 -3.342236e-04             0.590779840       1.701147e-01 2.282965e-01 0.2560981704
#issuecommentowner       -0.0001661826      1.0000000000 0.2619974 0.015787421  0.1904588 -0.0000277145 0.024394357 0.0151655057 -0.0002061013  9.823293e-01             0.012516939      -5.858629e-04 1.044114e-03 0.0012408898
#closed                   0.5993304811      0.2619973522 1.0000000 0.785994913  0.8631835  0.9518022167 0.909113296 0.8832102599  0.6237020452  2.583494e-01             0.900026935       2.864700e-01 3.531135e-01 0.3668455674
#reopened                 0.6566933164      0.0157874212 0.7859949 1.000000000  0.7882497  0.7479494571 0.811848895 0.8407251790  0.7173879287  2.217947e-03             0.786722616       2.223961e-01 2.847824e-01 0.3126131415
#subscribed               0.5258511840      0.1904588036 0.8631835 0.788249711  1.0000000  0.7841481837 0.830381019 0.8621427310  0.6107319082  1.783010e-01             0.911628785       2.770521e-01 3.338836e-01 0.3542329404
#merged                   0.5691781984     -0.0000277145 0.9518022 0.747949457  0.7841482  1.0000000000 0.922425810 0.8701840699  0.5840875193 -1.250576e-04             0.885626502       2.850737e-01 3.479799e-01 0.3561287742
#referenced               0.5737094548      0.0243943567 0.9091133 0.811848895  0.8303810  0.9224258104 1.000000000 0.8801905940  0.6154634911  3.126435e-03             0.888555499       2.717560e-01 4.149031e-01 0.4256627182
#mentioned                0.6185791148      0.0151655057 0.8832103 0.840725179  0.8621427  0.8701840699 0.880190594 1.0000000000  0.8318594753  8.198764e-04             0.887060050       3.531509e-01 4.161460e-01 0.4444022411
#assigned                 0.6457639354     -0.0002061013 0.6237020 0.717387929  0.6107319  0.5840875193 0.615463491 0.8318594753  1.0000000000 -4.332361e-04             0.567284449       2.814626e-01 3.011071e-01 0.3365895551
#issueowner              -0.0003342236      0.9823293277 0.2583494 0.002217947  0.1783010 -0.0001250576 0.003126435 0.0008198764 -0.0004332361  1.000000e+00             0.001932639       1.934063e-05 4.333392e-05 0.0001555305
#pullrequestcommentowner  0.5907798405      0.0125169387 0.9000269 0.786722616  0.9116288  0.8856265016 0.888555499 0.8870600496  0.5672844489  1.932639e-03             1.000000000       2.901031e-01 3.692753e-01 0.3844762805
#communityfollowers       0.1701146929     -0.0005858629 0.2864700 0.222396129  0.2770521  0.2850737403 0.271755975 0.3531509346  0.2814625901  1.934063e-05             0.290103102       1.000000e+00 2.841101e-01 0.3166726221
#committer                0.2282965487      0.0010441142 0.3531135 0.284782397  0.3338836  0.3479798525 0.414903068 0.4161459846  0.3011071202  4.333392e-05             0.369275317       2.841101e-01 1.000000e+00 0.9945583291
#commitauthor             0.2560981704      0.0012408898 0.3668456 0.312613142  0.3542329  0.3561287742 0.425662718 0.4444022411  0.3365895551  1.555305e-04             0.384476281       3.166726e-01 9.945583e-01 1.0000000000


coreDossiers <- read.csv("~/Google Drive/Ecosystem Research/Data/rails.db.20120505.dossiers.csv")

names(coreDossiers)

#summary(coreDossiers)

summary(coreDossiers[, c("issueassignee", "issuecommentowner", "closed", "reopened", "subscribed", "merged", "referenced", "mentioned", "assigned", "issueowner", "pullrequestcommentowner", "communityfollowers", "committer", "commitauthor")])
#Min.   : 0.00000   Min.   :  0.0000   Min.   :   0.000   Min.   : 0.00000   Min.   :   0.000   Min.   :  0.0000   Min.   :  0.000   Min.   :  0.00   Min.   : 0.0000   Min.   : 0.0000   Min.   :   0.000        Min.   :   0.00    Min.   :   0.00   Min.   :   0.00  
#1st Qu.: 0.00000   1st Qu.:  0.0000   1st Qu.:   0.000   1st Qu.: 0.00000   1st Qu.:   0.000   1st Qu.:  0.0000   1st Qu.:  0.000   1st Qu.:  0.00   1st Qu.: 0.0000   1st Qu.: 0.0000   1st Qu.:   0.000        1st Qu.:   0.00    1st Qu.:   0.00   1st Qu.:   0.00  
#Median : 0.00000   Median :  0.0000   Median :   0.000   Median : 0.00000   Median :   0.000   Median :  0.0000   Median :  0.000   Median :  0.00   Median : 0.0000   Median : 0.0000   Median :   0.000        Median :   1.00    Median :   0.00   Median :   0.00  
#Mean   : 0.08669   Mean   :  0.9357   Mean   :   2.007   Mean   : 0.06756   Mean   :   4.823   Mean   :  0.9858   Mean   :  1.605   Mean   :  1.81   Mean   : 0.1661   Mean   : 0.2267   Mean   :   2.388        Mean   :  13.22    Mean   :  19.24   Mean   :  17.52  
#3rd Qu.: 0.00000   3rd Qu.:  0.0000   3rd Qu.:   0.000   3rd Qu.: 0.00000   3rd Qu.:   0.000   3rd Qu.:  0.0000   3rd Qu.:  0.000   3rd Qu.:  0.00   3rd Qu.: 0.0000   3rd Qu.: 0.0000   3rd Qu.:   0.000        3rd Qu.:   6.00    3rd Qu.:   0.00   3rd Qu.:   1.00  
#Max.   :64.00000   Max.   :513.0000   Max.   :1426.000   Max.   :24.00000   Max.   :1813.000   Max.   :969.0000   Max.   :990.000   Max.   :761.00   Max.   :97.0000   Max.   :57.0000   Max.   :1280.000        Max.   :1571.00    Max.   :3802.00   Max.   :3783.00 

summary(coreDossiers[coreDossiers$repository == "rails/rails", c("issueassignee", "issuecommentowner", "closed", "reopened", "subscribed", "merged", "referenced", "mentioned", "assigned", "issueowner", "pullrequestcommentowner", "communityfollowers", "committer", "commitauthor")])
#issueassignee     issuecommentowner     closed           reopened         subscribed          merged         referenced       mentioned         assigned        issueowner      pullrequestcommentowner communityfollowers   committer       commitauthor   
#Min.   : 0.0000   Min.   :  0.000   Min.   :   0.00   Min.   : 0.0000   Min.   :   0.00   Min.   :  0.00   Min.   :  0.00   Min.   :  0.00   Min.   : 0.000   Min.   : 0.0000   Min.   :   0.00         Min.   :   0.00    Min.   :   0.0   Min.   :   0.0  
#1st Qu.: 0.0000   1st Qu.:  0.000   1st Qu.:   0.00   1st Qu.: 0.0000   1st Qu.:   1.00   1st Qu.:  0.00   1st Qu.:  0.00   1st Qu.:  0.00   1st Qu.: 0.000   1st Qu.: 0.0000   1st Qu.:   0.00         1st Qu.:   3.00    1st Qu.:   1.0   1st Qu.:   1.0  
#Median : 0.0000   Median :  0.000   Median :   0.00   Median : 0.0000   Median :   4.00   Median :  0.00   Median :  0.00   Median :  1.00   Median : 0.000   Median : 0.0000   Median :   2.00         Median :  11.00    Median :   2.0   Median :   5.0  
#Mean   : 0.9632   Mean   :  4.479   Mean   :  23.16   Mean   : 0.7684   Mean   :  56.76   Mean   : 11.99   Mean   : 19.13   Mean   : 22.41   Mean   : 1.963   Mean   : 0.7263   Mean   :  28.53         Mean   :  67.85    Mean   : 183.6   Mean   : 159.6  
#3rd Qu.: 0.0000   3rd Qu.:  0.000   3rd Qu.:   1.00   3rd Qu.: 0.0000   3rd Qu.:  12.75   3rd Qu.:  0.00   3rd Qu.:  4.00   3rd Qu.:  4.75   3rd Qu.: 0.000   3rd Qu.: 0.0000   3rd Qu.:   9.00         3rd Qu.:  31.75    3rd Qu.:  11.0   3rd Qu.:  16.0  
#Max.   :64.0000   Max.   :453.000   Max.   :1426.00   Max.   :24.0000   Max.   :1813.00   Max.   :969.00   Max.   :990.00   Max.   :761.00   Max.   :97.000   Max.   :31.0000   Max.   :1280.00         Max.   :1571.00    Max.   :3802.0   Max.   :3783.0 

cor(coreDossiers[coreDossiers$repository == "rails/rails", c("issueassignee", "issuecommentowner", "closed", "reopened", "subscribed", "merged", "referenced", "mentioned", "assigned", "issueowner", "pullrequestcommentowner", "communityfollowers", "committer", "commitauthor")])
#                        issueassignee issuecommentowner      closed    reopened subscribed      merged   referenced   mentioned    assigned   issueowner pullrequestcommentowner communityfollowers   committer commitauthor
#issueassignee             1.000000000       0.046031339  0.61684961 0.700929735  0.5394024  0.55565909  0.588870993  0.62048663  0.66639508 -0.008025669              0.59782512        0.191259763  0.27716040   0.30408279
#issuecommentowner         0.046031339       1.000000000  0.11969731 0.196634815  0.2019843  0.06914988  0.225063741  0.17566019  0.03612936  0.029272788              0.18104650       -0.001671641  0.03778558   0.03958993
#closed                    0.616849607       0.119697313  1.00000000 0.832804096  0.8668715  0.98717591  0.983372967  0.92072303  0.63018825 -0.021045833              0.94786221        0.358518817  0.48305118   0.48127249
#reopened                  0.700929735       0.196634815  0.83280410 1.000000000  0.8204957  0.76947300  0.864963638  0.85449603  0.72049519  0.004511751              0.80665098        0.244380269  0.39829847   0.41557390
#subscribed                0.539402415       0.201984299  0.86687148 0.820495737  1.0000000  0.81122982  0.881336448  0.87508540  0.60829103  0.023141896              0.92628975        0.312451177  0.50022844   0.50023740
#merged                    0.555659090       0.069149881  0.98717591 0.769473000  0.8112298  1.00000000  0.967547290  0.88201958  0.56813452 -0.035308583              0.91083208        0.354361701  0.44141273   0.43557980
#referenced                0.588870993       0.225063741  0.98337297 0.864963638  0.8813364  0.96754729  1.000000000  0.91970693  0.62247158 -0.009187701              0.93703996        0.335312325  0.46526791   0.46334437
#mentioned                 0.620486629       0.175660190  0.92072303 0.854496029  0.8750854  0.88201958  0.919706927  1.00000000  0.82342320 -0.023265146              0.88688258        0.415410865  0.53879170   0.55228788
#assigned                  0.666395076       0.036129365  0.63018825 0.720495190  0.6082910  0.56813452  0.622471581  0.82342320  1.00000000 -0.026186906              0.54602241        0.336502786  0.37000819   0.40162882
#issueowner               -0.008025669       0.029272788 -0.02104583 0.004511751  0.0231419 -0.03530858 -0.009187701 -0.02326515 -0.02618691  1.000000000              0.02240018       -0.056786081 -0.01252750  -0.01681427
#pullrequestcommentowner   0.597825125       0.181046498  0.94786221 0.806650975  0.9262898  0.91083208  0.937039956  0.88688258  0.54602241  0.022400178              1.00000000        0.319580051  0.53015833   0.52211173
#communityfollowers        0.191259763      -0.001671641  0.35851882 0.244380269  0.3124512  0.35436170  0.335312325  0.41541086  0.33650279 -0.056786081              0.31958005        1.000000000  0.43067116   0.46201573
#committer                 0.277160397       0.037785584  0.48305118 0.398298475  0.5002284  0.44141273  0.465267911  0.53879170  0.37000819 -0.012527495              0.53015833        0.430671159  1.00000000   0.99318940
#commitauthor              0.304082792       0.039589928  0.48127249 0.415573900  0.5002374  0.43557980  0.463344372  0.55228788  0.40162882 -0.016814269              0.52211173        0.462015735  0.99318940   1.00000000

