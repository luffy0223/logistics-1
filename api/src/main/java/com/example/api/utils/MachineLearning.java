package com.example.api.utils;

import org.apache.spark.ml.classification.*;
import org.apache.spark.ml.clustering.KMeans;
import org.apache.spark.ml.clustering.KMeansModel;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.regression.LinearRegression;
import org.apache.spark.ml.regression.LinearRegressionModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.apache.spark.sql.functions.*;

@Service
public class MachineLearning {
    //CARE_OPTIONS = ['有冰柜冷藏', '注意易碎', '防止高温']

    @Autowired
    private SparkSession spark;

    // 加载CSV数据并验证字段
    public Dataset<Row> loadAndValidate() {
        // 读取数据
        StructType schema = new StructType()
                .add("id", "string")
                .add("did", "string")
                .add("vid", "string")
                .add("address", "string")
                .add("care", "int")
                .add("driver", "string")     // 司机姓名或ID
                .add("number", "int")        // 任务编号
                .add("phone", "string")      // 联系电话
                .add("status", "int")     // 任务状态
                .add("time", "string")    // 时间戳
                .add("urgent", "int");   // 是否紧急任务


        Dataset<Row> df = spark.read()
                .schema(schema)
                .option("header", "true")
                .option("inferSchema", "true")
                .csv("transport_records.csv");

        // 将字符串列索引为数值
        StringIndexer addressIndexer = new StringIndexer()
                .setInputCol("address").setOutputCol("addressIdx");
        StringIndexer driverIndexer = new StringIndexer()
                .setInputCol("driver").setOutputCol("driverIdx");
        // 应用索引转换
        df = addressIndexer.fit(df).transform(df);
        df = driverIndexer.fit(df).transform(df);
        // 合并特征列为向量
        VectorAssembler assembler = new VectorAssembler()
                .setInputCols(new String[]{"addressIdx","driverIdx"})
                .setOutputCol("features");
        return assembler.transform(df);
    }


    public List<String> runLogisticRegression() {
        // 读取数据
        StructType schema = new StructType()
                .add("id", "string")
                .add("did", "string")
                .add("vid", "string")
                .add("address", "string")
                .add("care", "int")       // 可能是医护类型分类
                .add("driver", "string")     // 司机姓名或ID
                .add("number", "int")        // 任务编号或次数
                .add("phone", "string")      // 联系电话
                .add("status", "int")     // 任务状态（如"completed", "cancelled"）
                .add("time", "string")    // 时间戳
                .add("urgent", "int");   // 是否紧急任务


        Dataset<Row> df = spark.read()
                .schema(schema)
                .option("header", "true")
                .option("inferSchema", "true")
                .csv("transport_records.csv");

        // 将字符串列索引为数值
        StringIndexer addressIndexer = new StringIndexer()
                .setInputCol("address").setOutputCol("addressIdx");
        StringIndexer driverIndexer = new StringIndexer()
                .setInputCol("driver").setOutputCol("driverIdx");
        // 应用索引转换
        df = addressIndexer.fit(df).transform(df);
        df = driverIndexer.fit(df).transform(df);
        // 合并特征列为向量
        VectorAssembler assembler = new VectorAssembler()
                .setInputCols(new String[]{"addressIdx"})
                .setOutputCol("features");
        Dataset<Row> featureDF = assembler.transform(df);
        // 将目标列care转换为double类型
        featureDF = featureDF.withColumn("urgent", featureDF.col("urgent").cast("double"));
        Dataset<Row>[] splits = featureDF.randomSplit(new double[]{0.9, 0.2}, 1234L);
        Dataset<Row> trainingData = splits[0];
        Dataset<Row> testData = splits[1];

        // 训练Logistic回归模型
        LogisticRegression lr = new LogisticRegression()
                .setLabelCol("urgent")
                .setFeaturesCol("features");

        LogisticRegressionModel lrModel = lr.fit(trainingData);
        // 预测
        Dataset<Row> predictions = lrModel.transform(testData);
        // 返回id、urgent及预测值JSON列表
        return predictions.select("id", "address","driver","urgent", "prediction")
                .toJSON().collectAsList();
    }





    public List<String> runLinearRegression() {
        // 读取数据
        Dataset<Row> featureData = loadAndValidate();
        // 将目标列urgent转换为double类型
        featureData = featureData.withColumn("urgent", featureData.col("urgent").cast("double"));

        Dataset<Row>[] splits = featureData.randomSplit(new double[]{0.9, 0.1}, 1234L);
        Dataset<Row> trainingData = splits[0];
        Dataset<Row> testData = splits[1];
        // 训练线性回归模型
        LinearRegression lr = new LinearRegression()
                .setLabelCol("urgent")
                .setFeaturesCol("features");
        LinearRegressionModel lrModel = lr.fit(trainingData);
        Dataset<Row> predictions = lrModel.transform(testData);
        return predictions.select("id", "urgent", "prediction")
                .toJSON().collectAsList();
    }



    public List<String> runKMeans() {
        Dataset<Row> featureData = loadAndValidate();
        // 将目标列urgent转换为double类型
        //featureData = featureData.withColumn("urgent", featureData.col("urgent").cast(""));
        Dataset<Row>[] splits = featureData.randomSplit(new double[]{0.8, 0.2}, 1234L);
        Dataset<Row> trainingData = splits[0];
        Dataset<Row> testData = splits[1];
        // 训练KMeans模型（设置簇数为3）
        KMeans kmeans = new KMeans().setK(3);
        KMeansModel kmeansModel = kmeans.fit(trainingData);
        Dataset<Row> predictions = kmeansModel.transform(testData);
        return predictions.select("id", "address","driver","prediction")
                .toJSON().collectAsList();
    }

    //done
    public List<String> runDecisionTree() {
        Dataset<Row> df = loadAndValidate();
        // 将status转换为double标签
        Dataset<Row> data = df.withColumn("care", df.col("care").cast("double"));
        // 特征索引
        Dataset<Row>[] splits = data.randomSplit(new double[]{0.9, 0.1}, 1234L);
        Dataset<Row> trainingData = splits[0];
        Dataset<Row> testData = splits[1];

        // 训练决策树分类器
        DecisionTreeClassifier dt = new DecisionTreeClassifier()
                .setLabelCol("care")
                .setFeaturesCol("features");
        DecisionTreeClassificationModel dtModel = dt.fit(trainingData);
        Dataset<Row> predictions = dtModel.transform(testData);
        return predictions.select("id", "address","care", "prediction")
                .toJSON().collectAsList();
    }

    //done
    public Double runNaiveBayes() {
        Dataset<Row> df = loadAndValidate();
        // 将status转换为double标签
        Dataset<Row> data = df.withColumn("care", df.col("care").cast("double"));
        // 特征索引
        Dataset<Row>[] splits = data.randomSplit(new double[]{0.9, 0.1}, 1234L);
        Dataset<Row> trainingData = splits[0];
        Dataset<Row> testData = splits[1];

        NaiveBayes nb = new NaiveBayes()
                .setLabelCol("urgent")
                .setFeaturesCol("features");

// 训练模型
        NaiveBayesModel nbModel = nb.fit(trainingData);

// 预测
        Dataset<Row> nbPredictions = nbModel.transform(testData);
        nbPredictions.select("features", "urgent", "prediction").show(10);

// 可选评估
        MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                .setLabelCol("urgent")
                .setPredictionCol("prediction")
                .setMetricName("accuracy");
        System.out.println("Naive Bayes Accuracy: " + evaluator.evaluate(nbPredictions));
        return evaluator.evaluate(nbPredictions);
    }

}
