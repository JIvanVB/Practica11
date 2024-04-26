package mx.itson.edu.practica11

import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class MainActivity : AppCompatActivity() {

    private var lvDatos : ListView? = null
    var txtid : EditText? = null
    var txtnom : EditText? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        lvDatos = findViewById(R.id.lvDatos)
        txtid = findViewById<EditText>(R.id.txtid)
        txtnom = findViewById<EditText>(R.id.txtnom)

        findViewById<Button>(R.id.btnreg).setOnClickListener {botonRegistrar()}
        findViewById<Button>(R.id.btnbus).setOnClickListener{botonBuscar()}
        findViewById<Button>(R.id.btneli).setOnClickListener{botonEliminar()}
        findViewById<Button>(R.id.btnmod).setOnClickListener{botonModificar()}
        usuariosRegistrados()

    }

    fun botonRegistrar(){

        if (txtid!!.text.trim().isEmpty() || txtnom!!.text.trim().isEmpty())
            Toast.makeText(this@MainActivity, "Hay campos vacios", Toast.LENGTH_SHORT).show()
        else{
            val id: Int = txtid!!.getText().toString().toInt()
            val nom: String = txtnom!!.getText().toString()

            val db: FirebaseDatabase = FirebaseDatabase.getInstance()
            val dbref: DatabaseReference = db.getReference(Usuario::class.java.getSimpleName())
            dbref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val aux = id.toString()
                    val res = snapshot.children.any { x ->
                        x.child("id").value.toString().equals(aux, ignoreCase = true)
                    }
                    val res2 = snapshot.children.any { x ->
                        x.child("nombre").value.toString().equals(nom, ignoreCase = true)
                    }

                    if (res || res2) {
                        ocultarTeclado()
                        val mensaje = if (res) "Error. El ID ($aux) Ya Existe!!" else "Error. El Nombre ($nom) Ya Existe!!"
                        Toast.makeText(this@MainActivity, mensaje, Toast.LENGTH_SHORT).show()
                    } else {
                        val luc = Usuario(id, nom)
                        dbref.push().setValue(luc)
                        ocultarTeclado()
                        Toast.makeText(this@MainActivity, "Usuario Registrado Correctamente!!", Toast.LENGTH_SHORT).show()
                        txtid!!.setText("")
                        txtnom!!.setText("")
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }
    fun botonBuscar(){

        if(txtid!!.getText().toString().trim().isEmpty()){
            ocultarTeclado()
            Toast.makeText(this@MainActivity, "Digite El ID del Luchador a Buscar!!", Toast.LENGTH_SHORT).show()
        }else{
            val id = txtid!!.text.toString().toInt()
            val db = FirebaseDatabase.getInstance()
            val dbref = db.getReference(Usuario::class.java.getSimpleName())

            dbref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val aux = id.toString()
                    var usuariosEncontrado: String? = null

                    snapshot.getChildren().forEach { x ->
                        if (aux.equals(x.child("id").value.toString(), ignoreCase = true)) {
                            usuariosEncontrado = x.child("nombre").value.toString()
                            return@forEach
                        }
                    }
                    if (usuariosEncontrado != null) {
                        ocultarTeclado()
                        txtnom!!.setText(usuariosEncontrado!!)
                    } else {
                        ocultarTeclado()
                        Toast.makeText(this@MainActivity,"ID ($aux) No Encontrado!!",Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onCancelled(error: DatabaseError) {                    }

            })
        }
    }
    fun botonEliminar(){

        if(txtid!!.getText().toString().trim().isEmpty()){
            ocultarTeclado()
            Toast.makeText(this@MainActivity, "Digite El ID del Luchador a Eliminar!!", Toast.LENGTH_SHORT).show()
        }else{
            val id = txtid!!.getText().toString().toInt()
            val db = FirebaseDatabase.getInstance()
            val dbref = db.getReference(Usuario::class.java.getSimpleName()).apply {
                addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val aux = id.toString()
                        var encontrado = false
                        for (x in snapshot.children) {
                            if (aux.equals(x.child("id").value.toString(), ignoreCase = true)) {
                                encontrado = true
                                val a = AlertDialog.Builder(this@MainActivity)
                                a.setCancelable(false)
                                a.setTitle("Pregunta")
                                a.setMessage("¿Está Seguro(a) De Querer Eliminar El Registro?")
                                a.setNegativeButton("Cancelar") { dialogInterface, i -> }
                                a.setPositiveButton("Aceptar") { dialogInterface, i ->
                                    ocultarTeclado()
                                    x.ref.removeValue()
                                    usuariosRegistrados()
                                }
                                a.show()
                                break
                            }
                        }
                        if (!encontrado) {
                            ocultarTeclado()
                            Toast.makeText(
                                this@MainActivity,
                                "ID ($aux) No Encontrado.\nImposible Eliminar!!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {                    }
                })
            }
        }
    }
    fun botonModificar(){

        if (txtid!!.text.toString().trim().isEmpty() || txtnom!!.text.toString().trim().isEmpty()) {
            ocultarTeclado()
            Toast.makeText(this@MainActivity, "Complete Los Campos Faltantes Para Actualizar!!", Toast.LENGTH_SHORT).show()
        } else {
            val id = txtid!!.text.toString().toInt()
            val nom = txtnom!!.text.toString()
            val db = FirebaseDatabase.getInstance()
            val dbref = db.getReference(Usuario::class.java.simpleName).apply {
                addListenerForSingleValueEvent(object :  ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var res2 = false
                        for (x in snapshot.children) {
                            if (x.child("nombre").getValue(String::class.java)?.equals(nom, ignoreCase = true) == true) {
                                res2 = true
                                ocultarTeclado()
                                Toast.makeText(this@MainActivity, "El Nombre ($nom) Ya Existe.\nImposible Modificar!!", Toast.LENGTH_SHORT).show()
                                break
                            }
                        }
                        if (!res2) {
                            val aux = id.toString()
                            var res = false
                            var ses=0
                            for (x in snapshot.children) {
                                if (x.child("id").value.toString() == aux) {
                                    res = true
                                    ocultarTeclado()
                                    x.ref.child("nombre").setValue(nom)
                                    txtid!!.setText("")
                                    txtnom!!.setText("")
                                    usuariosRegistrados()
                                    break
                                }
                            }
                            if (!res) {
                                ocultarTeclado()
                                Toast.makeText(this@MainActivity, "ID ($aux) No Encontrado.\nImposible Modificar!!!!", Toast.LENGTH_SHORT).show()
                                txtid!!.setText("")
                                txtnom!!.setText("")
                            }
                        }

                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }

        }


    }

    private fun mostrarToast(mensaje: String) {
        Toast.makeText(this@MainActivity, mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun usuariosRegistrados() {
        val db: FirebaseDatabase = FirebaseDatabase.getInstance()
        val dbref = db.getReference(Usuario::class.java.simpleName)

        val listUsu = ArrayList<Usuario>()
        val ada = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, listUsu)
        lvDatos!!.adapter = ada

        dbref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val id = snapshot.child("id").getValue(Int::class.java)
                val nombre = snapshot.child("nombre").getValue(String::class.java)
                id?.let { idValue ->
                    nombre?.let { nombreValue ->
                        val usuario = Usuario(idValue, nombreValue)
                        listUsu.add(usuario)
                        ada.notifyDataSetChanged()
                    }
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {ada.notifyDataSetChanged()}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })

        lvDatos!!.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val luc = listUsu[i]
            val a = AlertDialog.Builder(this@MainActivity)
            a.setCancelable(true)
            a.setTitle("Usuario Seleccionado")
            var msg = "ID : " + luc.id + "\n\n"
            msg += "NOMBRE : " + luc.nombre

            a.setMessage(msg)
            a.show()
        }
    }

    private fun ocultarTeclado() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}