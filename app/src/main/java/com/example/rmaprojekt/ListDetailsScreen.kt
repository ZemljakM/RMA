package com.example.rmaprojekt

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.rmaprojekt.ui.theme.FirstColor
import com.example.rmaprojekt.ui.theme.FourthColor
import com.example.rmaprojekt.ui.theme.SecondColor
import com.example.rmaprojekt.ui.theme.ThirdColor
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


data class ListItem(
    val name: String,
    val quantity: String,
    var isBought: MutableState<Boolean>
)


@Composable
fun ListDetailsScreen(navController: NavHostController, listName: String, showCamera: () -> Unit, textResult: MutableState<String>, context: Context) {
    var friends by remember { mutableStateOf(listOf<String>()) }
    var items by remember { mutableStateOf(listOf<ListItem>()) }
    var totalItems by remember { mutableStateOf(0) }
    var boughtItems by remember { mutableStateOf(0) }
    var totalCost by remember { mutableStateOf(0.00f) }
    val db = FirebaseFirestore.getInstance()



    LaunchedEffect(Unit) {
        db.collection("lists")
            .whereEqualTo("name", listName)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents.first()
                    friends = document.get("friends") as? List<String> ?: emptyList()
                    val itemsList = document.get("items") as? List<HashMap<String, Any>> ?: emptyList()
                    itemsList.forEach { item ->
                        val itemName = item["itemName"] as? String ?: ""
                        val itemQuantity = item["itemQuantity"] as? String ?: ""
                        val isBought = mutableStateOf(item["isBought"] as? Boolean ?: false)
                        if (isBought.value) boughtItems++
                        totalItems++
                        items = items + ListItem(itemName, itemQuantity, isBought)
                    }
                    totalCost = (document.getDouble("totalCost") ?: 0.00).toFloat()
                }
            }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SecondColor
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(0.4f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var price by remember { mutableStateOf(0.00f) }
                        val parts = textResult.value.split("&")
                        for (part in parts) {
                            if (part.startsWith("izn=")) {
                                val foundValue = part.substringAfter("izn=")
                                if (foundValue[foundValue.length - 3] == '.') {
                                    price = foundValue.toFloat()
                                }
                                else if (foundValue[foundValue.length - 3] == ','){
                                    val formattedValue = foundValue.replace(',', '.')
                                    price = formattedValue.toFloat()
                                } else {
                                    val formattedValue = StringBuilder(foundValue).apply {
                                        insert(foundValue.length - 2, ".")
                                    }.toString()
                                    price = formattedValue.toFloat()
                                }
                                totalCost += price

                                db.collection("lists")
                                    .whereEqualTo("name", listName)
                                    .get()
                                    .addOnSuccessListener { documents ->
                                        if (!documents.isEmpty) {
                                            val document = documents.documents.first()
                                            db.collection("lists").document(document.id)
                                                .update("totalCost", totalCost)
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "Successfully updated total cost", Toast.LENGTH_SHORT).show()
                                                }
                                                .addOnFailureListener { }
                                        }
                                    }

                                textResult.value = ""
                                break
                            }
                        }

                        Button(
                            onClick = { showCamera() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.qr_scan ),
                                contentDescription = "QR Scan",
                                )
                        }

                        Button(
                            onClick = {
                                db.collection("lists")
                                    .whereEqualTo("name", listName)
                                    .get()
                                    .addOnSuccessListener { documents ->
                                        if (!documents.isEmpty) {
                                            val listDocument = documents.documents.first()
                                            val listId = listDocument.id

                                            val friends = listDocument.get("friends") as? List<String> ?: emptyList()

                                            friends.forEach { friendUsername ->
                                                println(friendUsername)
                                                db.collection("users")
                                                    .whereEqualTo("username", friendUsername)
                                                    .get()
                                                    .addOnSuccessListener { friendDocument ->
                                                        if(!friendDocument.isEmpty){
                                                            val friendId = friendDocument.documents.first().id

                                                            db.collection("users").document(friendId)
                                                                .update("lists", FieldValue.arrayRemove(listId))
                                                                .addOnSuccessListener {
                                                                    db.collection("lists").document(listId)
                                                                        .delete()
                                                                        .addOnSuccessListener {
                                                                            Toast.makeText(context, "List deleted", Toast.LENGTH_SHORT).show()
                                                                            navController.navigate("list_screen") {
                                                                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                                                            }
                                                                        }
                                                                        .addOnFailureListener { }
                                                                }
                                                        }
                                                }
                                            }
                                        }
                                    }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .width(45.dp)
                                .height(30.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove list",
                                tint = FirstColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
                Text(
                    text = listName,
                    textAlign = TextAlign.Left,
                    fontSize = 60.sp,
                    color = FirstColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Friends: ${friends.joinToString()}",
                    textAlign = TextAlign.Left,
                    fontSize = 20.sp,
                    color = FirstColor,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FourthColor, shape = RoundedCornerShape(12.dp))
                        .padding(start = 16.dp, top = 12.dp, bottom = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Items",
                            color = FirstColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(0.95f),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(0.7f)
                                        .padding(start = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "•   ", color = FirstColor, fontSize = 20.sp
                                    )
                                    Text(
                                        text = "${item.name} - ",
                                        color = FirstColor,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${item.quantity}",
                                        color = FirstColor,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        item.isBought.value = !item.isBought.value
                                        if (item.isBought.value) {
                                            boughtItems++
                                        } else {
                                            boughtItems--
                                        }


                                        db.collection("lists")
                                            .whereEqualTo("name", listName)
                                            .get()
                                            .addOnSuccessListener { documents ->
                                                if(!documents.isEmpty){
                                                    val document = documents.documents.first()
                                                    val itemsList = document.get("items") as? MutableList<HashMap<String, Any>> ?: mutableListOf()
                                                    val itemIndex = itemsList.indexOfFirst { it["itemName"] == item.name }
                                                    if(itemIndex != -1){
                                                        itemsList[itemIndex]["isBought"] = item.isBought.value
                                                        db.collection("lists").document(document.id)
                                                            .update("items", itemsList)
                                                            .addOnSuccessListener {  }
                                                            .addOnFailureListener {  }
                                                    }
                                                }
                                            }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .width(30.dp)
                                        .height(30.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    border = BorderStroke(1.dp, ThirdColor),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (item.isBought.value) ThirdColor else Color.Transparent,
                                        contentColor = if (item.isBought.value) SecondColor else ThirdColor,
                                        disabledContainerColor = if (item.isBought.value) ThirdColor else ThirdColor,
                                        disabledContentColor = if (item.isBought.value) SecondColor else ThirdColor
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Toggle item bought",
                                        tint = if (item.isBought.value) SecondColor else ThirdColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Progress: $boughtItems/$totalItems",
                    color = FirstColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Total Cost: %.2f EUR".format(totalCost),
                    color = FirstColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}







