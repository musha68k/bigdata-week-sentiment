/**
 * A simple text classification pipeline that recognizes "spark" from input text. This is to show
 * how to create and configure an ML pipeline. Run with
 *
 * Based on the Spark Example ::: bin/run-example ml.SimpleTextClassificationPipeline
 *
 */

import java.io.FileReader

import breeze.io.CSVReader
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.{HashingTF, Tokenizer}
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.linalg.Vector

import scala.beans.BeanInfo

@BeanInfo
case class LabeledDocument(id: Long, text: String, label: Double)

@BeanInfo
case class Document(id: Long, text: String)

/**
 * A simple text classification pipeline that recognizes "spark" from input text. This is to show
 * how to create and configure an ML pipeline.
 */

object Classification {

  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("Classification").setMaster("local")
    val sc = new SparkContext(conf)

    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    // Prepare training documents, which are labeled.
    var doc_id = 0L
    val training_set = CSVReader.read(new FileReader("target/tweet-sentiment-dataset/training.1600000.processed.noemoticon.csv"))
    val training = training_set.map { line =>
      doc_id = doc_id + 1L
      LabeledDocument(doc_id, line(5), line(0).toDouble/4.0)
    }


    // Configure an ML pipeline, which consists of three stages: tokenizer, hashingTF, and lr.
    val tokenizer = new Tokenizer()
                    .setInputCol("text")
                    .setOutputCol("words")
    val hashingTF = new HashingTF()
                    .setNumFeatures(10000)
                    .setInputCol(tokenizer.getOutputCol)
                    .setOutputCol("features")
    val lr = new LogisticRegression()
             .setMaxIter(10)
             .setRegParam(0.01)
    val pipeline = new Pipeline()
                   .setStages(Array(tokenizer, hashingTF, lr))

    // Fit the pipeline to training documents.
    val model = pipeline.fit(training.toDF())

    // Prepare test documents, which are unlabeled.
    val test_set = CSVReader.read(new FileReader("target/tweet-sentiment-dataset/testdata.manual.2009.06.14.csv"))
    val test = test_set.map { line =>
      doc_id = doc_id + 1L
      Document(doc_id, line(5))
    }

    // Make predictions on test documents.
    model.transform(test.toDF())
    .select("id", "text", "probability", "prediction")
    .collect()
    .foreach { case Row(id: Long, text: String, prob: Vector, prediction: Double) =>
      println(s"($id, $text) --> prob=$prob, prediction=$prediction")
    }

    sc.stop()
  }
}