package com.example.eventify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalOffer // Ícone de etiqueta
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration // Para riscar o preço antigo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.eventify.di.AppModule
import com.example.eventify.ui.Screen
import kotlinx.serialization.InternalSerializationApi

private val CardGradient = Brush.linearGradient(colors = listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2)))
private val AccentPurple = Color(0xFF7B61FF)

@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class)
@Composable
fun PurchaseScreen(
    eventId: String,
    navController: NavController
) {
    val viewModel = remember { AppModule.providePurchaseViewModel(eventId) }
    val event by viewModel.event.collectAsState()
    val isProcessing by viewModel.isProcessingPayment.collectAsState()

    // Estados de Desconto
    val discountPercent by viewModel.discountPercent.collectAsState()
    val promoMessage by viewModel.promoMessage.collectAsState()

    // Form
    var quantity by remember { mutableIntStateOf(1) }
    var promoCodeInput by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }

    // Lógica de Preço
    val basePrice = event?.price ?: 0.0
    val subTotal = basePrice * quantity
    val discountAmount = subTotal * (discountPercent / 100.0)
    val finalTotal = subTotal - discountAmount

    Scaffold(
        containerColor = Color(0xFF0B0A12),
        topBar = {
            TopAppBar(
                title = { Text("Checkout", color = Color.White) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0A12))
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    viewModel.processPaymentAndPurchase(quantity) {
                        navController.navigate(Screen.HOME_ROOT) { popUpTo(Screen.HOME_ROOT) { inclusive = true } }
                    }
                },
                enabled = !isProcessing && cardNumber.length >= 16,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    // Mostra o total final no botão
                    Text("Pay $${"%.2f".format(finalTotal)}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(androidx.compose.foundation.rememberScrollState()) // Scroll para ecrãs pequenos
        ) {
            event?.let { evt ->
                Text(evt.title, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Text("${evt.dateTime.take(10)} • ${evt.locationName}", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(24.dp))

            CreditCardView(cardNumber, cardName, cardExpiry)
            Spacer(Modifier.height(24.dp))

            // --- QUANTIDADE ---
            Text("Tickets", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().background(Color(0xFF151520), RoundedCornerShape(12.dp)).padding(8.dp)
            ) {
                Text("General Admission", color = Color.White, modifier = Modifier.weight(1f).padding(start = 8.dp))

                // Preço Riscado se houver desconto
                if (discountPercent > 0) {
                    Text("$${basePrice}", color = Color.Gray, textDecoration = TextDecoration.LineThrough, modifier = Modifier.padding(end = 8.dp))
                }
                Text("$${"%.2f".format(basePrice * (1 - discountPercent/100.0))}", color = AccentPurple, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 16.dp))

                FilledIconButton(onClick = { if (quantity > 1) quantity-- }, colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF2C2C3E))) { Text("-", color = Color.White, fontSize = 18.sp) }
                Text("$quantity", color = Color.White, modifier = Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold)
                FilledIconButton(onClick = { quantity++ }, colors = IconButtonDefaults.filledIconButtonColors(containerColor = AccentPurple)) { Text("+", color = Color.White, fontSize = 18.sp) }
            }

            Spacer(Modifier.height(24.dp))

            // --- CÓDIGO PROMOCIONAL (NOVO) ---
            Text("Promo Code", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = promoCodeInput,
                    onValueChange = { promoCodeInput = it.uppercase() }, // Força maiúsculas
                    placeholder = { Text("Enter Code (ex: SAVE10)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple, unfocusedBorderColor = Color(0xFF2C2C3E), focusedTextColor = Color.White, unfocusedTextColor = Color.White
                    )
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.applyPromoCode(promoCodeInput) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C3E)),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("Apply")
                }
            }
            // Mensagem de Feedback do Código
            if (promoMessage.isNotEmpty()) {
                Text(
                    text = promoMessage,
                    color = if (discountPercent > 0) Color(0xFF00E096) else Color(0xFFFF3D71),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // --- FORMULÁRIO DE PAGAMENTO ---
            Text("Payment Details", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            PaymentInput(cardNumber, { if (it.length <= 16) cardNumber = it }, "Card Number", Icons.Default.CreditCard, keyboardType = KeyboardType.Number)
            Spacer(Modifier.height(12.dp))
            PaymentInput(cardName, { cardName = it }, "Cardholder Name", Icons.Default.Person)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PaymentInput(cardExpiry, { if (it.length <= 5) cardExpiry = it }, "Expiry (MM/YY)", Icons.Default.DateRange, Modifier.weight(1f))
                PaymentInput(cardCvv, { if (it.length <= 3) cardCvv = it }, "CVV", Icons.Default.Lock, Modifier.weight(1f), KeyboardType.Number)
            }

            Spacer(Modifier.height(40.dp)) // Espaço extra fundo
        }
    }
}

// (Mantém CreditCardView e PaymentInput aqui no fundo do ficheiro, iguais ao passo anterior)
@Composable
fun CreditCardView(number: String, name: String, expiry: String) {
    Box(
        modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)).background(CardGradient).padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Credit Card", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Icon(Icons.Default.CreditCard, null, tint = Color.White)
            }
            Text(text = number.chunked(4).joinToString("  ").ifBlank { "**** **** **** ****" }, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column { Text("CARD HOLDER", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp); Text(name.ifBlank { "YOUR NAME" }, color = Color.White, fontWeight = FontWeight.Bold) }
                Column(horizontalAlignment = Alignment.End) { Text("EXPIRES", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp); Text(expiry.ifBlank { "MM/YY" }, color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
fun PaymentInput(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector, modifier: Modifier = Modifier, keyboardType: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) }, leadingIcon = { Icon(icon, null, tint = Color.Gray) }, modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPurple, unfocusedBorderColor = Color(0xFF2C2C3E), focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = AccentPurple)
    )
}