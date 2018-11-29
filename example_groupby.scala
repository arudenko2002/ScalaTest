val x= sc.parallelize(Array(("B","BB",5),("B","BB",4),("A","AA",3),("A","AA",2),("A","AA",1)))
//Desired:
//f1,f2,int,max(int)
//(A,AA,3,3)
//(A,AA,2,3)
//(A,AA,1,3)
//(B,BB,5,5)
//(B,BB,4,5)

val y = x.map(r=>((r._1+","+r._2),r))
y.foreach(println)
val z = x.map(r=>((r._1+","+r._2),r._3))
val z2 = z.groupByKey().map(t=>(t._1,t._2.max))
z2.foreach(println)
val a = y.join(z2)
a.foreach(println)

//a=
//(A,AA,((A,AA,3),3))
//(B,BB,((B,BB,5),5))
//(A,AA,((A,AA,2),3))
//(B,BB,((B,BB,4),5))
//(A,AA,((A,AA,1),3))

val result = a.map(f=>(f._2._1._1,f._2._1._2,f._2._1._3,f._2._2))

x.foreach(println)
//x=
//(B,BB,5)
//(A,AA,2)
//(A,AA,1)
//(A,AA,3)
//(B,BB,4)
//y.foreach(println)
result.foreach(println)
//result=
//(A,AA,3,3)
//(A,AA,2,3)
//(A,AA,1,3)
//(B,BB,5,5)
//(B,BB,4,5)