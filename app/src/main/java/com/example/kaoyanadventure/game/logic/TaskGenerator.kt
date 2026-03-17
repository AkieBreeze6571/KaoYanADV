package com.example.kaoyanadventure.game.logic

import com.example.kaoyanadventure.game.model.Rarity
import com.example.kaoyanadventure.game.model.Subject
import com.example.kaoyanadventure.game.model.TaskType
import java.time.LocalDate
import kotlin.random.Random

data class TaskTemplate(
    val subject: Subject,
    val type: TaskType,
    val title: String,
    val description: String,
    val baseDifficulty: Int,
    val minuteOptions: List<Int>
)

data class GenerateConfig(
    val date: LocalDate,
    val slots: Int,
    val enabledSubjects: Set<Subject>,
    val sprintMode: Boolean,
    val weakSubject: Subject?,
    val shortTaskGuaranteed: Boolean,
    val rarityRateUp: Boolean
)

data class GeneratedTask(
    val date: LocalDate,
    val subject: Subject,
    val type: TaskType,
    val difficulty: Int,
    val targetMinutes: Int,
    val rarity: Rarity,
    val title: String,
    val description: String
)

object TaskGenerator {
    private val templates = listOf(
        TaskTemplate(Subject.MATH, TaskType.PROBLEM_SET, "数学：刷题冲刺", "完成目标分钟数，写1句错因/要点", 3, listOf(10, 15, 20, 25)),
        TaskTemplate(Subject.MATH, TaskType.REVIEW, "数学：错题回城", "复盘1道错题：错因+改法", 2, listOf(10, 15, 20)),
        TaskTemplate(Subject.ENGLISH, TaskType.READING, "英语：阅读闯关", "阅读+标注生词，写1句总结", 3, listOf(10, 15, 20)),
        TaskTemplate(Subject.ENGLISH, TaskType.ESSAY, "英语：作文骨架", "列提纲/背模板，写1句复盘", 4, listOf(15, 20, 25, 30)),
        TaskTemplate(Subject.ENGLISH, TaskType.MEMORIZATION, "英语：词汇补给", "背诵/复习词汇，写1句复盘", 2, listOf(10, 15, 20)),
        TaskTemplate(Subject.POLITICS, TaskType.MEMORIZATION, "政治：速记营地", "背诵要点，写1句复盘", 3, listOf(10, 15, 20)),
        TaskTemplate(Subject.POLITICS, TaskType.REVIEW, "政治：选择题巡逻", "刷题+复盘错项", 4, listOf(15, 20, 25))
    )

    fun generate(config: GenerateConfig, rng: Random = Random.Default): List<GeneratedTask> {
        val enabled = config.enabledSubjects.toMutableSet()
        if (!config.sprintMode) enabled.remove(Subject.POLITICS)
        if (enabled.isEmpty()) return emptyList()

        val pool = templates.filter { it.subject in enabled }
        if (pool.isEmpty()) return emptyList()

        val subjectCount = mutableMapOf<Subject, Int>()
        val results = mutableListOf<GeneratedTask>()
        var mustShort = config.shortTaskGuaranteed

        fun pickRarity(): Rarity {
            val roll = rng.nextInt(100)
            val rareBoost = if (config.rarityRateUp) 6 else 0
            return when {
                roll < 70 - rareBoost -> Rarity.COMMON
                roll < 95 - rareBoost -> Rarity.RARE
                else -> Rarity.EPIC
            }
        }

        fun subjectAllowed(s: Subject): Boolean {
            val c = subjectCount[s] ?: 0
            return c < 2
        }

        fun addTask(template: TaskTemplate, forcedMinutes: Int? = null, forcedRarity: Rarity? = null) {
            val minutes = forcedMinutes ?: template.minuteOptions.random(rng)
            val difficulty = template.baseDifficulty.coerceIn(1, 5)
            val rarity = forcedRarity ?: pickRarity()

            results += GeneratedTask(
                date = config.date,
                subject = template.subject,
                type = template.type,
                difficulty = difficulty,
                targetMinutes = minutes,
                rarity = rarity,
                title = template.title,
                description = template.description
            )
            subjectCount[template.subject] = (subjectCount[template.subject] ?: 0) + 1
        }

        // 1) 弱科目保底
        config.weakSubject?.let { weak ->
            val weakPool = pool.filter { it.subject == weak && subjectAllowed(weak) }
            if (weakPool.isNotEmpty()) {
                val t = weakPool.random(rng)
                val forcedMinute = if (mustShort) {
                    val shortOptions = t.minuteOptions.filter { it <= 15 }
                    (shortOptions.ifEmpty { t.minuteOptions }).random(rng)
                } else {
                    null
                }
                addTask(t, forcedMinutes = forcedMinute)
                mustShort = false
            }
        }

        // 2) 填满 slots，且同科目最多 2 个
        while (results.size < config.slots.coerceAtLeast(1)) {
            val candidates = pool.filter { subjectAllowed(it.subject) }
            val t = (candidates.ifEmpty { pool }).random(rng)

            val forcedMinute = if (mustShort) {
                val shortOptions = t.minuteOptions.filter { it <= 15 }
                (shortOptions.ifEmpty { t.minuteOptions }).random(rng)
            } else {
                null
            }

            addTask(t, forcedMinutes = forcedMinute)
            mustShort = false
        }

        return results
    }
}
