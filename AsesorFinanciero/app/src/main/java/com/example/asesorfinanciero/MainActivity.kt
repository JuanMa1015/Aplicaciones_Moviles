package com.example.asesorfinanciero


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.roundToInt
import androidx.compose.material3.AssistChipDefaults


data class PortfolioSuggestion(
    val profile: String,
    val allocation: Map<String, Int>, // porcentajes
    val liquidity: String,
    val expectedReturnRange: String,
    val notes: String
)


class AdvisorViewModel : ViewModel() {

    private val _state = MutableStateFlow(AdvisorState())
    val state: StateFlow<AdvisorState> = _state


    fun updateState(transform: AdvisorState.() -> AdvisorState) {
        _state.value = _state.value.transform()
    }


    fun calculateProfile(): Pair<String, String> {
        val s = _state.value


        var score = 0.0

        // 1) Horizonte
        score += when (s.horizon) {
            "corto" -> 5.0
            "medio" -> 12.0
            "3-5" -> 20.0
            "5-10" -> 30.0
            ">10" -> 40.0
            else -> 15.0
        } * 0.20

        // 2) Situación financiera
        val incomeFactor = when (s.incomeRange) {
            "≤3M" -> 5.0
            "3-6M" -> 10.0
            "6-12M" -> 18.0
            "12-20M" -> 26.0
            ">20M" -> 35.0
            else -> 10.0
        }
        val saveFactor = when (s.savingsPercent) {
            "5-10%" -> 5.0
            "10-20%" -> 12.0
            "20-30%" -> 22.0
            ">30%" -> 30.0
            else -> 8.0
        }
        val emergencyFactor = when (s.emergencyMonths) {
            "0" -> 0.0
            "1-3" -> 5.0
            "3-6" -> 12.0
            ">6" -> 20.0
            else -> 5.0
        }
        score += ((incomeFactor + saveFactor + emergencyFactor) / 3.0) * 0.25

        // 3) Experiencia
        val expLevel = when (s.experienceLevel) {
            "básico" -> 4.0
            "intermedio" -> 12.0
            "avanzado" -> 25.0
            else -> 8.0
        }
        score += expLevel * 0.10

        // 4) Tolerancia al riesgo
        val dropTolerance = when (s.maxAnnualDrop) {
            "-5%" -> 5.0
            "-10%" -> 12.0
            "-20%" -> 25.0
            "-35%" -> 35.0
            else -> 12.0
        }
        val reaction = when (s.reactionToDrop) {
            "vendes" -> 2.0
            "mantienes" -> 12.0
            "compras" -> 30.0
            else -> 10.0
        }
        val prefChoice = when (s.preferenceExpectedReturn) {
            "6%" -> 5.0
            "10%" -> 18.0
            "15%" -> 30.0
            else -> 12.0
        }
        score += ((dropTolerance + reaction + prefChoice) / 3.0) * 0.30

        // 5) Restricciones
        val restrictionPenalty = when {
            s.liquidityMinPercent >= 50 -> -10.0
            s.liquidityMinPercent >= 20 -> -5.0
            else -> 0.0
        }
        val currencyRisk = when (s.currencyRisk) {
            "baja" -> -2.0
            "media" -> 0.0
            "alta" -> 4.0
            else -> 0.0
        }
        score += (restrictionPenalty + currencyRisk) * 0.15

        val finalScore = score.coerceIn(0.0, 100.0)
        val profile = when {
            finalScore < 20 -> "Conservador"
            finalScore < 40 -> "Moderado"
            finalScore < 60 -> "Balanceado"
            else -> "Arriesgado / Agresivo"
        }

        val explanation = "Score: ${finalScore.roundToInt()}. " +
                "Horizonte, situación financiera, experiencia y tolerancia determinan la capacidad y disposición al riesgo."

        return profile to explanation
    }

    fun suggestionForProfile(profile: String): PortfolioSuggestion {
        return when (profile) {
            "Conservador" -> PortfolioSuggestion(
                profile = profile,
                allocation = mapOf(
                    "Efectivo / CDT / liquidez" to 50,
                    "Renta fija (FIC, TES cortos)" to 30,
                    "Fondos mixtos conservadores / FVP" to 10,
                    "Renta variable local/internacional" to 5,
                    "Alternativos / inmobiliario" to 5
                ),
                liquidity = "Alta: gran parte en instrumentos rescatables en 1-7 días (≥40%).",
                expectedReturnRange = "2% - 6% anual (esperanza conservadora).",
                notes = "Baja tolerancia a drawdowns. Priorizar fondos con baja volatilidad, cuentas AFC si hay ventajas fiscales."
            )

            "Moderado" -> PortfolioSuggestion(
                profile = profile,
                allocation = mapOf(
                    "Efectivo / liquidez" to 25,
                    "Renta fija (TES, bonos corporativos)" to 35,
                    "Fondos mixtos / FIC" to 20,
                    "Renta variable (acciones/ETFs)" to 15,
                    "Alternativos" to 5
                ),
                liquidity = "Moderada: parte en instrumentos con 7-30 días de rescate y vencimientos cortos.",
                expectedReturnRange = "4% - 8% anual.",
                notes = "Mantener fondo de emergencia 3-6 meses. Balance entre protección y crecimiento."
            )

            "Balanceado" -> PortfolioSuggestion(
                profile = profile,
                allocation = mapOf(
                    "Efectivo" to 10,
                    "Renta fija" to 30,
                    "Renta variable (COLCAP y ETFs internacionales)" to 40,
                    "FIC / FVP" to 10,
                    "Inmobiliario / alternativos" to 10
                ),
                liquidity = "Equilibrada: algunas posiciones con lockups (90-365 días) aceptables.",
                expectedReturnRange = "6% - 12% anual.",
                notes = "Diversificación internacional recomendada (ETFs USD). Considerar cobertura cambiaria según tolerancia."
            )

            "Arriesgado / Agresivo" -> PortfolioSuggestion(
                profile = profile,
                allocation = mapOf(
                    "Renta variable local e internacional" to 60,
                    "ETFs y acciones directas" to 25,
                    "Renta fija (alto rendimiento)" to 5,
                    "Alternativos / private" to 10
                ),
                liquidity = "Baja aceptación de reembolsos inmediatos; hay posiciones con lockups largos.",
                expectedReturnRange = "10% - 20%+ anual (alto riesgo).",
                notes = "Alto drawdown posible. Recomendable experiencia previa y uso de cuentas internacionales si procede."
            )

            else -> PortfolioSuggestion(
                profile = "Meta-Mixto",
                allocation = mapOf(
                    "Objetivo A (corto plazo conservador)" to 50,
                    "Objetivo B (largo plazo arriesgado)" to 50
                ),
                liquidity = "Variable por objetivo.",
                expectedReturnRange = "Combinado según objetivos.",
                notes = "Recomendar creación de sub-carteras por objetivo/horizonte."
            )
        }
    }
}

data class AdvisorState(
    // Section 1
    val objectives: String? = null,
    val horizon: String? = null,
    val objectivePriority: String? = null,
    val objectiveAdmitDrop: String? = null,

    // Section 2
    val incomeRange: String? = null,
    val savingsPercent: String? = null,
    val emergencyMonths: String? = null,
    val debtDescription: String? = null,
    val withdrawNext36: String? = null,
    val initialInvestment: String? = null,
    val periodicContribution: String? = null,

    // Section 3
    val productsUsed: List<String> = emptyList(),
    val experienceLevel: String? = null,
    val investedIntl: String? = null,
    val preferManaged: String? = null,

    // Section 4
    val reactionToDrop: String? = null,
    val maxAnnualDrop: String? = null,
    val monthlyVolatilityDiscomfort: String? = null,
    val preferenceExpectedReturn: String? = null,
    val crisisReaction: String? = null,
    val percentEquityInCrisis: String? = null,
    val illiquidityAcceptance: String? = null,

    // Section 5
    val liquidityMinPercent: Int = 30,
    val esgPreference: String? = null,
    val allowedCurrencies: String? = null,
    val currencyRisk: String? = null,
    val incomePreference: String? = null,
    val taxPreferences: String? = null,
    val legalRestrictions: String? = null,
    val sectorPreferences: String? = null,
    val interestInRealEstate: String? = null,
    val interestAlternatives: String? = null,
    val minTicketSize: String? = null,

    // Section 6
    val patrimonyDistribution: Map<String, Int> = emptyMap(),
    val intermediaries: String? = null,
    val totalCostsPercent: String? = null,

    // Section 7
    val managementPreference: String? = null,
    val involvementFrequency: String? = null,
    val reportFormat: String? = null,
    val benchmark: String? = null,
    val milestoneDate: String? = null,
    val otherNotes: String? = null
)

// ---------- UI ----------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AdvisorApp()
                }
            }
        }
    }
}

@Composable
fun AdvisorApp(vm: AdvisorViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    var page by remember { mutableStateOf(0) } // 0: form, 1: result

    if (page == 0) {
        QuestionnaireScreen(
            state = state,
            onUpdate = { updateFn -> vm.updateState(updateFn) },
            onSubmit = { page = 1 }
        )
    } else {
        val (profile, explanation) = vm.calculateProfile()
        val suggestion = vm.suggestionForProfile(profile)
        ResultScreen(profile, explanation, suggestion) { page = 0 }
    }
}

@Composable
fun QuestionnaireScreen(
    state: AdvisorState,
    onUpdate: (AdvisorState.() -> AdvisorState) -> Unit,
    onSubmit: () -> Unit
) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp)
    ) {
        Header("Asesor Financiero — Perfil de Riesgo")
        Spacer(Modifier.height(8.dp))

        // ------- SECCIÓN 1 -------
        CardSection(title = "1) Objetivos y horizonte") {
            Label("¿Para qué inviertes? (breve)")
            OutlinedTextField(
                value = state.objectives.orEmpty(),
                onValueChange = { v -> onUpdate { copy(objectives = v) } },
                placeholder = { Text("ej.: crecimiento patrimonial, retiro...") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Label("Horizonte principal")
            HorizontalOptions(
                listOf("corto", "medio", "3-5", "5-10", ">10"),
                state.horizon
            ) { sel ->
                onUpdate { copy(horizon = sel) }
            }
            Spacer(Modifier.height(8.dp))
            Label("¿Admite caídas temporales?")
            HorizontalOptions(listOf("sí", "no", "depende"), state.objectiveAdmitDrop) { sel ->
                onUpdate { copy(objectiveAdmitDrop = sel) }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ------- SECCIÓN 2 -------
        CardSection(title = "2) Situación financiera actual (confidencial)") {
            Label("Ingreso mensual neto (rango)")
            HorizontalOptions(
                listOf("≤3M", "3-6M", "6-12M", "12-20M", ">20M"),
                state.incomeRange
            ) { sel ->
                onUpdate { copy(incomeRange = sel) }
            }
            Spacer(Modifier.height(8.dp))
            Label("Porcentaje que puedes ahorrar/invertir")
            HorizontalOptions(
                listOf("5-10%", "10-20%", "20-30%", ">30%"),
                state.savingsPercent
            ) { sel ->
                onUpdate { copy(savingsPercent = sel) }
            }
            Spacer(Modifier.height(8.dp))
            Label("Fondo de emergencia (meses)")
            HorizontalOptions(listOf("0", "1-3", "3-6", ">6"), state.emergencyMonths) { sel ->
                onUpdate { copy(emergencyMonths = sel) }
            }
            Spacer(Modifier.height(8.dp))
            Label("¿Necesitarás retirar en 12–36 meses? (monto/cuándo)")
            OutlinedTextField(
                value = state.withdrawNext36.orEmpty(),
                onValueChange = { v -> onUpdate { copy(withdrawNext36 = v) } },
                placeholder = { Text("ej.: No / \$X en 18 meses") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Label("Tamaño inicial y aportes periódicos")
            OutlinedTextField(
                value = state.initialInvestment.orEmpty(),
                onValueChange = { v -> onUpdate { copy(initialInvestment = v) } },
                placeholder = { Text("ej.: $5M inicial, $500k mensual") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(12.dp))

        // ------- SECCIÓN 3 -------
        CardSection(title = "3) Experiencia y conocimientos") {
            Label("Productos que has usado (separa comas)")
            OutlinedTextField(
                value = state.productsUsed.joinToString(", "),
                onValueChange = { v ->
                    onUpdate {
                        copy(productsUsed = v.split(",").map { it.trim() }
                            .filter { it.isNotEmpty() })
                    }
                },
                placeholder = { Text("CDT, FIC, TES, acciones, ETFs...") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Label("Nivel experiencia renta fija/variable")
            HorizontalOptions(
                listOf("básico", "intermedio", "avanzado"),
                state.experienceLevel
            ) { sel ->
                onUpdate { copy(experienceLevel = sel) }
            }
            Spacer(Modifier.height(8.dp))
            Label("¿Prefieres gestionados o autogestionados?")
            HorizontalOptions(
                listOf("gestionados", "autogestionados", "mixto"),
                state.preferManaged
            ) { sel ->
                onUpdate { copy(preferManaged = sel) }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ------- SECCIÓN 4 -------
        CardSection(title = "4) Tolerancia al riesgo (psicométrica y práctica)") {
            Label("Si $100 cae a $85 en un mes, ¿qué haces?")
            HorizontalOptions(
                listOf("vendes", "mantienes", "compras"),
                state.reactionToDrop
            ) { sel ->
                onUpdate { copy(reactionToDrop = sel) }
            }
            Spacer(Modifier.height(8.dp))
            Label("Caída máxima tolerable en un año")
            HorizontalOptions(listOf("-5%", "-10%", "-20%", "-35%"), state.maxAnnualDrop) { sel ->
                onUpdate { copy(maxAnnualDrop = sel) }
            }
            Spacer(Modifier.height(8.dp))
            Label("¿Cuál prefieres?")
            HorizontalOptions(listOf("6%", "10%", "15%"), state.preferenceExpectedReturn) { sel ->
                onUpdate { copy(preferenceExpectedReturn = sel) }
            }
            Spacer(Modifier.height(8.dp))
            Label("¿Qué porcentaje en renta variable aceptarías durante una crisis?")
            HorizontalOptions(
                listOf("0%", "10-30%", "30-60%", ">60%"),
                state.percentEquityInCrisis
            ) { sel ->
                onUpdate { copy(percentEquityInCrisis = sel) }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ------- SECCIÓN 5 -------
        CardSection(title = "5) Restricciones y preferencias") {
            Label("Liquidez mínima (¿% rescatable en 1–7 días hábiles?)")
            var percent by remember { mutableStateOf(state.liquidityMinPercent) }
            Slider(
                value = percent.toFloat(),
                onValueChange = {
                    percent = it.roundToInt()
                    onUpdate { copy(liquidityMinPercent = percent) }
                },
                valueRange = 0f..100f
            )
            Text("$percent% rescatable en 1–7 días")
            Spacer(Modifier.height(8.dp))
            Label("Preferencias ESG / exclusiones (si aplica)")
            OutlinedTextField(
                value = state.esgPreference.orEmpty(),
                onValueChange = { v -> onUpdate { copy(esgPreference = v) } },
                placeholder = { Text("ej.: sí - energías limpias; evitar tabaco...") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Label("Monedas permitidas")
            HorizontalOptions(
                listOf("COP solo", "COP+USD", "Incluye EUR/otros"),
                state.allowedCurrencies
            ) { sel ->
                onUpdate { copy(allowedCurrencies = sel) }
            }
            Spacer(Modifier.height(8.dp))
            Label("Tolerancia al riesgo cambiario")
            HorizontalOptions(listOf("baja", "media", "alta"), state.currencyRisk) { sel ->
                onUpdate { copy(currencyRisk = sel) }
            }
        }

        Spacer(Modifier.height(12.dp))


        // ------- SECCIÓN 6 COMPLETA CORREGIDA -------
        CardSection(title = "6) Estructura actual del patrimonio (si aplica)") {
            Label("Distribución aproximada (%) — escribir pares 'categoria:valor' separados por comas")

            // Necesitamos usar remember para mantener el estado local del texto
            var patrimonioText by remember(state.patrimonyDistribution) {
                mutableStateOf(
                    if (state.patrimonyDistribution.isEmpty()) {
                        ""
                    } else {
                        state.patrimonyDistribution.map { "${it.key}:${it.value}" }.joinToString(", ")
                    }
                )
            }

            OutlinedTextField(
                value = patrimonioText,
                onValueChange = { newText ->
                    patrimonioText = newText

                    // Parseamos el texto a mapa solo si no está vacío
                    val map: Map<String, Int> = if (newText.isBlank()) {
                        emptyMap()
                    } else {
                        newText.split(",")
                            .mapNotNull { item ->
                                val parts = item.split(":").map { p -> p.trim() }
                                if (parts.size == 2 && parts[0].isNotBlank()) {
                                    val key = parts[0]
                                    val value = parts[1].toIntOrNull() ?: 0
                                    key to value
                                } else null
                            }.toMap()
                    }

                    // Actualizamos el estado
                    onUpdate { copy(patrimonyDistribution = map) }
                },
                placeholder = { Text("ej.: efectivo:30, renta fija:40, acciones:30") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // Campo de Intermediarios actuales
            Label("Intermediarios actuales")
            OutlinedTextField(
                value = state.intermediaries ?: "",
                onValueChange = { v -> onUpdate { copy(intermediaries = v) } },
                placeholder = { Text("banco X, comisionista Y, broker Z") },
                modifier = Modifier.fillMaxWidth()
            )
        }


        Spacer(Modifier.height(12.dp))

        // ------- SECCIÓN 7 -------
        CardSection(title = "7) Operativa y servicios") {
            Label("Preferencia gestión")
            HorizontalOptions(
                listOf("pasiva", "activa", "mixta"),
                state.managementPreference
            ) { sel ->
                onUpdate { copy(managementPreference = sel) }
            }
            Spacer(Modifier.height(8.dp))
            Label("¿Con qué frecuencia quieres revisar el portafolio?")
            HorizontalOptions(
                listOf("trimestral", "semestral", "anual", "solo eventos"),
                state.involvementFrequency
            ) { sel ->
                onUpdate { copy(involvementFrequency = sel) }
            }
            Spacer(Modifier.height(8.dp))
            Label("¿Benchmark de referencia?")
            OutlinedTextField(
                value = state.benchmark.orEmpty(),
                onValueChange = { v -> onUpdate { copy(benchmark = v) } },
                placeholder = { Text("ej.: inflación+3%, COLCAP, MSCI ACWI...") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(20.dp))
        Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth()) {
            Text("Evaluar perfil y sugerir portafolio")
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun ResultScreen(
    profile: String,
    explanation: String,
    suggestion: PortfolioSuggestion,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Header("Resultado: $profile")
        Spacer(Modifier.height(8.dp))
        Text(explanation, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
        CardSection(title = "Sugerencia de portafolio") {
            suggestion.allocation.forEach { (k, v) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(k, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                    Text("$v%", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(6.dp))
            }
            Spacer(Modifier.height(8.dp))
            Label("Liquidez")
            Text(suggestion.liquidity)
            Spacer(Modifier.height(8.dp))
            Label("Rango de retorno esperado")
            Text(suggestion.expectedReturnRange)
            Spacer(Modifier.height(8.dp))
            Label("Notas")
            Text(suggestion.notes)
        }

        Spacer(Modifier.height(12.dp))
        CardSection(title = "Sugerencias prácticas (ejemplos)") {
            Text("Instrumentos (ejemplos generales):")
            Spacer(Modifier.height(6.dp))
            Text("- Renta fija local: TES, bonos corporativos, CDT; fondos FIC de renta fija.")
            Text("- Renta variable local: acciones incluidas en COLCAP, fondos indexados locales.")
            Text("- Internacional: ETFs (S&P500, MSCI), REITs, bonos internacionales; cuentas/brokers internacionales.")
            Text("- Vehículos de ahorro con beneficios fiscales: cuentas AFC, FVP según disponibilidad.")
            Spacer(Modifier.height(6.dp))
            Text("Ten en cuenta: debes revisar emisores concretos, comisiones y condiciones de liquidez antes de invertir.")
        }

        Spacer(Modifier.height(20.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Volver y ajustar respuestas")
        }
        Spacer(Modifier.height(40.dp))
    }
}


@Composable
fun Header(title: String) {
    Text(title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
}

@Composable
fun CardSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun Label(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun HorizontalOptions(options: List<String>, selected: String?, onSelect: (String) -> Unit) {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll)
            .padding(vertical = 6.dp)
    ) {
        options.forEach { opt ->
            val isSelected = opt == selected
            AssistChip(
                onClick = { onSelect(opt) },
                label = { Text(opt) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    else
                        MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}
