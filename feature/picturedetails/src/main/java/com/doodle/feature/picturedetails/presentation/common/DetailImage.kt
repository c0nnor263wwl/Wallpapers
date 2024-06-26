package com.doodle.feature.picturedetails.presentation.common

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.doodle.core.advertising.presentation.NativeAdCard
import com.doodle.core.domain.enums.ActionType
import com.doodle.core.domain.model.remote.RemoteImage
import com.doodle.core.ui.tweenMedium
import com.doodle.feature.picturedetails.domain.model.PageData
import com.doodle.feature.picturedetails.presentation.PictureDetailsScreenContentImage
import com.doodle.feature.picturedetails.presentation.PictureDetailsViewModel
import com.doodle.feature.picturedetails.presentation.common.actions.ActionRow
import com.doodle.feature.picturedetails.state.LocalPictureDetailsUiState
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailImage(
    modifier: Modifier = Modifier,
    pageData: PageData,
    isActiveNow: Boolean,
    onActionClick: (ActionType, RemoteImage.Hit?, Bitmap?) -> Unit,
    onNavigateBack: () -> Unit,
    onDismissAd: () -> Unit,
    onImageStateChanged: (AsyncImagePainter.State) -> Unit
) {
    val pictureDetailsUiState = LocalPictureDetailsUiState.current
    var localBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageStateBuffer by remember {
        mutableStateOf<AsyncImagePainter.State?>(null)
    }
    var isAdDismissed by remember { mutableStateOf(false) }
    val isAdNotExists = pageData.nativeAd.value == null || isAdDismissed
    val isActionRowEnabled =
        isActiveNow &&
                isAdNotExists &&
                pictureDetailsUiState == PictureDetailsViewModel.UiState.ImageStateLoaded ||
                pictureDetailsUiState == null
    LaunchedEffect(isActiveNow, imageStateBuffer) {
        if (isActiveNow) {
            imageStateBuffer?.let {
                onImageStateChanged(it)
            }
        }
    }



    AnchoredDraggableArea(
        modifier = modifier,
        onTopEnd = onNavigateBack
    ) { draggableInfo ->
        val isDragReachedThreshold = draggableInfo.progress < 0.035F
        val clipRoundedShapeAnimation by animateDpAsState(
            targetValue = if (isDragReachedThreshold) 0.dp else 24.dp,
            animationSpec = tweenMedium(),
            label = ""
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                pageData.image.value?.largeImageURL,
                onState = { state ->
                    if (state is AsyncImagePainter.State.Success) {
                        localBitmap = (state.result.drawable as BitmapDrawable).bitmap
                    }
                    imageStateBuffer = state
                },
                contentScale = ContentScale.Crop,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(PictureDetailsScreenContentImage)
                    .scale(1f - draggableInfo.progress)
                    .offset {
                        IntOffset(
                            x = 0,
                            y = draggableInfo.state
                                .requireOffset()
                                .roundToInt()
                        )
                    }
                    .anchoredDraggable(
                        state = draggableInfo.state,
                        orientation = Orientation.Vertical,
                        enabled = isActiveNow && isAdNotExists
                    )
                    .clip(
                        RoundedCornerShape(
                            bottomStart = clipRoundedShapeAnimation,
                            bottomEnd = clipRoundedShapeAnimation
                        )
                    )
                    .then(
                        if (!isAdNotExists) {
                            Modifier.blur(10.dp, 10.dp)
                        } else {
                            Modifier
                        }
                    )

            )

            NativeAdCard(
                nativeAd = pageData.nativeAd.value,
                isAdDismissed = isAdDismissed
            ) {
                isAdDismissed = true
                onDismissAd()
            }

            ActionRow(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                isActive = isActionRowEnabled,
                visible = isDragReachedThreshold,
                userImageUrl = pageData.image.value?.userImageURL ?: "",
                onActionClick = {
                    if (isActiveNow) {
                        onActionClick(it, pageData.image.value, localBitmap)
                    }
                }
            )
        }
    }
}
