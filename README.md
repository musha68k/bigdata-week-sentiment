# bigdata-week-sentiment
This project illustrates the use of an mllib pipeline to train a model to classify tweets according to the sentiment they convey (positive, negative).

*install maven*
```
sudo apt-get install maven
```
*run the code*
```
mvn scala:run
```

This is using Sentiment140 [dataset](http://help.sentiment140.com/for-students).


Here's one line from the .csv file in the dataset:

a line from training.1600000.processed.noemoticon.csv

"0","1467814438","Mon Apr 06 22:20:44 PDT 2009","NO_QUERY","ChicagoCubbie","I hate when I have to call and wake people up "


a line from from testdata.manual.2009.06.14.csv :

"4","23","Mon May 11 05:22:03 UTC 2009","lebron","princezzcutz","@sketchbug Lebron is a hometown hero to me, lol I love the Lakers but let's go Cavs, lol"


Data file format has 6 fields:
0 - the polarity of the tweet (0 = negative, 2 = neutral, 4 = positive)
1 - the id of the tweet (2087)
2 - the date of the tweet (Sat May 16 23:58:44 UTC 2009)
3 - the query (lyx). If there is no query, then this value is NO_QUERY.
4 - the user that tweeted (robotickilldozr)
5 - the text of the tweet (Lyx is cool)

Here's an example of the output:

Tweet-835886: "is studing math :wink: tomorrow exam and dentist :)" is NEGATIVE with probability 0.9236376896011784


## Running on AWS
This project includes a description of the infrastructure setup to run spack on AWS. We use Terrform. infra.ft is the definition file for the infrastructure.

Here's how to use it:
- Install [terraform](https://www.terraform.io/downloads.html)
- terraform apply -var access_key='access key to your AWS account' -var secret_key='secret key to your AWS account'
- This command creates a new ec2 instance and takes about 5 minutes to install spark and run the job
- ssh -i key.pem ec2-user@<ec2 instance host name> -- see terraform output for the hostname of the new ec2 instance
- Don't forget to destroy the infrastructure when you no longer need it: terraform apply -var access_key='access key to your AWS account' -var secret_key='secret key to your AWS account'

