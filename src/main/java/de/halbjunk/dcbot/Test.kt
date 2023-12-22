package de.halbjunk.dcbot

import java.io.IOException
import java.sql.DriverManager
import java.sql.SQLException

object Test {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val url = "jdbc:mysql://localhost:3306/halbjunk"
        val user = "root"
        val pass = "mysql"
        try {
            val con = DriverManager.getConnection(url, user, pass)
            println("Verbindung erfolgreich hergestellt")
            val result = con.createStatement().executeQuery("SELECT * FROM Clans")
            while (result.next()) {
                println(result.getString("id"))
            }
        } catch (e: SQLException) {
            println(e.message)
        }
    }
}