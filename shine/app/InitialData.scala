import models.{Permission, Role, User}
import play.api.Logger

/**
  * InitialData class. Inserts data into the database on application startup.
  * Defined in Module.scala as Eager Singleton, which makes it run on application start.
  */
class InitialData {
  Logger.logger.info("Initialised InitialData")

  if(User.findAll.isEmpty) {
    val create = new Permission("Create", "Create")
    create.save()
    val read = new Permission("Read", "Read")
    read.save()
    val update = new Permission("Update", "Update")
    update.save()
    val delete = new Permission("Delete", "Delete")
    delete.save()

    val role = new Role("Admin", "Admin")

    role.permissions.add(create)
    role.permissions.add(read)
    role.permissions.add(update)
    role.permissions.add(delete)

    role.save()

    val user = User.create("kinman.li@bl.uk", "secret")
    user.roles.add(role)
    user.save()
  }

  getExternalUsers()

  def getExternalUsers() {
    /*
    createUser("alison.kay@northumbria.ac.uk")
    createUser("r.cran@alumni.ucl.ac.uk")
    createUser("fryerc@parliament.uk")
    createUser("Gareth.Millward@lshtm.ac.uk")
    createUser("H.Raffal@2006.hull.ac.uk")
    createUser("helenlouisetaylor@gmail.com")
    createUser("mm2015@cam.ac.uk")
    createUser("R.Deswarte@uea.ac.uk")
    createUser("l.richardson@ucl.ac.uk")
    createUser("daust01@mail.bbk.ac.uk")
    createUser("S.V.Huc-Hepher@westminster.ac.uk")
    createUser("rowanaust@gmail.com")
    createUser("jane.winters@sas.ac.uk")
    createUser("jonathan.blaney@sas.ac.uk")
    createUser("ralph.schroeder@oii.ox.ac.uk")
    createUser("josh.cowls@oii.ox.ac.uk")
    createUser("nb@dac.au.dk")
    */
  }

  def createUser(email: String) {
    var role = Role.findByName("Reader")
    if (role == null) {
      role = new Role("Reader", "Reader")
      var read = Permission.findByName("Read")
      if (read == null) {
        read = new Permission("Read", "Read")
        read.save()
      }
      role.permissions.add(read)
      role.save()
    }

    var user = User.findByEmail(email)

    if (user == null) {
      user = User.create(email, "secret")
      user.roles.add(role)

    } else if (user.email.matches("^(.*?[A-Z]){1,}.*$")) {
      println("convert user found: " + user.email)
      val lowerCaseEmail = user.email.toLowerCase()
      println("convert user found: " + user.email + " to " + lowerCaseEmail)
      User.updatePassword(lowerCaseEmail, "secret")
      User.updateEmail(user.email, lowerCaseEmail)
    }
  }
}