package persistence

import com.google.inject.AbstractModule


class PersistenceModule extends AbstractModule {

  override def configure() : Unit = {
    //bind(classOf[DAOInterface]).to(classOf[persistence.fileIOJSONImpl.DAOImpl])
    //bind(classOf[DAOInterface]).toInstance(new persistence.postgresImpl.DAOImpl("jdbc:postgresql://postgres:5432/schachdb"))
    bind(classOf[DAOInterface]).toInstance(
      new persistence.mongoDBImpl.DAOImpl(
        "mongodb+srv://schach:schach123@schach.si7w8.mongodb.net/myFirstDatabase?retryWrites=true&w=majority"
      ))

  }
}
