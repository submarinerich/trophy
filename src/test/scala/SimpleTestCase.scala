package test.scala

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.BeforeAndAfterAll
import grizzled.slf4j.Logger

class SimpleTestCase extends FunSuite with ShouldMatchers
{
	val log = Logger(classOf[SimpleTestCase])
	
  test("math should work")
  {
		log.info("I'm in a test case right now!")
    3 > 1 should equal (true)
  }
}

