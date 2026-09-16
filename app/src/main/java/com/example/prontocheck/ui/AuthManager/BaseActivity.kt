package com.example.prontocheck.ui.AuthManager

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.prontocheck.R


open class BaseActivity : AppCompatActivity() {

    // Se ejecuta una sola vez para inflar el menú en cualquier actividad
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // Maneja el clic del botón de cerrar sesión para todas las actividades
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                mostrarConfirmacionCerrarSesion()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun mostrarConfirmacionCerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Deseas salir de la aplicación?")
            .setPositiveButton("Salir") { _, _ ->
                AuthManager.cerrarSesion(this)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}