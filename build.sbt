import Dependencies._
import sbt.Keys._
import StandaloneBuildTasks._
import xerial.sbt.Pack._

lazy val commonSettings = Seq(
  organization := "com.redbullmediahouse.dataservices.mds"
  , name := "data-services-mds-transformation"
  , version := "0.1.0-SNAPSHOT"
  , scalaVersion := "2.11.8"
  , fork := true
  , publishTo := {
    if (isSnapshot.value) {
      Some("RBMH Artifactory Snapshots" at "http://artifactory1.ops.prod.rbmhops.net/artifactory/libs-snapshot-local")
    } else {
      Some("RBMH Artifactory Releases" at "http://artifactory1.ops.prod.rbmhops.net/artifactory/libs-release-local")
    }
  }
  , resolvers ++= repos
  , parallelExecution in Test := false
  , test in assembly := {}
  , assembleArtifact in assemblyPackageScala := false
  , assemblyMergeStrategy in assembly := {
    case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
    case m if m.startsWith("META-INF") => MergeStrategy.discard
    case PathList("javax", "servlet", xs@_*) => MergeStrategy.first
    case PathList("org", "apache", xs@_*) => MergeStrategy.first
    case PathList("org", "jboss", xs@_*) => MergeStrategy.first
    case "log4j.properties" => MergeStrategy.discard
    case "about.html" => MergeStrategy.rename
    case "reference.conf" => MergeStrategy.concat
    case _ => MergeStrategy.first
  }
  , run in Compile :=
    Defaults.runTask(fullClasspath in Compile, mainClass in(Compile, run), runner in(Compile, run)).evaluated

) ++ codeQualitySettings

lazy val codeQualitySettings = Seq(
  checkstyleOutputFile := target(_ / "scalastyle-result.xml").value
  , checkstyleConfigLocation := CheckstyleConfigLocation.Classpath("google_checks.xml")
  , checkstyleXsltTransformations := {
    Some(Set(CheckstyleXSLTSettings(file("checkstyle-noframes.xsl"), target(_ / "scalastyle-report.html").value)))
  }
  , scapegoatVersion := "1.3.0"
  , mappings in makeSite ++= Seq(
    target(_ / "scalastyle-report.html").value -> "scalastyle.html",
    target(_ / "scala-2.11/scapegoat-report/scapegoat.html").value -> "scapegoat.html"
  )

)

lazy val buildInfoSettings = Seq(
  git.useGitDescribe := true
  , buildInfoPackage := "com.redbullmediahouse.dataservices.report"
  , buildInfoOptions ++= Seq(
    BuildInfoOption.BuildTime, BuildInfoOption.ToMap, BuildInfoOption.ToJson
  )
  , buildInfoKeys := Seq[BuildInfoKey](
    name, version, scalaVersion, sbtVersion
    , BuildInfoKey.action("build") {
      Option(System.getenv("BUILD_NUMBER")).getOrElse("unknown")
    }
    , BuildInfoKey.action("branch") {
      git.gitCurrentBranch.value
    }
    , BuildInfoKey.action("commit") {
      git.gitHeadCommit.value.getOrElse("unknown")
    }
    , BuildInfoKey.action("gitDescribedVersion") {
      git.gitDescribedVersion.value.getOrElse("unknown")
    }
    , BuildInfoKey.action("commitIdAbbrev") {
      git.gitHeadCommit.value.getOrElse("unknown").substring(0, 7)
    }
    , BuildInfoKey.action("gitFormattedShaVersion") {
      git.formattedShaVersion.value.getOrElse("unknown")
    }
    , BuildInfoKey.action("gitFormattedDateVersion") {
      Option(git.formattedDateVersion.value).getOrElse("unknown")
    }
    , BuildInfoKey.action("gitUncommittedSignifier") {
      git.uncommittedSignifier.value.getOrElse("unknown")
    }
    , BuildInfoKey.action("gitUncommittedChanges") {
      Option(git.gitUncommittedChanges.value).getOrElse("unknown")
    }
  )
)

lazy val root = (project in file("."))
  .enablePlugins(GitVersioning, BuildInfoPlugin)
  .settings(commonSettings: _*)
  .settings(buildInfoSettings: _*)
  .settings(packAutoSettings)
  .settings(Seq(packArchiveStem := "" // Prevents the module name from being used as the root path in the tarball
    ))
  .settings(publishPackArchiveTgz)
  .settings(standaloneBuildTasks: _*)
  .settings(
    libraryDependencies ++=
      Deps(
        loggingDependencies
          ++ commonDependencies
          ++ testDependencies
          ++ sparkDependencies
          //++ batchCommonDependencies
          ++ hardwareEnrichmentDependencies
          ++ awsLambdaDependencies
          ++ metricsDependencies
          ++ jsonDependencies
      ).withDefaultExcludes
  )
  .settings(
    initialCommands in console :=
      s"""
         |import org.apache.spark.{SparkConf, SparkContext}
         |import org.apache.spark.sql.SparkSession
         |import com.typesafe.config._
         |import scala.collection.JavaConversions._
         |
         |val uiPort = scala.util.Random.nextInt(1000)+20000
         |
         |val conf = new SparkConf()
         |conf.setMaster("local[*]")
         |conf.set("spark.app.name", "${name.value}-spark-shell")
         |conf.set("spark.ui.port", String.valueOf(uiPort))
         |
         |val spark = SparkSession.builder().config(conf).getOrCreate()
         |
         |import spark.implicits._
         |
         |spark.sparkContext.setLogLevel("WARN")
         |
         |implicit val sqlContenxt = spark.sqlContext
         |implicit val ss = spark
         |
         |java.awt.Desktop.getDesktop.browse(new java.net.URI("http://localhost:"+uiPort))
      """.stripMargin,

    cleanupCommands in console :=
      """
        |spark.stop()
      """.stripMargin
  )