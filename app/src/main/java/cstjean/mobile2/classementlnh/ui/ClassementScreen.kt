package cstjean.mobile2.classementlnh.ui

import android.Manifest


import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.work.WorkManager
import cstjean.mobile2.classementlnh.business.EquipeUiModel
import cstjean.mobile2.classementlnh.service.ExportService
import cstjean.mobile2.classementlnh.worker.SyncClassementWorker
import cstjean.mobile2.classementlnh.viewmodel.ClassementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassementScreen(
    viewModel: ClassementViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Observation de l'état du travail périodique WorkManager
    val workInfos by WorkManager.getInstance(context)
        .getWorkInfosForUniqueWorkFlow(SyncClassementWorker.NOM_TRAVAIL_UNIQUE)
        .collectAsState(initial = emptyList())
    val etatSync = workInfos.firstOrNull()?.state?.name

    // Observation de la tâche unique « Synchroniser maintenant » (test)
    val workInfosImmediat by WorkManager.getInstance(context)
        .getWorkInfosForUniqueWorkFlow(SyncClassementWorker.NOM_TRAVAIL_IMMEDIAT)
        .collectAsState(initial = emptyList())
    val etatSyncImmediate = workInfosImmediat.firstOrNull()?.state?.name

    // Exportation en attente de la réponse à la demande de permission (true = échec simulé)
    var simulerEchecEnAttente by remember { mutableStateOf(false) }

    // Demande de la permission POST_NOTIFICATIONS (Android 13+)
    val launcherPermissionNotification = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { estAccorde ->
        if (!estAccorde) {
            Toast.makeText(
                context,
                "Permission refusée : l'exportation s'exécutera sans notification visible.",
                Toast.LENGTH_LONG
            ).show()
        }
        ExportService.demarrer(context, simulerEchecEnAttente)
    }

    val lancerExportation: (Boolean) -> Unit = { simulerEchec ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            simulerEchecEnAttente = simulerEchec
            launcherPermissionNotification.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            ExportService.demarrer(context, simulerEchec)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Classement LNH") }) }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.synchroniser() },
                    enabled = !state.estEnChargement
                ) {
                    Text("Synchroniser")
                }

                Button(onClick = { lancerExportation(false) }) {
                    Text("Exporter")
                }
            }

            // Boutons temporaires pour les tests de l'Exercice 5
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { SyncClassementWorker.synchroniserMaintenant(context) }) {
                    Text("Sync maintenant")
                }
                TextButton(onClick = { lancerExportation(true) }) {
                    Text("Export (échec)")
                }
            }

            etatSyncImmediate?.let { etat ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Sync maintenant (tâche unique) :", style = MaterialTheme.typography.bodySmall)
                    Badge { Text(etat) }
                }
            }

            etatSync?.let { etat ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Sync auto (WorkManager) :", style = MaterialTheme.typography.bodySmall)
                    Badge { Text(etat) }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            state.dateDerniereSync?.let { date ->
                Text(text = "Dernière sync : $date", style = MaterialTheme.typography.bodySmall)
            }

            if (state.estEnChargement) {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 8.dp))
            }

            state.messageErreur?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(state.equipes, key = { it.abbrev }) { equipe ->
                    EquipeItem(equipe = equipe)
                }
            }
        }
    }
}

@Composable
fun EquipeItem(equipe: EquipeUiModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${equipe.nom} (${equipe.abbrev})",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${equipe.conference} - ${equipe.division}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "MJ: ${equipe.matchsJoues} | V: ${equipe.victoires} | D: ${equipe.defaites} | DP: ${equipe.defaitesProlongation}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(text = "${equipe.points} PTS", style = MaterialTheme.typography.titleLarge)
        }
    }
}
