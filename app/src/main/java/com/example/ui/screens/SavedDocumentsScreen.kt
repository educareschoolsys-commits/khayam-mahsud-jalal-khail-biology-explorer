package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SavedDocument
import com.example.export.DocumentPrinter
import com.example.export.DocumentSharer
import com.example.ui.theme.SchoolNavy
import com.example.ui.theme.StatusUnpaid
import java.io.File

@Composable
fun SavedDocumentsScreen(
    documents: List<SavedDocument>,
    onDeleteDocument: (SavedDocument) -> Unit,
    onUpdateDocument: (SavedDocument) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var documentToDelete by remember { mutableStateOf<SavedDocument?>(null) }
    var renamingDocument by remember { mutableStateOf<SavedDocument?>(null) }

    val categories = listOf("All", "PDF", "DOCX", "RESULT", "FEE_RECEIPT", "TIMETABLE")

    val filteredDocs = documents.filter { doc ->
        val matchesSearch = doc.title.contains(searchQuery, ignoreCase = true)
        val matchesCategory = when (selectedCategory) {
            "All" -> true
            "PDF" -> doc.fileFormat.equals("PDF", ignoreCase = true)
            "DOCX" -> doc.fileFormat.equals("DOCX", ignoreCase = true)
            else -> doc.docType.contains(selectedCategory, ignoreCase = true)
        }
        matchesSearch && matchesCategory
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("saved_documents_screen")
    ) {
        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search saved documents...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Category Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = {
                        val label = when (cat) {
                            "All" -> "All Documents"
                            "RESULT" -> "📊 Results"
                            "FEE_RECEIPT" -> "💰 Fee Sheets"
                            "TIMETABLE" -> "🕐 Timetables"
                            "PDF" -> "📄 PDF Files"
                            "DOCX" -> "📝 Word Files"
                            else -> cat
                        }
                        Text(label)
                    }
                )
            }
        }

        // List
        if (filteredDocs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(54.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No saved documents found in this filter.\nExported PDFs and Word docs are saved here permanently.", color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredDocs, key = { it.id }) { doc ->
                    SavedDocCard(
                        doc = doc,
                        onOpen = {
                            val f = File(doc.filePath)
                            val mime = if (doc.fileFormat.equals("PDF", ignoreCase = true)) "application/pdf"
                            else "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                            DocumentSharer.openFile(context, f, mime)
                        },
                        onPrint = {
                            val f = File(doc.filePath)
                            if (doc.fileFormat.equals("PDF", ignoreCase = true)) {
                                DocumentPrinter.printPdf(context, f)
                            } else {
                                DocumentSharer.shareFile(context, f, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                            }
                        },
                        onShare = {
                            val f = File(doc.filePath)
                            val mime = if (doc.fileFormat.equals("PDF", ignoreCase = true)) "application/pdf"
                            else "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                            DocumentSharer.shareFile(context, f, mime)
                        },
                        onRename = { renamingDocument = doc },
                        onDelete = { documentToDelete = doc }
                    )
                }
            }
        }
    }

    if (renamingDocument != null) {
        var newTitle by remember { mutableStateOf(renamingDocument?.title ?: "") }
        AlertDialog(
            onDismissRequest = { renamingDocument = null },
            title = { Text("Rename Document") },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Document Title") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        renamingDocument?.let {
                            onUpdateDocument(it.copy(title = newTitle.trim()))
                        }
                        renamingDocument = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SchoolNavy)
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { renamingDocument = null }) { Text("Cancel") }
            }
        )
    }

    if (documentToDelete != null) {
        AlertDialog(
            onDismissRequest = { documentToDelete = null },
            title = { Text("Delete Document") },
            text = { Text("Are you sure you want to permanently delete '${documentToDelete?.title}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        documentToDelete?.let { onDeleteDocument(it) }
                        documentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusUnpaid)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { documentToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun SavedDocCard(
    doc: SavedDocument,
    onOpen: () -> Unit,
    onPrint: () -> Unit,
    onShare: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = if (doc.fileFormat.equals("PDF", ignoreCase = true)) Icons.Default.PictureAsPdf else Icons.Default.Description
                val iconColor = if (doc.fileFormat.equals("PDF", ignoreCase = true)) Color(0xFFC62828) else Color(0xFF1565C0)

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doc.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = SchoolNavy)
                    )
                    Text(
                        text = "${doc.fileFormat} • ${doc.fileSize} • ${doc.dateCreated}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = Color.Gray)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onOpen) {
                    Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open", fontSize = 11.sp)
                }

                IconButton(onClick = onPrint, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Print, contentDescription = "Print", tint = Color.DarkGray, modifier = Modifier.size(16.dp))
                }

                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = SchoolNavy, modifier = Modifier.size(16.dp))
                }

                IconButton(onClick = onRename, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Rename", tint = SchoolNavy, modifier = Modifier.size(16.dp))
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusUnpaid, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
