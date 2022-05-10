package persistence

import com.google.inject.AbstractModule


class PersistenceModule extends AbstractModule {

  override def configure() : Unit = {
    //bind(classOf[DAOInterface]).to(classOf[persistence.fileIOJSONImpl.DAOImpl])
    bind(classOf[DAOInterface]).to(classOf[persistence.postgresImpl.DAOImpl])

  }
}
