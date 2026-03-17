package com.example.kaoyanadventure.game.logic

import com.example.kaoyanadventure.game.db.entities.ShopItemEntity
import com.example.kaoyanadventure.game.model.EffectType

object ShopCatalog {
    val items: List<ShopItemEntity> = listOf(
        // A 成长/权限
        ShopItemEntity("SLOT_PLUS_1_I", "任务栏扩容券 I", "每日任务上限 +1（3→4）", 800, EffectType.PERM_TASK_SLOTS_PLUS_1, effectValue = 1, isConsumable = false, levelReq = 1, sortOrder = 10),
        ShopItemEntity("SLOT_PLUS_1_II", "任务栏扩容券 II", "每日任务上限 +1（4→5）", 1500, EffectType.PERM_TASK_SLOTS_PLUS_1, effectValue = 1, isConsumable = false, levelReq = 3, sortOrder = 11),
        ShopItemEntity("SPRINT_LICENSE", "冲刺期开关许可", "允许政治进入任务池（建议考前使用）", 600, EffectType.PERM_ENABLE_SPRINT_MODE, effectValue = 1, isConsumable = false, levelReq = 1, sortOrder = 12),
        ShopItemEntity("FREE_REROLL_PLUS_1", "任务刷新权限+1", "每日免费刷新次数 +1", 1200, EffectType.PERM_FREE_REROLL_PLUS_1, effectValue = 1, isConsumable = false, levelReq = 2, sortOrder = 13),
        ShopItemEntity("RARITY_RATE_UP", "稀有任务许可", "稀有/史诗任务概率小幅提高", 1600, EffectType.PERM_RARITY_RATE_UP, effectValue = 1, isConsumable = false, levelReq = 4, sortOrder = 14),

        // B 刷新/重做
        ShopItemEntity("REROLL_ONE", "随机刷新券", "重抽 1 个任务", 120, EffectType.CONSUME_REROLL_ONE, 1, true, 1, 20),
        ShopItemEntity("REROLL_ALL", "整组刷新券", "重抽今日全部任务", 300, EffectType.CONSUME_REROLL_ALL, 1, true, 2, 21),
        ShopItemEntity("WITHDRAW", "撤回印章", "把“已领取未完成”的任务退回一次", 180, EffectType.CONSUME_WITHDRAW_ONE, 1, true, 1, 22),
        ShopItemEntity("DELAY_ONE", "延期徽记", "把一个任务延到明天（保护连胜）", 220, EffectType.CONSUME_DELAY_ONE, 1, true, 3, 23),
        ShopItemEntity("SHORT_GUARANTEE", "保底通行证", "今天至少出现 1 个短任务（≤15分钟）", 150, EffectType.CONSUME_SHORT_TASK_GUARANTEE, 1, true, 1, 24),

        // C 倍率道具（轻度）
        ShopItemEntity("EXP_10", "专注药水·小", "下一个任务 EXP +10%", 140, EffectType.CONSUME_EXP_BOOST_10, 10, true, 1, 30),
        ShopItemEntity("EXP_20", "专注药水·中", "下一个任务 EXP +20%", 260, EffectType.CONSUME_EXP_BOOST_20, 20, true, 3, 31),
        ShopItemEntity("GOLD_10", "赏金令·小", "下一个任务 GOLD +10%", 140, EffectType.CONSUME_GOLD_BOOST_10, 10, true, 1, 32),
        ShopItemEntity("GOLD_20", "赏金令·中", "下一个任务 GOLD +20%", 260, EffectType.CONSUME_GOLD_BOOST_20, 20, true, 3, 33),
        ShopItemEntity("DROP_UP", "稀有徽章", "下一个任务材料掉落率提高", 200, EffectType.CONSUME_DROP_RATE_UP, 1, true, 2, 34),

        // D 连胜/防断
        ShopItemEntity("STREAK_SHIELD", "连胜护符", "今日允许 1 次放弃不影响连胜", 350, EffectType.CONSUME_STREAK_SHIELD, 1, true, 2, 40),
        ShopItemEntity("STREAK_REKINDLE", "归来之火", "断连后重燃（连胜记为1）", 500, EffectType.CONSUME_STREAK_REKINDLE, 1, true, 4, 41),
        ShopItemEntity("EARLY_BIRD", "早鸟印记", "上午完成首个任务额外奖励", 120, EffectType.CONSUME_EARLY_BIRD, 1, true, 1, 42),
        ShopItemEntity("NIGHT_CLOAK", "夜行披风", "深夜完成任务不触发疲劳惩罚（如启用）", 180, EffectType.CONSUME_NIGHT_CLOAK, 1, true, 2, 43),
        ShopItemEntity("SOFTCAP_DELAY", "节奏校准器", "当日收益递减推迟一次触发", 400, EffectType.CONSUME_SOFTCAP_DELAY, 1, true, 3, 44),

        // E 模板/复盘（不靠 AI 判题）
        ShopItemEntity("T_WRONG", "错题封印卷", "生成错因复盘模板并保存", 160, EffectType.CONSUME_REVIEW_TEMPLATE_WRONG, 1, true, 1, 50),
        ShopItemEntity("T_ESSAY", "作文骨架卡", "提供作文结构提示模板", 160, EffectType.CONSUME_TEMPLATE_ESSAY, 1, true, 1, 51),
        ShopItemEntity("T_POL", "政治速记卡", "政治速记模板（冲刺期更好用）", 160, EffectType.CONSUME_TEMPLATE_POLITICS, 1, true, 3, 52),
        ShopItemEntity("T_MEM", "记忆钩子卡", "背诵自测问题模板", 160, EffectType.CONSUME_TEMPLATE_MEMORY, 1, true, 1, 53),
        ShopItemEntity("RECAP_COMPASS", "复盘罗盘", "完成后弹出20秒复盘，额外小奖励", 200, EffectType.CONSUME_RECAP_COMPASS, 1, true, 2, 54),

        // F 外观/收藏
        ShopItemEntity("TITLE_NOVICE", "称号：新手冒险者", "仅展示用", 100, EffectType.COSMETIC_TITLE, 1, false, 1, 60),
        ShopItemEntity("TITLE_DIVER", "称号：题海潜行者", "仅展示用", 300, EffectType.COSMETIC_TITLE, 2, false, 2, 61),
        ShopItemEntity("FRAME_BRONZE", "头像框：青铜边框", "仅展示用", 250, EffectType.COSMETIC_FRAME, 1, false, 1, 62),
        ShopItemEntity("FRAME_SILVER", "头像框：银色边框", "仅展示用", 600, EffectType.COSMETIC_FRAME, 2, false, 3, 63),
        ShopItemEntity("CAMP_NIGHT", "营地皮肤：夜色营火", "主页卡片背景皮肤", 900, EffectType.COSMETIC_CAMP_SKIN, 1, false, 4, 64)
    )
}
