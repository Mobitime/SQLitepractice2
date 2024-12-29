package com.example.sqlitepractice2

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: ProductAdapter
    private lateinit var productList: ArrayList<Product>

    private lateinit var etName: EditText
    private lateinit var etWeight: EditText
    private lateinit var etPrice: EditText
    private lateinit var listView: ListView
    private lateinit var btnSave: Button
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button

    private var selectedProduct: Product? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Потребительская корзина"


        initViews()


        dbHelper = DatabaseHelper(this)
        productList = ArrayList()


        adapter = ProductAdapter(this, productList)
        listView.adapter = adapter


        loadProducts()


        setupClickListeners()
    }

    private fun initViews() {
        etName = findViewById(R.id.etProductName)
        etWeight = findViewById(R.id.etWeight)
        etPrice = findViewById(R.id.etPrice)
        listView = findViewById(R.id.listView)
        btnSave = findViewById(R.id.btnSave)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)
    }

    private fun setupClickListeners() {

        listView.setOnItemClickListener { _, _, position, _ ->
            showOperationsDialog(productList[position])
        }


        btnSave.setOnClickListener { saveProduct() }
        btnUpdate.setOnClickListener { updateProduct() }
        btnDelete.setOnClickListener { deleteSelectedProduct() }
    }

    private fun showOperationsDialog(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Выберите действие")
            .setItems(arrayOf("Изменить", "Удалить", "Отмена")) { dialog, which ->
                when (which) {
                    0 -> prepareForUpdate(product)
                    1 -> confirmDelete(product)
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun prepareForUpdate(product: Product) {
        selectedProduct = product
        etName.setText(product.name)
        etWeight.setText(product.weight)
        etPrice.setText(product.price)

        btnSave.visibility = View.GONE
        btnUpdate.visibility = View.VISIBLE
        btnDelete.visibility = View.VISIBLE

        btnUpdate.isEnabled = true
        btnDelete.isEnabled = true
    }

    private fun confirmDelete(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Подтверждение")
            .setMessage("Удалить ${product.name}?")
            .setPositiveButton("Да") { _, _ ->
                deleteProduct(product)
            }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun saveProduct() {
        if (validateInputs()) {
            val product = Product(
                name = etName.text.toString().trim(),
                weight = etWeight.text.toString().trim(),
                price = etPrice.text.toString().trim()
            )
            dbHelper.insertProduct(product)
            clearInputs()
            loadProducts()
            Toast.makeText(this, "Продукт сохранен", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProduct() {
        if (validateInputs()) {
            selectedProduct?.let { product ->
                product.name = etName.text.toString().trim()
                product.weight = etWeight.text.toString().trim()
                product.price = etPrice.text.toString().trim()

                dbHelper.updateProduct(product)
                clearInputs()
                loadProducts()
                resetButtons()
                Toast.makeText(this, "Продукт обновлен", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteProduct(product: Product) {
        dbHelper.deleteProduct(product.id)
        loadProducts()
        if (product.id == selectedProduct?.id) {
            clearInputs()
            resetButtons()
        }
        Toast.makeText(this, "Продукт удален", Toast.LENGTH_SHORT).show()
    }

    private fun deleteSelectedProduct() {
        selectedProduct?.let { confirmDelete(it) }
    }

    private fun validateInputs(): Boolean {
        if (etName.text.toString().trim().isEmpty() ||
            etWeight.text.toString().trim().isEmpty() ||
            etPrice.text.toString().trim().isEmpty()
        ) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun loadProducts() {
        productList.clear()
        productList.addAll(dbHelper.getAllProducts())
        adapter.notifyDataSetChanged()
    }

    private fun clearInputs() {
        etName.text.clear()
        etWeight.text.clear()
        etPrice.text.clear()
        selectedProduct = null
    }

    private fun resetButtons() {
        btnSave.visibility = View.VISIBLE
        btnUpdate.visibility = View.VISIBLE
        btnDelete.visibility = View.VISIBLE

        btnUpdate.isEnabled = false
        btnDelete.isEnabled = false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_exit -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}