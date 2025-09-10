package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppUI()
            }
        }
    }
}

@Composable
fun AppUI() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEDEDED))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Apartado 1: Número único con incrementar/dividir
        NumeroUnicoApartado()

        // Apartado 2: Comparación de dos números aleatorios
        ComparacionNumerosApartado()

        // Apartado 3: Generador Baloto
        BalotoApartado()
    }
}

@Composable
fun NumeroUnicoApartado() {
    var numero by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFD0F0C0), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Número: $numero",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32),
            textAlign = TextAlign.Center
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { if (numero < 100) numero += 1 },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
            ) {
                Text("+1", color = Color.White)
            }
            Button(
                onClick = { numero /= 2 },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("/2", color = Color.White)
            }
            Button(
                onClick = { numero = Random.nextInt(0, 100) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                Text("Generar", color = Color.White)
            }
        }
    }
}

@Composable
fun ComparacionNumerosApartado() {
    var numeroA by remember { mutableStateOf(0) }
    var numeroB by remember { mutableStateOf(0) }
    var mayor by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFBBDEFB), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Comparación de Números",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0D47A1)
        )

        Text(
            text = "Número A: $numeroA",
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF0D47A1)
        )

        Text(
            text = "Número B: $numeroB",
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF0D47A1)
        )

        Button(
            onClick = {
                numeroA = Random.nextInt(0, 100)
                numeroB = Random.nextInt(0, 100)
                mayor = when {
                    numeroA > numeroB -> "El número mayor es: A ($numeroA)"
                    numeroB > numeroA -> "El número mayor es: B ($numeroB)"
                    else -> "Ambos son iguales ($numeroA)"
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            Text("Generar A y B", color = Color.White)
        }

        if (mayor.isNotEmpty()) {
            Text(
                text = mayor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BalotoApartado() {
    var bolasAmarillas by remember { mutableStateOf(listOf<Int>()) }
    var bolaRoja by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF9C4), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Generador de Baloto",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF57F17)
        )

        // Mostrar bolas amarillas
        if (bolasAmarillas.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                bolasAmarillas.forEach { num ->
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFFFFEB3B), RoundedCornerShape(25.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = num.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
            }

            // Mostrar bola roja
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color(0xFFD32F2F), RoundedCornerShape(25.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = bolaRoja.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
            }
        }

        Button(
            onClick = {
                // Generar 5 números únicos para bolas amarillas
                val amarillasSet = mutableSetOf<Int>()
                while (amarillasSet.size < 5) {
                    amarillasSet.add(Random.nextInt(1, 44))
                }
                bolasAmarillas = amarillasSet.toList().sorted()

                // Generar la bola roja
                bolaRoja = Random.nextInt(1, 17)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBC02D))
        ) {
            Text("Generar Baloto", color = Color.Black)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AppUIPreview() {
    MaterialTheme {
        AppUI()
    }
}
