package com.example.delitrack.frontEnd

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.delitrack.R
import com.google.firebase.firestore.FirebaseFirestore
import java.io.FileOutputStream

class DeliveryReceiptActivity : AppCompatActivity() {

    private lateinit var tvReceiptTrackingId: TextView
    private lateinit var tvReceiptStatus: TextView
    private lateinit var tvReceiptDateTime: TextView

    private lateinit var tvReceiptSenderName: TextView
    private lateinit var tvReceiptSenderContact: TextView
    private lateinit var tvReceiptPickupAddress: TextView

    private lateinit var tvReceiptReceiverName: TextView
    private lateinit var tvReceiptReceiverContact: TextView
    private lateinit var tvReceiptDestinationAddress: TextView

    private lateinit var tvReceiptPackageDesc: TextView
    private lateinit var tvReceiptPackageClassification: TextView
    private lateinit var tvReceiptPackageWeight: TextView
    private lateinit var tvReceiptPackageDimensions: TextView
    private lateinit var tvReceiptNumPackages: TextView
    private lateinit var tvReceiptSpecialHandling: TextView

    private lateinit var tvReceiptBaseFee: TextView
    private lateinit var tvReceiptWeightCharge: TextView
    private lateinit var tvReceiptTotalAmount: TextView

    private lateinit var receiptCardContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_delivery_receipt)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.backBtn).setOnClickListener { finish() }

        tvReceiptTrackingId = findViewById(R.id.tvReceiptTrackingId)
        tvReceiptStatus = findViewById(R.id.tvReceiptStatus)
        tvReceiptDateTime = findViewById(R.id.tvReceiptDateTime)

        tvReceiptSenderName = findViewById(R.id.tvReceiptSenderName)
        tvReceiptSenderContact = findViewById(R.id.tvReceiptSenderContact)
        tvReceiptPickupAddress = findViewById(R.id.tvReceiptPickupAddress)

        tvReceiptReceiverName = findViewById(R.id.tvReceiptReceiverName)
        tvReceiptReceiverContact = findViewById(R.id.tvReceiptReceiverContact)
        tvReceiptDestinationAddress = findViewById(R.id.tvReceiptDestinationAddress)

        tvReceiptPackageDesc = findViewById(R.id.tvReceiptPackageDesc)
        tvReceiptPackageClassification = findViewById(R.id.tvReceiptPackageClassification)
        tvReceiptPackageWeight = findViewById(R.id.tvReceiptPackageWeight)
        tvReceiptPackageDimensions = findViewById(R.id.tvReceiptPackageDimensions)
        tvReceiptNumPackages = findViewById(R.id.tvReceiptNumPackages)
        tvReceiptSpecialHandling = findViewById(R.id.tvReceiptSpecialHandling)

        tvReceiptBaseFee = findViewById(R.id.tvReceiptBaseFee)
        tvReceiptWeightCharge = findViewById(R.id.tvReceiptWeightCharge)
        tvReceiptTotalAmount = findViewById(R.id.tvReceiptTotalAmount)

        receiptCardContainer = findViewById(R.id.receiptCardContainer)

        val deliveryId = intent.getStringExtra("deliveryId") ?: "DLV-10001"
        tvReceiptTrackingId.text = deliveryId

        loadReceiptDetails(deliveryId)

        findViewById<Button>(R.id.btnPrintSavePdf).setOnClickListener {
            printOrSavePdfReceipt()
        }

        findViewById<Button>(R.id.btnTrackPackage).setOnClickListener {
            val intent = Intent(this, DeliveryTrackingActivity::class.java)
            intent.putExtra("deliveryId", deliveryId)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnDoneReceipt).setOnClickListener {
            finish()
        }
    }

    private fun loadReceiptDetails(deliveryId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("deliveries").document(deliveryId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val data = doc.data ?: return@addOnSuccessListener

                    val status = data["status"] as? String ?: "Pending"
                    val senderName = data["senderName"] as? String ?: data["customerName"] as? String ?: "N/A"
                    val senderContact = data["senderContact"] as? String ?: data["contactNumber"] as? String ?: "N/A"
                    val pickup = data["pickupLocation"] as? String ?: data["pickupAddress"] as? String ?: "Nasugbu, Batangas"

                    val receiverName = data["receiverName"] as? String ?: "N/A"
                    val receiverContact = data["receiverContact"] as? String ?: "N/A"
                    val dest = data["destination"] as? String ?: data["deliveryAddress"] as? String ?: "Lian, Batangas"

                    val packageDesc = data["packageDescription"] as? String ?: "Standard Parcel"
                    val packageType = data["packageType"] as? String ?: "Lightweight"
                    val weightKg = (data["packageWeightKg"] as? Number)?.toDouble() ?: (data["weightKg"] as? Number)?.toDouble() ?: 1.0
                    val dimensions = data["packageDimensions"] as? String ?: "Standard Size"
                    val numPackages = (data["numberOfPackages"] as? Number)?.toInt() ?: 1
                    val special = data["specialHandling"] as? String ?: data["deliveryNotes"] as? String ?: "None"
                    val date = data["dateCreated"] as? String ?: "Today"

                    tvReceiptStatus.text = status
                    tvReceiptDateTime.text = "Date & Time: $date"

                    tvReceiptSenderName.text = "Name: $senderName"
                    tvReceiptSenderContact.text = "Contact: $senderContact"
                    tvReceiptPickupAddress.text = "Pickup Address: $pickup"

                    tvReceiptReceiverName.text = "Name: $receiverName"
                    tvReceiptReceiverContact.text = "Contact: $receiverContact"
                    tvReceiptDestinationAddress.text = "Destination Address: $dest"

                    tvReceiptPackageDesc.text = "Description: $packageDesc"
                    tvReceiptPackageClassification.text = "Classification: $packageType"
                    tvReceiptPackageWeight.text = "Weight: $weightKg kg"
                    tvReceiptPackageDimensions.text = "Dimensions: $dimensions"
                    tvReceiptNumPackages.text = "Number of Packages: $numPackages"
                    tvReceiptSpecialHandling.text = "Special Instructions: $special"

                    // Calculate Fee Summary (Nasugbu-Lian Delivery)
                    val baseFee = 50.0
                    val weightCharge = weightKg * 10.0
                    val totalFee = baseFee + weightCharge

                    tvReceiptBaseFee.text = "₱${String.format("%.2f", baseFee)}"
                    tvReceiptWeightCharge.text = "₱${String.format("%.2f", weightCharge)}"
                    tvReceiptTotalAmount.text = "₱${String.format("%.2f", totalFee)}"
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Unable to load receipt details", Toast.LENGTH_SHORT).show()
            }
    }

    private fun printOrSavePdfReceipt() {
        val printManager = getSystemService(PRINT_SERVICE) as? PrintManager
        if (printManager != null) {
            val jobName = "RoutGo_Receipt_${tvReceiptTrackingId.text}"
            val printAdapter = object : PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes?,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback?,
                    extras: Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }
                    val info = PrintDocumentInfo.Builder(jobName)
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1)
                        .build()
                    callback?.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor?,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback?
                ) {
                    try {
                        val pdfDocument = PdfDocument()
                        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                        val page = pdfDocument.startPage(pageInfo)

                        val canvas = page.canvas
                        receiptCardContainer.draw(canvas)
                        pdfDocument.finishPage(page)

                        destination?.let {
                            pdfDocument.writeTo(FileOutputStream(it.fileDescriptor))
                        }
                        pdfDocument.close()
                        callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback?.onWriteFailed(e.message)
                    }
                }
            }
            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
            Toast.makeText(this, "Opening Print / Save PDF Manager...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Print / PDF service unavailable on this device", Toast.LENGTH_SHORT).show()
        }
    }
}
