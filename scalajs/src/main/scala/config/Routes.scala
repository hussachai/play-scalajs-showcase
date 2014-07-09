package config

object Routes {

  object Todos {
    val base = "/todos"
    def all = base + "/all"
    def create = base + "/create"
    def update(id: Long) = base + s"/update/$id"
  }
}
