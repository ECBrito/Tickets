package com.example.eventify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.eventify.di.AppModule
import com.example.eventify.ui.Screen
import kotlinx.serialization.InternalSerializationApi

// Cores do Cartão
private val CardGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2))
)

@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class)
@Composable
fun PurchaseScreen(
    eventId: String,
    navController: NavController
) {
    val viewModel = remember { AppModule.providePurchaseViewModel(eventId) }
    val event by viewModel.event.collectAsState()
    val isProcessing by viewModel.isProcessingPayment.collectAsState() // Novo estado

    // Estado do Formulário
    var quantity by remember { mutableIntStateOf(1) }
    var cardNumber by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }

    val totalAmount = (event?.price ?: 0.0) * quantity

    Scaffold(
        containerColor = Color(0xFF0B0A12),
        topBar = {
            TopAppBar(
                title = { Text("Checkout", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0A12))
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    viewModel.processPaymentAndPurchase(quantity) {
                        // Sucesso: Volta à Home e limpa
                        navController.navigate(Screen.HOME_ROOT) {
                            popUpTo(Screen.HOME_ROOT) { inclusive = true }
                        }
                    }
                },
                // Validação simples: campos não vazios
                enabled = !isProcessing && cardNumber.length >= 16 && cardName.isNotBlank() && cardCvv.length >= 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF))
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Processing...")
                } else {
                    Text("Pay $$totalAmount", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // 1. Resumo do Evento
            event?.let { evt ->
                Text(evt.title, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Text("${evt.dateTime.take(10)} • ${evt.location}", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(24.dp))

            // 2. Cartão de Crédito Visual
            CreditCardView(cardNumber, cardName, cardExpiry)

            Spacer(Modifier.height(24.dp))

            // 3. Seletor de Quantidade
            Text("Tickets", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF151520), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Text("General Admission", color = Color.White, modifier = Modifier.weight(1f).padding(start = 8.dp))
                Text("$${event?.price ?: 0.0}", color = Color(0xFF7B61FF), fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 16.dp))

                FilledIconButton(
                    onClick = { if (quantity > 1) quantity-- },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF2C2C3E))
                ) { Text("-", color = Color.White, fontSize = 18.sp) }

                Text("$quantity", color = Color.White, modifier = Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold)

                FilledIconButton(
                    onClick = { quantity++ },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF7B61FF))
                ) { Text("+", color = Color.White, fontSize = 18.sp) }
            }

            Spacer(Modifier.height(24.dp))

            // 4. Formulário de Pagamento
            Text("Payment Details", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            PaymentInput(
                value = cardNumber,
                onValueChange = { if (it.length <= 16) cardNumber = it },
                label = "Card Number",
                icon = Icons.Default.CreditCard,
                keyboardType = KeyboardType.Number
            )

            Spacer(Modifier.height(12.dp))

            PaymentInput(
                value = cardName,
                onValueChange = { cardName = it },
                label = "Cardholder Name",
                icon = Icons.Default.Person
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PaymentInput(
                    value = cardExpiry,
                    onValueChange = { if (it.length <= 5) cardExpiry = it },
                    label = "Expiry (MM/YY)",
                    icon = Icons.Default.DateRange,
                    modifier = Modifier.weight(1f)
                )
                PaymentInput(
                    value = cardCvv,
                    onValueChange = { if (it.length <= 3) cardCvv = it },
                    label = "CVV",
                    icon = Icons.Default.Lock,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun CreditCardView(number: String, name: String, expiry: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardGradient)
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Credit Card", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Icon(Icons.Default.CreditCard, null, tint = Color.White)
            }

            // Número do Cartão (Formatado **** ****)
            Text(
                text = number.chunked(4).joinToString("  ").ifBlank { "**** **** **** ****" },
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp
            )

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("CARD HOLDER", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                    Text(name.ifBlank { "YOUR NAME" }, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("EXPIRES", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                    Text(expiry.ifBlank { "MM/YY" }, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PaymentInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = Color.Gray) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF7B61FF),
            unfocusedBorderColor = Color(0xFF2C2C3E),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = Color(0xFF7B61FF),
            unfocusedLabelColor = Color.Gray,
            cursorColor = Color(0xFF7B61FF)
        )
    )
}