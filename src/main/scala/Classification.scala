/**
 * A simple text classification pipeline that classify tweets according to the sentiment they convey. This is to show
 * how to create and configure an ML pipeline. Run with mvn scala:run.
 *
 * Based on the Spark exmaple https://github.com/apache/spark/blob/master/examples/src/main/scala/org/apache/spark/examples/ml/SimpleTextClassificationPipeline.scala
 */

// Need this to read .csv files
import java.io.FileReader
import breeze.io.CSVReader

// The top-level Spark classes we need to run the processing pipeline
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.ml.Pipeline

// The classification algo we've choosing to run here called LogisticRegression
import org.apache.spark.ml.classification.LogisticRegression

// the features will be words with their frequencies, so we'll need to 
// tokenize the tweets into words (thus, Tokenizer) and HashstringTF is 
import org.apache.spark.ml.feature.{HashingTF, Tokenizer}


import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.mllib.linalg.Vector

import scala.beans.BeanInfo

@BeanInfo
case class LabeledDocument(id: Long, text: String, label: Double)

@BeanInfo
case class Document(id: Long, text: String)

object Classification {

  def main(args: Array[String]) {

    // While Spark is running, connect to http://localhost:4040/ to see the progress
    // We'll run on a single box (local machine) using up to 16 cores
    val conf = new SparkConf().setAppName("Sentiment classification example").setMaster("local[16]") 
    val sc = new SparkContext(conf)

    // Prepare training documents, which are labeled.
    var doc_id = 0L
    val training_set = CSVReader.read(new FileReader("./tweet-sentiment-dataset/training.1600000.processed.noemoticon.csv"))
    val training = training_set.map { line =>
      doc_id = doc_id + 1L
      LabeledDocument(doc_id, line(5), line(0).toDouble/4.0) // line(0) is sentiment - a number 0, 2 or 4, LR needs a number from 0 to 1
    }


    // Configure an ML pipeline, which consists of three stages: tokenizer, hashingTF, and lr.

    // step 1 : tokenize the tweets into words
    val tokenizer = new Tokenizer()
                    .setInputCol("text")
                    .setOutputCol("words")

    // step 2: create features - word (aka term) frequencies
    val hashingTF = new HashingTF()
                    .setNumFeatures(10000)
                    .setInputCol(tokenizer.getOutputCol)
                    .setOutputCol("features")

    // step 3: classify the fratures
    val lr = new LogisticRegression()
             .setMaxIter(10)
             .setRegParam(0.01)

    // create a pipeline with 3 steps defined above
    val pipeline = new Pipeline()
                   .setStages(Array(tokenizer, hashingTF, lr))

    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    // Here we're feeding training documents into the pipeline.
    // The pipeline takes data in the datastructure called "Data Frame",
    // to learn more about it see https://databricks.com/blog/2015/02/17/introducing-dataframes-in-spark-for-large-scale-data-science.html
    //
    // This takes about 10 minutes on a MacBook Pro with 1,600,000 tweets
    val model = pipeline.fit(training.toDF())

    //TODO: Now that the model is done, it would be very nice to persist 
    // it right about now, so that we can load it and classify other 


    // Load the test documents
    // The data file actually have the sentiment in the first column, but we won't load it
    val test_set = CSVReader.read(new FileReader("./tweet-sentiment-dataset/testdata.manual.2009.06.14.csv"))
    val test = test_set.map { line =>
      doc_id = doc_id + 1L
      Document(doc_id, line(5)) // NB we're not reading line(0) from the data set, just id and text
    }

    // A utility funciton to format the output
    def prettyPrint(n:Double) : String = n match {
     case 0 => "NEGATIVE"
     case 1 => "POSITIVE"
    }
    // Make predictions on test documents.
    model.transform(test.toDF())
    .select("id", "text", "probability", "prediction")
    .collect()
    .foreach { case Row(id: Long, text: String, prob: Vector, prediction: Double) =>
      println(s"Tweet-$id: '$text' is ${prettyPrint(prediction)} with probability ${prob(prediction.toInt)}")
    }

    // We're done. Stopping Spark.
    sc.stop()
  }
}