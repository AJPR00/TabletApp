package com.ajpr00.components.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ajpr00.uicommon.R


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ButtonCustonPanelPreview() {
    ButtonCustonPanel(
        icon = painterResource(id = R.drawable.email_ic),
        label = "Enviar Imagen",
        iconSize = 50.dp,
        onClick = { })
}

@Composable
fun ButtonCustonPanel(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .aspectRatio(1f),
    shape: Shape = RoundedCornerShape(10),
    icon: Painter,
    iconSize: Dp = 90.dp,
    label: String,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    ElevatedButton(
        modifier = modifier,
        shape = shape,
        colors = ButtonDefaults.textButtonColors(
            containerColor = color,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        onClick = onClick
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            /** Usamos Icon para recursos vectoriales porque permiten aplicar tint y mantienen su forma.
            Usamos Image para imágenes no vectoriales (como avatares remotos) porque respetan sus colores,
            se ajustan mejor al recorte circular y permiten ContentScale.Crop para llenar el espacio.*/

            val esVector = icon is VectorPainter

            if (esVector) {
                // 👉 Iconos vectoriales: tint + no circular
                Icon(
                    painter = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(iconSize)
                )
            } else {
                // 👉 Imágenes NO vectoriales: sin tint + circular + ajustada
                Image(
                    painter = icon,
                    contentDescription = label,
                    modifier = Modifier
                        .size(iconSize)
                        .clip(CircleShape)
                        .border(2.dp, Color.White, CircleShape),
                    contentScale = ContentScale.Crop   // 👈 CLAVE: ajusta la imagen al círculo
                )
            }

            Text(text = label, textAlign = TextAlign.Center)
        }
    }
}
