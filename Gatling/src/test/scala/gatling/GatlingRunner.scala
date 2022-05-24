package gatling

import io.gatling.app.Gatling
import io.gatling.core.config.GatlingPropertiesBuilder

/**
 * This object simply provides a `main` method that wraps
 * [[io.gatling.app.Gatling]].main, which
 * allows us to do some configuration and setup before
 * Gatling launches.
 */
object GatlingRunner {

  def main(args: Array[String]): Unit = {

    val simClass: String = classOf[EnduranceSimulation].getName

    val props = new GatlingPropertiesBuilder().
      simulationClass(simClass)

    Gatling.fromMap(props.build)
  }
}
