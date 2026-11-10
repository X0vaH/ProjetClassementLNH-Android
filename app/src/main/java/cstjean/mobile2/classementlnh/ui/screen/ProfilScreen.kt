package cstjean.mobile2.classementlnh.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cstjean.mobile2.classementlnh.viewmodel.ProfilViewModel

@Composable
fun ProfilScreen(viewModel: ProfilViewModel, onRetour: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)) {
        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.errorMessage != null -> Text(uiState.errorMessage!!)
            uiState.profil != null -> {
                Text(
                    "Bonjour, ${uiState.profil!!.prenom} !",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(uiState.profil!!.courriel)
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = viewModel::deconnexion) { Text("Se déconnecter") }
        TextButton(onClick = onRetour) { Text("Retour") }
    }
}
