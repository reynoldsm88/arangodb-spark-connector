package com.arangodb.spark

import com.arangodb.mapping.ArangoJack
import com.arangodb.util.ArangoSerialization
import com.arangodb.{ArangoDB, ArangoDBException}
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite, Matchers}

import scala.beans.BeanProperty

class ArangoSparkCustomSerializationTest extends FunSuite with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with SharedSparkContext {

    case class ComplexEntity(@BeanProperty bigDecimalValue : BigDecimal = BigDecimal(Double.MaxValue))

    val DB = "spark_test_db"
    val COLLECTION = "spark_test_col"



    val customSerialization : ArangoSerialization = {
        val bigDecimalConverterModule = new SimpleModule
        bigDecimalConverterModule.addSerializer( classOf[ BigDecimal ], new ToStringSerializer )
        val serialization = new ArangoJack()
        serialization.configure( mapper => {
            mapper.registerModule( DefaultScalaModule )
            mapper.registerModule( new JavaTimeModule )
            mapper.registerModule( bigDecimalConverterModule )
        } )
        serialization
    }

    val arangoDB = new ArangoDB.Builder().serializer(customSerialization).build()

    override def beforeAll( ) {
        super.beforeAll()
        try {
            arangoDB.db( DB ).drop()
        } catch {
            case e : ArangoDBException =>
        }
        arangoDB.createDatabase( DB )
        arangoDB.db( DB ).createCollection( COLLECTION )
    }

    override def afterAll( ) {
        try {
            arangoDB.db( DB ).drop()
            arangoDB.shutdown()
        } finally {
            super.afterAll()
        }
    }

    test("read and write data with customized serialization") {
        val documents = ( 1 to 100 ).map( i => ComplexEntity( BigDecimal(i) ) )
        val docRDD = sc.parallelize( documents )

        ArangoSpark.save( docRDD, COLLECTION, WriteOptions( DB ).serialization( customSerialization ) )
        val results : Seq[ComplexEntity] = ArangoSpark.load[ComplexEntity](sc, COLLECTION, ReadOptions(DB).serialization(customSerialization)).collect()
        results should contain theSameElementsAs(documents)
    }

}
