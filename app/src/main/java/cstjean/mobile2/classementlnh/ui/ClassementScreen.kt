package cstjean.mobile2.classementlnh.ui

import android.content.Intent
import android.os.Build
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
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cstjean.mobile2.classementlnh.business.EquipeUiModel
import cstjean.mobile2.classementlnh.viewmodel.ClassementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassementScreen(
    viewModel: ClassementViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

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
                Button(onClick = {
                    //TODO : Exporter les données
                }) {
                    Text("Exporter")
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
