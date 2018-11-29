//MAP
println("MAP")
val x= sc.parallelize(Array("b", "a", "c"))
val y= x.map(z => (z,1))
println(x.collect().mkString(", "))
println(y.collect().mkString(", "))
//x:['b', 'a', 'c']
//y: [('b', 1), ('a', 1), ('c', 1)]

//FILTER
println("FILTER")
val x= sc.parallelize(Array(1,2,3))
val y= x.filter(n => n%2 == 1)
println(x.collect().mkString(", "))
println(y.collect().mkString(", "))
//x:[1,2,3]
//y:[1,3]

//FLATMAP
println("FLATMAP")
val x= sc.parallelize(Array(1,2,3))
val y= x.flatMap(n => Array(n, n*100, 42))
println(x.collect().mkString(", "))
println(y.collect().mkString(", "))
//x:[1, 2, 3]
//y: [1, 100, 42, 2, 200, 42, 3, 300, 42]

//GROUPBY
println("GROUPBY")
val x= sc.parallelize(Array("John", "Fred", "Anna", "James"))
val y= x.groupBy(w => w.charAt(0))
println(x)
println(y.collect().mkString(", "))
//x:['John', 'Fred', 'Anna', 'James']
//y:[('A',['Anna']),('J',['John','James']),('F',['Fred'])]

//GROUPBYKEY
println("GROUPBYKEY")
val x= sc.parallelize(Array(('B',5),('B',4),('A',3),('A',2),('A',1)))
val y= x.groupByKey()
println(x.collect().mkString(", "))
println(y.collect().mkString(", "))
//x:[('B', 5),('B', 4),('A', 3),('A', 2),('A', 1)]
//y:[('A', [2, 3, 1]),('B',[5, 4])]

//REDUCEBYKEY VS. GROUPBYKEY
println("REDUCEBYKEY VS. GROUPBYKEY")
val words = Array("one", "two", "two", "three", "three", "three")
val wordPairsRDD= sc.parallelize(words).map(word => (word, 1))
val wordCountsWithReduce= wordPairsRDD
.reduceByKey(_ + _)
.collect()
val wordCountsWithGroup= wordPairsRDD
.groupByKey()
.map(t => (t._1, t._2.sum))
.collect()

//MAPPARTITIONS
println("MAPPARTITIONS")
val x= sc.parallelize(Array(1,2,3), 2)
def f(i:Iterator[Int])={ (i.sum,42).productIterator}
val y= x.mapPartitions(f)
// glom() flattens elements on the same partition
val xOut= x.glom().collect()
val yOut= y.glom().collect()
println(x)
println(xOut)
println(yOut)
//x:Array(Array(1), Array(2, 3))
//y:Array(Array(1, 42), Array(5, 42))

//MAPPARTITIONSWITHINDEX
println("MAPPARTITIONSWITHINDEX")
val x= sc.parallelize(Array(1,2,3), 2)
def f(partitionIndex:Int, i:Iterator[Int]) = {
(partitionIndex, i.sum).productIterator
}
val y= x.mapPartitionsWithIndex(f)
// glom() flattens elements on the same partition
val xOut= x.glom().collect()
val yOut= y.glom().collect()
println(x)
println(xOut)
println(yOut)
//x:Array(Array(1), Array(2, 3))
//y:Array(Array(0, 1), Array(1, 5))

//SAMPLE
println("SAMPLE")
val x= sc.parallelize(Array(1, 2, 3, 4, 5))
val y= x.sample(false, 0.4)
// omitting seed will yield different output
println(x)
println(y.collect().mkString(", "))
//x:[1, 2, 3, 4, 5]
//y:[1, 3]

//UNION
println("UNION")
val x= sc.parallelize(Array(1,2,3), 2)
val y= sc.parallelize(Array(3,4), 1)
val z= x.union(y)
val zOut= z.glom().collect()
println(x)
println(y)
println(zOut)
//x:[1, 2, 3]
//y:[3, 4]
//z:[[1], [2, 3], [3, 4]]

//JOIN
println("JOIN")
val x= sc.parallelize(Array(("a", 1), ("b", 2)))
val y= sc.parallelize(Array(("a", 3), ("a", 4), ("b", 5)))
val z= x.join(y)
println(x)
println(y)
println(z.collect().mkString(", "))
//x:[("a", 1), ("b", 2)]
//y:[("a", 3), ("a", 4), ("b", 5)]
//z:[('a', (1, 3)), ('a', (1, 4)), ('b', (2, 5))]

//DISTINCT
println("DISTINCT")
val x= sc.parallelize(Array(1,2,3,3,4))
val y= x.distinct()
println(x)
println(y.collect().mkString(", "))
//x:[1, 2, 3, 3, 4]
//y:[1, 2, 3, 4]

//COALESCE
println("COALESCE")
val x= sc.parallelize(Array(1, 2, 3, 4, 5), 3)
val y= x.coalesce(2)
val xOut= x.glom().collect()
val yOut= y.glom().collect()
println(xOut)
println(yOut)
//x:[[1], [2, 3], [4, 5]]
//y:[[1], [2, 3, 4, 5]]

//KEYBY
println("KEYBY")
val x= sc.parallelize(
Array("John", "Fred", "Anna", "James"))        
////cache()
val y= x.keyBy(w => w.charAt(0))
println(x)
println(y.collect().mkString(", "))
//x:['John', 'Fred', 'Anna', 'James']
//y:[('J','John'),('F','Fred'),('A','Anna'),('J','James')]

//PARTITIONBY
//Return a new RDD with the specified number of partitions, 
//placing original items into the partition returned by a user supplied function.
println("PARTITIONBY")
import org.apache.spark.Partitioner
val x= sc.parallelize(Array(('J',"James"),('F',"Fred"),
('A',"Anna"),('J',"John")), 3)
val y= x.partitionBy(new Partitioner() { 
val numPartitions= 2
def getPartition(k:Any) = { 
if (k.asInstanceOf[Char] < 'H') 0 else 1 
}
})
val yOut= y.glom().collect()
//x:Array(Array((A,Anna), (F,Fred)), Array((J,John), (J,James)))
//y:Array(Array((F,Fred), (A,Anna)), Array((J,John), (J,James)))

//ZIP
println("ZIP")
val x= sc.parallelize(Array(1,2,3))
val y= x.map(n=>n*n)
val z= x.zip(y)
println(x)
println(y)
println(z.collect().mkString(", "))
//x:[1, 2, 3]
//y:[1, 4, 9]
//z:[(1, 1), (2, 4), (3, 9)]









//ACTIONS
//GETNUMPARTITION
println("GETNUMPARTITION")
val x= sc.parallelize(Array(1,2,3), 2)
val y= x.partitions.size
val xOut= x.glom().collect()
println(x)
println(y)
//x:[[1], [2, 3]]
//y:2

//COLLECT
println("COLLECT")
val x= sc.parallelize(Array(1,2,3), 2)
val y= x.collect()
val xOut= x.glom().collect()
println(x)
println(y)
//x:[[1], [2, 3]]
//y:[1, 2, 3]

//REDUCE
println("REDUCE")
val x= sc.parallelize(Array(1,2,3,4))
val y= x.reduce((a,b) => a+b)
println(x.collect.mkString(", "))
println(x)
println(y)
//x:[1, 2, 3, 4]
//y:10

//AGGREGATE
println("AGGREGATE")
//Aggregate all the elements of the RDD by: 
//-applying a user function to combine elements with user-//supplied objects, 
//-then combining those user-defined results via a second //user function, 
//-and finally returning a result to the driver.
def seqOp= (data:(Array[Int], Int), item:Int) => (data._1 :+ item, data._2 + item)
def combOp= (d1:(Array[Int], Int), d2:(Array[Int], Int)) => (d1._1.union(d2._1), d1._2 + d2._2)
val x= sc.parallelize(Array(1,2,3,4))
val y= x.aggregate((Array[Int](), 0))(seqOp, combOp)
println(x)
println(y)
//x:[1, 2, 3, 4]
//y:(Array(3, 1, 2, 4),10)


//MAX
println("MAX")
val x= sc.parallelize(Array(2,4,1))
val y= x.max
println(x.collect().mkString(", "))
println(x)
println(y)
//x:[2, 4, 1]
//y:4

//SUM
println("SUM")
val x= sc.parallelize(Array(2,4,1))
val y= x.sum
println(x.collect().mkString(", "))
println(x)
println(y)
//x:[2, 4, 1]
//y:7

//MEAN
println("MEAN")
val x= sc.parallelize(Array(2,4,1))
val y= x.mean
println(x.collect().mkString(", "))
println(x)
println(y)
//x:[2, 4, 1]
//y:2.3333333

//STDEV
println("STDEV")
val x= sc.parallelize(Array(2,4,1))
val y= x.stdev
println(x.collect().mkString(", "))
println(x)
println(y)
//x:[2, 4, 1]
//y:1.2472191

//COUNTBYKEY
println("COUNTBYKEY")
val x= sc.parallelize(Array(('J',"James"),('F',"Fred"),
('A',"Anna"),('J',"John")))
val y= x.countByKey()
println(x)
println(y)
//x:[('J', 'James'), ('F','Fred'),('A','Anna'), ('J','John')]
//y:{'A': 1, 'J': 2, 'F': 1}

//SAVEASTEXTFILE
//dbutils.fs.rm("/tmp/demo", true)
//val x= sc.parallelize(Array(2,4,1))
//x.saveAsTextFile("/tmp/demo")
//val y= sc.textFile("/tmp/demo")
//println(y.collect().mkString(", "))
//x:[2, 4, 1]
//y:[u'2', u'4', u'1']
 
