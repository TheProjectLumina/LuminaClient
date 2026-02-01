package com.project.lumina.client.overlay.entityradar

import android.graphics.BitmapFactory
import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import com.project.lumina.client.R
import com.project.lumina.client.constructors.EntityStorage
import com.project.lumina.client.constructors.MobAlertManager
import com.project.lumina.client.constructors.SelectedMobsManager
import com.project.lumina.client.constructors.NetBound
import com.project.lumina.client.overlay.manager.OverlayManager
import com.project.lumina.client.overlay.manager.OverlayWindow
import com.project.lumina.client.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.sqrt

class EntityRadarOverlay(
    private val entityStorage: EntityStorage,
    private val session: NetBound,
    private val onFollowEntity: (EntityStorage.EntityInfo) -> Unit,
    private val onStopFollowing: () -> Unit,
    private val onSpectateEntity: (EntityStorage.EntityInfo) -> Unit,
    private val onDismiss: (() -> Unit)? = null
) : OverlayWindow() {

    private val modernFont = FontFamily(Font(R.font.fredoka_light))

    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv() and
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM.inv()
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL

            if (Build.VERSION.SDK_INT >= 31) {
                blurBehindRadius = 20
            }

            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

            dimAmount = 0.7f
            windowAnimations = 0
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT

            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    private var selectedEntityName by mutableStateOf<String?>(null)
    private var selectedEntityId by mutableStateOf<Long?>(null)
    private var searchQuery by mutableStateOf("")
    private var isFollowing by mutableStateOf(false)
    private var isSpectating by mutableStateOf(false)
    private var showingSelectedList by mutableStateOf(false)

    data class EntityGroup(
        val name: String,
        val count: Int,
        val imagePath: String?,
        val entities: List<EntityStorage.EntityInfo>
    )

    @Composable
    override fun Content() {
        var shouldAnimate by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            delay(50)
            shouldAnimate = true
        }

        val translateY by animateFloatAsState(
            targetValue = if (shouldAnimate) 0f else 100f,
            animationSpec = tween(400, easing = FastOutSlowInEasing),
            label = "slideAnimation"
        )

        val alpha by animateFloatAsState(
            targetValue = if (shouldAnimate) 1f else 0f,
            animationSpec = tween(350, easing = LinearOutSlowInEasing),
            label = "fadeAnimation"
        )

        val dismissWithAnimation = remember {
            {
                shouldAnimate = false
                scope.launch {
                    delay(200)
                    OverlayManager.dismissOverlayWindow(this@EntityRadarOverlay)
                    onDismiss?.invoke()
                }
                Unit
            }
        }

        val entities = entityStorage.getEntities()
        val entityGroups = remember(entities, searchQuery) {
            entities.values
                .groupBy { it.name }
                .map { (name, entitiesList) ->
                    EntityGroup(
                        name = name,
                        count = entitiesList.size,
                        imagePath = entitiesList.firstOrNull()?.imagePath,
                        entities = entitiesList.sortedBy {
                            val coords = it.coords
                            sqrt((coords.x * coords.x + coords.y * coords.y + coords.z * coords.z).toDouble())
                        }
                    )
                }
                .filter { it.name.contains(searchQuery, ignoreCase = true) }
                .sortedByDescending { it.count }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0x70000000), Color(0x90000000))
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (selectedEntityName == null && selectedEntityId == null) {
                        dismissWithAnimation()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KitsuSurface),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.88f)
                    .graphicsLayer {
                        translationY = translateY
                        this.alpha = alpha
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { },
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    when {
                        showingSelectedList -> {
                            SelectedMobsListView(
                                entities = entities,
                                onBack = { showingSelectedList = false },
                                onEntityClick = { entityId ->
                                    selectedEntityId = entityId
                                    showingSelectedList = false
                                }
                            )
                        }
                        entityGroups.isEmpty() -> {
                            CompactHeader(
                                entities.size,
                                entityGroups.size,
                                onSearch = { searchQuery = it },
                                onClose = dismissWithAnimation
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            EmptyState()
                        }
                        selectedEntityId != null -> {
                            val entity = entities[selectedEntityId]
                            if (entity != null) {
                                IndividualEntityView(entity) {
                                    selectedEntityId = null
                                    isFollowing = false
                                    isSpectating = false
                                }
                            }
                        }
                        selectedEntityName != null -> {
                            SimpleBackButton { selectedEntityName = null }
                            Spacer(modifier = Modifier.height(12.dp))
                            EntityDetailView(
                                entityGroup = entityGroups.first { it.name == selectedEntityName },
                                onEntityClick = { selectedEntityId = it }
                            )
                        }
                        else -> {
                            CompactHeader(
                                entities.size,
                                entityGroups.size,
                                onSearch = { searchQuery = it },
                                onClose = dismissWithAnimation
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = KitsuOnSurfaceVariant.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(12.dp))
                            EntityGrid(entityGroups) { selectedEntityName = it }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun CompactHeader(
        totalEntities: Int,
        uniqueTypes: Int,
        onSearch: (String) -> Unit,
        onClose: () -> Unit
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.moon_stars_24),
                    contentDescription = null,
                    tint = KitsuPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Entity Radar",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontFamily = modernFont,
                            fontWeight = FontWeight.Bold,
                            color = KitsuOnSurface
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "$totalEntities entities • $uniqueTypes types",
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontFamily = modernFont,
                                color = KitsuOnSurfaceVariant
                            )
                        )

                        val alertCount = MobAlertManager.getAlerts().size
                        if (alertCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFFB800).copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$alertCount alerts",
                                    style = TextStyle(
                                        fontSize = 9.sp,
                                        fontFamily = modernFont,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFB800)
                                    )
                                )
                            }
                        }

                        val selectedCount = SelectedMobsManager.getSelectedCount()
                        if (selectedCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF9B59B6).copy(alpha = 0.2f))
                                    .clickable { showingSelectedList = !showingSelectedList }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = ir.alirezaivaz.tablericons.R.drawable.ic_star_filled),
                                        contentDescription = null,
                                        tint = Color(0xFF9B59B6),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = "$selectedCount",
                                        style = TextStyle(
                                            fontSize = 9.sp,
                                            fontFamily = modernFont,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF9B59B6)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            CloseButton(onClose)
        }

        Spacer(modifier = Modifier.height(10.dp))
        SearchBar(onSearch)
    }

    @Composable
    private fun SearchBar(onSearch: (String) -> Unit) {
        var query by remember { mutableStateOf("") }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(KitsuSurfaceVariant),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.moon_stars_24),
                    contentDescription = null,
                    tint = KitsuOnSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        onSearch(it)
                    },
                    textStyle = TextStyle(
                        fontSize = 13.sp,
                        fontFamily = modernFont,
                        color = KitsuOnSurface
                    ),
                    cursorBrush = SolidColor(KitsuPrimary),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (query.isEmpty()) {
                            Text(
                                text = "Search entities...",
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    fontFamily = modernFont,
                                    color = KitsuOnSurfaceVariant.copy(alpha = 0.5f)
                                )
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }
    }

    @Composable
    private fun SimpleBackButton(onBack: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack
                )
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.cross_circle_24),
                contentDescription = "Back",
                tint = KitsuPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Back",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontFamily = modernFont,
                    fontWeight = FontWeight.SemiBold,
                    color = KitsuPrimary
                )
            )
        }
    }

    @Composable
    private fun EntityGrid(
        entityGroups: List<EntityGroup>,
        onEntityClick: (String) -> Unit
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            contentPadding = PaddingValues(6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(entityGroups) { group ->
                EntityCard(group, onClick = { onEntityClick(group.name) })
            }
        }
    }

    @Composable
    private fun EntityCard(group: EntityGroup, onClick: () -> Unit) {
        var isPressed by remember { mutableStateOf(false) }
        var isVisible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(50)
            isVisible = true
        }

        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.95f else if (isVisible) 1f else 0.8f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "cardScale"
        )

        val alpha by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0f,
            animationSpec = tween(300, easing = FastOutSlowInEasing),
            label = "cardAlpha"
        )

        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = KitsuSurfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .scale(scale)
                .alpha(alpha)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isPressed = true
                    onClick()
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(KitsuSurface),
                    contentAlignment = Alignment.Center
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "iconPulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseScale"
                    )

                    Box(modifier = Modifier.scale(pulseScale)) {
                        if (group.imagePath != null) {
                            EntityImage(group.imagePath, group.name, 36.dp)
                        } else {
                            PlaceholderIcon(20.dp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = group.name,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontFamily = modernFont,
                            fontWeight = FontWeight.SemiBold,
                            color = KitsuOnSurface,
                            textAlign = TextAlign.Center
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (MobAlertManager.hasAlert(group.name)) {
                        val infiniteTransition = rememberInfiniteTransition(label = "alertPulse")
                        val alertAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.6f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "alertAlpha"
                        )

                        Icon(
                            painter = painterResource(id = R.drawable.moon_stars_24),
                            contentDescription = "Alert Active",
                            tint = Color(0xFFFFB800).copy(alpha = alertAlpha),
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    if (group.entities.any { SelectedMobsManager.isSelected(it.id) }) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(id = ir.alirezaivaz.tablericons.R.drawable.ic_star_filled),
                            contentDescription = "Selected",
                            tint = Color(0xFF9B59B6),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(KitsuPrimary.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "×${group.count}",
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontFamily = modernFont,
                            fontWeight = FontWeight.Bold,
                            color = KitsuPrimary
                        )
                    )
                }
            }
        }

        LaunchedEffect(isPressed) {
            if (isPressed) {
                delay(100)
                isPressed = false
            }
        }
    }

    @Composable
    private fun EntityDetailView(
        entityGroup: EntityGroup,
        onEntityClick: (Long) -> Unit
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            var hasAlert by remember { mutableStateOf(MobAlertManager.hasAlert(entityGroup.name)) }

            val infiniteTransition = rememberInfiniteTransition(label = "alertCardPulse")
            val cardAlpha by infiniteTransition.animateFloat(
                initialValue = 0.1f,
                targetValue = 0.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "cardAlpha"
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (hasAlert) Color(0xFFFFB800).copy(alpha = cardAlpha) else KitsuSurfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = entityGroup.name,
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontFamily = modernFont,
                                fontWeight = FontWeight.Bold,
                                color = KitsuOnSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (hasAlert) "Alert enabled for this mob" else "No alert set",
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontFamily = modernFont,
                                color = if (hasAlert) Color(0xFFFFB800) else KitsuOnSurfaceVariant
                            )
                        )
                    }

                    Button(
                        onClick = {
                            if (hasAlert) {
                                MobAlertManager.removeAlert(entityGroup.name)
                                hasAlert = false
                            } else {
                                MobAlertManager.addAlert(entityGroup.name)
                                hasAlert = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasAlert) Color(0xFFE81123) else KitsuPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (hasAlert) R.drawable.cross_circle_24 else R.drawable.moon_stars_24
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hasAlert) "Remove" else "Add Alert",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontFamily = modernFont,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entityGroup.entities) { entity ->
                    EntityListItemCard(entity) { onEntityClick(entity.id) }
                }
            }
        }
    }

    @Composable
    private fun EntityListItemCard(entity: EntityStorage.EntityInfo, onClick: () -> Unit) {
        var isVisible by remember { mutableStateOf(false) }
        var isPressed by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(30)
            isVisible = true
        }

        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.98f else if (isVisible) 1f else 0.95f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "listItemScale"
        )

        val alpha by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0f,
            animationSpec = tween(250, easing = FastOutSlowInEasing),
            label = "listItemAlpha"
        )

        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = KitsuSurfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .alpha(alpha)
                .clickable {
                    isPressed = true
                    onClick()
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(KitsuSurface),
                    contentAlignment = Alignment.Center
                ) {
                    if (entity.imagePath != null) {
                        EntityImage(entity.imagePath, entity.name, 40.dp)
                    } else {
                        PlaceholderIcon(24.dp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entity.name,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = modernFont,
                            fontWeight = FontWeight.Bold,
                            color = KitsuOnSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "(${ceil(entity.coords.x).toInt()}, ${ceil(entity.coords.y).toInt()}, ${ceil(entity.coords.z).toInt()})",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontFamily = modernFont,
                            color = KitsuOnSurfaceVariant
                        )
                    )
                }

                
                if (entity.direction != "IDLE") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(KitsuPrimary.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = entity.direction,
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontFamily = modernFont,
                                fontWeight = FontWeight.Bold,
                                color = KitsuPrimary
                            )
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun IndividualEntityView(entity: EntityStorage.EntityInfo, onBack: () -> Unit) {
        Column(modifier = Modifier.fillMaxSize()) {
            SimpleBackButton(onBack)

            Spacer(modifier = Modifier.height(12.dp))

            
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(KitsuSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (entity.imagePath != null) {
                                EntityImage(entity.imagePath, entity.name, 64.dp)
                            } else {
                                PlaceholderIcon(36.dp)
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        VerticalDivider(
                            modifier = Modifier
                                .height(72.dp)
                                .width(1.dp),
                            color = KitsuOnSurfaceVariant.copy(alpha = 0.3f)
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = entity.name,
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontFamily = modernFont,
                                    fontWeight = FontWeight.Bold,
                                    color = KitsuOnSurface
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = "ID: #${entity.id}",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontFamily = modernFont,
                                    color = KitsuPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                item {
                    HorizontalDivider(color = KitsuOnSurfaceVariant.copy(alpha = 0.2f))
                }

                item {
                    
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = KitsuSurfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val playerPos = session.localPlayer.vec3Position
                            val dx = entity.coords.x - playerPos.x
                            val dy = entity.coords.y - playerPos.y
                            val dz = entity.coords.z - playerPos.z

                            val distance = sqrt(dx * dx + dy * dy + dz * dz)
                            val direction = getCardinalDirection(dx, dz)

                            val speed = if (entity.direction != "IDLE") "Moving" else "Stationary"

                            val timeTracked = (System.currentTimeMillis() - entity.firstSeen) / 1000
                            val timeText = when {
                                timeTracked < 60 -> "${timeTracked}s"
                                timeTracked < 3600 -> "${timeTracked / 60}m ${timeTracked % 60}s"
                                else -> "${timeTracked / 3600}h ${(timeTracked % 3600) / 60}m"
                            }

                            val velocityText = if (entity.velocity < 0.1f) "Stationary" else String.format("%.2f m/s", entity.velocity)

                            InfoRowWithIcon(ir.alirezaivaz.tablericons.R.drawable.ic_compass, "Direction", direction)
                            InfoRowWithIcon(ir.alirezaivaz.tablericons.R.drawable.ic_directions, "Heading", if (entity.direction == "IDLE") "Standing Still" else entity.direction)
                            InfoRowWithIcon(ir.alirezaivaz.tablericons.R.drawable.ic_body_scan, "Status", speed)
                            InfoRowWithIcon(ir.alirezaivaz.tablericons.R.drawable.ic_location, "Distance", String.format("%.1f blocks", distance))
                            InfoRowWithIcon(ir.alirezaivaz.tablericons.R.drawable.ic_map_pin, "Location", "(${ceil(entity.coords.x).toInt()}, ${ceil(entity.coords.y).toInt()}, ${ceil(entity.coords.z).toInt()})")
                            InfoRowWithIcon(ir.alirezaivaz.tablericons.R.drawable.ic_line_height, "Height", entity.relativeHeight)
                            InfoRowWithIcon(ir.alirezaivaz.tablericons.R.drawable.ic_clock, "Tracked", timeText)
                            InfoRowWithIcon(ir.alirezaivaz.tablericons.R.drawable.ic_gauge, "Velocity", velocityText)
                            if (entity.minDistance < Float.MAX_VALUE) {
                                InfoRowWithIcon(ir.alirezaivaz.tablericons.R.drawable.ic_arrows_minimize, "Min Distance", String.format("%.1f blocks", entity.minDistance))
                            }
                            if (entity.maxDistance > 0f) {
                                InfoRowWithIcon(ir.alirezaivaz.tablericons.R.drawable.ic_arrows_maximize, "Max Distance", String.format("%.1f blocks", entity.maxDistance))
                            }
                        }
                    }
                }
            }

            var isSelected by remember { mutableStateOf(SelectedMobsManager.isSelected(entity.id)) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (isSelected) {
                            SelectedMobsManager.unselectMob(entity.id)
                            isSelected = false
                        } else {
                            SelectedMobsManager.selectMob(entity)
                            isSelected = true
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFFFFB800) else KitsuSurfaceVariant,
                        contentColor = if (isSelected) Color.Black else KitsuOnSurface
                    )
                ) {
                    Icon(
                        painter = painterResource(id = if (isSelected) ir.alirezaivaz.tablericons.R.drawable.ic_star_filled else ir.alirezaivaz.tablericons.R.drawable.ic_star),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSelected) "Selected" else "Select",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontFamily = modernFont,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Button(
                    onClick = {
                        if (isFollowing) {
                            onStopFollowing()
                            isFollowing = false
                        } else {
                            onFollowEntity(entity)
                            isFollowing = true
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFollowing) Color(0xFFE81123) else KitsuPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        painter = painterResource(id = if (isFollowing) R.drawable.cross_circle_24 else R.drawable.moon_stars_24),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isFollowing) "Stop" else "Follow",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontFamily = modernFont,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (isSpectating) {
                        isSpectating = false
                    } else {
                        onSpectateEntity(entity)
                        isSpectating = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSpectating) Color(0xFFE81123) else Color(0xFF9B59B6),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    painter = painterResource(id = if (isSpectating) R.drawable.cross_circle_24 else ir.alirezaivaz.tablericons.R.drawable.ic_eye),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSpectating) "Stop Spectating" else "Spectate Entity",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontFamily = modernFont,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }

    @Composable
    private fun InfoRowWithIcon(iconRes: Int, label: String, value: String) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = KitsuPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontFamily = modernFont,
                        fontWeight = FontWeight.Medium,
                        color = KitsuOnSurfaceVariant
                    )
                )
            }
            Text(
                text = value,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontFamily = modernFont,
                    fontWeight = FontWeight.SemiBold,
                    color = KitsuOnSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    private fun getCardinalDirection(dx: Float, dz: Float): String {
        val angle = Math.toDegrees(atan2(dz.toDouble(), dx.toDouble()))
        val normalizedAngle = (angle + 360) % 360

        return when {
            normalizedAngle < 22.5 || normalizedAngle >= 337.5 -> "East"
            normalizedAngle < 67.5 -> "South-East"
            normalizedAngle < 112.5 -> "South"
            normalizedAngle < 157.5 -> "South-West"
            normalizedAngle < 202.5 -> "West"
            normalizedAngle < 247.5 -> "North-West"
            normalizedAngle < 292.5 -> "North"
            else -> "North-East"
        }
    }

    @Composable
    private fun EntityImage(
        imagePath: String,
        contentDescription: String,
        size: androidx.compose.ui.unit.Dp
    ) {
        val context = LocalContext.current
        val bitmap = remember(imagePath) {
            try {
                val cleanPath = imagePath.removePrefix("/")
                val inputStream = context.assets.open(cleanPath)
                val bmp = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                bmp
            } catch (e: Exception) {
                Log.e("EntityRadarOverlay", "Failed to load: $imagePath", e)
                null
            }
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = Modifier.size(size)
            )
        } else {
            PlaceholderIcon(size)
        }
    }

    @Composable
    private fun PlaceholderIcon(size: androidx.compose.ui.unit.Dp = 32.dp) {
        Icon(
            painter = painterResource(id = R.drawable.moon_stars_24),
            contentDescription = "No Image",
            tint = KitsuOnSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(size)
        )
    }

    @Composable
    private fun EmptyState() {
        val infiniteTransition = rememberInfiniteTransition(label = "emptyStatePulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "emptyAlpha"
        )

        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "emptyScale"
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.moon_stars_24),
                    contentDescription = "No Entities",
                    tint = KitsuOnSurfaceVariant.copy(alpha = alpha),
                    modifier = Modifier
                        .size(56.dp)
                        .scale(scale)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No entities in range",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = modernFont,
                        fontWeight = FontWeight.Medium,
                        color = KitsuOnSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Try increasing your range or moving",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontFamily = modernFont,
                        color = KitsuOnSurfaceVariant.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }

    @Composable
    private fun CloseButton(onClick: () -> Unit) {
        var isPressed by remember { mutableStateOf(false) }

        val backgroundColor by animateColorAsState(
            targetValue = if (isPressed) Color(0xFFE81123) else Color(0xFFE81123).copy(alpha = 0.9f),
            animationSpec = tween(durationMillis = 150),
            label = "closeButtonColor"
        )

        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.95f else 1f,
            animationSpec = tween(durationMillis = 100),
            label = "closeButtonScale"
        )

        Box(
            modifier = Modifier
                .size(32.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(backgroundColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isPressed = true
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.cross_circle_24),
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        LaunchedEffect(isPressed) {
            if (isPressed) {
                delay(100)
                isPressed = false
            }
        }
    }

    @Composable
    private fun SelectedMobsListView(
        entities: Map<Long, EntityStorage.EntityInfo>,
        onBack: () -> Unit,
        onEntityClick: (Long) -> Unit
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = ir.alirezaivaz.tablericons.R.drawable.ic_star_filled),
                        contentDescription = null,
                        tint = Color(0xFF9B59B6),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Selected Mobs",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontFamily = modernFont,
                            fontWeight = FontWeight.Bold,
                            color = KitsuOnSurface
                        )
                    )
                }
                Icon(
                    painter = painterResource(id = R.drawable.cross_circle_24),
                    contentDescription = "Close",
                    tint = KitsuOnSurfaceVariant,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(onClick = onBack)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = KitsuOnSurfaceVariant.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            val selectedMobs = SelectedMobsManager.getSelectedMobs()

            if (selectedMobs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = ir.alirezaivaz.tablericons.R.drawable.ic_star),
                            contentDescription = null,
                            tint = KitsuOnSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No mobs selected",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = modernFont,
                                color = KitsuOnSurfaceVariant
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedMobs) { selectedMob ->
                        val currentEntity = entities[selectedMob.mobId]
                        val isInRange = currentEntity != null

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isInRange) KitsuSurfaceVariant else KitsuSurfaceVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isInRange) {
                                    if (isInRange) {
                                        onEntityClick(selectedMob.mobId)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(KitsuSurface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.moon_stars_24),
                                            contentDescription = null,
                                            tint = if (isInRange) KitsuPrimary else KitsuOnSurfaceVariant.copy(alpha = 0.3f),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = selectedMob.mobName,
                                            style = TextStyle(
                                                fontSize = 14.sp,
                                                fontFamily = modernFont,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isInRange) KitsuOnSurface else KitsuOnSurfaceVariant
                                            )
                                        )

                                        val timeAgo = (System.currentTimeMillis() - selectedMob.selectedAt) / 1000
                                        val timeText = when {
                                            timeAgo < 60 -> "${timeAgo}s ago"
                                            timeAgo < 3600 -> "${timeAgo / 60}m ago"
                                            else -> "${timeAgo / 3600}h ago"
                                        }

                                        Text(
                                            text = if (isInRange) "In range • $timeText" else "Out of range • $timeText",
                                            style = TextStyle(
                                                fontSize = 11.sp,
                                                fontFamily = modernFont,
                                                color = if (isInRange) Color(0xFF4CAF50) else Color(0xFFFF5252)
                                            )
                                        )
                                    }
                                }

                                Icon(
                                    painter = painterResource(id = R.drawable.cross_circle_24),
                                    contentDescription = "Remove",
                                    tint = KitsuOnSurfaceVariant,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable {
                                            SelectedMobsManager.unselectMob(selectedMob.mobId)
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}