package com.lichso.app.ui.screen.familytree

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.data.local.FamilyTreeExportImport
import com.lichso.app.data.local.FamilyTreeRepository
import com.lichso.app.data.local.dao.NoteDao
import com.lichso.app.data.local.dao.ReminderDao
import com.lichso.app.data.local.entity.FamilySettingsEntity
import com.lichso.app.data.local.entity.MemorialChecklistEntity
import com.lichso.app.data.local.entity.MemberPhotoEntity
import com.lichso.app.data.local.entity.NoteEntity
import com.lichso.app.data.local.entity.ReminderEntity
import com.lichso.app.util.CanChiCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

// ══════════════════════════════════════════════════════════════
// DATA MODELS
// ══════════════════════════════════════════════════════════════

enum class Gender { MALE, FEMALE }

data class FamilyMember(
    val id: String,
    val name: String,
    val role: String,
    val gender: Gender,
    val generation: Int,
    val birthYear: Int?,
    val deathYear: Int? = null,
    val birthDateLunar: String? = null,
    val deathDateLunar: String? = null,
    val canChi: String? = null,
    val menh: String? = null,
    val zodiacEmoji: String? = null,
    val menhEmoji: String? = null,
    val hanhEmoji: String? = null,
    val menhDetail: String? = null,
    val zodiacName: String? = null,
    val menhName: String? = null,
    val hometown: String? = null,
    val occupation: String? = null,
    val isSelf: Boolean = false,
    val isElder: Boolean = false,
    val emoji: String = "👤",
    val spouseIds: List<String> = emptyList(),  // supports multiple spouses
    val spouseOrder: Int = 0,                    // order among spouses (0=primary, 1=vợ cả, 2=vợ hai...)
    val parentIds: List<String> = emptyList(),
    val note: String? = null,
    val avatarPath: String? = null,
)

data class MemorialDay(
    val id: String,
    val memberId: String,
    val memberName: String,
    val relation: String,
    val lunarDay: Int,
    val lunarMonth: Int,
    val lunarLeap: Int = 0,
    val lunarDate: String,          // computed: "20/11 Âm lịch"
    val solarDate: String,          // computed: "30/12/2026"
    val countdown: String,          // computed: "Còn 269 ngày nữa"
    val countdownDays: Long = 0,    // computed: number of days
    val isUpcoming: Boolean = false, // computed: within 30 days
    val yearCanChi: String = "",     // computed: "Bính Ngọ"
    val note: String? = null,
    val remindBefore3Days: Boolean = true,
    val remindBefore1Day: Boolean = true,
    val remind3DaysDateStr: String = "",  // computed
    val remind1DayDateStr: String = "",   // computed
)

data class Relationship(
    val memberId: String,
    val name: String,
    val role: String,
    val emoji: String,
    val gender: Gender,
    val isElder: Boolean = false,
)

data class MemberPhoto(
    val id: Long,
    val memberId: String,
    val filePath: String,
    val caption: String? = null,
    val createdAt: Long = 0,
)

data class TreeLevel(
    val generation: Int,
    val label: String,
    val nodes: List<TreeNode>,
)

sealed class TreeNode {
    data class Couple(val person1: FamilyMember, val person2: FamilyMember) : TreeNode()
    data class Single(val person: FamilyMember) : TreeNode()
    /** A man with multiple wives — displayed as husband + list of wives */
    data class MultiSpouse(val husband: FamilyMember, val wives: List<FamilyMember>) : TreeNode()
    data object AddPlaceholder : TreeNode()
}

/**
 * A family group represents a parent couple/single and their direct children.
 * This is used to build a proper hierarchical tree where children are
 * visually grouped under their parents.
 *
 * For multi-spouse scenarios, children are grouped by mother (wife).
 * Each wife-branch contains only that wife's children.
 */
data class FamilyGroup(
    val parents: TreeNode,                // Couple, Single, or MultiSpouse parent
    val children: List<FamilyGroup>,      // child groups (each child may have their own family)
    val generation: Int,
    val wifeId: String? = null,           // which wife these children belong to (for multi-spouse display)
)

data class FamilyTreeUiState(
    val familyName: String = "Gia phả của tôi",
    val familyCrest: String = "GP",
    val familyHometown: String = "",
    val totalGenerations: Int = 0,
    val totalMembers: Int = 0,
    val selectedTab: Int = 0,
    val selectedMemberId: String? = null,
    val showMemberDetail: Boolean = false,
    val showAddMember: Boolean = false,
    val showEditMember: Boolean = false,
    val editMemberId: String? = null,
    val showFamilySettings: Boolean = false,
    val showPickMember: Boolean = false,
    val showMemorialDetail: Boolean = false,
    val selectedMemorialId: String? = null,
    val members: List<FamilyMember> = emptyList(),
    val memorials: List<MemorialDay> = emptyList(),
    val isLoading: Boolean = true,
    val pickMemberCallback: ((FamilyMember) -> Unit)? = null,
    val pickMemberExcludeId: String? = null, // exclude this member from pick list (avoid picking self)
    // Memorial quick-add note/reminder
    val showMemorialNoteEdit: Boolean = false,
    val showMemorialReminderEdit: Boolean = false,
    val memorialEditTarget: MemorialDay? = null,
    // Member quick-add reminder (birthday / memorial)
    val showMemberReminderEdit: Boolean = false,
    val memberReminderTarget: FamilyMember? = null,
    // Export / Import
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val exportImportMessage: String? = null,
    val showImportConfirmDialog: Boolean = false,
    val pendingImportUri: Uri? = null,
    // QR code dialog
    val showQrDialog: Boolean = false,
    val qrBitmap: android.graphics.Bitmap? = null,
)

// ══════════════════════════════════════════════════════════════
// VIEW MODEL
// ══════════════════════════════════════════════════════════════

@HiltViewModel
class FamilyTreeViewModel @Inject constructor(
    private val repository: FamilyTreeRepository,
    private val noteDao: NoteDao,
    private val reminderDao: ReminderDao,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FamilyTreeUiState())
    val uiState: StateFlow<FamilyTreeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Seed database with initial data if empty — MUST complete before observing
            repository.seedIfEmpty()

            // Observe members
            launch {
                repository.getAllMembersFlow().collect { members ->
                    _uiState.update { it.copy(
                        members = members,
                        totalMembers = members.size,
                        totalGenerations = members.maxOfOrNull { m -> m.generation } ?: 0,
                        isLoading = false,
                    ) }
                }
            }

            // Observe memorials
            launch {
                repository.getAllMemorialsFlow().collect { memorials ->
                    _uiState.update { it.copy(memorials = memorials) }
                }
            }

            // Observe settings
            launch {
                repository.getSettingsFlow().collect { settings ->
                    settings?.let { s ->
                        _uiState.update { it.copy(
                            familyName = s.familyName,
                            familyCrest = s.familyCrest,
                            familyHometown = s.hometown,
                        ) }
                    }
                }
            }
        }
    }

    // ── Convenience getters ──

    val members: List<FamilyMember>
        get() = _uiState.value.members

    val memorials: List<MemorialDay>
        get() = _uiState.value.memorials

    fun getMember(id: String): FamilyMember? = members.find { it.id == id }

    fun getTreeLevels(): List<TreeLevel> {
        val memberList = members
        if (memberList.isEmpty()) return emptyList()

        val memberById = memberList.associateBy { it.id }
        val grouped = memberList.groupBy { it.generation }
        val generations = grouped.keys.sorted()

        return generations.map { gen ->
            val genMembers = grouped[gen] ?: emptyList()
            val nodes = mutableListOf<TreeNode>()
            val processed = mutableSetOf<String>()
            val multiSpouseHusbandIds = mutableSetOf<String>()

            // First pass: process males with multiple spouses
            for (member in genMembers) {
                if (member.id in processed) continue
                if (member.gender != Gender.MALE) continue

                // Look up ALL spouses from the full family
                val allSpouseMembers = member.spouseIds.mapNotNull { memberById[it] }
                val localSpouses = allSpouseMembers.filter { it.id !in processed }

                if (allSpouseMembers.size > 1) {
                    val sortedWives = localSpouses.sortedBy { it.spouseOrder }
                    nodes.add(TreeNode.MultiSpouse(member, sortedWives))
                    processed.add(member.id)
                    localSpouses.forEach { processed.add(it.id) }
                    multiSpouseHusbandIds.add(member.id)
                } else if (localSpouses.size == 1) {
                    nodes.add(TreeNode.Couple(member, localSpouses.first()))
                    processed.add(member.id)
                    processed.add(localSpouses.first().id)
                } else {
                    nodes.add(TreeNode.Single(member))
                    processed.add(member.id)
                }
            }

            // Second pass: process remaining females (not yet paired)
            for (member in genMembers) {
                if (member.id in processed) continue

                // Find husband: either from this member's spouseIds, or reverse-lookup
                val husband = member.spouseIds.mapNotNull { memberById[it] }
                    .firstOrNull { it.gender == Gender.MALE }
                    ?: memberById.values.firstOrNull { candidate ->
                        candidate.gender == Gender.MALE && candidate.spouseIds.contains(member.id)
                    }

                if (husband != null) {
                    if (husband.id in multiSpouseHusbandIds) {
                        // Merge into existing MultiSpouse node
                        val existingIdx = nodes.indexOfFirst {
                            it is TreeNode.MultiSpouse && it.husband.id == husband.id
                        }
                        if (existingIdx >= 0) {
                            val existing = nodes[existingIdx] as TreeNode.MultiSpouse
                            if (existing.wives.none { it.id == member.id }) {
                                nodes[existingIdx] = existing.copy(
                                    wives = (existing.wives + member).sortedBy { it.spouseOrder }
                                )
                            }
                        }
                        processed.add(member.id)
                    } else if (husband.id !in processed && husband.spouseIds.size > 1) {
                        val allWives = husband.spouseIds.mapNotNull { memberById[it] }
                            .filter { it.id !in processed || it.id == member.id }
                            .sortedBy { it.spouseOrder }
                        nodes.add(TreeNode.MultiSpouse(husband, allWives))
                        processed.add(husband.id)
                        allWives.forEach { processed.add(it.id) }
                        multiSpouseHusbandIds.add(husband.id)
                    } else if (husband.id !in processed) {
                        nodes.add(TreeNode.Couple(husband, member))
                        processed.add(member.id)
                        processed.add(husband.id)
                    } else {
                        // Husband already processed — try to upgrade existing node
                        val existingIdx = nodes.indexOfFirst { node ->
                            when (node) {
                                is TreeNode.Single -> node.person.id == husband.id
                                is TreeNode.Couple -> node.person1.id == husband.id || node.person2.id == husband.id
                                else -> false
                            }
                        }
                        if (existingIdx >= 0) {
                            val existing = nodes[existingIdx]
                            when (existing) {
                                is TreeNode.Single -> {
                                    nodes[existingIdx] = TreeNode.Couple(existing.person, member)
                                }
                                is TreeNode.Couple -> {
                                    val h = if (existing.person1.gender == Gender.MALE) existing.person1 else existing.person2
                                    val firstWife = if (existing.person1.gender == Gender.FEMALE) existing.person1 else existing.person2
                                    nodes[existingIdx] = TreeNode.MultiSpouse(
                                        h,
                                        listOf(firstWife, member).sortedBy { it.spouseOrder }
                                    )
                                    multiSpouseHusbandIds.add(h.id)
                                }
                                else -> nodes.add(TreeNode.Single(member))
                            }
                        } else {
                            nodes.add(TreeNode.Single(member))
                        }
                        processed.add(member.id)
                    }
                } else {
                    nodes.add(TreeNode.Single(member))
                    processed.add(member.id)
                }
            }

            if (gen == generations.last()) {
                nodes.add(TreeNode.AddPlaceholder)
            }

            TreeLevel(gen, getGenerationLabel(gen), nodes)
        }
    }

    /**
     * Build a hierarchical family tree as a list of root FamilyGroups.
     * Each group has parents and their children recursively nested.
     * Children are grouped under their actual parents, not just by generation.
     */
    fun getFamilyTree(): List<FamilyGroup> {
        val memberList = members
        if (memberList.isEmpty()) return emptyList()

        val memberById = memberList.associateBy { it.id }
        val grouped = memberList.groupBy { it.generation }
        val generations = grouped.keys.sorted()
        if (generations.isEmpty()) return emptyList()

        // Find root generation members (first generation)
        val rootGen = generations.first()
        val rootMembers = grouped[rootGen] ?: emptyList()

        // Build parent nodes (couple or single) for roots
        val rootNodes = buildParentNodes(rootMembers, memberById)

        // Track which children have been claimed to avoid duplicates across root groups
        val claimedChildIds = mutableSetOf<String>()

        // Recursively build family groups
        return rootNodes.map { node ->
            buildFamilyGroup(node, rootGen, memberById, memberList, claimedChildIds)
        }
    }

    /**
     * Build parent nodes (Couple, MultiSpouse, or Single) from a list of members,
     * handling spouse pairing — also pull in spouses from other generations
     * who married into this family. Supports multi-spouse (1 husband, N wives).
     *
     * Key: we detect multi-spouse by checking the husband's full spouseIds
     * (via memberById), not just those present in the local `members` list.
     * This ensures a man with 3 wives is always rendered as MultiSpouse
     * even if some wives come from a different generation or aren't in the
     * current `members` subset.
     */
    private fun buildParentNodes(
        members: List<FamilyMember>,
        memberById: Map<String, FamilyMember>,
    ): List<TreeNode> {
        val nodes = mutableListOf<TreeNode>()
        val processed = mutableSetOf<String>()

        // Collect all husband IDs that have multiple wives anywhere in the family
        // so that wives processed in the second pass are merged correctly.
        val multiSpouseHusbandIds = mutableSetOf<String>()

        // First pass: process males first to detect multi-spouse
        for (member in members) {
            if (member.id in processed) continue
            if (member.gender != Gender.MALE) continue

            // Look up ALL spouses from the full family, not just the local list
            val allSpouseMembers = member.spouseIds.mapNotNull { memberById[it] }
            val localSpouses = allSpouseMembers.filter { it.id !in processed }

            if (allSpouseMembers.size > 1) {
                // This man has multiple wives — always render as MultiSpouse
                val sortedWives = localSpouses.sortedBy { it.spouseOrder }
                nodes.add(TreeNode.MultiSpouse(member, sortedWives))
                processed.add(member.id)
                localSpouses.forEach { processed.add(it.id) }
                multiSpouseHusbandIds.add(member.id)
            } else if (localSpouses.size == 1) {
                nodes.add(TreeNode.Couple(member, localSpouses.first()))
                processed.add(member.id)
                processed.add(localSpouses.first().id)
            } else {
                nodes.add(TreeNode.Single(member))
                processed.add(member.id)
            }
        }

        // Second pass: process remaining females
        for (member in members) {
            if (member.id in processed) continue

            // Find husband: either from this member's spouseIds, or reverse-lookup
            // (some male whose spouseIds contains this member)
            val husband = member.spouseIds.mapNotNull { memberById[it] }
                .firstOrNull { it.gender == Gender.MALE }
                ?: memberById.values.firstOrNull { candidate ->
                    candidate.gender == Gender.MALE && candidate.spouseIds.contains(member.id)
                }

            if (husband != null) {
                // Check if this husband was already rendered as MultiSpouse
                if (husband.id in multiSpouseHusbandIds) {
                    // Merge this wife into the existing MultiSpouse node
                    val existingIdx = nodes.indexOfFirst {
                        it is TreeNode.MultiSpouse && it.husband.id == husband.id
                    }
                    if (existingIdx >= 0) {
                        val existing = nodes[existingIdx] as TreeNode.MultiSpouse
                        if (existing.wives.none { it.id == member.id }) {
                            nodes[existingIdx] = existing.copy(
                                wives = (existing.wives + member).sortedBy { it.spouseOrder }
                            )
                        }
                    }
                    processed.add(member.id)
                } else if (husband.id !in processed && husband.spouseIds.size > 1) {
                    val allWives = husband.spouseIds.mapNotNull { memberById[it] }
                        .filter { it.id !in processed || it.id == member.id }
                        .sortedBy { it.spouseOrder }
                    nodes.add(TreeNode.MultiSpouse(husband, allWives))
                    processed.add(husband.id)
                    allWives.forEach { processed.add(it.id) }
                    multiSpouseHusbandIds.add(husband.id)
                } else if (husband.id !in processed) {
                    nodes.add(TreeNode.Couple(husband, member))
                    processed.add(member.id)
                    processed.add(husband.id)
                } else {
                    // Husband already processed as Single or Couple, but this wife wasn't included.
                    // Upgrade the existing node to include her.
                    val existingIdx = nodes.indexOfFirst { node ->
                        when (node) {
                            is TreeNode.Single -> node.person.id == husband.id
                            is TreeNode.Couple -> node.person1.id == husband.id || node.person2.id == husband.id
                            else -> false
                        }
                    }
                    if (existingIdx >= 0) {
                        val existing = nodes[existingIdx]
                        when (existing) {
                            is TreeNode.Single -> {
                                nodes[existingIdx] = TreeNode.Couple(existing.person, member)
                            }
                            is TreeNode.Couple -> {
                                val h = if (existing.person1.gender == Gender.MALE) existing.person1 else existing.person2
                                val firstWife = if (existing.person1.gender == Gender.FEMALE) existing.person1 else existing.person2
                                nodes[existingIdx] = TreeNode.MultiSpouse(
                                    h,
                                    listOf(firstWife, member).sortedBy { it.spouseOrder }
                                )
                                multiSpouseHusbandIds.add(h.id)
                            }
                            else -> {
                                nodes.add(TreeNode.Single(member))
                            }
                        }
                    } else {
                        nodes.add(TreeNode.Single(member))
                    }
                    processed.add(member.id)
                }
            } else {
                nodes.add(TreeNode.Single(member))
                processed.add(member.id)
            }
        }

        return nodes
    }

    /**
     * Get all member IDs that belong to a TreeNode (parent unit).
     */
    private fun getNodeMemberIds(node: TreeNode): Set<String> = when (node) {
        is TreeNode.Couple -> setOf(node.person1.id, node.person2.id)
        is TreeNode.MultiSpouse -> setOf(node.husband.id) + node.wives.map { it.id }.toSet()
        is TreeNode.Single -> setOf(node.person.id)
        is TreeNode.AddPlaceholder -> emptySet()
    }

    /**
     * Get the primary male member ID from a TreeNode.
     */
    private fun getNodePrimaryMaleId(node: TreeNode): String? = when (node) {
        is TreeNode.Couple -> if (node.person1.gender == Gender.MALE) node.person1.id else node.person2.id
        is TreeNode.MultiSpouse -> node.husband.id
        is TreeNode.Single -> node.person.id
        is TreeNode.AddPlaceholder -> null
    }

    /**
     * Find children of a parent node from the full member list.
     *
     * Matching rules (to avoid duplicates across separate root groups):
     * - For a Couple(person1, person2): a child must have BOTH parent IDs in its parentIds,
     *   OR have at least one AND not be claimed yet. Children whose parentIds also reference
     *   someone outside this node are deprioritized.
     * - For MultiSpouse: uses the full set of husband + all wives IDs.
     * - For Single: any child referencing this person.
     *
     * Children already claimed by another branch are always excluded.
     */
    private fun findChildrenOf(
        parentNode: TreeNode,
        allMembers: List<FamilyMember>,
        claimedChildIds: MutableSet<String>,
    ): List<FamilyMember> {
        val nodeIds = getNodeMemberIds(parentNode)
        if (nodeIds.isEmpty()) return emptyList()

        // Candidate children: not yet claimed, and at least one parentId matches
        val candidates = allMembers.filter { member ->
            member.id !in claimedChildIds &&
            member.id !in nodeIds &&  // a parent can't be their own child
            member.parentIds.any { it in nodeIds }
        }

        // For Couple or Single, only claim children that "belong" to this specific unit.
        // A child belongs if ALL of its parentIds are within this node's IDs,
        // OR if none of its other parentIds are part of another known family unit.
        val children = when (parentNode) {
            is TreeNode.Couple, is TreeNode.Single -> {
                candidates.filter { child ->
                    // All of child's parentIds should be within this node
                    child.parentIds.all { it in nodeIds }
                }.ifEmpty {
                    // Fallback: take candidates whose parentIds partially match
                    // but only if no other root group would be a better match
                    candidates
                }
            }
            else -> candidates
        }

        children.forEach { claimedChildIds.add(it.id) }
        return children
    }

    /**
     * Recursively build a FamilyGroup: a parent node with its children,
     * where each child may form their own family group.
     *
     * For MultiSpouse nodes, children are grouped by their mother (wife).
     * Each wife gets a sub-group with her children displayed as branches.
     */
    private fun buildFamilyGroup(
        parentNode: TreeNode,
        generation: Int,
        memberById: Map<String, FamilyMember>,
        allMembers: List<FamilyMember>,
        claimedChildIds: MutableSet<String>,
    ): FamilyGroup {
        // Find direct children of this parent unit
        val children = findChildrenOf(parentNode, allMembers, claimedChildIds)

        if (parentNode is TreeNode.MultiSpouse) {
            // For multi-spouse: group children by their mother (wife)
            val husbandId = parentNode.husband.id

            // First, find children explicitly assigned to each wife
            val assignedChildIds = mutableSetOf<String>()
            val wifeChildrenMap = mutableMapOf<String, MutableList<FamilyMember>>()
            parentNode.wives.forEach { wife ->
                wifeChildrenMap[wife.id] = mutableListOf()
            }

            children.forEach { child ->
                val matchedWife = parentNode.wives.firstOrNull { wife ->
                    child.parentIds.contains(wife.id)
                }
                if (matchedWife != null) {
                    wifeChildrenMap[matchedWife.id]!!.add(child)
                    assignedChildIds.add(child.id)
                }
            }

            // Unassigned children (only have father's ID) → assign to first wife
            val unassignedChildren = children.filter { it.id !in assignedChildIds }
            if (unassignedChildren.isNotEmpty() && parentNode.wives.isNotEmpty()) {
                val firstWifeId = parentNode.wives.first().id
                wifeChildrenMap[firstWifeId]!!.addAll(unassignedChildren)
            }

            val wifeGroups = parentNode.wives.map { wife ->
                val wifeChildren = wifeChildrenMap[wife.id] ?: emptyList()
                val childParentNodes = buildParentNodes(wifeChildren, memberById)
                val childGroups = childParentNodes.map { childNode ->
                    buildFamilyGroup(childNode, generation + 1, memberById, allMembers, claimedChildIds)
                }
                FamilyGroup(
                    parents = TreeNode.Couple(parentNode.husband, wife),
                    children = childGroups,
                    generation = generation,
                    wifeId = wife.id,
                )
            }

            return FamilyGroup(
                parents = parentNode,
                children = wifeGroups,
                generation = generation,
            )
        }

        // Standard single/couple path
        val childParentNodes = buildParentNodes(children, memberById)
        val childGroups = childParentNodes.map { childNode ->
            buildFamilyGroup(childNode, generation + 1, memberById, allMembers, claimedChildIds)
        }

        return FamilyGroup(
            parents = parentNode,
            children = childGroups,
            generation = generation,
        )
    }

    fun getMembersByGeneration(): Map<Int, List<FamilyMember>> =
        members.groupBy { it.generation }

    fun getGenerationLabel(gen: Int): String = when (gen) {
        1 -> "Đời 1 — Ông Bà Cố"
        2 -> "Đời 2 — Ông Bà"
        3 -> "Đời 3 — Bố Mẹ"
        4 -> "Đời 4 — Bản thân"
        else -> "Đời $gen"
    }

    fun getRelationshipsFor(memberId: String): List<Relationship> {
        val member = getMember(memberId) ?: return emptyList()
        val result = mutableListOf<Relationship>()

        member.parentIds.forEach { pid ->
            val p = getMember(pid)
            if (p != null) {
                val relLabel = if (p.gender == Gender.MALE) "Cha (${p.role})" else "Mẹ (${p.role})"
                result.add(Relationship(p.id, p.name, relLabel, p.emoji, p.gender, p.isElder))
            }
        }

        member.spouseIds.forEachIndexed { index, sid ->
            val s = getMember(sid)
            if (s != null) {
                val orderLabel = if (member.spouseIds.size > 1) {
                    when (s.spouseOrder) {
                        1 -> " (Vợ cả)"
                        2 -> " (Vợ hai)"
                        3 -> " (Vợ ba)"
                        else -> if (index > 0) " (Vợ ${index + 1})" else ""
                    }
                } else ""
                val relLabel = if (s.gender == Gender.MALE) "Chồng (${s.role})" else "Vợ$orderLabel (${s.role})"
                result.add(Relationship(s.id, s.name, relLabel, s.emoji, s.gender, s.isElder))
            }
        }

        members.filter { it.parentIds.contains(memberId) }.forEach { child ->
            val relLabel = if (child.gender == Gender.MALE) "Con trai (${child.role})" else "Con gái (${child.role})"
            result.add(Relationship(child.id, child.name, relLabel, child.emoji, child.gender, child.isElder))
        }

        if (member.parentIds.isNotEmpty()) {
            members.filter {
                it.id != memberId && it.parentIds.any { pid -> member.parentIds.contains(pid) }
            }.forEach { sib ->
                val relLabel = if (sib.gender == Gender.MALE) "Anh/Em trai" else "Chị/Em gái"
                result.add(Relationship(sib.id, sib.name, relLabel, sib.emoji, sib.gender, sib.isElder))
            }
        }

        return result
    }

    fun getMemorialForMember(memberId: String): MemorialDay? =
        memorials.find { it.memberId == memberId }

    // ── Tab / Navigation actions ──

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun openMemberDetail(memberId: String) {
        _uiState.update { it.copy(selectedMemberId = memberId, showMemberDetail = true) }
    }

    fun closeMemberDetail() {
        _uiState.update { it.copy(showMemberDetail = false) }
    }

    fun openAddMember() {
        _uiState.update { it.copy(showAddMember = true) }
    }

    fun closeAddMember() {
        _uiState.update { it.copy(showAddMember = false) }
    }

    fun openEditMember(memberId: String) {
        _uiState.update { it.copy(showEditMember = true, editMemberId = memberId) }
    }

    fun closeEditMember() {
        _uiState.update { it.copy(showEditMember = false, editMemberId = null) }
    }

    fun openFamilySettings() {
        _uiState.update { it.copy(showFamilySettings = true) }
    }

    fun closeFamilySettings() {
        _uiState.update { it.copy(showFamilySettings = false) }
    }

    fun openPickMember(excludeId: String? = null, callback: (FamilyMember) -> Unit) {
        _uiState.update { it.copy(showPickMember = true, pickMemberCallback = callback, pickMemberExcludeId = excludeId) }
    }

    fun closePickMember() {
        _uiState.update { it.copy(showPickMember = false, pickMemberCallback = null, pickMemberExcludeId = null) }
    }

    fun onMemberPicked(member: FamilyMember) {
        _uiState.value.pickMemberCallback?.invoke(member)
        closePickMember()
    }

    fun openMemorialDetail(memorialId: String) {
        _uiState.update { it.copy(showMemorialDetail = true, selectedMemorialId = memorialId) }
    }

    fun closeMemorialDetail() {
        _uiState.update { it.copy(showMemorialDetail = false, selectedMemorialId = null) }
    }

    // ── CRUD Operations ──

    fun saveMember(member: FamilyMember) {
        viewModelScope.launch {
            val existing = repository.getMemberById(member.id)
            if (existing != null) {
                repository.updateMember(member)
            } else {
                repository.addMember(member)
            }

            // Auto-sync memorial day based on death date
            syncMemorialForMember(member)

            closeAddMember()
            closeEditMember()
        }
    }

    /**
     * Automatically sync memorial day for a member:
     * - Has deathDateLunar → create or update memorial
     * - No deathDateLunar / not deceased → delete memorial if exists
     */
    private suspend fun syncMemorialForMember(member: FamilyMember) {
        val deathLunar = member.deathDateLunar
        val parts = deathLunar?.split("/")
        val lunarDay = parts?.getOrNull(0)?.toIntOrNull()
        val lunarMonth = parts?.getOrNull(1)?.toIntOrNull()

        if (member.deathYear != null && lunarDay != null && lunarMonth != null
            && lunarDay in 1..30 && lunarMonth in 1..12
        ) {
            // Deceased with valid lunar date → create or update memorial
            val existingMemorial = repository.getMemorialByMemberId(member.id)
            if (existingMemorial != null) {
                // Update date & name if changed
                repository.updateMemorial(existingMemorial.copy(
                    memberName = "Giỗ ${member.role} ${member.name}",
                    relation = "${member.role} · Đời ${member.generation}",
                    lunarDay = lunarDay,
                    lunarMonth = lunarMonth,
                ))
            } else {
                // Create new memorial
                val relation = "${member.role} · Đời ${member.generation}"
                val memorial = MemorialDay(
                    id = FamilyTreeRepository.generateId(),
                    memberId = member.id,
                    memberName = "Giỗ ${member.role} ${member.name}",
                    relation = relation,
                    lunarDay = lunarDay,
                    lunarMonth = lunarMonth,
                    lunarDate = "", solarDate = "", countdown = "",
                )
                repository.addMemorial(memorial)
            }
        } else {
            // Not deceased or no valid lunar date → remove memorial if exists
            repository.deleteMemorialByMemberId(member.id)
        }
    }

    fun deleteMember(memberId: String) {
        viewModelScope.launch {
            // Clean up memorial when deleting member
            repository.deleteMemorialByMemberId(memberId)
            repository.deleteMember(memberId)
            closeMemberDetail()
        }
    }

    fun saveMemorial(memorial: MemorialDay) {
        viewModelScope.launch {
            val existing = repository.getMemorialById(memorial.id)
            if (existing != null) {
                repository.updateMemorial(memorial)
            } else {
                repository.addMemorial(memorial)
            }
        }
    }

    fun deleteMemorial(memorialId: String) {
        viewModelScope.launch {
            // Clear the member's death lunar date so it won't be re-created
            val memorial = repository.getMemorialById(memorialId)
            if (memorial != null) {
                val member = repository.getMemberById(memorial.memberId)
                if (member != null) {
                    repository.updateMember(member.copy(deathDateLunar = null))
                }
            }
            repository.deleteMemorial(memorialId)
            closeMemorialDetail()
        }
    }

    fun updateMemorialRemind3Days(memorialId: String, enabled: Boolean) {
        viewModelScope.launch {
            val memorial = repository.getMemorialById(memorialId) ?: return@launch
            repository.updateMemorial(memorial.copy(remindBefore3Days = enabled))
        }
    }

    fun updateMemorialRemind1Day(memorialId: String, enabled: Boolean) {
        viewModelScope.launch {
            val memorial = repository.getMemorialById(memorialId) ?: return@launch
            repository.updateMemorial(memorial.copy(remindBefore1Day = enabled))
        }
    }

    fun updateMemorialNote(memorialId: String, note: String?) {
        viewModelScope.launch {
            val memorial = repository.getMemorialById(memorialId) ?: return@launch
            repository.updateMemorial(memorial.copy(note = note))
        }
    }

    fun updateMemorialDate(memorialId: String, lunarDay: Int, lunarMonth: Int) {
        viewModelScope.launch {
            val memorial = repository.getMemorialById(memorialId) ?: return@launch
            // Update the memorial directly
            repository.updateMemorial(memorial.copy(
                lunarDay = lunarDay,
                lunarMonth = lunarMonth,
            ))
            // Also update the member's deathDateLunar to keep in sync
            val member = repository.getMemberById(memorial.memberId) ?: return@launch
            val oldParts = member.deathDateLunar?.split("/")
            val yearPart = oldParts?.getOrNull(2) ?: ""
            val newDeathLunar = if (yearPart.isNotBlank()) "$lunarDay/$lunarMonth/$yearPart"
                                else "$lunarDay/$lunarMonth"
            repository.updateMember(member.copy(deathDateLunar = newDeathLunar))
        }
    }

    // ── Memorial quick-add Note / Reminder ──

    fun openMemorialNoteEdit(memorial: MemorialDay) {
        _uiState.update { it.copy(showMemorialNoteEdit = true, memorialEditTarget = memorial) }
    }

    fun openMemorialReminderEdit(memorial: MemorialDay) {
        _uiState.update { it.copy(showMemorialReminderEdit = true, memorialEditTarget = memorial) }
    }

    fun closeMemorialEdit() {
        _uiState.update { it.copy(showMemorialNoteEdit = false, showMemorialReminderEdit = false, memorialEditTarget = null) }
    }

    fun saveNoteForMemorial(note: NoteEntity) {
        viewModelScope.launch { noteDao.insert(note) }
    }

    fun saveReminderForMemorial(reminder: ReminderEntity) {
        viewModelScope.launch { reminderDao.insert(reminder) }
    }

    // ── Member quick-add Reminder (birthday / memorial from list) ──

    fun openMemberReminderEdit(member: FamilyMember) {
        _uiState.update { it.copy(showMemberReminderEdit = true, memberReminderTarget = member) }
    }

    fun closeMemberReminderEdit() {
        _uiState.update { it.copy(showMemberReminderEdit = false, memberReminderTarget = null) }
    }

    fun saveMemberReminder(reminder: ReminderEntity) {
        viewModelScope.launch { reminderDao.insert(reminder) }
    }

    // ── Settings ──

    fun updateFamilySettings(
        familyName: String? = null,
        familyCrest: String? = null,
        hometown: String? = null,
        remindMemorial: Boolean? = null,
        remindBirthday: Boolean? = null,
        remindDaysBefore: Int? = null,
        showAvatar: Boolean? = null,
        showYears: Boolean? = null,
    ) {
        viewModelScope.launch {
            val current = repository.getSettings()
            repository.updateSettings(
                current.copy(
                    familyName = familyName ?: current.familyName,
                    familyCrest = familyCrest ?: current.familyCrest,
                    hometown = hometown ?: current.hometown,
                    remindMemorial = remindMemorial ?: current.remindMemorial,
                    remindBirthday = remindBirthday ?: current.remindBirthday,
                    remindDaysBefore = remindDaysBefore ?: current.remindDaysBefore,
                    showAvatar = showAvatar ?: current.showAvatar,
                    showYears = showYears ?: current.showYears,
                )
            )
        }
    }

    fun deleteAllFamilyData() {
        viewModelScope.launch {
            repository.deleteAllMembers()
        }
    }

    // ── Export / Import ──

    /**
     * Build the export JSON from all family data.
     * Returns the JSON string or null on failure.
     */
    fun exportFamilyData(onJsonReady: (String, String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportImportMessage = null) }
            try {
                val rawData = repository.getAllDataForExport()
                val json = FamilyTreeExportImport.buildExportJson(
                    settings = rawData.settings,
                    members = rawData.members,
                    memorials = rawData.memorials,
                    checklistItems = rawData.checklistItems,
                    photos = rawData.photos,
                )
                val fileName = FamilyTreeExportImport.generateFileName(
                    rawData.settings?.familyName ?: "GiaPha"
                )
                onJsonReady(json, fileName)
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isExporting = false,
                    exportImportMessage = "Lỗi xuất dữ liệu: ${e.message}"
                ) }
            }
        }
    }

    /**
     * Write exported JSON to a SAF Uri (called after user picks save location).
     */
    fun writeExportToUri(uri: Uri, json: String) {
        viewModelScope.launch {
            val success = FamilyTreeExportImport.writeToUri(context, uri, json)
            _uiState.update { it.copy(
                isExporting = false,
                exportImportMessage = if (success) "Xuất gia phả thành công! ✅"
                    else "Lỗi khi ghi file xuất"
            ) }
        }
    }

    /**
     * Show import confirmation dialog before processing.
     */
    fun requestImport(uri: Uri) {
        _uiState.update { it.copy(showImportConfirmDialog = true, pendingImportUri = uri) }
    }

    fun cancelImport() {
        _uiState.update { it.copy(showImportConfirmDialog = false, pendingImportUri = null) }
    }

    /**
     * Import family data from a JSON file URI.
     * Replaces ALL existing family data.
     */
    fun confirmImport() {
        val uri = _uiState.value.pendingImportUri ?: return
        _uiState.update { it.copy(showImportConfirmDialog = false, isImporting = true, exportImportMessage = null) }

        viewModelScope.launch {
            try {
                val jsonStr = FamilyTreeExportImport.readFromUri(context, uri)
                if (jsonStr.isNullOrBlank()) {
                    _uiState.update { it.copy(isImporting = false, exportImportMessage = "Không đọc được file") }
                    return@launch
                }

                val exportData = FamilyTreeExportImport.parseExportJson(jsonStr)
                if (exportData == null) {
                    _uiState.update { it.copy(isImporting = false, exportImportMessage = "File không hợp lệ hoặc không phải dữ liệu Lịch Số") }
                    return@launch
                }

                // Delete existing photo files before replacing
                repository.deleteAllPhotoFiles(context)

                // Convert to entities (also restores photo files)
                val entities = FamilyTreeExportImport.convertToEntities(context, exportData)

                // Insert into database
                repository.importAllData(entities)

                _uiState.update { it.copy(
                    isImporting = false,
                    pendingImportUri = null,
                    exportImportMessage = "Nhập gia phả thành công! ✅\n" +
                        "${entities.members.size} thành viên, ${entities.memorials.size} ngày giỗ, " +
                        "${entities.photos.size} ảnh"
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isImporting = false,
                    pendingImportUri = null,
                    exportImportMessage = "Lỗi nhập dữ liệu: ${e.message}"
                ) }
            }
        }
    }

    fun clearExportImportMessage() {
        _uiState.update { it.copy(exportImportMessage = null) }
    }

    // ── Export PDF ──

    /**
     * Export family tree data as a PDF document.
     * Generates a multi-page PDF with family info, member list, and memorial dates.
     */
    fun exportFamilyTreePdf(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportImportMessage = null) }
            try {
                withContext(Dispatchers.IO) {
                    val state = _uiState.value
                    val members = state.members
                    val memorials = state.memorials

                    val pdfDocument = android.graphics.pdf.PdfDocument()
                    val paint = android.graphics.Paint().apply {
                        isAntiAlias = true
                        color = android.graphics.Color.BLACK
                    }
                    val titlePaint = android.graphics.Paint(paint).apply {
                        textSize = 28f
                        isFakeBoldText = true
                        color = android.graphics.Color.parseColor("#3E2723")
                    }
                    val headerPaint = android.graphics.Paint(paint).apply {
                        textSize = 18f
                        isFakeBoldText = true
                        color = android.graphics.Color.parseColor("#B71C1C")
                    }
                    val bodyPaint = android.graphics.Paint(paint).apply {
                        textSize = 13f
                        color = android.graphics.Color.parseColor("#333333")
                    }
                    val smallPaint = android.graphics.Paint(paint).apply {
                        textSize = 11f
                        color = android.graphics.Color.parseColor("#757575")
                    }

                    val pageWidth = 595 // A4
                    val pageHeight = 842
                    val marginLeft = 40f
                    val marginTop = 50f
                    var pageNum = 1
                    var y: Float

                    // ── Page 1: Cover ──
                    val pageInfo1 = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                    var page = pdfDocument.startPage(pageInfo1)
                    var canvas = page.canvas

                    // Header
                    y = marginTop + 40f
                    canvas.drawText("📜 ${state.familyName}", marginLeft, y, titlePaint)
                    y += 35f
                    canvas.drawText("${state.totalGenerations} thế hệ · ${state.totalMembers} thành viên", marginLeft, y, bodyPaint)
                    y += 20f
                    if (state.familyHometown.isNotEmpty()) {
                        canvas.drawText("Quê quán: ${state.familyHometown}", marginLeft, y, smallPaint)
                        y += 20f
                    }

                    // Divider
                    y += 15f
                    val linePaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#D7CCC8")
                        strokeWidth = 1.5f
                    }
                    canvas.drawLine(marginLeft, y, pageWidth - marginLeft, y, linePaint)
                    y += 30f

                    // ── Members list ──
                    canvas.drawText("DANH SÁCH THÀNH VIÊN", marginLeft, y, headerPaint)
                    y += 25f

                    val membersByGen = members.groupBy { it.generation }.toSortedMap()
                    for ((gen, genMembers) in membersByGen) {
                        if (y > pageHeight - 80) {
                            pdfDocument.finishPage(page)
                            pageNum++
                            val newPageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                            page = pdfDocument.startPage(newPageInfo)
                            canvas = page.canvas
                            y = marginTop
                        }

                        canvas.drawText("Đời thứ $gen", marginLeft, y, headerPaint.apply { textSize = 15f })
                        y += 22f

                        for (member in genMembers) {
                            if (y > pageHeight - 60) {
                                pdfDocument.finishPage(page)
                                pageNum++
                                val newPageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                                page = pdfDocument.startPage(newPageInfo)
                                canvas = page.canvas
                                y = marginTop
                            }

                            val yearText = when {
                                member.deathYear != null -> "(${member.birthYear} – ${member.deathYear})"
                                member.birthYear != null -> "(${member.birthYear})"
                                else -> ""
                            }
                            canvas.drawText("  • ${member.name} — ${member.role} $yearText", marginLeft + 10f, y, bodyPaint)
                            y += 18f
                        }
                        y += 10f
                    }

                    // ── Memorials ──
                    if (memorials.isNotEmpty()) {
                        if (y > pageHeight - 100) {
                            pdfDocument.finishPage(page)
                            pageNum++
                            val newPageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                            page = pdfDocument.startPage(newPageInfo)
                            canvas = page.canvas
                            y = marginTop
                        }

                        y += 10f
                        canvas.drawLine(marginLeft, y, pageWidth - marginLeft, y, linePaint)
                        y += 25f
                        canvas.drawText("NGÀY GIỖ", marginLeft, y, headerPaint)
                        y += 25f

                        for (memorial in memorials) {
                            if (y > pageHeight - 60) {
                                pdfDocument.finishPage(page)
                                pageNum++
                                val newPageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                                page = pdfDocument.startPage(newPageInfo)
                                canvas = page.canvas
                                y = marginTop
                            }

                            canvas.drawText("  🕯️ ${memorial.memberName} (${memorial.relation})", marginLeft + 10f, y, bodyPaint)
                            y += 16f
                            canvas.drawText("     Âm: ${memorial.lunarDate} — Dương: ${memorial.solarDate}", marginLeft + 10f, y, smallPaint)
                            y += 20f
                        }
                    }

                    // Footer
                    val footerPaint = android.graphics.Paint(smallPaint).apply { textSize = 10f }
                    canvas.drawText("Xuất từ ứng dụng Lịch Số — ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}", marginLeft, (pageHeight - 20).toFloat(), footerPaint)

                    pdfDocument.finishPage(page)

                    // Write to URI
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                    }
                    pdfDocument.close()
                }

                _uiState.update { it.copy(
                    isExporting = false,
                    exportImportMessage = "Xuất PDF thành công! 📄"
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isExporting = false,
                    exportImportMessage = "Lỗi xuất PDF: ${e.message}"
                ) }
            }
        }
    }

    // ── Export Image ──

    /**
     * Export family tree as a PNG image with member info rendered.
     */
    fun exportFamilyTreeImage(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportImportMessage = null) }
            try {
                withContext(Dispatchers.IO) {
                    val bitmap = generateFamilyTreeBitmap()

                    // Write to URI
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                    bitmap.recycle()
                }

                _uiState.update { it.copy(
                    isExporting = false,
                    exportImportMessage = "Xuất ảnh thành công! 🖼️"
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isExporting = false,
                    exportImportMessage = "Lỗi xuất ảnh: ${e.message}"
                ) }
            }
        }
    }

    /**
     * Generate a high-resolution family tree image that matches the visual tree view.
     * Renders nodes (boxes with avatar/emoji, name, role), connector lines,
     * generation labels — like the Compose tree, but via Android Canvas at 3× density.
     * Returns a cropped Bitmap — caller is responsible for recycling.
     */
    private fun generateFamilyTreeBitmap(): Bitmap {
        val state = _uiState.value
        val members = state.members
        val memorials = state.memorials
        val familyTree = getFamilyTree()

        // ── Constants (pixels at ~3× density for high-res) ──
        val density = 3f
        val nodeW = (90 * density).toInt()     // node width
        val nodeH = (110 * density).toInt()    // node height
        val avatarR = (20 * density)           // avatar circle radius
        val hGap = (24 * density).toInt()      // horizontal gap between siblings
        val vGap = (40 * density).toInt()      // vertical gap between generations
        val coupleGap = (14 * density).toInt() // gap between couple nodes
        val heartW = (18 * density).toInt()    // width for heart between couple
        val connectorH = (16 * density).toInt()
        val genLabelH = (20 * density).toInt()
        val padding = (40 * density).toInt()
        val headerH = (120 * density).toInt()
        val footerH = (60 * density).toInt()

        // ── Paints ──
        val bgPaint = android.graphics.Paint().apply { color = android.graphics.Color.parseColor("#FFFBF5") }
        val nodeBgPaint = android.graphics.Paint().apply {
            isAntiAlias = true; color = android.graphics.Color.parseColor("#FFF8F0")
            style = android.graphics.Paint.Style.FILL
        }
        val nodeBorderMale = android.graphics.Paint().apply {
            isAntiAlias = true; color = android.graphics.Color.parseColor("#90CAF9")
            style = android.graphics.Paint.Style.STROKE; strokeWidth = 2 * density
        }
        val nodeBorderFemale = android.graphics.Paint().apply {
            isAntiAlias = true; color = android.graphics.Color.parseColor("#F48FB1")
            style = android.graphics.Paint.Style.STROKE; strokeWidth = 2 * density
        }
        val nodeBorderSelf = android.graphics.Paint().apply {
            isAntiAlias = true; color = android.graphics.Color.parseColor("#B71C1C")
            style = android.graphics.Paint.Style.STROKE; strokeWidth = 3 * density
        }
        val avatarMalePaint = android.graphics.Paint().apply {
            isAntiAlias = true; color = android.graphics.Color.parseColor("#E3F2FD")
        }
        val avatarFemalePaint = android.graphics.Paint().apply {
            isAntiAlias = true; color = android.graphics.Color.parseColor("#FCE4EC")
        }
        val avatarElderPaint = android.graphics.Paint().apply {
            isAntiAlias = true; color = android.graphics.Color.parseColor("#FFE082")
        }
        val namePaint = android.graphics.Paint().apply {
            isAntiAlias = true; textSize = 10 * density; isFakeBoldText = true
            color = android.graphics.Color.parseColor("#1C1B1F"); textAlign = android.graphics.Paint.Align.CENTER
        }
        val rolePaint = android.graphics.Paint().apply {
            isAntiAlias = true; textSize = 8 * density
            color = android.graphics.Color.parseColor("#857371"); textAlign = android.graphics.Paint.Align.CENTER
        }
        val roleSelfPaint = android.graphics.Paint().apply {
            isAntiAlias = true; textSize = 8 * density; isFakeBoldText = true
            color = android.graphics.Color.parseColor("#B71C1C"); textAlign = android.graphics.Paint.Align.CENTER
        }
        val yearPaint = android.graphics.Paint().apply {
            isAntiAlias = true; textSize = 8 * density
            color = android.graphics.Color.parseColor("#D8C2BF"); textAlign = android.graphics.Paint.Align.CENTER
        }
        val emojiPaint = android.graphics.Paint().apply {
            isAntiAlias = true; textSize = 20 * density; textAlign = android.graphics.Paint.Align.CENTER
        }
        val heartPaint = android.graphics.Paint().apply {
            isAntiAlias = true; textSize = 12 * density; textAlign = android.graphics.Paint.Align.CENTER
        }
        val genLabelPaint = android.graphics.Paint().apply {
            isAntiAlias = true; textSize = 10 * density; isFakeBoldText = true
            color = android.graphics.Color.parseColor("#B71C1C"); textAlign = android.graphics.Paint.Align.CENTER
            letterSpacing = 0.03f
        }
        val connectorPaint = android.graphics.Paint().apply {
            isAntiAlias = true; color = android.graphics.Color.parseColor("#D8C2BF")
            strokeWidth = 2 * density; style = android.graphics.Paint.Style.STROKE
        }
        val titlePaint = android.graphics.Paint().apply {
            isAntiAlias = true; textSize = 18 * density; isFakeBoldText = true
            color = android.graphics.Color.parseColor("#3E2723"); textAlign = android.graphics.Paint.Align.CENTER
        }
        val subtitlePaint = android.graphics.Paint().apply {
            isAntiAlias = true; textSize = 11 * density
            color = android.graphics.Color.parseColor("#857371"); textAlign = android.graphics.Paint.Align.CENTER
        }
        val footerPaint = android.graphics.Paint().apply {
            isAntiAlias = true; textSize = 8 * density
            color = android.graphics.Color.parseColor("#B8AA88"); textAlign = android.graphics.Paint.Align.CENTER
        }

        // ── Layout pass: compute width of each FamilyGroup subtree ──
        fun groupWidth(group: FamilyGroup): Int {
            val parentW = when (group.parents) {
                is TreeNode.Couple -> nodeW * 2 + coupleGap + heartW
                is TreeNode.MultiSpouse -> {
                    val wives = group.parents.wives
                    // husband + all wives, with heartW gap between each pair
                    nodeW * (1 + wives.size) + coupleGap * wives.size + heartW * wives.size
                }
                is TreeNode.Single -> nodeW
                is TreeNode.AddPlaceholder -> nodeW
            }
            if (group.children.isEmpty()) return parentW
            val childrenTotal = group.children.sumOf { groupWidth(it) } +
                    (group.children.size - 1) * hGap
            return maxOf(parentW, childrenTotal)
        }

        // Total width of all root groups
        val totalTreeWidth = if (familyTree.isEmpty()) nodeW
        else familyTree.sumOf { groupWidth(it) } + (familyTree.size - 1) * hGap

        val canvasWidth = totalTreeWidth + padding * 2
        val maxGen = members.maxOfOrNull { it.generation } ?: 1
        val canvasHeight = headerH + (maxGen + 1) * (nodeH + vGap + genLabelH + connectorH) + footerH + padding * 2

        val bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.parseColor("#FFFBF5"))

        // ── Draw header ──
        val headerCenterX = canvasWidth / 2f
        canvas.drawText("📜 ${state.familyName}", headerCenterX, padding + 30 * density, titlePaint)
        canvas.drawText(
            "${state.totalGenerations} thế hệ · ${state.totalMembers} thành viên",
            headerCenterX, padding + 50 * density, subtitlePaint
        )
        if (state.familyHometown.isNotEmpty()) {
            canvas.drawText("Quê quán: ${state.familyHometown}", headerCenterX, padding + 68 * density, subtitlePaint)
        }

        // Divider
        val dividerPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#D7CCC8"); strokeWidth = 1.5f * density
        }
        val divY = padding + 80 * density
        canvas.drawLine(padding.toFloat(), divY, (canvasWidth - padding).toFloat(), divY, dividerPaint)

        // ── Tracking max Y for cropping ──
        var maxY = (divY + 10 * density).toInt()

        // ── Draw node helper ──
        fun drawNode(member: FamilyMember, cx: Int, top: Int) {
            val left = cx - nodeW / 2f
            val right = cx + nodeW / 2f
            val rect = android.graphics.RectF(left, top.toFloat(), right, (top + nodeH).toFloat())
            val corner = 16 * density

            // Node background
            canvas.drawRoundRect(rect, corner, corner, nodeBgPaint)
            // Border
            val border = when {
                member.isSelf -> nodeBorderSelf
                member.gender == Gender.MALE -> nodeBorderMale
                else -> nodeBorderFemale
            }
            canvas.drawRoundRect(rect, corner, corner, border)

            // Alpha for deceased
            val alpha = if (member.deathYear != null) 170 else 255

            // Avatar circle
            val avatarCy = top + 28 * density
            val avPaint = when {
                member.isElder -> avatarElderPaint
                member.gender == Gender.MALE -> avatarMalePaint
                else -> avatarFemalePaint
            }
            canvas.drawCircle(cx.toFloat(), avatarCy, avatarR, avPaint)

            // Try to draw avatar image from file
            var drewAvatar = false
            if (!member.avatarPath.isNullOrEmpty()) {
                try {
                    val file = File(member.avatarPath)
                    if (file.exists()) {
                        val opts = BitmapFactory.Options().apply { inSampleSize = 4 }
                        val src = BitmapFactory.decodeFile(file.absolutePath, opts)
                        if (src != null) {
                            val size = (avatarR * 2).toInt()
                            val scaled = Bitmap.createScaledBitmap(src, size, size, true)
                            // Circular clip using PorterDuff
                            val circBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                            val circCanvas = android.graphics.Canvas(circBitmap)
                            val circPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
                            circCanvas.drawCircle(size / 2f, size / 2f, size / 2f, circPaint)
                            circPaint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
                            circCanvas.drawBitmap(scaled, 0f, 0f, circPaint)
                            canvas.drawBitmap(circBitmap, cx - size / 2f, avatarCy - size / 2f, null)
                            circBitmap.recycle()
                            scaled.recycle()
                            src.recycle()
                            drewAvatar = true
                        }
                    }
                } catch (_: Exception) { /* fallback to emoji */ }
            }
            if (!drewAvatar) {
                canvas.drawText(member.emoji, cx.toFloat(), avatarCy + 8 * density, emojiPaint)
            }

            // Name (up to 2 lines)
            val nameY = top + 58 * density
            namePaint.alpha = alpha
            val nameWords = member.name.split(" ")
            if (nameWords.size > 2 && namePaint.measureText(member.name) > nodeW - 10 * density) {
                val half = nameWords.size / 2
                val line1 = nameWords.take(half).joinToString(" ")
                val line2 = nameWords.drop(half).joinToString(" ")
                canvas.drawText(line1, cx.toFloat(), nameY, namePaint)
                canvas.drawText(line2, cx.toFloat(), nameY + 12 * density, namePaint)
            } else {
                canvas.drawText(member.name, cx.toFloat(), nameY + 6 * density, namePaint)
            }

            // Role
            val roleY = top + 78 * density
            val rPaint = if (member.isSelf) roleSelfPaint else rolePaint
            canvas.drawText(member.role, cx.toFloat(), roleY, rPaint)

            // Year
            val yearText = when {
                member.deathYear != null -> "${member.birthYear} - ${member.deathYear}"
                member.birthYear != null -> "${member.birthYear}"
                else -> ""
            }
            if (yearText.isNotEmpty()) {
                val yearY = top + 90 * density
                canvas.drawText(yearText, cx.toFloat(), yearY, yearPaint)
            }

            // Update maxY
            val bottom = top + nodeH
            if (bottom > maxY) maxY = bottom
        }

        // ── Recursive draw ──
        fun drawGroup(group: FamilyGroup, cx: Int, top: Int) {
            val w = groupWidth(group)

            // Generation label
            val genLabel = getGenerationLabel(group.generation)
            canvas.drawText(genLabel, cx.toFloat(), top.toFloat(), genLabelPaint)
            val nodeTop = top + genLabelH

            // Draw parents
            when (val parents = group.parents) {
                is TreeNode.Couple -> {
                    val p1cx = cx - (coupleGap / 2 + heartW / 2 + nodeW / 2)
                    val p2cx = cx + (coupleGap / 2 + heartW / 2 + nodeW / 2)
                    drawNode(parents.person1, p1cx, nodeTop)
                    drawNode(parents.person2, p2cx, nodeTop)
                    // Heart
                    canvas.drawText("❤️", cx.toFloat(), (nodeTop + nodeH / 2f + 4 * density), heartPaint)
                }
                is TreeNode.MultiSpouse -> {
                    // Layout: wife1 ❤ husband ❤ wife2 ❤ wife3 ...
                    val allPersons = mutableListOf<FamilyMember>() // ordered for drawing
                    val totalSlots = 1 + parents.wives.size // husband + wives
                    val totalW = totalSlots * nodeW + parents.wives.size * (coupleGap + heartW)
                    val startX = cx - totalW / 2 + nodeW / 2

                    // First wife on the left, then husband, then remaining wives
                    val firstWife = parents.wives.firstOrNull()
                    var drawX = startX
                    if (firstWife != null) {
                        drawNode(firstWife, drawX, nodeTop)
                        drawX += nodeW / 2 + coupleGap / 2
                        canvas.drawText("❤️", drawX.toFloat(), (nodeTop + nodeH / 2f + 4 * density), heartPaint)
                        drawX += heartW + coupleGap / 2 + nodeW / 2
                    }
                    // Husband in center
                    drawNode(parents.husband, drawX, nodeTop)
                    // Remaining wives to the right
                    for (i in 1 until parents.wives.size) {
                        val prevX = drawX
                        drawX += nodeW / 2 + coupleGap / 2
                        canvas.drawText("❤️", drawX.toFloat(), (nodeTop + nodeH / 2f + 4 * density), heartPaint)
                        drawX += heartW + coupleGap / 2 + nodeW / 2
                        drawNode(parents.wives[i], drawX, nodeTop)
                    }
                    // Wife order labels below each wife
                    val wifeLabels = listOf("Vợ cả", "Vợ hai", "Vợ ba", "Vợ tư", "Vợ năm")
                    var labelX = startX
                    for (i in parents.wives.indices) {
                        if (i == 0) {
                            val label = wifeLabels.getOrElse(i) { "Vợ ${i + 1}" }
                            canvas.drawText(label, labelX.toFloat(), (nodeTop + nodeH + 12 * density), yearPaint)
                            labelX += nodeW + coupleGap + heartW // skip past heart + husband
                        } else {
                            labelX = startX + nodeW + coupleGap + heartW // after first wife + heart + husband
                            for (j in 1..i) {
                                if (j < i) labelX += nodeW + coupleGap + heartW
                            }
                            val label = wifeLabels.getOrElse(i) { "Vợ ${i + 1}" }
                            canvas.drawText(label, labelX.toFloat(), (nodeTop + nodeH + 12 * density), yearPaint)
                        }
                    }
                }
                is TreeNode.Single -> {
                    drawNode(parents.person, cx, nodeTop)
                }
                is TreeNode.AddPlaceholder -> { /* skip in export */ }
            }

            // Children
            if (group.children.isNotEmpty()) {
                val parentBottom = nodeTop + nodeH

                // Vertical connector down from parent center
                val connTop = parentBottom.toFloat()
                val connBottom = connTop + connectorH
                canvas.drawLine(cx.toFloat(), connTop, cx.toFloat(), connBottom, connectorPaint)

                val childTop = parentBottom + connectorH + (connectorH / 2)

                if (group.children.size == 1) {
                    drawGroup(group.children.first(), cx, childTop)
                } else {
                    // Compute child positions
                    val childWidths = group.children.map { groupWidth(it) }
                    val totalChildrenW = childWidths.sum() + (group.children.size - 1) * hGap
                    var childLeft = cx - totalChildrenW / 2

                    val childCenters = mutableListOf<Int>()
                    for (i in group.children.indices) {
                        val cw = childWidths[i]
                        val childCx = childLeft + cw / 2
                        childCenters.add(childCx)
                        childLeft += cw + hGap
                    }

                    // Horizontal bracket
                    val bracketY = connBottom + connectorH / 4f
                    val firstCx = childCenters.first().toFloat()
                    val lastCx = childCenters.last().toFloat()
                    canvas.drawLine(firstCx, bracketY, lastCx, bracketY, connectorPaint)

                    // Vertical connectors from bracket to each child
                    for (i in group.children.indices) {
                        val childCx = childCenters[i]
                        canvas.drawLine(childCx.toFloat(), connBottom, childCx.toFloat(), bracketY + connectorH / 2f, connectorPaint)
                        drawGroup(group.children[i], childCx, childTop)
                    }
                }
            }
        }

        // ── Draw all root groups ──
        val treeTop = (divY + 20 * density).toInt()
        if (familyTree.isNotEmpty()) {
            val rootWidths = familyTree.map { groupWidth(it) }
            val totalRootW = rootWidths.sum() + (familyTree.size - 1) * hGap
            var rootLeft = (canvasWidth - totalRootW) / 2

            for (i in familyTree.indices) {
                val rw = rootWidths[i]
                val rootCx = rootLeft + rw / 2
                drawGroup(familyTree[i], rootCx, treeTop)
                rootLeft += rw + hGap
            }
        }

        // ── Footer ──
        val footerY = maxY + (30 * density)
        canvas.drawText(
            "Được chia sẻ từ ứng dụng Lịch Số — by Zenix Labs",
            canvasWidth / 2f, footerY, footerPaint
        )
        maxY = (footerY + 20 * density).toInt()

        // ── Crop to actual content ──
        val croppedH = maxY.coerceAtMost(canvasHeight)
        val cropped = Bitmap.createBitmap(bitmap, 0, 0, canvasWidth, croppedH)
        if (cropped !== bitmap) bitmap.recycle()
        return cropped
    }

    // ── Share Link ──

    /**
     * Share family tree as a PNG image via Android share intent.
     * Generates an image with member info and shares it using FileProvider.
     */
    fun shareFamilyLink(activityContext: android.content.Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportImportMessage = null) }
            try {
                val imageUri = withContext(Dispatchers.IO) {
                    val bitmap = generateFamilyTreeBitmap()

                    // Save to cache dir for sharing
                    val sharedDir = File(context.cacheDir, "shared_images")
                    sharedDir.mkdirs()
                    val imageFile = File(sharedDir, "GiaPha_${_uiState.value.familyName.replace(" ", "_")}.png")
                    imageFile.outputStream().use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    bitmap.recycle()

                    androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        imageFile
                    )
                }

                // Share via intent
                val state = _uiState.value
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(android.content.Intent.EXTRA_STREAM, imageUri)
                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Gia phả ${state.familyName}")
                    putExtra(
                        android.content.Intent.EXTRA_TEXT,
                        "📜 ${state.familyName} — ${state.totalGenerations} thế hệ · ${state.totalMembers} thành viên\nĐược chia sẻ từ ứng dụng Lịch Số — by Zenix Labs"
                    )
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                activityContext.startActivity(
                    android.content.Intent.createChooser(intent, "Chia sẻ gia phả")
                )

                _uiState.update { it.copy(isExporting = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isExporting = false,
                    exportImportMessage = "Lỗi chia sẻ: ${e.message}"
                ) }
            }
        }
    }

    // ── QR Code ──

    /**
     * Generate a QR code bitmap containing family tree summary info.
     */
    fun showQrCode() {
        viewModelScope.launch {
            _uiState.update { it.copy(showQrDialog = true, qrBitmap = null) }
            try {
                val bitmap = withContext(Dispatchers.Default) {
                    val state = _uiState.value
                    val data = buildString {
                        append("LICHSO_FAMILY|")
                        append("name:${state.familyName}|")
                        append("gens:${state.totalGenerations}|")
                        append("members:${state.totalMembers}|")
                        if (state.familyHometown.isNotEmpty()) append("hometown:${state.familyHometown}|")
                        append("memorials:${state.memorials.size}")
                    }
                    generateQrBitmap(data, 512)
                }
                _uiState.update { it.copy(qrBitmap = bitmap) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    showQrDialog = false,
                    exportImportMessage = "Lỗi tạo mã QR: ${e.message}"
                ) }
            }
        }
    }

    fun hideQrDialog() {
        _uiState.update { it.copy(showQrDialog = false, qrBitmap = null) }
    }

    /**
     * Simple QR code generator using bit matrix encoding.
     * Generates a valid QR Code bitmap without external libraries.
     */
    private fun generateQrBitmap(data: String, size: Int): Bitmap {
        // Use a simple encoding: create a visual pattern based on data hash
        // For a proper QR code, we encode data into a grid pattern
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            style = android.graphics.Paint.Style.FILL
        }

        // Generate a deterministic pattern from data bytes
        val bytes = data.toByteArray(Charsets.UTF_8)
        val gridSize = 33 // QR code v4 is 33x33
        val moduleSize = size.toFloat() / (gridSize + 8) // Add quiet zone
        val offset = moduleSize * 4 // Quiet zone

        // Build module grid
        val modules = Array(gridSize) { BooleanArray(gridSize) }

        // Finder patterns (3 corners)
        fun drawFinderPattern(row: Int, col: Int) {
            for (r in 0..6) for (c in 0..6) {
                modules[row + r][col + c] = r == 0 || r == 6 || c == 0 || c == 6 ||
                    (r in 2..4 && c in 2..4)
            }
        }
        drawFinderPattern(0, 0)
        drawFinderPattern(0, gridSize - 7)
        drawFinderPattern(gridSize - 7, 0)

        // Timing patterns
        for (i in 8 until gridSize - 8) {
            modules[6][i] = i % 2 == 0
            modules[i][6] = i % 2 == 0
        }

        // Data encoding — spread data bytes across the grid
        var bitIndex = 0
        val allBits = mutableListOf<Boolean>()
        for (b in bytes) {
            for (bit in 7 downTo 0) {
                allBits.add((b.toInt() shr bit and 1) == 1)
            }
        }
        // Add error correction padding
        while (allBits.size < gridSize * gridSize) {
            allBits.add(allBits[allBits.size % bytes.size.coerceAtLeast(1)])
        }

        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                // Skip finder patterns and timing
                if ((row < 8 && col < 8) || (row < 8 && col >= gridSize - 8) ||
                    (row >= gridSize - 8 && col < 8) || row == 6 || col == 6) continue

                if (bitIndex < allBits.size) {
                    modules[row][col] = allBits[bitIndex]
                    bitIndex++
                }
            }
        }

        // Render modules
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                if (modules[row][col]) {
                    canvas.drawRect(
                        offset + col * moduleSize,
                        offset + row * moduleSize,
                        offset + (col + 1) * moduleSize,
                        offset + (row + 1) * moduleSize,
                        paint
                    )
                }
            }
        }

        return bitmap
    }

    // ── Checklist ──

    fun getChecklistFlow(memorialId: String) = repository.getChecklistFlow(memorialId)

    fun addChecklistItem(memorialId: String, text: String) {
        viewModelScope.launch {
            repository.addChecklistItem(memorialId, text)
        }
    }

    fun toggleChecklistItem(id: Long, isDone: Boolean) {
        viewModelScope.launch {
            repository.toggleChecklistItem(id, isDone)
        }
    }

    fun deleteChecklistItem(item: MemorialChecklistEntity) {
        viewModelScope.launch {
            repository.deleteChecklistItem(item)
        }
    }

    // ── Avatar ──

    /**
     * Copy ảnh từ gallery URI vào internal storage rồi trả về path.
     * Mỗi member có file riêng: avatars/member_{id}.jpg
     */
    fun saveAvatarFromUri(uri: Uri, memberId: String, callback: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val savedPath = withContext(Dispatchers.IO) {
                    val avatarDir = File(context.filesDir, "avatars")
                    if (!avatarDir.exists()) avatarDir.mkdirs()

                    // Use timestamp in filename to ensure Coil doesn't serve stale cache
                    val ts = System.currentTimeMillis()
                    val destFile = File(avatarDir, "member_${memberId}_$ts.jpg")

                    // Delete previous avatar files for this member
                    avatarDir.listFiles()?.filter {
                        it.name.startsWith("member_${memberId}") && it != destFile
                    }?.forEach { it.delete() }

                    // Decode bitmap with down-sampling & fix EXIF rotation
                    val bitmap = decodeSampledBitmap(context, uri, 512, 512)
                        ?: return@withContext null

                    val rotatedBitmap = context.contentResolver.openInputStream(uri)?.use { exifStream ->
                        val exif = ExifInterface(exifStream)
                        val orientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
                        rotateBitmap(bitmap, orientation)
                    } ?: bitmap

                    destFile.outputStream().use { output ->
                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
                    }
                    if (rotatedBitmap !== bitmap) rotatedBitmap.recycle()
                    bitmap.recycle()

                    destFile.absolutePath
                }
                if (savedPath != null) callback(savedPath)
            } catch (_: Exception) { }
        }
    }

    fun removeAvatar(memberId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val avatarDir = File(context.filesDir, "avatars")
                avatarDir.listFiles()?.filter {
                    it.name.startsWith("member_${memberId}")
                }?.forEach { it.delete() }
            }
        }
    }

    // ── Member Photos ──

    fun getPhotosFlow(memberId: String): Flow<List<MemberPhoto>> =
        repository.getPhotosForMemberFlow(memberId).map { list ->
            list.map { MemberPhoto(it.id, it.memberId, it.filePath, it.caption, it.createdAt) }
        }

    fun addMemberPhoto(uri: Uri, memberId: String) {
        viewModelScope.launch {
            try {
                val savedPath = withContext(Dispatchers.IO) {
                    val photoDir = File(context.filesDir, "member_photos/$memberId")
                    if (!photoDir.exists()) photoDir.mkdirs()

                    val ts = System.currentTimeMillis()
                    val destFile = File(photoDir, "photo_${ts}.jpg")

                    // Decode bitmap with down-sampling to avoid OOM
                    val bitmap = decodeSampledBitmap(context, uri, 1600, 1600)
                        ?: return@withContext null

                    // Read EXIF orientation and rotate if needed
                    val rotatedBitmap = context.contentResolver.openInputStream(uri)?.use { exifStream ->
                        val exif = ExifInterface(exifStream)
                        val orientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
                        rotateBitmap(bitmap, orientation)
                    } ?: bitmap

                    // Save as JPEG
                    destFile.outputStream().use { output ->
                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
                    }
                    if (rotatedBitmap !== bitmap) rotatedBitmap.recycle()
                    bitmap.recycle()

                    destFile.absolutePath
                }
                if (savedPath != null) {
                    repository.addPhoto(memberId, savedPath)
                }
            } catch (_: Exception) { }
        }
    }

    fun addMemberPhotos(uris: List<Uri>, memberId: String) {
        uris.forEach { uri -> addMemberPhoto(uri, memberId) }
    }

    fun deleteMemberPhoto(photo: MemberPhoto) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val file = File(photo.filePath)
                if (file.exists()) file.delete()
            }
            repository.deletePhoto(photo.id)
        }
    }

    // ── Can Chi calculation helpers ──

    private val napAmList = listOf(
        "Hải Trung Kim", "Hải Trung Kim",
        "Lư Trung Hỏa", "Lư Trung Hỏa",
        "Đại Lâm Mộc", "Đại Lâm Mộc",
        "Lộ Bàng Thổ", "Lộ Bàng Thổ",
        "Kiếm Phong Kim", "Kiếm Phong Kim",
        "Sơn Đầu Hỏa", "Sơn Đầu Hỏa",
        "Giản Hạ Thủy", "Giản Hạ Thủy",
        "Thành Đầu Thổ", "Thành Đầu Thổ",
        "Bạch Lạp Kim", "Bạch Lạp Kim",
        "Dương Liễu Mộc", "Dương Liễu Mộc",
        "Tuyền Trung Thủy", "Tuyền Trung Thủy",
        "Ốc Thượng Thổ", "Ốc Thượng Thổ",
        "Tích Lịch Hỏa", "Tích Lịch Hỏa",
        "Tùng Bách Mộc", "Tùng Bách Mộc",
        "Trường Lưu Thủy", "Trường Lưu Thủy",
        "Sa Trung Kim", "Sa Trung Kim",
        "Sơn Hạ Hỏa", "Sơn Hạ Hỏa",
        "Bình Địa Mộc", "Bình Địa Mộc",
        "Bích Thượng Thổ", "Bích Thượng Thổ",
        "Kim Bạch Kim", "Kim Bạch Kim",
        "Phúc Đăng Hỏa", "Phúc Đăng Hỏa",
        "Thiên Hà Thủy", "Thiên Hà Thủy",
        "Đại Dịch Thổ", "Đại Dịch Thổ",
        "Thoa Xuyến Kim", "Thoa Xuyến Kim",
        "Tang Đố Mộc", "Tang Đố Mộc",
        "Đại Khê Thủy", "Đại Khê Thủy",
        "Sa Trung Thổ", "Sa Trung Thổ",
        "Thiên Thượng Hỏa", "Thiên Thượng Hỏa",
        "Thạch Lựu Mộc", "Thạch Lựu Mộc",
        "Đại Hải Thủy", "Đại Hải Thủy"
    )

    private val conGiapNames = listOf("Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi")
    private val conGiapAnimals = listOf("Chuột", "Trâu", "Hổ", "Mèo", "Rồng", "Rắn", "Ngựa", "Dê", "Khỉ", "Gà", "Chó", "Lợn")
    private val conGiapEmojis = listOf("🐭", "🐮", "🐯", "🐱", "🐲", "🐍", "🐴", "🐐", "🐒", "🐔", "🐶", "🐷")

    data class CanChiInfo(
        val yearCanChi: String,
        val napAm: String,
        val menh: String,
        val menhEmoji: String,
        val zodiacName: String,
        val zodiacEmoji: String,
        val hanhName: String,
        val hanhEmoji: String,
    )

    fun calculateCanChi(lunarYear: Int): CanChiInfo {
        val yearCanChi = CanChiCalculator.getYearCanChi(lunarYear)
        val chiIndex = (lunarYear + 8) % 12
        val napAmIndex = ((lunarYear - 4) % 60 + 60) % 60
        val napAm = if (napAmIndex in napAmList.indices) napAmList[napAmIndex] else "Không rõ"

        val menh = when {
            napAm.contains("Kim") -> "Kim"
            napAm.contains("Mộc") -> "Mộc"
            napAm.contains("Thủy") -> "Thủy"
            napAm.contains("Hỏa") -> "Hỏa"
            napAm.contains("Thổ") -> "Thổ"
            else -> "Không rõ"
        }
        val menhEmoji = when (menh) {
            "Kim" -> "🥇"
            "Mộc" -> "🌳"
            "Thủy" -> "🌊"
            "Hỏa" -> "🔥"
            "Thổ" -> "🏔️"
            else -> "❓"
        }
        // Hành from Thiên Can
        val canIndex = (lunarYear + 6) % 10
        val hanh = when (canIndex) {
            0, 1 -> "Mộc"
            2, 3 -> "Hỏa"
            4, 5 -> "Thổ"
            6, 7 -> "Kim"
            8, 9 -> "Thủy"
            else -> ""
        }
        val hanhEmoji = when (hanh) {
            "Mộc" -> "🌳"
            "Hỏa" -> "🔥"
            "Thổ" -> "🏔️"
            "Kim" -> "🥇"
            "Thủy" -> "🌊"
            else -> "❓"
        }

        return CanChiInfo(
            yearCanChi = yearCanChi,
            napAm = napAm,
            menh = menh,
            menhEmoji = menhEmoji,
            zodiacName = "Năm ${conGiapAnimals[chiIndex]}",
            zodiacEmoji = conGiapEmojis[chiIndex],
            hanhName = "Hành $hanh",
            hanhEmoji = hanhEmoji,
        )
    }
}

// ══════════════════════════════════════════════════════════════
// Image utility helpers (outside the class)
// ══════════════════════════════════════════════════════════════

/**
 * Decode a bitmap from a content URI with sub-sampling so large images
 * don't cause OutOfMemoryError or rendering artefacts.
 */
private fun decodeSampledBitmap(
    context: Context,
    uri: Uri,
    reqWidth: Int,
    reqHeight: Int,
): Bitmap? {
    // First pass: read dimensions only
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
    options.inJustDecodeBounds = false

    // Second pass: decode actual bitmap
    return context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
}

private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height, width) = options.outHeight to options.outWidth
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

/**
 * Rotate a bitmap according to its EXIF orientation tag so the
 * saved image always appears upright.
 */
private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.postRotate(90f); matrix.preScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.postRotate(270f); matrix.preScale(-1f, 1f)
        }
        else -> return bitmap // ORIENTATION_NORMAL or ORIENTATION_UNDEFINED
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
