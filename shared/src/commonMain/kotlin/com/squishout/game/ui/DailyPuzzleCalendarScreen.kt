package com.squishout.game.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.squishout.game.data.entity.DailyPuzzleRecordEntity
import com.squishout.game.data.entity.UserSessionEntity
import com.squishout.game.data.repository.GameRepository
import com.squishout.game.data.repository.MONTHLY_MILESTONES
import com.squishout.game.data.repository.MonthlyMilestone
import com.squishout.game.theme.AmberGlow
import com.squishout.game.theme.EmeraldMint
import com.squishout.game.theme.SlateCharcoal
import com.squishout.game.ui.components.Canvas3DStar
import com.squishout.game.ui.components.CanvasGiftChest
import com.squishout.game.ui.components.CanvasGoldenPadlock
import com.squishout.game.ui.components.ModalButtonVariant
import com.squishout.game.ui.components.ModalExtrudedButton
import com.squishout.game.util.CalendarDate
import com.squishout.game.util.CalendarUtils

/**
 * AAA 3D Daily Puzzle Calendar Screen.
 * Features:
 * - Porcelain Plaque calendar grid for the active month
 * - Streak tracking & stamp cards
 * - Past day catch-up & today highlight
 * - Monthly skin milestone rewards track (unlocks "Cosmic Nebula" legendary skin)
 */
@Composable
fun DailyPuzzleCalendarScreen(
    session: UserSessionEntity,
    repository: GameRepository?,
    onPlayPuzzle: (epochDay: Long, dayOfMonth: Int, dateString: String, monthKey: String) -> Unit,
    onBack: () -> Unit,
    onClaimMilestone: (monthKey: String, milestoneDays: Int, completions: Int) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val todayEpochDay = remember { CalendarUtils.currentEpochDay() }
    val todayDate = remember(todayEpochDay) { CalendarUtils.epochDayToDate(todayEpochDay) }

    // Month browsing state (defaults to current month)
    var viewYear by remember { mutableStateOf(todayDate.year) }
    var viewMonth by remember { mutableStateOf(todayDate.month) }

    val currentViewMonthKey = remember(viewYear, viewMonth) {
        "$viewYear-${viewMonth.toString().padStart(2, '0')}"
    }

    val dailyRecordsFlow = remember(currentViewMonthKey, repository) {
        repository?.getDailyRecordsForMonth(currentViewMonthKey)
    }
    val monthlyRecords by dailyRecordsFlow?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList<DailyPuzzleRecordEntity>()) }

    val recordsByDay = remember(monthlyRecords) {
        monthlyRecords.associateBy { it.dayOfMonth }
    }

    val daysInMonth = remember(viewYear, viewMonth) { CalendarUtils.daysInMonth(viewYear, viewMonth) }
    val firstDayOffset = remember(viewYear, viewMonth) { CalendarUtils.firstDayOfWeekInMonth(viewYear, viewMonth) }

    // Selected day in current viewed month
    var selectedDay by remember(viewMonth, viewYear) {
        mutableStateOf(if (viewYear == todayDate.year && viewMonth == todayDate.month) todayDate.day else 1)
    }

    val selectedEpochDay = remember(viewYear, viewMonth, selectedDay) {
        CalendarUtils.dateToEpochDay(viewYear, viewMonth, selectedDay)
    }
    val selectedDateString = remember(viewYear, viewMonth, selectedDay) {
        "$viewYear-${viewMonth.toString().padStart(2, '0')}-${selectedDay.toString().padStart(2, '0')}"
    }
    val selectedRecord = recordsByDay[selectedDay]
    val isSelectedFuture = selectedEpochDay > todayEpochDay

    val completedCount = monthlyRecords.size
    val claimedMilestonesSet = remember(session.claimedMonthlyMilestones) {
        session.claimedMonthlyMilestones.split(",").filter { it.isNotBlank() }.toSet()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFE8F5E9),
                        Color(0xFFD0EBD5),
                        Color(0xFFB5E4BE),
                        Color(0xFF90D9A1)
                    )
                )
            )
            .displayCutoutPadding()
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 460.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Screen Header (Back Button, Title, Streak Pill)
            CalendarTopHeader(
                streakDays = session.dailyPuzzleStreak,
                diamonds = session.diamonds,
                onBack = onBack
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Month Selector & Porcelain Plaque Calendar Box
            MonthPlaqueCard(
                year = viewYear,
                month = viewMonth,
                todayDate = todayDate,
                daysInMonth = daysInMonth,
                firstDayOffset = firstDayOffset,
                recordsByDay = recordsByDay,
                selectedDay = selectedDay,
                todayEpochDay = todayEpochDay,
                onSelectDay = { day -> selectedDay = day },
                onPrevMonth = {
                    if (viewMonth == 1) {
                        viewYear -= 1
                        viewMonth = 12
                    } else {
                        viewMonth -= 1
                    }
                },
                onNextMonth = {
                    if (viewMonth == 12) {
                        viewYear += 1
                        viewMonth = 1
                    } else {
                        viewMonth += 1
                    }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Play Action Card for Selected Day
            SelectedDayActionCard(
                selectedDay = selectedDay,
                monthName = CalendarDate(viewYear, viewMonth, selectedDay, 0).monthName,
                record = selectedRecord,
                isFuture = isSelectedFuture,
                isToday = selectedEpochDay == todayEpochDay,
                onPlay = {
                    onPlayPuzzle(selectedEpochDay, selectedDay, selectedDateString, currentViewMonthKey)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Monthly Milestone Stamp Rewards Card
            MonthlyMilestoneCard(
                monthKey = currentViewMonthKey,
                completedCount = completedCount,
                claimedSet = claimedMilestonesSet,
                onClaim = { milestone ->
                    onClaimMilestone(currentViewMonthKey, milestone.requiredDays, completedCount)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/* =========================================================================
   TOP HEADER BAR
   ========================================================================= */

@Composable
private fun CalendarTopHeader(
    streakDays: Int,
    diamonds: Int,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Pill
        Box(
            modifier = Modifier
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF8D4F0E))
                .padding(bottom = 2.5.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFFFFFDF8), Color(0xFFF3E7D7))))
                    .border(1.dp, Color(0xFFE8D5C0), RoundedCornerShape(14.dp))
                    .clickable(onClick = onBack)
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "◀ MAP",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF78350F)
                )
            }
        }

        // Streak Pill
        Box(
            modifier = Modifier
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFC2410C))
                .padding(bottom = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFFEDD5), Color(0xFFFED7AA))
                        )
                    )
                    .border(1.dp, Color(0xFFFDBA74), RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "🔥", fontSize = 13.sp)
                    Text(
                        text = "$streakDays DAY STREAK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF9A3412)
                    )
                }
            }
        }

        // Economy Stats (Diamonds)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.85f))
                .border(1.dp, Color(0xFFBAE6FD).copy(alpha = 0.70f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = "💎 $diamonds",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0284C7)
            )
        }
    }
}

/* =========================================================================
   MONTH CALENDAR PLAQUE
   ========================================================================= */

@Composable
private fun MonthPlaqueCard(
    year: Int,
    month: Int,
    todayDate: CalendarDate,
    daysInMonth: Int,
    firstDayOffset: Int,
    recordsByDay: Map<Int, DailyPuzzleRecordEntity>,
    selectedDay: Int,
    todayEpochDay: Long,
    onSelectDay: (Int) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthName = CalendarDate(year, month, 1, 0).monthName

    val plaqueShape = RoundedCornerShape(24.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, plaqueShape, spotColor = Color(0xFF264626).copy(alpha = 0.45f))
            .clip(plaqueShape)
            .background(Color(0xFF8D4F0E)) // 3D wood bevel rim
            .padding(bottom = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFFFDF8), Color(0xFFFAF3E3))
                    )
                )
                .border(2.dp, Color(0xFFFFECC4), RoundedCornerShape(22.dp))
                .padding(14.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Month Header with Prev/Next Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3E7D7))
                            .clickable(onClick = onPrevMonth),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "◀", fontSize = 11.sp, color = Color(0xFF78350F), fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "$monthName $year",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF78350F),
                        letterSpacing = 0.6.sp
                    )

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3E7D7))
                            .clickable(onClick = onNextMonth),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "▶", fontSize = 11.sp, color = Color(0xFF78350F), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Weekday Header Row
                val weekdays = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (day in weekdays) {
                        Text(
                            text = day,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFA19788),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Month Days Grid (7 Columns)
                val totalCells = firstDayOffset + daysInMonth
                val totalRows = (totalCells + 6) / 7

                for (row in 0 until totalRows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNum = cellIndex - firstDayOffset + 1
                            if (dayNum in 1..daysInMonth) {
                                val cellEpochDay = CalendarUtils.dateToEpochDay(year, month, dayNum)
                                val isToday = (cellEpochDay == todayEpochDay)
                                val isFuture = (cellEpochDay > todayEpochDay)
                                val isSelected = (dayNum == selectedDay)
                                val record = recordsByDay[dayNum]

                                CalendarDayCell(
                                    day = dayNum,
                                    isToday = isToday,
                                    isFuture = isFuture,
                                    isSelected = isSelected,
                                    isCompleted = record != null,
                                    stars = record?.stars ?: 0,
                                    onClick = { onSelectDay(dayNum) },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    isToday: Boolean,
    isFuture: Boolean,
    isSelected: Boolean,
    isCompleted: Boolean,
    stars: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseBorder by infiniteTransition.animateFloat(
        initialValue = 1.5f,
        targetValue = if (isToday && !isCompleted) 3f else 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    isCompleted -> Brush.verticalGradient(listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9)))
                    isToday -> Brush.verticalGradient(listOf(Color(0xFFFFF9C4), Color(0xFFFFF176)))
                    isSelected -> Brush.verticalGradient(listOf(Color(0xFFFFEDD5), Color(0xFFFED7AA)))
                    isFuture -> Brush.verticalGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0)))
                    else -> Brush.verticalGradient(listOf(Color.White, Color(0xFFF8F4EC)))
                }
            )
            .border(
                width = if (isToday || isSelected) pulseBorder.dp else 1.dp,
                color = when {
                    isToday -> Color(0xFFF59E0B)
                    isSelected -> Color(0xFFEA580C)
                    isCompleted -> Color(0xFF4CAF50)
                    else -> Color(0xFFE2D6C5)
                },
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$day",
                fontSize = 11.sp,
                fontWeight = if (isToday || isSelected || isCompleted) FontWeight.Black else FontWeight.Bold,
                color = when {
                    isCompleted -> Color(0xFF1B5E20)
                    isToday -> Color(0xFFB45309)
                    isFuture -> Color(0xFF94A3B8)
                    else -> SlateCharcoal
                }
            )

            when {
                isCompleted -> {
                    // Star Stamp
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(1.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Canvas3DStar(isEarned = true, modifier = Modifier.size(11.dp))
                    }
                }
                isFuture -> {
                    CanvasGoldenPadlock(modifier = Modifier.size(9.dp, 10.dp))
                }
                isToday -> {
                    Text(
                        text = "TODAY",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFB45309)
                    )
                }
            }
        }
    }
}

/* =========================================================================
   SELECTED DAY ACTION CARD
   ========================================================================= */

@Composable
private fun SelectedDayActionCard(
    selectedDay: Int,
    monthName: String,
    record: DailyPuzzleRecordEntity?,
    isFuture: Boolean,
    isToday: Boolean,
    onPlay: () -> Unit
) {
    val cardShape = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, cardShape)
            .clip(cardShape)
            .background(Color(0xFF8D4F0E))
            .padding(bottom = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFFFFFDF8), Color(0xFFFAF2E4))))
                .border(1.5.dp, Color(0xFFFFECC4), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$monthName $selectedDay PUZZLE",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = SlateCharcoal
                )

                Spacer(modifier = Modifier.height(4.dp))

                val subtitle = when {
                    isFuture -> "Locks unlock on this day!"
                    record != null -> "Completed! (${record.stars} ⭐ • ${record.score} pts)"
                    isToday -> "Today's challenge is ready! Complete to advance streak!"
                    else -> "Catch up on past puzzle and claim stamps!"
                }
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (record != null) Color(0xFF059669) else Color(0xFF78572A)
                )

                Spacer(modifier = Modifier.height(10.dp))

                when {
                    isFuture -> {
                        ModalExtrudedButton(
                            text = "LOCKED 🔒",
                            variant = ModalButtonVariant.CREAM,
                            onClick = {}
                        )
                    }
                    record != null -> {
                        ModalExtrudedButton(
                            text = "↺ REPLAY PUZZLE",
                            variant = ModalButtonVariant.CREAM,
                            onClick = onPlay
                        )
                    }
                    else -> {
                        ModalExtrudedButton(
                            text = "▶ PLAY TODAY (+25 💎)",
                            variant = ModalButtonVariant.EMERALD,
                            onClick = onPlay
                        )
                    }
                }
            }
        }
    }
}

/* =========================================================================
   MONTHLY MILESTONE REWARDS CARD
   ========================================================================= */

@Composable
private fun MonthlyMilestoneCard(
    monthKey: String,
    completedCount: Int,
    claimedSet: Set<String>,
    onClaim: (MonthlyMilestone) -> Unit
) {
    val cardShape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, cardShape)
            .clip(cardShape)
            .background(Color(0xFF8D4F0E))
            .padding(bottom = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFFFFFDF8), Color(0xFFF9F0E1))))
                .border(1.5.dp, Color(0xFFFFECC4), RoundedCornerShape(18.dp))
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MONTHLY STAMP GOAL",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF78350F),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "$completedCount / 20 COMPLETED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFD97706)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Milestones List
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (milestone in MONTHLY_MILESTONES) {
                        val milestoneKey = "$monthKey:${milestone.requiredDays}"
                        val isClaimed = claimedSet.contains(milestoneKey)
                        val canClaim = completedCount >= milestone.requiredDays && !isClaimed

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFEADBCE), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = milestone.iconEmoji, fontSize = 18.sp)
                                Column {
                                    Text(
                                        text = "${milestone.requiredDays} Days: ${milestone.rewardTitle}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SlateCharcoal
                                    )
                                    Text(
                                        text = if (completedCount >= milestone.requiredDays) "Goal reached!" else "Need ${milestone.requiredDays - completedCount} more days",
                                        fontSize = 10.sp,
                                        color = if (completedCount >= milestone.requiredDays) Color(0xFF059669) else Color(0xFFA19788)
                                    )
                                }
                            }

                            when {
                                isClaimed -> {
                                    Text(
                                        text = "CLAIMED ✓",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF059669)
                                    )
                                }
                                canClaim -> {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(EmeraldMint)
                                            .clickable { onClaim(milestone) }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = "CLAIM",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }
                                }
                                else -> {
                                    Text(
                                        text = "${completedCount}/${milestone.requiredDays}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFA19788)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
